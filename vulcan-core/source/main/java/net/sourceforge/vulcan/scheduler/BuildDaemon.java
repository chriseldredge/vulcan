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
package net.sourceforge.vulcan.scheduler;

import net.sourceforge.vulcan.dto.ProjectConfigDto;


public interface BuildDaemon extends Scheduler {
	String PHASE_CHECK_UPDATES = "build.phase.check.updates";
	String PHASE_CHECKOUT = "build.phase.checkout";
	String PHASE_CHANGELOG = "build.phase.changelog";
	String PHASE_BUILD = "build.phase.build";

	void abortCurrentBuild(String requestUsername);

	boolean isBuilding();

	ProjectConfigDto getCurrentTarget();

	String getPhaseMessageKey();
	
	String getDetail();
}