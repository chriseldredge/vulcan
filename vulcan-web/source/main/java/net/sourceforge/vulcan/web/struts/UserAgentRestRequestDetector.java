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
package net.sourceforge.vulcan.web.struts;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import javax.servlet.http.HttpServletRequest;


public class UserAgentRestRequestDetector implements RestRequestDetector {
	private String restRepsonseForwardName;
	
	public String getRestResponseForwardName() {
		return restRepsonseForwardName;
	}
	
	public void setRestRepsonseForwardName(String restRepsonseForwardName) {
		this.restRepsonseForwardName = restRepsonseForwardName;
	}
	
	public boolean isRestRequest(HttpServletRequest request) {
		final String userAgent = request.getHeader("User-Agent");
		if (isNotBlank(userAgent)) {
			if (userAgent.contains("Mozilla") || userAgent.contains("Lynx")) {
				return false;
			}
		}
		return true;
	}
}
