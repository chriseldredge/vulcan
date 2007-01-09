/*
 * Vulcan Build Manager
 * Copyright (C) 2005-2006 Chris Eldredge
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
package net.sourceforge.vulcan.dotnet;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.sourceforge.vulcan.ant.AntBuildTool;
import net.sourceforge.vulcan.ant.io.ByteSerializer;
import net.sourceforge.vulcan.ant.receiver.UdpEventSource;
import net.sourceforge.vulcan.dotnet.dto.DotNetBuildEnvironmentDto;
import net.sourceforge.vulcan.dotnet.dto.DotNetGlobalConfigDto;
import net.sourceforge.vulcan.dotnet.dto.DotNetProjectConfigDto;
import net.sourceforge.vulcan.dto.ProjectConfigDto;
import net.sourceforge.vulcan.dto.ProjectStatusDto;

public abstract class DotNetBuildToolBase extends AntBuildTool {
	protected final DotNetGlobalConfigDto globalConfig;
	protected final DotNetProjectConfigDto dotNetProjectConfig;
	protected final DotNetBuildEnvironmentDto buildEnv;
	protected final File pluginDir;

	public DotNetBuildToolBase(DotNetGlobalConfigDto globalConfig, DotNetProjectConfigDto dotNetProjectConfig, DotNetBuildEnvironmentDto buildEnv, File pluginDir) {
		super(dotNetProjectConfig, null, new UdpEventSource(new ByteSerializer()));
		
		this.globalConfig = globalConfig;
		this.dotNetProjectConfig = dotNetProjectConfig;
		this.buildEnv = buildEnv;
		this.pluginDir = pluginDir;

	}

	protected void addConfigurationProperty(List<String> args) {
		final DotNetProjectConfigDto.BuildConfiguration buildConfiguration = dotNetProjectConfig.getBuildConfiguration();

		if (buildConfiguration == null || buildConfiguration.equals(DotNetProjectConfigDto.BuildConfiguration.Unspecified)) {
			return;
		}

		final DotNetGlobalConfigDto.GlobalBuildConfiguration globalConfiguration = globalConfig.getBuildConfiguration();
		
		if (!buildConfiguration.equals(DotNetProjectConfigDto.BuildConfiguration.Inherit)) {
			args.add("/p:Configuration=" + buildConfiguration.name());
		} else if (globalConfiguration != null && !globalConfiguration.equals(DotNetGlobalConfigDto.GlobalBuildConfiguration.Unspecified)) {
			args.add("/p:Configuration=" + globalConfiguration.name());
		}
	}

	protected void addDotNetProperties(List<String> args, ProjectConfigDto projectConfig, ProjectStatusDto status) {
		addConfigurationProperty(args);
	
		addProps(null, globalConfig.getProperties(), true);
		addProps(null, dotNetProjectConfig.getAntProperties(), true);
		
		final Map<String, String> antProps = getAntProps();
		
		final String buildNumKey = globalConfig.getBuildNumberProperty();
		if (isNotBlank(buildNumKey)) {
			antProps.put(buildNumKey, status.getBuildNumber().toString());
		}
		
		final String revPropKey = globalConfig.getRevisionProperty();
		if (isNotBlank(revPropKey)) {
			antProps.put(revPropKey, status.getRevision().getLabel());
		}
		
		final String numericRevPropKey = globalConfig.getNumericRevisionProperty();
		if (isNotBlank(numericRevPropKey)) {
			antProps.put(numericRevPropKey, status.getRevision().getRevision().toString());
		}
		
		final String tagPropKey = globalConfig.getTagProperty();
		if (isNotBlank(tagPropKey)) {
			antProps.put(tagPropKey, status.getTagName());
		}
		
		for (Map.Entry<String, String> e : antProps.entrySet()) {
			final StringBuilder sb = new StringBuilder();
			
			sb.append("/p:");
			sb.append(e.getKey());
			sb.append("=");
			sb.append(e.getValue());
			
			args.add(sb.toString());
		}
	}
}
