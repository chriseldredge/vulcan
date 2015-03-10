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

import java.io.File;

import net.sourceforge.vulcan.PluginManager;
import net.sourceforge.vulcan.ProjectRepositoryConfigurator;
import net.sourceforge.vulcan.dto.PluginConfigDto;
import net.sourceforge.vulcan.dto.ProjectConfigDto;
import net.sourceforge.vulcan.exception.ConfigException;
import net.sourceforge.vulcan.integration.ConfigurablePlugin;
import net.sourceforge.vulcan.integration.RepositoryAdaptorPlugin;

public class GitPlugin implements RepositoryAdaptorPlugin, ConfigurablePlugin {
	public static String PLUGIN_ID = "net.sourceforge.vulcan.git";
	public static String PLUGIN_NAME = "Git";
	
	private GitConfig config = new GitConfig();
	private PluginManager pluginManager;
	
	public GitRepository createInstance(ProjectConfigDto projectConfig)	throws ConfigException {
		GitRepository instance = new GitRepository(projectConfig, config);
		return instance;
	}
	
	public ProjectRepositoryConfigurator createProjectConfigurator(String url, String username, String password) throws ConfigException {
		return null;
	}
	
	public GitProjectConfig getDefaultConfig() {
		return new GitProjectConfig();
	}

	public GitConfig getConfiguration() {
		return config;
	}
	
	public void setConfiguration(PluginConfigDto bean) {
		config = (GitConfig) bean;
	}
	
	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}
	
	public String getId() {
		return PLUGIN_ID;
	}

	public String getName() {
		return PLUGIN_NAME;
	}
}
