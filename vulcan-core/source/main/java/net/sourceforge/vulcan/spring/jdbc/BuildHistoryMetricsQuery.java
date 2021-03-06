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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.sourceforge.vulcan.dto.BuildOutcomeQueryDto;
import net.sourceforge.vulcan.dto.MetricDto;
import net.sourceforge.vulcan.dto.MetricDto.MetricType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.object.MappingSqlQuery;

class BuildHistoryMetricsQuery extends MappingSqlQuery implements BuilderQuery {
	private static Log LOG = LogFactory.getLog(BuildHistoryMetricsQuery.class);
	private Object[] parameterValues;
	
	public BuildHistoryMetricsQuery(DataSource dataSource, BuildOutcomeQueryDto query) {
		setDataSource(dataSource);
		HistoryQueryBuilder.buildQuery(HistoryQueryBuilder.BUILD_HISTORY_METRICS_SQL, query, this);
	}
	
	public void queryMetrics(List<JdbcBuildOutcomeDto> builds) {
		final List<JdbcMetricDto> metrics = execute(parameterValues);
		
		if (metrics.isEmpty()) {
			return;
		}
		
		splitMetricsByBuildId(builds, metrics);
	}

	@Override
	public void setSql(String sql) {
		super.setSql(sql + " order by build_id, message_key");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<JdbcMetricDto> execute(Object[] params) throws DataAccessException {
		return super.execute(params);
	}

	public void setParameterValues(Object[] parameterValues) {
		this.parameterValues = parameterValues;
	}
	
	@Override
	protected JdbcMetricDto mapRow(ResultSet rs, int rowNumber) throws SQLException {
		final JdbcMetricDto dto = new JdbcMetricDto();
		
		dto.setBuildId(rs.getInt("build_id"));
		dto.setMessageKey(rs.getString("message_key"));
		dto.setType(MetricType.fromId(rs.getString("metric_type").charAt(0)));
		dto.setValue(rs.getString("data"));
		
		return dto;
	}

	static void splitMetricsByBuildId(List<JdbcBuildOutcomeDto> builds, List<JdbcMetricDto> metrics) {
		final Map<Integer, JdbcBuildOutcomeDto> buildsById = new HashMap<Integer, JdbcBuildOutcomeDto>();
		
		for (JdbcBuildOutcomeDto build : builds) {
			buildsById.put(build.getPrimaryKey(), build);
		}

		final int size = metrics.size();

		final Integer buildId = metrics.get(0).getBuildId();
		if (size == 1) {
			if (buildsById.containsKey(buildId)) {
				buildsById.get(buildId).setMetrics(Collections.<MetricDto>unmodifiableList(metrics));
			} else {
				LOG.error("Got metrics for missing build " + buildId);
			}
			return;
		}
		
		Integer currentBuildId = buildId;
		int i = 0;
		int j = 1;
		
		while (j < size) {
			while (j < size && metrics.get(j).getBuildId().equals(currentBuildId)) {
				j++;
			}
			
			final List<MetricDto> metricsForBuild = Collections.<MetricDto>unmodifiableList(metrics.subList(i, j));
			
			if (buildsById.containsKey(currentBuildId)) {
				buildsById.get(currentBuildId).setMetrics(metricsForBuild);
			} else {
				LOG.error("Got metrics for missing build " + currentBuildId);
			}
			
			i = j;
			
			if (j < size) {
				currentBuildId = metrics.get(j).getBuildId();
			}
		}
	}
}