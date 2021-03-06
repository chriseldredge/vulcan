/*
 * Vulcan Build Manager
 * Copyright (C) 2005-2014 Chris Eldredge
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
package net.sourceforge.vulcan.web.struts.actions;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.vulcan.ProjectManager;
import net.sourceforge.vulcan.RepositoryAdaptor;
import net.sourceforge.vulcan.StateManager;
import net.sourceforge.vulcan.core.BuildDetailCallback;
import net.sourceforge.vulcan.core.BuildManager;
import net.sourceforge.vulcan.core.DependencyBuildPolicy;
import net.sourceforge.vulcan.core.DependencyGroup;
import net.sourceforge.vulcan.dto.MetricDto;
import net.sourceforge.vulcan.dto.ProjectConfigDto;
import net.sourceforge.vulcan.dto.ProjectStatusDto;
import net.sourceforge.vulcan.dto.RepositoryTagDto;
import net.sourceforge.vulcan.exception.ConfigException;
import net.sourceforge.vulcan.exception.NoSuchProjectException;
import net.sourceforge.vulcan.exception.ProjectsLockedException;
import net.sourceforge.vulcan.scheduler.BuildDaemon;
import net.sourceforge.vulcan.web.struts.forms.ManualBuildForm;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public final class ManualBuildAction extends Action {
	private BuildManager buildManager;
	private ProjectManager projectManager;
	private StateManager stateManager;
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final ManualBuildForm buildForm = (ManualBuildForm) form;
		
		final String[] selectedTags = buildForm.getSelectedTags();
		
		final boolean chooseTags = buildForm.isChooseTags();
		final boolean needToChooseTags = chooseTags && selectedTags == null;
		boolean needToPopulateAvailableTags = false;
		
		if (buildForm.getDependencyGroup() == null) {
			ActionForward errorForward = buildDependencyGroup(mapping, buildForm, request);
			
			if (errorForward != null) {
				return errorForward;
			}
			
			needToPopulateAvailableTags = chooseTags && true;
			buildForm.setFetchAvailableTags(selectedTags == null);
		}
		
		DependencyGroup dg = buildForm.getDependencyGroup();;

		if (needToChooseTags || needToPopulateAvailableTags) {
			fetchAvailableTags(buildForm, request, dg);
		}
		
		if (needToChooseTags) {
			return mapping.findForward("chooseTags");
		} else if (chooseTags) {
			buildForm.applyTagNamesOnTargets();
		}
		
		buildManager.add(dg);
		
		wakeUpBuildDaemons(dg.size());
		
		return mapping.findForward("dashboard");
	}

	protected ActionForward buildDependencyGroup(ActionMapping mapping,
			final ManualBuildForm buildForm, HttpServletRequest request)
			throws ConfigException {
		final List<ProjectConfigDto> projects = new ArrayList<ProjectConfigDto>();

		for (String target : buildForm.getTargets()) {
			try {
				projects.add(projectManager.getProjectConfig(target));
			} catch (NoSuchProjectException e) {
				BaseDispatchAction.saveError(request, "targets",
						new ActionMessage("errors.no.such.project", new String[] {target}));
				return mapping.getInputForward();
			}
		}
		
		final DependencyBuildPolicy policy =
			DependencyBuildPolicy.valueOf(buildForm.getDependencies());
		
		boolean buildOnNoUpdates = buildForm.isBuildOnNoUpdates();
		
		if (buildForm.isChooseTags()) {
			buildOnNoUpdates = true;
		}
		
		final DependencyGroup dg;
		
		try {
			dg = projectManager.buildDependencyGroup(
					projects.toArray(new ProjectConfigDto[projects.size()]),
					policy,
					buildForm.getWorkingCopyUpdateStrategy(),
					buildForm.isBuildOnDependencyFailure(), buildOnNoUpdates);
		} catch (ProjectsLockedException e) {
			BaseDispatchAction.saveError(request, ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.projects.locked", StringUtils.join(e.getLockedProjectNames(), ", ")));
			return mapping.getInputForward();
		}
		
		if (dg.isEmpty()) {
			BaseDispatchAction.saveError(request, ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.projects.up.to.date"));
			return mapping.getInputForward();
		}
		
		dg.setName(getRequestUsernameOrHostname(request));
		dg.setManualBuild(true);
		
		buildForm.setDependencyGroup(dg);
		return null;
	}

	public BuildManager getBuildManager() {
		return buildManager;
	}

	public void setBuildManager(BuildManager buildManager) {
		this.buildManager = buildManager;
	}

	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public void setProjectManager(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}

	public StateManager getStateManager() {
		return stateManager;
	}
	
	public void setStateManager(StateManager stateManager) {
		this.stateManager = stateManager;
	}
	
	static String getRequestUsernameOrHostname(HttpServletRequest request) {
		final Principal userPrincipal = request.getUserPrincipal();
		
		if (userPrincipal != null) {
			return userPrincipal.getName();
		}
		
		return request.getRemoteHost();
	}

	// Wake up an idle build daemon if one is found in order to show that
	// the project is building when the user is brought back to the dashboard.
	private void wakeUpBuildDaemons(int numProjects) {
		final List<BuildDaemon> buildDaemons = stateManager.getBuildDaemons();
		int count = 0;
		
		for (BuildDaemon bd : buildDaemons) {
			if (bd.isRunning() && !bd.isBuilding()) {
				bd.wakeUp();
				
				// sleep to allow buildDaemon a chance to fetch the target
				// before client is shown the dashboard.
				try {
					Thread.sleep(100);
				} catch (InterruptedException ignore) {
					// propagate interrupt that is not intended for us.
					Thread.currentThread().interrupt();
				}
				if (++count == numProjects)
				{
					return;	
				}
			}
		}
	}

	private void fetchAvailableTags(ManualBuildForm buildForm, HttpServletRequest request, DependencyGroup dg) {
		final List<ProjectConfigDto> targets = dg.getPendingProjects();
		
		final List<String> projectNames = new ArrayList<String>();
		final List<List<RepositoryTagDto>> tags = new ArrayList<List<RepositoryTagDto>>();
		final List<String> selectedTags = new ArrayList<String>();
		final List<String> workDirOverrides = new ArrayList<String>();
		
		for (ProjectConfigDto target : targets) {
			final String projectName = target.getName();
			projectNames.add(projectName);
			
			List<RepositoryTagDto> projectTags = null;
			
			if (buildForm.isFetchAvailableTags()) {
				projectTags = fetchTagsAndBranchesFromRepository(request, target);
				setPreviouslySelectedTag(selectedTags, projectName, projectTags);
			} else {
				projectTags = Collections.emptyList();
			}
			
			tags.add(projectTags);
			workDirOverrides.add(target.getWorkDir());
		}
		
		buildForm.populateTagChoices(projectNames, tags, workDirOverrides, dg);
		if (buildForm.isFetchAvailableTags()) {
			buildForm.setSelectedTags(selectedTags.toArray(new String[selectedTags.size()]));	
		}
	}

	protected List<RepositoryTagDto> fetchTagsAndBranchesFromRepository(
			HttpServletRequest request, ProjectConfigDto target) {
		List<RepositoryTagDto> projectTags;
		try {
			final RepositoryAdaptor repository = projectManager.getRepositoryAdaptor(target);
			
			try {
				repository.prepareRepository(new NoOpBuildDetailCallback());
			} catch (InterruptedException e) {
				throw new ConfigException("errors.interrupted", null, e);
			}
			
			projectTags = repository.getAvailableTagsAndBranches();
		} catch (ConfigException e) {
			BaseDispatchAction.saveError(request, target.getName(),
					new ActionMessage(e.getKey(), e.getArgs()));
			
			projectTags = Collections.emptyList();
		}
		return projectTags;
	}

	private void setPreviouslySelectedTag(List<String> selectedTags, String projectName, List<RepositoryTagDto> projectTags) {
		final ProjectStatusDto latestStatus = buildManager.getLatestStatus(projectName);
		
		if (latestStatus != null) {
			final String previousTagName = latestStatus.getTagName();
			
			for (RepositoryTagDto tag : projectTags) {
				if (tag.getName().equals(previousTagName)) {
					selectedTags.add(previousTagName);
					return;
				}
			}
		}
		
		String tagName = "";
		
		if (projectTags.size() > 0) {
			tagName = projectTags.get(projectTags.size()-1).getName();
		}
		
		selectedTags.add(tagName);
	}
	
	class NoOpBuildDetailCallback implements BuildDetailCallback {
		public void addMetric(MetricDto metric) {
		}
		public void reportError(String message, String file,
				Integer lineNumber, String code) {
		}
		public void reportWarning(String message, String file,
				Integer lineNumber, String code) {
		}
		public void setDetail(String detail) {
		}
		public void setDetailMessage(String messageKey, Object[] args) {
		}
		public void setPhaseMessageKey(String messageKey) {
		}
	}
}
