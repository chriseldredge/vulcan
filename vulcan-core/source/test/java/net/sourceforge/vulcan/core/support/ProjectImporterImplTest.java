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
package net.sourceforge.vulcan.core.support;

import static org.easymock.EasyMock.anyBoolean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.io.File;
import java.io.IOException;

import net.sourceforge.vulcan.EasyMockTestCase;
import net.sourceforge.vulcan.PluginManager;
import net.sourceforge.vulcan.ProjectBuildConfigurator;
import net.sourceforge.vulcan.ProjectRepositoryConfigurator;
import net.sourceforge.vulcan.StateManager;
import net.sourceforge.vulcan.core.ConfigurationStore;
import net.sourceforge.vulcan.core.NameCollisionResolutionMode;
import net.sourceforge.vulcan.core.support.ProjectImporterImpl.PathInfo;
import net.sourceforge.vulcan.core.support.StateManagerImplTest.FakeRepoConfig;
import net.sourceforge.vulcan.dto.ConfigUpdatesDto;
import net.sourceforge.vulcan.dto.ProjectConfigDto;
import net.sourceforge.vulcan.exception.ConfigException;
import net.sourceforge.vulcan.exception.DuplicateNameException;
import net.sourceforge.vulcan.exception.PluginNotConfigurableException;
import net.sourceforge.vulcan.integration.BuildToolPlugin;
import net.sourceforge.vulcan.integration.PluginStub;
import net.sourceforge.vulcan.integration.RepositoryAdaptorPlugin;

import org.apache.commons.lang.ArrayUtils;

public class ProjectImporterImplTest extends EasyMockTestCase {
	ProjectImporterImpl importer = new ProjectImporterImpl() {
		@Override
		protected File createTempFile() throws IOException {
			return tmpFiles.remove(0);
		}
	};
	
	PluginManager pluginManager = createMock(PluginManager.class);
	StateManager stateManager = createMock(StateManager.class);
	ConfigurationStore store = createMock(ConfigurationStore.class);
	
	RepositoryAdaptorPlugin rap1 = createMock(RepositoryAdaptorPlugin.class);
	RepositoryAdaptorPlugin rap2 = createMock(RepositoryAdaptorPlugin.class);
	
	ProjectRepositoryConfigurator repoConfigurator = createMock(ProjectRepositoryConfigurator.class);
	
	BuildToolPlugin btp1 = createMock(BuildToolPlugin.class);
	BuildToolPlugin btp2 = createMock(BuildToolPlugin.class);
	
	List<String> existingProjectNames = Arrays.asList("fakeExistingProject1");
	
	ProjectBuildConfigurator buildConfiguratorMock = createMock(ProjectBuildConfigurator.class);
	ProjectBuildConfigurator buildConfigurator = new ProjectBuildConfigurator() {
		public void applyConfiguration(ProjectConfigDto projectConfig, String buildSpecRelativePath, List<String> existingProjectNames, boolean createSubprojects) {
			buildConfiguratorMock.applyConfiguration(projectConfig, buildSpecRelativePath, existingProjectNames, createSubprojects);
			if (playback) {
				projectConfig.setName(projectName);
				projectConfig.setWorkDir(workDir);
			}
		}
		public String getRelativePathToProjectBasedir() {
			return buildConfiguratorMock.getRelativePathToProjectBasedir();
		}
		public boolean isStandaloneProject() {
			return buildConfiguratorMock.isStandaloneProject();
		}
		public List<String> getSubprojectUrls() {
			return buildConfiguratorMock.getSubprojectUrls();
		}
		public boolean shouldCreate() {
			return true;
		}
	};
	
	List<RepositoryAdaptorPlugin> repositoryPlugins = Arrays.asList(rap1, rap2);
	List<BuildToolPlugin> buildToolPlugins = Arrays.asList(btp1, btp2);
	
	String url = "http://localhost/project/build_script.txt";
	String projectUrl = "http://localhost/project/";
	
	File tmpFile1;
	File tmpFile2;
	
	List<File> tmpFiles = new ArrayList<File>();
	
	IOException ioException = new IOException("foo");
	
