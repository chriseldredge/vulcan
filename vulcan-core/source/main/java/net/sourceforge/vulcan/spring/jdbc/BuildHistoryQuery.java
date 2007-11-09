/*
 * Vulcan Build Manager
 * Copyright (C) 2005-2007 Chris Eldredge
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
package net.sourceforge.vulcan.spring.jdbc;

import java.util.List;

import javax.sql.DataSource;

import net.sourceforge.vulcan.dto.BuildOutcomeQueryDto;

class BuildHistoryQuery extends BuildQuery implements BuilderQuery {
	protected Object[] parameterValues;
	
	public BuildHistoryQuery(DataSource dataSource, BuildOutcomeQueryDto queryDto) {
		super(dataSource, true);
		
		HistoryQueryBuilder.buildQuery(queryDto, this);
	}

	public void setParameterValues(Object[] parameterValues) {
		this.parameterValues = parameterValues;
	}
	
	@Override
	public void setSql(String sql) {
		super.setSql(sql + " order by completion_date");
	}
	
	@SuppressWarnings("unchecked")
	public List<JdbcBuildOutcomeDto> queryForHistory() {
		return execute(parameterValues);
	}
}
