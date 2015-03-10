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

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sourceforge.vulcan.dto.RepositoryAdaptorConfigDto;

public class GitProjectConfig extends RepositoryAdaptorConfigDto {
	private String branch = "";
	private String remoteRepositoryUrl = "";
	private String remoteName = "";

	//TODO: look into subrepos
	
	@Override
	public List<PropertyDescriptor> getPropertyDescriptors(Locale locale) {
		final ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>();
		
		addProperty(list, "remoteRepositoryUrl", "git.remote.url.label", "git.remote.url.text", locale);
		addProperty(list, "remoteName", "git.remote.name.label", "git.remote.name.text", locale);
		addProperty(list, "branch", "git.branch.label", "git.branch.text", locale);
				
		return list;
	}
	
	@Override
	public String getPluginId() {
		return GitPlugin.PLUGIN_ID;
	}

	@Override
	public String getPluginName() {
		return GitPlugin.PLUGIN_NAME;
	}

	@Override
	public String getHelpTopic() {
		return "GitProjectConfig";
	}
	
	public String getBranch() {
		return branch;
	}
	
	public void setBranch(String branch) {
		this.branch = branch;
	}
	
	public String getRemoteName() {
		return remoteName;
	}
	
	public void setRemoteName(String remoteName) {
		this.remoteName = remoteName;
	}
	
	public String getRemoteRepositoryUrl() {
		return remoteRepositoryUrl;
	}
	
	public void setRemoteRepositoryUrl(String remoteRepositoryUrl) {
		this.remoteRepositoryUrl = remoteRepositoryUrl;
	}
}
