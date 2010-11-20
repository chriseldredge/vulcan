/*
 * Vulcan Build Manager
 * Copyright (C) 2005-2009 Chris Eldredge
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.sourceforge.vulcan.core.support;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import net.sourceforge.vulcan.BuildTool;
import net.sourceforge.vulcan.EasyMockTestCase;
import net.sourceforge.vulcan.ProjectManager;
import net.sourceforge.vulcan.RepositoryAdaptor;
import net.sourceforge.vulcan.core.BuildDetailCallback;
import net.sourceforge.vulcan.core.BuildManager;
import net.sourceforge.vulcan.core.BuildPhase;
import net.sourceforge.vulcan.core.BuildStatusListener;
import net.sourceforge.vulcan.core.ProjectBuilder;
import net.sourceforge.vulcan.dto.BuildDaemonInfoDto;
import net.sourceforge.vulcan.dto.BuildMessageDto;
import net.sourceforge.vulcan.dto.ChangeLogDto;
import net.sourceforge.vulcan.dto.MetricDto;
import net.sourceforge.vulcan.dto.ProjectConfigDto;
import net.sourceforge.vulcan.dto.ProjectStatusDto;
import net.sourceforge.vulcan.dto.RevisionTokenDto;
import net.sourceforge.vulcan.dto.MetricDto.MetricType;
import net.sourceforge.vulcan.dto.ProjectStatusDto.Status;
import net.sourceforge.vulcan.dto.ProjectStatusDto.UpdateType;
import net.sourceforge.vulcan.exception.BuildFailedException;
import net.sourceforge.vulcan.exception.ConfigException;
import net.sourceforge.vulcan.exception.RepositoryException;
import net.sourceforge.vulcan.exception.StoreException;
import net.sourceforge.vulcan.metadata.SvnRevision;

@SvnRevision(id = "$Id$", url = "$HeadURL$")
public class ProjectBuilderTest extends EasyMockTestCase {
	boolean createWorkingDirectoriesSuccess = true;
	long sleepTime = -1;
	
	ProjectConfigDto project;

	ProjectStatusDto buildToolStatus = new ProjectStatusDto();
	ProjectStatusDto previousStatusByTagName;
	ProjectStatusDto previousStatusByWorkDir;
	
	Exception re;
	BuildFailedException be;
	
	boolean gotInterrupt;
	boolean invokedBuild;
	boolean suppressStartDate = true;
	boolean projectIsUpToDate;
	
	File logFile = new File("fakeBuildLog.log");
	File diffFile = new File("fakeDiff.log");
	
	String errorMessage;
	String warningMessage;
	MetricDto metric;
	
	UpdateType updateType = null;
	
	Long estimatedBuildTimeMillis = null;
	
	BuildDetailCallback buildDetailCallback = new BuildDetailCallback() {
		public void setPhaseMessageKey(String phase) {}
		public void setDetail(String detail) {}
		public void setDetailMessage(String messageKey, Object[] args) {}
		public void reportError(String message, String file, Integer lineNumber, String code) {}
		public void reportWarning(String message, String file, Integer lineNumber, String code) {}
		public void addMetric(MetricDto metric) {}
		@Override
		public boolean equals(Object obj) {
			return true;
		}
	};
	
	ProjectBuilderImpl builder = new ProjectBuilderImpl() {
		@Override
		protected void buildProject(ProjectConfigDto currentTarget) throws Exception {
			if (re != null) {
				doPhase(BuildPhase.Build, new PhaseCallback() {
					public void execute() throws Exception {
						throw re;						
					}
				});
			}
			super.buildProject(currentTarget);
		}
		@Override
		protected boolean createWorkingDirectories(File path) throws ConfigException {
			return createWorkingDirectoriesSuccess;
		}
		@Override
		protected void invokeBuilder(ProjectConfigDto currentTarget) throws TimeoutException, KilledException, BuildFailedException, ConfigException, IOException, StoreException {
			if (be != null) {
				throw be;
			}
			synchronized(this) {
				invokedBuild = true;
				notifyAll();
			}
			if (errorMessage != null) {
				buildDetailCallback.reportError(errorMessage, null, null, null);
			}
			if (warningMessage != null) {
				buildDetailCallback.reportWarning(warningMessage, null, null, null);
			}
			if (metric != null) {
				buildDetailCallback.addMetric(metric);
			}
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					gotInterrupt = true;
					Thread.currentThread().interrupt();
				}
			} else {
				super.invokeBuilder(currentTarget);
			}
		}
		@Override
		protected ProjectStatusDto createBuildStatus(ProjectConfigDto currentTarget) {
			final ProjectStatusDto projectStatusDto = super.createBuildStatus(currentTarget);
			if (suppressStartDate) {
				projectStatusDto.setStartDate(null);
			}
			return projectStatusDto;
		}
		@Override
		protected void determineBuildReason(ProjectConfigDto currentTarget) throws ConfigException, ProjectUpToDateException {
			if (projectIsUpToDate) {
				throw new ProjectUpToDateException();
			}
		}
	};
	
	BuildManager mgr = createStrictMock(BuildManager.class);
	ProjectManager projectMgr = createStrictMock(ProjectManager.class);
	RepositoryAdaptor ra = createStrictMock(RepositoryAdaptor.class);
	BuildTool tool = createStrictMock(BuildTool.class);

	UUID id = UUID.randomUUID();
	
	StoreStub store = new StoreStub(null) {
		@Override
		public File getBuildLog(String projectName, UUID diffId)
				throws StoreException {
			return logFile;
		}
		@Override
		public File getChangeLog(String projectName, UUID diffId)
				throws StoreException {
			return diffFile;
		}
		@Override
		public Long loadAverageBuildTimeMillis(String name, UpdateType updateType) {
			return estimatedBuildTimeMillis;
		}
		@Override
		public ProjectStatusDto loadMostRecentBuildOutcomeByTagName(
				String projectName, String tagName) {
			assertEquals(project.getName(), projectName);
			assertEquals(project.getRepositoryTagName(), tagName);
			return previousStatusByTagName;
		}
		@Override
		public ProjectStatusDto loadMostRecentBuildOutcomeByWorkDir(
				String projectName, String workDir) {
			assertEquals(project.getName(), projectName);
			assertEquals(project.getWorkDir(), workDir);
			return previousStatusByWorkDir;
		}
	};
	
	BuildDaemonInfoDto info = new BuildDaemonInfoDto();
	ProjectStatusDto previousStatus = new ProjectStatusDto();

	RevisionTokenDto rev0 = new RevisionTokenDto(0l);
	RevisionTokenDto rev1 = new RevisionTokenDto(1l);

	Throwable error;
	
	WorkingCopyUpdateExpert updateExpert = new WorkingCopyUpdateExpert() {
		UpdateType determineUpdateStrategy(ProjectConfigDto currentTarget, ProjectStatusDto previousStatus) {
			if (previousStatusByWorkDir != null) {
				assertSame(previousStatusByWorkDir, previousStatus);
			}

			return updateType != null ? updateType : UpdateType.Full;
		}
	};
	
	@Override
	public void setUp() throws Exception {
		checkOrder(false);
		
		UUIDUtils.setForcedUUID(id);
		
		builder.setWorkingCopyUpdateExpert(updateExpert);
		builder.setConfigurationStore(store);
		builder.setBuildOutcomeStore(store);
		builder.setBuildManager(mgr);
		builder.setProjectManager(projectMgr);
		builder.setDiffsEnabled(true);
		
		builder.init();
		
		expect(ra.getRepositoryUrl()).andReturn("http://localhost").anyTimes();
		expect(projectMgr.getPluginModificationDate((String)anyObject())).andReturn(null).anyTimes();

		mgr.registerBuildStatus((BuildDaemonInfoDto)notNull(), (ProjectBuilder) notNull(),
				(ProjectConfigDto)notNull(), (ProjectStatusDto)notNull());
		expectLastCall().anyTimes();
		
		info.setHostname(InetAddress.getLocalHost());
		info.setName("mock");

		previousStatus.setStatus(Status.PASS);
		previousStatus.setRevision(rev0);
		previousStatus.setBuildNumber(42);
		previousStatus.setTagName("trunk");
		
		buildToolStatus.setName("a name");
		buildToolStatus.setRevision(rev0);
		buildToolStatus.setStatus(Status.BUILDING);
		buildToolStatus.setTagName("trunk");
		buildToolStatus.setId(id);
		buildToolStatus.setDiffId(id);
		buildToolStatus.setBuildLogId(id);
		buildToolStatus.setRepositoryUrl("http://localhost");
		buildToolStatus.setErrors(new ArrayList<BuildMessageDto>());
		buildToolStatus.setWarnings(new ArrayList<BuildMessageDto>());
		buildToolStatus.setMetrics(new ArrayList<MetricDto>());
		buildToolStatus.setBuildNumber(0);
		buildToolStatus.setWorkDir("dir");
		buildToolStatus.setWorkDirSupportsIncrementalUpdate(true);
		
		logFile.deleteOnExit();
		diffFile.deleteOnExit();
	}

	public void testSetsStartDate() throws Exception {
		suppressStartDate = false;
		
		project = new ProjectConfigDto();
		project.setName("foo");

		ProjectStatusDto status = builder.createBuildStatus(project);
		assertNotNull(status.getStartDate());
	}
	
	public void testKillProjectDuringBuild() throws Throwable {
		sleepTime = 10000;
		
		project = new ProjectConfigDto();
		project.setName("foo");
		project.setWorkDir("dir");
		
		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(mgr.getLatestStatus("foo")).andReturn(null);
		expect(ra.getLatestRevision(null)).andReturn(rev1);
		
		ra.createWorkingCopy(new File(project.getWorkDir()).getAbsoluteFile(), buildDetailCallback);
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev1, "trunk", Status.ERROR, "messages.build.killed", new String[] {"a user"}, null, "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, false));

		replay();

		assertFalse(builder.isBuilding());

		final Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					builder.build(info, project, buildDetailCallback);
				} catch (Throwable e) {
					error = e;
				}
			}
		};

		thread.start();

		synchronized (builder) {
			if (!invokedBuild) {
				builder.wait(1000);
			}
		}
		
		assertTrue(builder.isBuilding());
		
		synchronized (builder) {
			if (!invokedBuild) {
				builder.wait(1000);
			}
		}

		builder.abortCurrentBuild(false, "a user");

		thread.join();
		
		if (error != null) {
			throw error;
		}
		
		verify();
		
		assertTrue("did not interrupt", gotInterrupt);
		
		assertFalse(builder.isBuilding());
	}

	public void testInformsBuildManagerWhenProjectUpToDate() throws Exception {
		projectIsUpToDate = true;
		
		project = new ProjectConfigDto();
		project.setWorkDir("a");
		project.setName("foo");

		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(rev0)).andReturn(rev0);
		
		expect(mgr.getLatestStatus("foo")).andReturn(previousStatus);
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 43, rev0, "trunk", Status.UP_TO_DATE, null, null, null, "http://localhost", false, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, true));

		checkBuild();
	}

	public void testBuildProjectPreviousNull() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");

		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));

		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(null)).andReturn(rev0);

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);

		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		//buildToolStatus.setBuildReasonKey("messages.build.reason.repository.changes");
		buildToolStatus.setWorkDir("a");
		buildToolStatus.setDiffId(null);
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev0, "trunk", Status.PASS, null, null, null, "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, false));
		
		checkBuild();
		
		assertTrue(invokedBuild);
	}
	public void testBuildProjectRequestedByUser() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		project.setRequestedBy("Deborah");

		expect(projectMgr
		.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(null)).andReturn(rev0);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);

		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		buildToolStatus.setBuildReasonKey("messages.build.reason.repository.changes");
		buildToolStatus.setRequestedBy("Deborah");
		buildToolStatus.setDiffId(null);
		
		tool.buildProject(
				eq(project),
				(ProjectStatusDto) notNull(),
				eq(logFile),
				(BuildDetailCallback)notNull());
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev0, "trunk", Status.PASS, null, null, null, "http://localhost", true, "Deborah", null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, false));
		
		checkBuild();
	}
	public void testCapturesErrorsAndWarnings() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		project.setRequestedBy("Deborah");

		
		final List<BuildPhase> listenedPhases = new ArrayList<BuildPhase>();
		final List<BuildMessageDto> listenedErrors = new ArrayList<BuildMessageDto>();
		final List<BuildMessageDto> listenedWarnings = new ArrayList<BuildMessageDto>();
		
		builder.addBuildStatusListener(new BuildStatusListener() {
			public void onBuildPhaseChanged(Object source, BuildPhase phase) {
				listenedPhases.add(phase);
			}
			public void onErrorLogged(Object source, BuildMessageDto error) {
				listenedErrors.add(error);
			}
			public void onWarningLogged(Object source, BuildMessageDto warning) {
				listenedWarnings.add(warning);
			}
		});
		
		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(null)).andReturn(rev0);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);

		expect(projectMgr.getBuildTool(project)).andReturn(tool);

		errorMessage = "An error occurred!";
		warningMessage = "This api is deprecated.";
		
		buildToolStatus.setRequestedBy("Deborah");
		buildToolStatus.getErrors().add(new BuildMessageDto(errorMessage, null, null, null));
		buildToolStatus.getWarnings().add(new BuildMessageDto(warningMessage, null, null, null));
		buildToolStatus.setWorkDir("a");
		buildToolStatus.setDiffId(null);
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		

		final ProjectStatusDto outcome = createFakeBuildOutcome(project.getName(), 0, rev0, "trunk", Status.PASS, null, null, null, "http://localhost", true, "Deborah", null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, false);
		
		outcome.getErrors().add(new BuildMessageDto(errorMessage, null, null, null));
		outcome.getWarnings().add(new BuildMessageDto(warningMessage, null, null, null));
		
		mgr.targetCompleted(info, project, outcome);
		
		checkBuild();
		
		assertTrue("expected listenedPhases.size() > 0 but was " + listenedPhases.size(), listenedPhases.size() > 0);
		assertTrue("expected listenedErrors.size() > 0 but was " + listenedErrors.size(), listenedErrors.size() > 0);
		assertTrue("expected listenedWarnings.size() > 0 but was " + listenedWarnings.size(), listenedWarnings.size() > 0);
	}
	public void testCapturesMetrics() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		project.setRequestedBy("Deborah");

		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(null)).andReturn(rev0);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);

		expect(projectMgr.getBuildTool(project)).andReturn(tool);

		metric = new MetricDto();
		metric.setMessageKey("m.key");
		metric.setValue("0.99");
		metric.setType(MetricType.PERCENT);
		buildToolStatus.setDiffId(null);
		
		buildToolStatus.setRequestedBy("Deborah");
		buildToolStatus.setWorkDir("a");
		buildToolStatus.getMetrics().add(metric);
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		

		final ProjectStatusDto outcome = createFakeBuildOutcome(project.getName(), 0, rev0, "trunk", Status.PASS, null, null, null, "http://localhost", true, "Deborah", null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, false);
		
		outcome.getMetrics().add(metric);
		
		mgr.targetCompleted(info, project, outcome);
		
		checkBuild();
	}
	public void testSupressErrors() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		project.setRequestedBy("Deborah");
		project.setSuppressErrors(true);
		
		expect(projectMgr
				.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(null)).andReturn(rev0);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);

		expect(projectMgr.getBuildTool(project)).andReturn(tool);

		errorMessage = "An error occurred!";
		warningMessage = "This api is deprecated.";
		
		buildToolStatus.setRequestedBy("Deborah");
		buildToolStatus.getWarnings().add(new BuildMessageDto(warningMessage, null, null, null));
		buildToolStatus.setWorkDir("a");
		buildToolStatus.setDiffId(null);
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		

		final ProjectStatusDto outcome = createFakeBuildOutcome(project.getName(), 0, rev0, "trunk", Status.PASS, null, null, null, "http://localhost", true, "Deborah", null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, false);
		
		outcome.getWarnings().add(new BuildMessageDto(warningMessage, null, null, null));
		
		mgr.targetCompleted(info, project, outcome);
		
		checkBuild();
	}
	public void testGetsChangeLog() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		
		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(rev0)).andReturn(rev1);
		
		final ProjectStatusDto prevStatus = new ProjectStatusDto();
		prevStatus.setRevision(rev0);
		prevStatus.setTagName("trunk");
		prevStatus.setStatus(Status.ERROR);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(prevStatus);

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);
		
		ra.getChangeLog(eq(rev0), eq(rev1), (OutputStream) notNull());
		expectLastCall().andReturn(new ChangeLogDto());

		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		buildToolStatus.setTagName("trunk");
		buildToolStatus.setRevision(rev1);
		buildToolStatus.setChangeLog(new ChangeLogDto());
		buildToolStatus.setWorkDir("a");
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev1, "trunk", Status.PASS, null, null, new ChangeLogDto(), "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, true));
		
		checkBuild();
	}
	public void testGetsChangeLogSkipWhenFlagIsFalse() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		
		builder.setDiffsEnabled(false);
		
		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(rev0)).andReturn(rev1);
		
		final ProjectStatusDto prevStatus = new ProjectStatusDto();
		prevStatus.setRevision(rev0);
		prevStatus.setTagName("trunk");
		prevStatus.setStatus(Status.ERROR);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(prevStatus);

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);
		
		ra.getChangeLog(eq(rev0), eq(rev1), (OutputStream) eq(null));
		expectLastCall().andReturn(new ChangeLogDto());

		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		buildToolStatus.setTagName("trunk");
		buildToolStatus.setRevision(rev1);
		buildToolStatus.setChangeLog(new ChangeLogDto());
		buildToolStatus.setWorkDir("a");
		buildToolStatus.setDiffId(null);
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		
		final ProjectStatusDto fakeBuildOutcome = createFakeBuildOutcome(project.getName(), 0, rev1, "trunk", Status.PASS, null, null, new ChangeLogDto(), "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, false);
				
		mgr.targetCompleted(info, project, fakeBuildOutcome);
		
		checkBuild();
	}
	public void testGetsChangeLogWithRevisionFromLastBuildWithSameTag() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		
		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(rev0)).andReturn(rev1);
		
		previousStatus.setRevision(rev0);
		previousStatus.setTagName("tags/not-trunk");
		previousStatus.setStatus(Status.ERROR);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(previousStatus);

		previousStatusByTagName = new ProjectStatusDto();
		previousStatusByTagName.setTagName("trunk");
		previousStatusByTagName.setRevision(rev0);
		
		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);
		
		ra.getChangeLog(eq(rev0), eq(rev1), (OutputStream) notNull());
		expectLastCall().andReturn(new ChangeLogDto());

		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		buildToolStatus.setTagName("trunk");
		buildToolStatus.setRevision(rev1);
		buildToolStatus.setChangeLog(new ChangeLogDto());
		buildToolStatus.setWorkDir("a");
		buildToolStatus.setBuildNumber(previousStatus.getBuildNumber() + 1);
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 43, rev1, "trunk", Status.PASS, null, null, new ChangeLogDto(), "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, true));
		
		checkBuild();
	}
	public void testIncremental() throws Exception {
		updateType = UpdateType.Incremental;
		estimatedBuildTimeMillis = 11242341234l;
		
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		project.setUpdateStrategy(ProjectConfigDto.UpdateStrategy.IncrementalAlways);
		
		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(rev0)).andReturn(rev1);
		
		final ProjectStatusDto prevStatus = new ProjectStatusDto();
		prevStatus.setRevision(rev0);
		prevStatus.setTagName("trunk");
		prevStatus.setStatus(Status.ERROR);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(prevStatus);

		ra.updateWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);
		
		ra.getChangeLog(eq(rev0), eq(rev1), (OutputStream)notNull());
		expectLastCall().andReturn(new ChangeLogDto());

		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		buildToolStatus.setTagName("trunk");
		buildToolStatus.setRevision(rev1);
		buildToolStatus.setUpdateType(ProjectStatusDto.UpdateType.Incremental);
		buildToolStatus.setChangeLog(new ChangeLogDto());
		buildToolStatus.setWorkDir("a");
		buildToolStatus.setEstimatedBuildTimeMillis(estimatedBuildTimeMillis);
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev1, "trunk", Status.PASS, null, null, new ChangeLogDto(), "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Incremental, project.getWorkDir(), true, estimatedBuildTimeMillis, true));
		
		checkBuild();
	}
	public void testIncrementalGetsPreviousBuildByWorkDir() throws Exception {
		updateType = UpdateType.Incremental;
		estimatedBuildTimeMillis = 11242341234l;
		
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		project.setUpdateStrategy(ProjectConfigDto.UpdateStrategy.IncrementalAlways);
		
		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(rev0)).andReturn(rev1);
		
		final ProjectStatusDto prevStatus = new ProjectStatusDto();
		prevStatus.setRevision(rev0);
		prevStatus.setTagName("trunk");
		prevStatus.setStatus(Status.ERROR);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(prevStatus);

		previousStatusByWorkDir = new ProjectStatusDto();
		previousStatusByWorkDir.setStatus(Status.PASS);
		previousStatusByWorkDir.setWorkDir("a");
		
		ra.updateWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);
		
		ra.getChangeLog(eq(rev0), eq(rev1), (OutputStream)notNull());
		expectLastCall().andReturn(new ChangeLogDto());

		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		buildToolStatus.setTagName("trunk");
		buildToolStatus.setRevision(rev1);
		buildToolStatus.setUpdateType(ProjectStatusDto.UpdateType.Incremental);
		buildToolStatus.setChangeLog(new ChangeLogDto());
		buildToolStatus.setWorkDir("a");
		buildToolStatus.setEstimatedBuildTimeMillis(estimatedBuildTimeMillis);
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev1, "trunk", Status.PASS, null, null, new ChangeLogDto(), "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Incremental, project.getWorkDir(), true, estimatedBuildTimeMillis, true));
		
		checkBuild();
	}
	public void testChangeLogUsesLastKnownRevisionWhenPrevNull() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		
		expect(projectMgr
		.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(rev0)).andReturn(rev1);
		
		final ProjectStatusDto prevStatus = new ProjectStatusDto();
		prevStatus.setLastKnownRevision(rev0);
		prevStatus.setRevision(null);
		prevStatus.setTagName("trunk");
		prevStatus.setStatus(Status.ERROR);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(prevStatus);

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);
		
		ra.getChangeLog(eq(rev0), eq(rev1), (OutputStream)notNull());
		expectLastCall().andReturn(new ChangeLogDto());

		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		buildToolStatus.setTagName("trunk");
		buildToolStatus.setRevision(rev1);
		buildToolStatus.setChangeLog(new ChangeLogDto());
		buildToolStatus.setWorkDir("a");
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());

		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev1, "trunk", Status.PASS, null, null, new ChangeLogDto(), "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, true));
		
		checkBuild();
	}
	public void testBuildProjectWithTag() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		project.setWorkDir("a");
		project.setRepositoryTagName("rc1");
		
		expect(projectMgr
		.getRepositoryAdaptor(project)).andReturn(ra);

		ra.setTagName("rc1");
		expect(ra.getLatestRevision(null)).andReturn(rev1);
		
		final ProjectStatusDto prevStatus = new ProjectStatusDto();
		prevStatus.setRevision(rev0);
		prevStatus.setStatus(Status.PASS);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(prevStatus);

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);
		
		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		buildToolStatus.setTagName("rc1");
		buildToolStatus.setRevision(rev1);
		buildToolStatus.setWorkDir("a");
		buildToolStatus.setDiffId(null);
		
		tool.buildProject(
				eq(project),
				eq(buildToolStatus),
				eq(logFile),
				(BuildDetailCallback)notNull());
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev1, "rc1", Status.PASS, null, null, null, "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, false));
		
		checkBuild();
	}
	public void testBuildFailsWithNoBuildTarget() throws Exception {
		project = new ProjectConfigDto();
		project.setWorkDir("a");

		expect(projectMgr.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(null)).andReturn(rev0);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));

		ra.createWorkingCopy(new File("a").getAbsoluteFile(), buildDetailCallback);

		tool = new BuildTool() {
			public void buildProject(ProjectConfigDto projectConfig, ProjectStatusDto buildStatus, File logFile, BuildDetailCallback buildDetailCallback) throws BuildFailedException, ConfigException {
				throw new BuildFailedException("none", null, 0);
			}
		};
		
		expect(projectMgr.getBuildTool(project)).andReturn(tool);
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev0, "trunk", Status.FAIL, "messages.build.failure", new String[] {"none"}, null, "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), true, estimatedBuildTimeMillis, false));
		
		sleepTime = 0;
		
		checkBuild();
	}
	public void testBuildProjectNullOrBlankWorkDir() throws Exception {
		project = new ProjectConfigDto();
		project.setWorkDir("");

		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, null,
				null, Status.ERROR, "messages.build.null.work.dir", null, null, null, true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), false, estimatedBuildTimeMillis, true));
		

		checkBuild();
	}
	public void testBuildProjectCannotCreateWorkDir() throws Exception {
		createWorkingDirectoriesSuccess = false;

		project = new ProjectConfigDto();
		project.setWorkDir("hello");

		expect(projectMgr
		.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(null)).andReturn(rev0);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));

		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev0,
				"trunk", Status.ERROR, "errors.cannot.create.dir", new Object[] {new File("hello").getCanonicalPath()}, null, "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), false, estimatedBuildTimeMillis, true));
		
		checkBuild();
	}
	public void testBuildProjectValidatesWorkingCopyBeforeDelete() throws Exception {
		final File invalid = new File(".").getCanonicalFile();

		createWorkingDirectoriesSuccess = false;

		project = new ProjectConfigDto();
		project.setWorkDir(invalid.getPath());

		expect(projectMgr
		.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(null)).andReturn(rev0);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));

		expect(ra.isWorkingCopy(invalid)).andReturn(true);
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev0,
				"trunk", Status.ERROR, "errors.cannot.create.dir", new Object[] {invalid.getCanonicalPath()}, null, "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), false, estimatedBuildTimeMillis, true));
		
		checkBuild();
	}
	public void testBuildProjectRefusesToDeleteNonWorkingCopy() throws Exception {
		final File invalid = new File(".").getCanonicalFile();

		createWorkingDirectoriesSuccess = false;

		project = new ProjectConfigDto();
		project.setWorkDir(invalid.getPath());

		expect(projectMgr
		.getRepositoryAdaptor(project)).andReturn(ra);

		expect(ra.getTagName()).andReturn("trunk");
		expect(ra.getLatestRevision(null)).andReturn(rev0);
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(((ProjectStatusDto) null));

		expect(ra.isWorkingCopy(invalid)).andReturn(false);
		
		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 0, rev0,
				"trunk", Status.ERROR, "errors.wont.delete.non.working.copy", new Object[] {invalid.getCanonicalPath()}, null, "http://localhost", true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), false, estimatedBuildTimeMillis, true));
		
		checkBuild();
	}

	public void testGetLoc() throws Exception {
		project = new ProjectConfigDto();
		project.setName("a");
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(previousStatus);
		
		re = new RepositoryException(new SQLException(
				"table or view does not exist"));

		mgr.targetCompleted(info, project, createFakeBuildOutcome(project.getName(), 43, null,
				null, Status.ERROR, "messages.repository.error",
				new Object[]{"table or view does not exist"}, null, null, true, null, null, null,
				ProjectStatusDto.UpdateType.Full, project.getWorkDir(), false, estimatedBuildTimeMillis, true));

		checkBuild();
	}
	public void testTreatsGeneralExceptionAsError()
			throws Exception {
		project = new ProjectConfigDto();
		project.setName("a name");
		
		expect(mgr.getLatestStatus(project.getName())).andReturn(previousStatus);

		re = new RuntimeException("this should not have happened");

		final ProjectStatusDto completedOutcome = createFakeBuildOutcome(project.getName(), 43, null,
				null, Status.ERROR, "messages.build.uncaught.exception",
				new String[]{project.getName(), re.getMessage(), BuildPhase.Build.name()},
				null, null, true, null, null, null, ProjectStatusDto.UpdateType.Full, project.getWorkDir(), false, estimatedBuildTimeMillis, true);
		
		mgr.targetCompleted((BuildDaemonInfoDto)anyObject(), eq(project), eq(completedOutcome));

		checkBuild();
	}

	private void checkBuild() throws Exception {
		replay();

		try {
			builder.build(info, project, buildDetailCallback);
		} catch (Exception e) {
			verify();
			throw e;
		}
		verify();
	}

	private ProjectStatusDto createFakeBuildOutcome(String name, int buildNumber, RevisionTokenDto rev, String tagName, Status status, String key, Object[] objects, ChangeLogDto changeLog, String repoUrl, boolean statusChanged, String requestedBy, String reasonKey, String reasonArg, UpdateType updateType, String workDir, boolean workDirSupportsIncrementalUpdate, Long estimatedBuildTimeMillis, boolean diffAvailable) {
		final ProjectStatusDto dto = new ProjectStatusDto();
		dto.setBuildNumber(buildNumber);
		dto.setId(id);
		dto.setDiffId(diffAvailable ? id : null);
		dto.setBuildLogId(id);
		dto.setName(name);
		dto.setRevision(rev);
		dto.setStatus(status);
		dto.setMessageKey(key);
		dto.setMessageArgs(objects);
		dto.setChangeLog(changeLog);
		dto.setTagName(tagName);
		dto.setRepositoryUrl(repoUrl);
		dto.setStatusChanged(statusChanged);
		dto.setRequestedBy(requestedBy);
		dto.setErrors(new ArrayList<BuildMessageDto>());
		dto.setWarnings(new ArrayList<BuildMessageDto>());
		dto.setMetrics(new ArrayList<MetricDto>());
		dto.setBuildReasonKey(reasonKey);
		dto.setUpdateType(updateType);
		dto.setWorkDir(workDir);
		dto.setWorkDirSupportsIncrementalUpdate(workDirSupportsIncrementalUpdate);
		dto.setEstimatedBuildTimeMillis(estimatedBuildTimeMillis);
		
		if (reasonArg != null) {
			dto.setBuildReasonArgs(new String[] {reasonArg});
		}
		
		return dto;
	}
}
