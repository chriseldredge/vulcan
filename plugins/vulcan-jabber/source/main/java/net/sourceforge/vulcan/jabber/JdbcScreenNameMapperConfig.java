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
package net.sourceforge.vulcan.jabber;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class JdbcScreenNameMapperConfig extends ScreenNameMapperConfig {

	private String jndiName = "";
	private String sql = "SELECT im_name FROM employees WHERE login = ?";
	
	@Override
	public List<PropertyDescriptor> getPropertyDescriptors(Locale locale) {
		final List<PropertyDescriptor> pds = super.getPropertyDescriptors(locale);

		addProperty(pds, "jndiName", "JdbcScreenNameMapperConfig.jndiName.name", "JdbcScreenNameMapperConfig.jndiName.description", locale);
		addProperty(pds, "sql", "JdbcScreenNameMapperConfig.sql.name", "JdbcScreenNameMapperConfig.sql.description", locale,
				Collections.singletonMap(ATTR_WIDGET_TYPE, Widget.TEXTAREA));
		
		return pds;
	}
	
	@Override
	public String getHelpTopic() {
		return "JabberJdbcScreenNameMapperConfig";
	}

	public String getJndiName() {
		return jndiName;
	}
	
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}
	
	public String getSql() {
		return sql;
	}
	
	public void setSql(String sql) {
		this.sql = sql;
	}
}
