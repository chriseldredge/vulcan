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
package net.sourceforge.vulcan.spring.jdbc;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import net.sourceforge.vulcan.dto.TestFailureDto;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

class TestFailureInserter extends SqlUpdate {
	public TestFailureInserter(DataSource dataSource) {
		setDataSource(dataSource);
		setSql("insert into test_failures " +
				"(build_id, name, message, details, first_consecutive_build_number) " +
				"values (?, ?, ?, ?, ?)");
		
		declareParameter(new SqlParameter(Types.NUMERIC));
		declareParameter(new SqlParameter(Types.VARCHAR));
		declareParameter(new SqlParameter(Types.VARCHAR));
		declareParameter(new SqlParameter(Types.VARCHAR));
		declareParameter(new SqlParameter(Types.NUMERIC));
		
		compile();
	}
	
	public int insert(int buildId, List<TestFailureDto> failures) {
		int count = 0;
		
		final Object[] params = new Object[5];
		
		params[0] = buildId;

		for (TestFailureDto dto : failures) {
			params[1] = dto.getName();
			params[2] = JdbcBuildOutcomeStore.truncate(dto.getMessage(), JdbcBuildOutcomeStore.MAX_TEST_FAILURE_MESSAGE_LENGTH);
			params[3] = JdbcBuildOutcomeStore.truncate(dto.getDetails(), JdbcBuildOutcomeStore.MAX_TEST_FAILURE_DETAILS_LENGTH);
			params[4] = dto.getBuildNumber();
			
			count += update(params);
		}
		
		return count;
	}
}