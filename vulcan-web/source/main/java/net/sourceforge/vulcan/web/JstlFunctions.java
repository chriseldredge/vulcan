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
package net.sourceforge.vulcan.web;

import static org.apache.commons.lang.time.DateUtils.MILLIS_PER_DAY;
import static org.apache.commons.lang.time.DateUtils.MILLIS_PER_HOUR;
import static org.apache.commons.lang.time.DateUtils.MILLIS_PER_MINUTE;
import static org.apache.commons.lang.time.DateUtils.MILLIS_PER_SECOND;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import net.sourceforge.vulcan.StateManager;
import net.sourceforge.vulcan.core.BuildManager;
import net.sourceforge.vulcan.dto.LockDto;
import net.sourceforge.vulcan.dto.MetricDto;
import net.sourceforge.vulcan.dto.PreferencesDto;
import net.sourceforge.vulcan.dto.ProjectStatusDto;
import net.sourceforge.vulcan.event.Event;
import net.sourceforge.vulcan.event.EventPool;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;
import org.springframework.web.context.WebApplicationContext;

public abstract class JstlFunctions {
	private static WebApplicationContext webApplicationContext;
	
	public static String mangle(String s) {
		return s.replaceAll("[ \\.+\\[\\]<>&]", "_");
	}
	
