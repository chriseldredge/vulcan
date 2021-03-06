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
package net.sourceforge.vulcan.core;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sourceforge.vulcan.dto.BuildDaemonInfoDto;
import net.sourceforge.vulcan.dto.BuildManagerConfigDto;
import net.sourceforge.vulcan.dto.ProjectConfigDto;
import net.sourceforge.vulcan.dto.ProjectStatusDto;
import net.sourceforge.vulcan.exception.AlreadyScheduledException;
import net.sourceforge.vulcan.exception.StoreException;

public interface BuildManager {
	void init(BuildManagerConfigDto buildManagerConfig);
	
	BuildTarget getTarget(BuildDaemonInfoDto buildDaemonInfo);

	/**
	 * Register an instance of ProjectStatusDto which can be updated as the build proceeds.
	 * This instance can be referenced during the build to obtain up-to-date information
	 * about the outcome while the build is executing.
	 */
	void registerBuildStatus(BuildDaemonInfoDto info, ProjectBuilder builder, ProjectConfigDto currentTarget, ProjectStatusDto buildStatus);

	void add(DependencyGroup dg) throws AlreadyScheduledException;

	void targetCompleted(BuildDaemonInfoDto info, ProjectConfigDto currentTarget, ProjectStatusDto buildStatus);

	/**
	 * @return true if the build was previously unclaimed, false
	 * if it has already been claimed by someone else.
	 * @throws StoreException 
	 */
	boolean claimBrokenBuild(String projectName, int buildNumber, String claimUser);
	
	/**
	 * @return true if any project is currently being built or is in the build queue; false otherwise.
	 */
	boolean isBuildingOrInQueue(String... projectNames);
	
	ProjectStatusDto[] getPendingTargets();
	
	Map<String, ProjectStatusDto> getProjectsBeingBuilt();

	/**
	 * Flush projects waiting to be built.  Does not flush building projects.
	 */
	void clear();

	ProjectStatusDto getLatestStatus(String projectName);

	ProjectStatusDto getStatus(UUID statusId);
	
	ProjectStatusDto getStatusByBuildNumber(String projectName, int buildNumber);
	
	ProjectBuilder getProjectBuilder(String projectName);
	
	List<UUID> getAvailableStatusIds(String projectName);
	
	Map<String, ProjectStatusDto> getProjectStatus();

	Integer getMostRecentBuildNumberByWorkDir(String workDir);

	ProjectStatusDto getMostRecentBuildByWorkDir(String projectName, String workDir);
}