	String defaultWorkDirPattern = "a workdir ${projectName} location";
	
	String projectName = "importedProject";
	String workDir = null;
	
	boolean pluginConfigChanged = true;
	
	boolean playback = false;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		tmpFile1 = File.createTempFile("vulcan-ProjectImporterImplTest", ".tmp");
		tmpFile2 = File.createTempFile("vulcan-ProjectImporterImplTest", ".tmp");
		
		tmpFile1.deleteOnExit();
		tmpFile2.deleteOnExit();
		
		tmpFiles.add(tmpFile1);
		tmpFiles.add(tmpFile2);
		
		importer.setPluginManager(pluginManager);
		importer.setStateManager(stateManager);
		importer.setConfigurationStore(store);
		
		expect(store.getWorkingCopyLocationPattern()).andReturn(defaultWorkDirPattern).anyTimes();
		expect(stateManager.getProjectConfigNames()).andReturn(existingProjectNames).anyTimes();
		
		expect(btp1.getName()).andReturn("a name").anyTimes();
		expect(btp2.getName()).andReturn("a name").anyTimes();
		expect(rap1.getName()).andReturn("a name").anyTimes();
		expect(rap2.getName()).andReturn("a name").anyTimes();
	}

	@Override
	public void replay() {
		super.replay();
		playback = true;
	}
	
	public void trainNoRepositorySupportsUrl() throws Exception {
		expect(pluginManager.getPlugins(RepositoryAdaptorPlugin.class))
			.andReturn(repositoryPlugins);
		expect(pluginManager.getPlugins(BuildToolPlugin.class))
			.andReturn(buildToolPlugins);

		expect(rap1.createProjectConfigurator(url, null, null)).andReturn(null);
		expect(rap2.createProjectConfigurator(url, null, null)).andReturn(null);
	}
	
	@TrainingMethod("trainNoRepositorySupportsUrl")
	public void testNoRepositorySupportsUrl() throws Exception {
		try {
			importer.createProjectsForUrl(url, null, null, false, NameCollisionResolutionMode.Abort, ArrayUtils.EMPTY_STRING_ARRAY, null, null);
			fail("Expected exception");
		} catch (ConfigException e) {
			assertEquals("errors.url.unsupported", e.getKey());
		}
	}
	
	public void trainWrapsIOExceptionDuringDownload() throws Exception {
		expect(pluginManager.getPlugins(RepositoryAdaptorPlugin.class))
			.andReturn(repositoryPlugins);
		expect(pluginManager.getPlugins(BuildToolPlugin.class))
			.andReturn(buildToolPlugins);

		expect(rap1.createProjectConfigurator(url, null, null)).andReturn(repoConfigurator);
		expect(rap1.getId()).andReturn("a.fake.repo.plugin");
		
		repoConfigurator.download(tmpFile1);
		expectLastCall().andThrow(ioException);
	}

	@TrainingMethod("trainWrapsIOExceptionDuringDownload")
	public void testWrapsIOExceptionDuringDownload() throws Exception {
		try {
			importer.createProjectsForUrl(url, null, null, false, NameCollisionResolutionMode.Abort, ArrayUtils.EMPTY_STRING_ARRAY, null, null);
			fail("Expected exception");
		} catch (ConfigException e) {
			assertSame(ioException, e.getCause());
			assertEquals("errors.import.download", e.getKey());
			assertEquals(ioException.getMessage(), e.getArgs()[0]);
		}
		assertFalse(tmpFile1.exists());
	}
	
	public void trainNoBuildToolSupportsFile() throws Exception {
		expect(pluginManager.getPlugins(RepositoryAdaptorPlugin.class))
			.andReturn(repositoryPlugins);
		expect(pluginManager.getPlugins(BuildToolPlugin.class))
			.andReturn(buildToolPlugins);

		expect(rap1.createProjectConfigurator(url, null, null)).andReturn(repoConfigurator);
		expect(rap1.getId()).andReturn("a.fake.repo.plugin");
		
		repoConfigurator.download(tmpFile1);
		
		expect(btp1.createProjectConfigurator(url, tmpFile1, null)).andReturn(null);
		expect(btp2.createProjectConfigurator(url, tmpFile1, null)).andReturn(null);
	}

	@TrainingMethod("trainNoBuildToolSupportsFile")
	public void testNoBuildToolSupportsFile() throws Exception {
		try {
			importer.createProjectsForUrl(url, null, null, false, NameCollisionResolutionMode.Abort, ArrayUtils.EMPTY_STRING_ARRAY, null, null);
			fail("expected exception");
		} catch (ConfigException e) {
			assertEquals("errors.build.file.unsupported", e.getKey());
		}
		
		assertFalse(tmpFile1.exists());
	}
	
	public void trainConfigures() throws Exception {
		expect(pluginManager.getPlugins(RepositoryAdaptorPlugin.class))
			.andReturn(repositoryPlugins);
	
		expect(rap1.createProjectConfigurator(url, null, null)).andReturn(repoConfigurator);
		expect(rap1.getId()).andReturn("a.fake.repo.plugin");
		
		repoConfigurator.download(tmpFile1);
		
		expect(pluginManager.getPlugins(BuildToolPlugin.class))
			.andReturn(buildToolPlugins);
		
		expect(btp1.createProjectConfigurator(url, tmpFile1, null)).andReturn(null);
		expect(btp2.createProjectConfigurator(url, tmpFile1, null)).andReturn(buildConfigurator);
		expect(btp2.getId()).andReturn("a.fake.build.plugin");
		
		final ProjectConfigDto projectConfig = new ProjectConfigDto();
		
		projectConfig.setRepositoryAdaptorPluginId("a.fake.repo.plugin");
		projectConfig.setBuildToolPluginId("a.fake.build.plugin");
		
		expect(buildConfigurator.getRelativePathToProjectBasedir()).andReturn(".");
		
		buildConfigurator.applyConfiguration(eq(projectConfig), eq("build_script.txt"), eq(existingProjectNames), anyBoolean());
		
		final ProjectConfigDto projectConfigWithName = (ProjectConfigDto) projectConfig.copy();
		projectConfigWithName.setName(projectName);
		projectConfigWithName.setWorkDir(workDir);
		
		repoConfigurator.applyConfiguration(projectConfigWithName, projectUrl);
	}

	public void trainSaves() throws Exception {
		expect(pluginManager.getPluginConfigInfo("a.fake.repo.plugin"))
			.andThrow(new PluginNotConfigurableException());

		final ProjectConfigDto projectConfig = new ProjectConfigDto();
		
		projectConfig.setRepositoryAdaptorPluginId("a.fake.repo.plugin");
		projectConfig.setBuildToolPluginId("a.fake.build.plugin");
		projectConfig.setName(projectName);
		projectConfig.setWorkDir("a workdir importedProject location");
		
		ConfigUpdatesDto updates = new ConfigUpdatesDto();
		updates.setNewProjectConfigs(Arrays.asList(projectConfig));
		stateManager.applyMultipleUpdates(updates);
	}
	
	public void trainPluginConfigUnchanged() {
		pluginConfigChanged = false;
	}
	
	public void trainSavesWithPluginConfig() throws Exception {
		final FakeRepoConfig repoConfig = new FakeRepoConfig("a.fake.repo.plugin");
		expect(pluginManager.getPluginConfigInfo("a.fake.repo.plugin"))
			.andReturn(repoConfig);

		final ProjectConfigDto projectConfig = new ProjectConfigDto();
		
		projectConfig.setRepositoryAdaptorPluginId("a.fake.repo.plugin");
		projectConfig.setBuildToolPluginId("a.fake.build.plugin");
		projectConfig.setName(projectName);
		projectConfig.setWorkDir("a workdir importedProject location");
		
		expect(repoConfigurator.updateGlobalConfig(repoConfig)).andReturn(pluginConfigChanged);
		
		ConfigUpdatesDto updates = new ConfigUpdatesDto();
		updates.setNewProjectConfigs(Arrays.asList(projectConfig));
		
		if (pluginConfigChanged) {
			updates.setModifiedPluginConfigs(Collections.singletonMap(repoConfig.getPluginId(), repoConfig));
		}
		
		stateManager.applyMultipleUpdates(updates);
	}
	
	public void trainSavesWithAlternateWorkDir() throws Exception {
		expect(pluginManager.getPluginConfigInfo("a.fake.repo.plugin"))
			.andThrow(new PluginNotConfigurableException());

		final ProjectConfigDto projectConfig = new ProjectConfigDto();
		
		projectConfig.setRepositoryAdaptorPluginId("a.fake.repo.plugin");
		projectConfig.setBuildToolPluginId("a.fake.build.plugin");
		projectConfig.setName(projectName);
		projectConfig.setWorkDir(workDir);
		projectConfig.setLabels(Collections.singleton("a label"));
		
		ConfigUpdatesDto updates = new ConfigUpdatesDto();
		updates.setNewProjectConfigs(Arrays.asList(projectConfig));
		stateManager.applyMultipleUpdates(updates);
	}
	
	@TrainingMethod("trainConfigures,trainSaves")
	public void testConfiguresAndSaves() throws Exception {
		importer.createProjectsForUrl(url, null, null, false, NameCollisionResolutionMode.Abort, ArrayUtils.EMPTY_STRING_ARRAY, null, null);
	}
	
	@TrainingMethod("trainConfigures,trainSavesWithPluginConfig")
	public void testConfiguresAndSavesWithPluginChanges() throws Exception {
		importer.createProjectsForUrl(url, null, null, false, NameCollisionResolutionMode.Abort, ArrayUtils.EMPTY_STRING_ARRAY, null, null);
	}
	
	@TrainingMethod("trainConfigures,trainPluginConfigUnchanged,trainSavesWithPluginConfig")
	public void testConfiguresAndSavesWithPluginChangesPluginNotModified() throws Exception {
		importer.createProjectsForUrl(url, null, null, false, NameCollisionResolutionMode.Abort, ArrayUtils.EMPTY_STRING_ARRAY, null, null);
	}
	
	public void trainStandalone() throws Exception {
		expect(buildConfigurator.isStandaloneProject()).andReturn(true);
		repoConfigurator.setNonRecursive();
		
		expect(buildConfigurator.getSubprojectUrls()).andReturn(null);
	}
	
	@TrainingMethod("trainConfigures,trainStandalone,trainSaves")
	public void testConfiguresAndSavesStandalone() throws Exception {
		importer.createProjectsForUrl(url, null, null, true, NameCollisionResolutionMode.Abort, ArrayUtils.EMPTY_STRING_ARRAY, null, null);
	}

	public void trainNotStandalone() throws Exception {
		expect(buildConfigurator.isStandaloneProject()).andReturn(false);
		expect(buildConfigurator.getSubprojectUrls()).andReturn(null);
	}

	@TrainingMethod("trainConfigures,trainNotStandalone,trainSaves")
	public void testConfiguresAndSavesNotStandalone() throws Exception {
		importer.createProjectsForUrl(url, null, null, true, NameCollisionResolutionMode.Abort, ArrayUtils.EMPTY_STRING_ARRAY, null, null);
	}

	public void trainSetupAlternateWorkDir() throws Exception {
		workDir = "alternate work dir";
	}
	
	@TrainingMethod("trainSetupAlternateWorkDir,trainConfigures,trainNotStandalone,trainSavesWithAlternateWorkDir")
	public void testConfiguresAndSavesBuildToolSetsWorkDir() throws Exception {
		importer.createProjectsForUrl(url, null, null, true, NameCollisionResolutionMode.Abort, ArrayUtils.EMPTY_STRING_ARRAY, Collections.singleton("a label"), null);
	}
	
	public void trainGetSubprojects() throws Exception {
		expect(buildConfigurator.isStandaloneProject()).andReturn(false);
		
		expect(buildConfigurator.getSubprojectUrls()).andReturn(
				Collections.singletonList("http://localhost/a-sub-project"));
		
		expect(rap1.createProjectConfigurator("http://localhost/a-sub-project", null, null)).andReturn(
				repoConfigurator);
		
		expect(rap1.getId()).andReturn("a.fake.repo.plugin");
		
		repoConfigurator.download(tmpFile2);
		
		expect(btp1.createProjectConfigurator("http://localhost/a-sub-project", tmpFile2, null)).andReturn(buildConfigurator);
		expect(btp1.getId()).andReturn("a.fake.build.plugin");
		
		final ProjectConfigDto projectConfig = new ProjectConfigDto();
		
		projectConfig.setRepositoryAdaptorPluginId("a.fake.repo.plugin");
		projectConfig.setBuildToolPluginId("a.fake.build.plugin");

		final List<String> updatedExistingProjectNames = new ArrayList<String>(existingProjectNames);
		updatedExistingProjectNames.add(projectName);
		
		expect(buildConfigurator.getRelativePathToProjectBasedir()).andReturn(null);

		buildConfigurator.applyConfiguration(eq(projectConfig), eq("a-sub-project"), eq(updatedExistingProjectNames), eq(true));
		
		repoConfigurator.applyConfiguration((ProjectConfigDto)notNull(), (String)notNull());
		
		expect(buildConfigurator.isStandaloneProject()).andReturn(false);
		expect(buildConfigurator.getSubprojectUrls()).andReturn(null);
	}
	
	public void trainSavesSubproject() throws Exception {
		expect(pluginManager.getPluginConfigInfo("a.fake.repo.plugin"))
			.andThrow(new PluginNotConfigurableException());
	
		final ProjectConfigDto projectConfig1 = new ProjectConfigDto();
		
		projectConfig1.setRepositoryAdaptorPluginId("a.fake.repo.plugin");
		projectConfig1.setBuildToolPluginId("a.fake.build.plugin");
		projectConfig1.setName(projectName);
		projectConfig1.setWorkDir("a workdir importedProject location");
		
		PluginStub pluginConfig = new PluginStub();
		
		expect(pluginManager.getPluginConfigInfo("a.fake.repo.plugin"))
			.andReturn(pluginConfig);
		
		expect(repoConfigurator.updateGlobalConfig(pluginConfig)).andReturn(false);
		
		final ProjectConfigDto projectConfig2 = new ProjectConfigDto();
		
		projectConfig2.setRepositoryAdaptorPluginId("a.fake.repo.plugin");
		projectConfig2.setBuildToolPluginId("a.fake.build.plugin");
		projectConfig2.setName(projectName);
		projectConfig2.setWorkDir("a workdir importedProject location");
		
		ConfigUpdatesDto updates = new ConfigUpdatesDto();
		updates.setNewProjectConfigs(Arrays.asList(projectConfig1, projectConfig2));
		stateManager.applyMultipleUpdates(updates);
	}
	
	@TrainingMethod("trainConfigures,trainGetSubprojects,trainSavesSubproject")
	public void testCreatesProjectsRecursivelyOnFlag() throws Exception {
		importer.createProjectsForUrl(url, null, null, true, NameCollisionResolutionMode.Overwrite, ArrayUtils.EMPTY_STRING_ARRAY, null, null);
	}
	
	@SuppressWarnings("unchecked")
	public void trainConfigureDontCare() throws Exception {
		buildConfigurator.applyConfiguration((ProjectConfigDto)notNull(),
				(String)anyObject(), (List<String>) notNull(), eq(false));
		expect(buildConfigurator.getRelativePathToProjectBasedir()).andReturn(null);
		repoConfigurator.applyConfiguration((ProjectConfigDto)notNull(), (String)notNull());
	}
	
	@TrainingMethod("trainConfigureDontCare")
	public void testConfigureReturnsFalseOnCollision() throws Exception {
		final ProjectConfigDto projectConfig = new ProjectConfigDto();

		existingProjectNames = Arrays.asList(projectName);
		
		assertFalse(importer.configureProject(projectConfig, repoConfigurator, buildConfigurator,
				url, existingProjectNames, NameCollisionResolutionMode.UseExisting, false, null));
	}

	@TrainingMethod("trainConfigureDontCare")
	public void testThrowsDuplicateNameOnCollision() throws Exception {
		final ProjectConfigDto projectConfig = new ProjectConfigDto();

		existingProjectNames = Arrays.asList(projectName);
		
		try {
			importer.configureProject(projectConfig, repoConfigurator, buildConfigurator,
				url, existingProjectNames, NameCollisionResolutionMode.Abort, false, null);
			fail("Expected exception");
		} catch (DuplicateNameException e) {
			assertEquals(projectName, e.getName());
		}
	}
	
	public void testComputePathParent() throws Exception {
		assertPathInfoEquals(
				"http://localhost/foo/",
				"bar/baz.xml",
				importer.computeProjectBasedirUrl("http://localhost/foo/bar/baz.xml", "..", true));
	}
	public void testComputePathRelative() throws Exception {
		assertPathInfoEquals(
				"http://localhost/foo/bar/",
				"baz.xml",
				importer.computeProjectBasedirUrl("http://localhost/foo/bar/baz.xml", ".", true));
	}
	public void testComputePathBlank() throws Exception {
		assertPathInfoEquals(
				"http://localhost/foo/bar/",
				"baz.xml",
				importer.computeProjectBasedirUrl("http://localhost/foo/bar/baz.xml", "", true));
	}
	public void testComputePathNull() throws Exception {
		assertPathInfoEquals(
				"http://localhost/foo/bar/",
				"baz.xml",
				importer.computeProjectBasedirUrl("http://localhost/foo/bar/baz.xml", null, true));
	}
	public void testComputePathObscureProtocol() throws Exception {
		assertPathInfoEquals(
				"pretzels://localhost/foo/bar/",
				"baz.xml",
				importer.computeProjectBasedirUrl("pretzels://localhost/foo/bar/baz.xml", null, true));
	}
	public void testComputePathAdjacentSlashesAtRoot() throws Exception {
		assertPathInfoEquals(
				"file:///tmp/dir/",
				"nested/nant.build",
				importer.computeProjectBasedirUrl("file:///tmp/dir/nested/nant.build", "..", true));
	}
	public void testComputePathCvs() throws Exception {
		assertPathInfoEquals(
				":pserver:anon@localhost:/cvsroot:some_module/",
				"nested/build.xml",
				importer.computeProjectBasedirUrl(":pserver:anon@localhost:/cvsroot:some_module/nested/build.xml", "..", true));
	}
	public void testMakeAbsoluteAlreadyAbsolute() throws Exception {
		List<String> urls = new ArrayList<String>(Arrays.asList("http://example.com/module/build.xml"));
		
		importer.makeAbsolute("http://example.com/", urls);
		
		assertEquals(Arrays.asList("http://example.com/module/build.xml"), urls);
	}
	public void testMakeAbsoluteAlreadyAbsoluteCvs() throws Exception {
		List<String> urls = new ArrayList<String>(Arrays.asList(":ext:foo:/bar:baz"));
		
		importer.makeAbsolute(":ext:foo:/bar:baz", urls);
		
		assertEquals(Arrays.asList(":ext:foo:/bar:baz"), urls);
	}
	public void testMakeAbsolute() throws Exception {
		List<String> urls = new ArrayList<String>(Arrays.asList("../foo.xml"));
		
		importer.makeAbsolute("http://example.com/nested/build.xml", urls);
		
		assertEquals(Arrays.asList("http://example.com/foo.xml"), urls);
	}
	public void testMakeAbsoluteCvs() throws Exception {
		List<String> urls = new ArrayList<String>(Arrays.asList("../subproject/pom.xml"));
		
		importer.makeAbsolute(":ext:foo:/bar:module/parent/pom.xml", urls);
		
		assertEquals(Arrays.asList(":ext:foo:/bar:module/subproject/pom.xml"), urls);
	}
	private static void assertPathInfoEquals(String expectedBasedirUrl, String expectedBuildSpecPath, PathInfo info) {
		assertEquals(expectedBasedirUrl, info.getProjectBasedirUrl());
		assertEquals(expectedBuildSpecPath, info.getBuildSpecPath());
	}
}
