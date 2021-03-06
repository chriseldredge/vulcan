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
package net.sourceforge.vulcan.dto;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Locale;


public class PluginProfileDtoStub extends PluginProfileDto {
	private String pluginId;
	private String pluginName;
	private String projectConfigProfilePropertyName; 
	
	@Override
	public String getProjectConfigProfilePropertyName() {
		return projectConfigProfilePropertyName;
	}
	
	public void setProjectConfigProfilePropertyName(
			String projectConfigProfilePropertyName) {
		this.projectConfigProfilePropertyName = projectConfigProfilePropertyName;
	}
	
	@Override
	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	@Override
	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	@Override
	public List<PropertyDescriptor> getPropertyDescriptors(Locale locale) {
		return null;
	}
}