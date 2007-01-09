/*
 * Vulcan Build Manager
 * Copyright (C) 2005-2006 Chris Eldredge
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.vulcan.StateManager;
import net.sourceforge.vulcan.metadata.SvnRevision;
import net.sourceforge.vulcan.scheduler.BuildDaemon;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;


@SvnRevision(id="$Id$", url="$HeadURL$")
public final class KillBuildAction extends Action {
	private StateManager stateManager;
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String name = request.getParameter("daemonName");
		final BuildDaemon daemon = stateManager.getBuildDaemon(name);
		
		if (daemon == null) {
			final ActionMessages msgs = new ActionMessages();
			msgs.add(ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("errors.build.daemon.not.found",
							new Object[] {name}));
			saveErrors(request, msgs);
			return mapping.findForward("failure");
		}

		//TODO: if user tries to kill project "A", but "A" completes and the daemon is now building "B", we should not abort the build
		daemon.abortCurrentBuild(ManualBuildAction.getRequestUsernameOrHostname(request));
		
		return mapping.findForward("dashboard");
	}

	public StateManager getStateManager() {
		return stateManager;
	}

	public void setStateManager(StateManager manager) {
		this.stateManager = manager;
	}
}
