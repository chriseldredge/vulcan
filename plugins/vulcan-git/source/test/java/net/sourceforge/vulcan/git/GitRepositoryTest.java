/*
 * Vulcan Build Manager
 * Copyright (C) 2005-2012 Chris Eldredge
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
package net.sourceforge.vulcan.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.vulcan.EasyMockTestCase;
import net.sourceforge.vulcan.core.BuildDetailCallback;
import net.sourceforge.vulcan.core.support.FileSystem;
import net.sourceforge.vulcan.dto.ChangeLogDto;
import net.sourceforge.vulcan.dto.ProjectConfigDto;
import net.sourceforge.vulcan.dto.ProjectStatusDto;
import net.sourceforge.vulcan.dto.RepositoryTagDto;
import net.sourceforge.vulcan.dto.RevisionTokenDto;
import net.sourceforge.vulcan.exception.RepositoryException;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.filefilter.IOFileFilter;

public class GitRepositoryTest extends EasyMockTestCase {
	GitConfig globals;
	ProjectConfigDto project;
	GitRepository repo;
	FileSystem fileSystem;
	
	Invoker invoker;
	BuildDetailCallback buildDetail;
	
	File workDir;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		globals = new GitConfig();
		
		GitProjectConfig settings = new GitProjectConfig();
		settings.setRemoteRepositoryUrl("http://localhost/git/repo1");
		
		project = new ProjectConfigDto();
		project.setWorkDir("work_dir");
		
		workDir = new File(project.getWorkDir());
		
		project.setRepositoryAdaptorConfig(settings);
		invoker = createMock(Invoker.class);
		buildDetail = createMock(BuildDetailCallback.class);
		
		repo = new GitRepository(project, globals) {
			@Override
			protected Invoker createInvoker() {
				return invoker;
			}
		};
		
		fileSystem = createMock(FileSystem.class);
		repo.setFileSystem(fileSystem);
		
		expect(fileSystem.directoryExists(workDir)).andReturn(true).anyTimes();
	}
	
	public void testCreateInvokerSetsExecutable() throws Exception {
		globals.setExecutable("custom-git.exe");
		
		ProcessInvoker invoker = (ProcessInvoker) new GitRepository(project, globals).createInvoker();
		
		assertEquals("custom-git.exe", invoker.getExecutable());
	}
	
	public void testGetSelectedBranchDefaultWhenBlank() throws Exception {
		repo.setTagOrBranch("");
		
		assertEquals("master", repo.getSelectedBranch());
	}
	
	public void testGetSelectedBranchOther() throws Exception {
		repo.setTagOrBranch("other");
		
		assertEquals("other", repo.getSelectedBranch());
	}
	
	public void testHasIncomingChangesFromRemote() throws Exception {
		expectIncomingCommand();
		
		returnSuccess();
		
		replay();
		
		//TODO: vulcan-mercurial has true here
		assertEquals("hasIncomingChangesFromRemote", false, repo.hasIncomingChangesFromRemote());
		
		verify();
	}

	public void testHasIncomingChangesFromRemoteUsesTagNoChanges() throws Exception {
		repo.getSettings().setBranch("v2");
		
		expectIncomingCommand();
		
		returnFailure();
		
		replay();
		
		assertEquals("hasIncomingChangesFromRemote", false, repo.hasIncomingChangesFromRemote());
		
		verify();
	}

	public void testHasIncomingChangesFromRemoteFalseOnNoRemoteRepo() throws Exception {
		repo.getSettings().setRemoteRepositoryUrl("");
		
		replay();
		
		assertEquals("", repo.getSettings().getRemoteRepositoryUrl());
		assertEquals("hasIncomingChangesFromRemote", false, repo.hasIncomingChangesFromRemote());
		
		verify();
	}

	public void testGetLatestRevision() throws Exception {
		expectLogCommand();
		
		returnSuccessWithOutput("8:9bd7475fd513\n");
		
		replay();
		
		RevisionTokenDto rev = repo.getLatestRevision(null);
		
		verify();
		
		assertNotNull("getLatestRevision", rev);
		assertEquals("8", rev.getRevision());
		assertEquals("8:9bd7475fd513", rev.getLabel());
	}
	
	public void testHasIncomingChangesComparesRevisions() throws Exception {
		expectLogCommand();
		
		returnSuccessWithOutput("8:9bd7475fd513\n");
		
		replay();
		
		ProjectStatusDto previous = new ProjectStatusDto();
		previous.setRevision(new RevisionTokenDto(7L, "7:different"));
		
		assertEquals("hasIncomingChanges", true, repo.hasIncomingChanges(previous));
		
		verify();
	}
	
	public void testHasIncomingChangesGetsIncoming() throws Exception {
		expectLogCommand();
		
		returnSuccessWithOutput("8:9bd7475fd513\n");
		
		expectIncomingCommand();
		
		returnSuccess();
		
		replay();
		
		ProjectStatusDto previous = new ProjectStatusDto();
		previous.setRevision(new RevisionTokenDto(8L, "8:9bd7475fd513"));
		
		//TODO: vulcan-mercurial has true here
		assertEquals("hasIncomingChanges", false, repo.hasIncomingChanges(previous));
		
		verify();
	}
	
	public void testHasIncomingChangesFalseOnNoRemoteRepository() throws Exception {
		repo.getSettings().setRemoteRepositoryUrl("");
		
		expectLogCommand();
		
		returnSuccessWithOutput("8:9bd7475fd513\n");
		
		replay();
		
		ProjectStatusDto previous = new ProjectStatusDto();
		previous.setRevision(new RevisionTokenDto(8L, "8:9bd7475fd513"));
		
		assertEquals("hasIncomingChanges", false, repo.hasIncomingChanges(previous));
		
		verify();
	}

	public void testIsWorkingCopy() throws Exception {
		expectSummaryCommand();
		
		returnSuccess();
		
		replay();
		
		assertEquals("isWorkingCopy", true, repo.isWorkingCopy());
		
		verify();
	}

	public void testIsWorkingCopyFalseOnMissingDir() throws Exception {
		reset();
		
		expect(fileSystem.directoryExists(workDir)).andReturn(false);
		
		replay();
		
		assertEquals("isWorkingCopy", false, repo.isWorkingCopy());
		
		verify();
	}

	public void testIsWorkingCopyFalseOnCommandFailure() throws Exception {
		expectSummaryCommand();
		
		expectLastCall().andThrow(new IOException("not a working copy"));
		
		replay();
		
		assertEquals("isWorkingCopy", false, repo.isWorkingCopy());
		
		verify();
	}
	
	public void testPrepareMakesClone() throws Exception {
		expectSummaryCommand();
		
		returnFailure();
	
		expectCloneCommand();
		
		returnSuccess();
		
		replay();
		
		repo.prepareRepository(buildDetail);
		
		verify();
	}

	public void testPrepareThrowsOnMissingLocalRepoAndNoRemoteRepo() throws Exception {
		repo.getSettings().setRemoteRepositoryUrl("");
		
		expectSummaryCommand();
		
		returnFailure();
	
		replay();
		
		try {
			repo.prepareRepository(buildDetail);
			fail("expected RepositoryException");
		} catch (RepositoryException e) {
			
		}
		
		verify();
	}
	
	public void testPrepareDoesNotPullOnNoRemoteRepo() throws Exception {
		repo.getSettings().setRemoteRepositoryUrl("");
		
		expectSummaryCommand();
		
		returnSuccess();
	
		replay();
		
		repo.prepareRepository(buildDetail);
		
		verify();
	}

	public void testPreparePullsWhenRepositoryPresent() throws Exception {
		expectSummaryCommand();
		
		returnSuccess();
	
		expectPullCommand();
		
		returnSuccess();
		
		replay();
		
		repo.prepareRepository(buildDetail);
		
		verify();
	}
	
	public void testUpdateWorkingCopy() throws Exception {
		expectUpdateCommand();
		
		returnSuccess();
		
		replay();
		
		repo.updateWorkingCopy(buildDetail);
		
		verify();
	}
	
	public void testCreatePristineWorkingCopy() throws Exception {
		buildDetail.setDetailMessage("git.activity.clean", null);
		
		invoker.invoke("clean", workDir, "-f", "-d", "-x");
		returnSuccess();
		
		buildDetail.setDetailMessage("git.activity.checkout", null);
		invoker.invoke("checkout", workDir, "-f", "-B", "master", "origin/master");
		
		returnSuccess();

		replay();
		
		repo.createPristineWorkingCopy(buildDetail);
		
		verify();
	}
	
	public void testGetChangeLogEmptyNullDiffStream() throws Exception {
		invoker.invoke("log", workDir, "--name-status", "--pretty=\"%x1e%h%x1f%H%x1f%cn%x1f%ce%x1f%ci%x1f%d%x1f%B%x1f\"", "123..456");
		returnSuccessWithOutput("<log/>");
		
		replay();
		
		final ChangeLogDto changeLog = repo.getChangeLog(new RevisionTokenDto(123L, "123:9bd7475fd513"), new RevisionTokenDto(456L, "456:9bd7475fd513"), null);
		
		verify();
		
		assertNotNull("return value", changeLog);
		assertNotNull("change sets", changeLog.getChangeSets());
	}
	
	public void testGetChangeLogEmptyFormatsRevision() throws Exception {
		invoker.invoke("log", workDir, "--name-status", "--pretty=\"%x1e%h%x1f%H%x1f%cn%x1f%ce%x1f%ci%x1f%d%x1f%B%x1f\"", "1123456..1123458");
		returnSuccessWithOutput("<log/>");
		
		replay();
		
		final ChangeLogDto changeLog = repo.getChangeLog(new RevisionTokenDto(1123456L, "1123456:9bd7475fd513"), new RevisionTokenDto(1123458L, "1123458:9bd7475fd513"), null);
		
		verify();
		
		assertNotNull("return value", changeLog);
		assertNotNull("change sets", changeLog.getChangeSets());
	}

	boolean throwOnClose;
	boolean closedFlag;
	
	final ByteArrayOutputStream diffOut = new ByteArrayOutputStream() {
		@Override
		public void close() throws IOException {
			if (throwOnClose) {
				throw new IOException("you can't close me!");
			}

			super.close();
			closedFlag = true;
		}
	};

	public void testGetChangeLogDiff() throws Exception {
		invoker.setOutputStream(diffOut);
		invoker.invoke("diff", workDir, "1..2");
		returnSuccess();
		
		invoker.invoke("log", workDir, "--name-status", "--pretty=\"%x1e%h%x1f%H%x1f%cn%x1f%ce%x1f%ci%x1f%d%x1f%B%x1f\"", "1..2");
		returnSuccessWithOutput("<log/>");
		
		replay();
		
		repo.getChangeLog(new RevisionTokenDto(1L, "1:9bd7475fd513"), new RevisionTokenDto(2L, "2:9bd7475fd513"), diffOut);
		
		verify();
		
		assertEquals("should close stream", true, closedFlag);
	}
	
	public void testGetChangeLogDiffRethrowsExceptionOnClose() throws Exception {
		invoker.setOutputStream(diffOut);
		invoker.invoke("diff", workDir, "123..456");
		returnSuccess();
		
		throwOnClose = true;
		
		replay();
		
		try {
			repo.getChangeLog(new RevisionTokenDto(123L, "123:9bd7475fd513"), new RevisionTokenDto(456L, "456:9bd7475fd513"), diffOut);
			fail("expected exception");
		} catch (RepositoryException e) {
		}
		
		verify();
	}
	
	public void testGetChangeLogDiffClosesStreamOnDiffError() throws Exception {
		invoker.setOutputStream(diffOut);
		invoker.invoke("diff", workDir, "123..456");
		expectLastCall().andThrow(new IOException());
		expect(invoker.getErrorText()).andReturn("invalid command");
		expect(invoker.getExitCode()).andReturn(-1);
		
		replay();
		
		try {
			repo.getChangeLog(new RevisionTokenDto(123L, "123:9bd7475fd513"), new RevisionTokenDto(456L, "456:9bd7475fd513"), diffOut);
		} catch (Exception ignore) {
		}
		
		verify();
		
		assertEquals("should close stream", true, closedFlag);
	}
	
	public void testGetTagsAndBranchesOneBranch() throws Exception {
		invoker.invoke("branch", workDir, "-r");
		returnSuccessWithOutput("origin/first_draft");
		invoker.invoke("tag", workDir, "-l");
		returnSuccessWithOutput("");

		
		replay();
		
		final List<RepositoryTagDto> actual = repo.getAvailableTagsAndBranches();
		
		verify();
		
		final List<RepositoryTagDto> expected = Arrays.asList(
				new RepositoryTagDto("first_draft", "first_draft"));
		
		assertEquals(expected, actual);
	}
	
	public void testGetTagsAndBranches() throws Exception {
		invoker.invoke("branch", workDir, "-r");
		returnSuccessWithOutput("origin/b1");
		invoker.invoke("tag", workDir, "-l");
		returnSuccessWithOutput("t1\nt2");
		
		replay();
		
		final List<RepositoryTagDto> actual = repo.getAvailableTagsAndBranches();
		
		verify();
		
		final List<RepositoryTagDto> expected = Arrays.asList(
				new RepositoryTagDto("b1", "b1"),
				new RepositoryTagDto("t1", "t1"),
				new RepositoryTagDto("t2", "t2")
				);
		
		assertEquals(expected, actual);
	}
	
	private void expectPurgeAllCommand() throws IOException {
		buildDetail.setDetailMessage("git.activity.purge", null);
		invoker.invoke("purge", workDir, "--all", "--config", "extensions.purge=");
	}

	private void expectUpdateCommandWithBuildDetail(String... extraArgs) throws IOException {
		buildDetail.setDetailMessage("git.activity.update", null);
		expectUpdateCommand(extraArgs);
	}
	
	private void expectUpdateCommand(String... extraArgs) throws IOException {
		List<String> args = new ArrayList<String>();
		args.add("-f");
		args.add("-B");
		args.add(repo.getSelectedBranch());
		args.add(repo.getSelectedRemoteBranch());
		
		invoker.invoke("checkout", workDir, args.toArray(new String[args.size()]));
	}

	private void expectCloneCommand() throws IOException {
		buildDetail.setDetailMessage("git.activity.clone", null);
		
		List<String> args = new ArrayList<String>();
		args.add("--no-checkout");
		args.add(repo.getSettings().getRemoteRepositoryUrl());
		args.add(".");
		
		invoker.invoke("clone", workDir, args.toArray(new String[args.size()]));
	}
	
	private void expectPullCommand() throws IOException {
		buildDetail.setDetailMessage("git.activity.pull", null);
		invoker.invoke("fetch", workDir, "--all");
	}

	private void expectSummaryCommand() throws IOException {
		invoker.invoke("status", workDir, "-sb");
	}

	private void expectLogCommand() throws IOException {
		invoker.invoke("log", workDir, new String[] {"-1", repo.getSelectedRemoteBranch(), "--pretty=format:%h:%H"});
	}

	private void expectIncomingCommand() throws IOException {
		invoker.invoke("fetch", workDir, new String[] { "--all" });
		returnSuccess();
		invoker.invoke("log", workDir, new String[] {"-1", repo.getSelectedBranch() + ".." + repo.getSelectedRemoteBranch(), "--format=oneline" });
	}

	private void returnSuccess() {
		returnSuccessWithOutput("");
	}

	private void returnSuccessWithOutput(String output) {
		expectLastCall().andReturn(new InvocationResult(output, "", true));
	}
	
	private void returnFailure() {
		expectLastCall().andReturn(new InvocationResult("", "", false));
	}
}
