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

import org.apache.commons.lang.StringUtils;

import net.sourceforge.vulcan.dto.PluginConfigDto;
import net.sourceforge.vulcan.exception.ValidationException;

public class GitConfig extends PluginConfigDto {
	private String executable = "git";
	
	@Override
	public List<PropertyDescriptor> getPropertyDescriptors(Locale locale) {
		final List<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>();
		
		addProperty(list, "executable", "git.executable.label", "git.executable.text", locale);
		
		return list;
	}
	
	@Override
	public void validate() throws ValidationException {
		if (StringUtils.isBlank(executable)) {
			throw new ValidationException("executable", "errors.required.with.name", "executable");
		}
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
		return "GitConfig";
	}
	
	public String getExecutable() {
		return executable;
	}
	
	public void setExecutable(String executable) {
		this.executable = executable;
	}
	
}