	public static String encode(String s) {
		try {
			// replace + with %20 because + is only appropriate for query strings, not for paths.
			return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setStatus(HttpServletResponse response, int code) {
		response.setStatus(code);
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getActionErrorPropertyList(HttpServletRequest request) {
		final List<String> errorList = new ArrayList<String>();
		
		final ActionMessages errors = (ActionMessages) request.getAttribute(Globals.ERROR_KEY);
		if (errors != null) {
			Iterator<String> itr = errors.properties();
			while (itr.hasNext()) {
				errorList.add(itr.next());
			}
		}
		
		return errorList;
	}

	public static String formatElapsedTime(PageContext pageContext, long elapsedTime, int verbosity) {
		final StringBuilder sb = new StringBuilder();

		int count = 0;
		
		for (long millisPerUnit : Arrays.asList(MILLIS_PER_DAY, MILLIS_PER_HOUR, MILLIS_PER_MINUTE, MILLIS_PER_SECOND)) {
			final String messageKey;
			
			if (millisPerUnit == MILLIS_PER_DAY) {
				messageKey = "time.day";
			} else if (millisPerUnit == MILLIS_PER_HOUR) {
				messageKey = "time.hour";
			} else if (millisPerUnit == MILLIS_PER_MINUTE) {
				messageKey = "time.minute";
			} else {
				messageKey = "time.second";
			}
			
			final long units = elapsedTime / millisPerUnit;

			if (units == 0) {
				continue;
			}
			
			elapsedTime -= units * millisPerUnit;

			if (sb.length() > 0) {
				sb.append(", ");
			}

			sb.append(units);
			sb.append(" ");
			sb.append(formatMessage(pageContext, messageKey,  units > 1));
			
			if (++count == verbosity) {
				break;
			}
		}
		
		return sb.toString();
	}
	
	public static boolean isProjectLocked(String projectName) {
		final StateManager mgr = (StateManager) webApplicationContext.getBean(Keys.STATE_MANAGER, StateManager.class);
		return mgr.getProjectConfig(projectName).isLocked();
	}
	
	public static String getProjectLockMessage(String projectName) {
		final StateManager mgr = (StateManager) webApplicationContext.getBean(Keys.STATE_MANAGER, StateManager.class);
		final List<LockDto> locks = mgr.getProjectConfig(projectName).getLocks();
		
		final StringBuilder sb = new StringBuilder();
		
		for (LockDto lock : locks) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append(lock.getMessage());
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getProjectNamesByLabels(Object labels) {
		final StateManager mgr = (StateManager) webApplicationContext.getBean(Keys.STATE_MANAGER, StateManager.class);
		
		if (labels == null) {
			return mgr.getProjectConfigNames();
		}
		
		final Iterable<String> itr;
		
		if (labels instanceof String) {
			labels = Collections.singleton(labels);
		}
		
		if (labels instanceof Iterable) {
			itr = (Iterable<String>) labels;
		} else if (labels instanceof String[]) {
			itr = Arrays.asList((String[])labels);
		} else {
			throw new IllegalArgumentException("labels must be java.lang.String, java.lang.String[] or Iterable<String>");
		}
		
		return getProjectNamesByLabels(mgr, itr);
	}

	public static List<String> getAvailableDashboardColumns(PageContext pageContext) {
		final List<String> availableColumns = getAllDashboardColumns(Keys.DASHBOARD_COLUMNS);
		
		availableColumns.addAll(getAvailableMetrics());

		final PreferencesDto prefs = (PreferencesDto) pageContext.findAttribute(Keys.PREFERENCES);
		
		if (prefs != null && prefs.getDashboardColumns() != null) {
			// Start with the chosen columns in the chosen order.
			final List<String> userSorted = new ArrayList<String> (Arrays.asList(prefs.getDashboardColumns()));
			
			// Remove any columns that are no longer valid.
			userSorted.retainAll(availableColumns);
			
			// Remove duplicates from set of unselected columns.
			availableColumns.removeAll(userSorted);
			
			// Add the unselected columns to the end of the list.
			userSorted.addAll(availableColumns);
			
			return userSorted;
		}
		
		return availableColumns;
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAllDashboardColumns(String listName) {
		return (List<String>) webApplicationContext.getBean(listName, List.class);
	}
	
	public static List<String> getAvailableMetrics() {
		final StateManager mgr = (StateManager) webApplicationContext.getBean(Keys.STATE_MANAGER, StateManager.class);
		final BuildManager buildMgr = (BuildManager) webApplicationContext.getBean("buildManager", BuildManager.class);
		final Set<String> metrics = new HashSet<String>();
		
		for (String projectName : mgr.getProjectConfigNames()) {
			final ProjectStatusDto status = buildMgr.getLatestStatus(projectName);
			if (status == null || status.getMetrics() == null) {
				continue;
			}
			for (MetricDto metric : status.getMetrics()) {
				metrics.add(metric.getMessageKey());
			}
		}
		
		final List<String> metricsList = new ArrayList<String> (metrics);
		Collections.sort(metricsList);	
		return metricsList;
	}
	
	public static ProjectStatusDto getOutcomeByBuildNumber(String projectName, int buildNumber) {
		final BuildManager mgr = (BuildManager) webApplicationContext.getBean("buildManager", BuildManager.class);
		return mgr.getStatusByBuildNumber(projectName, buildNumber);
	}
	
	public static List<Event> getEvents(String eventTypes) {
		final EventPool eventPool = (EventPool) webApplicationContext.getBean(Keys.EVENT_POOL, EventPool.class);
		
		return eventPool.getEvents(eventTypes);
	}

	private static List<String> getProjectNamesByLabels(StateManager mgr, Iterable<String> itr) {
		final Set<String> set = new HashSet<String>();
		boolean empty = true;
		
		for (String label : itr) {
			set.addAll(mgr.getProjectConfigNamesByLabel(label));
			empty = false;
		}
		
		if (empty) {
			return mgr.getProjectConfigNames();
		}
		
		final ArrayList<String> list = new ArrayList<String>(set);
		Collections.sort(list);
		return list;
	}
	
	static WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}
	
	static void setWebApplicationContext(
			WebApplicationContext webApplicationContext) {
		JstlFunctions.webApplicationContext = webApplicationContext;
	}
	
	private static String formatMessage(PageContext pageContext, String key, boolean plural) {
		if (plural) {
			key += "s";
		}
		
		return webApplicationContext.getMessage(key, null, pageContext.getRequest().getLocale());
	}
}
