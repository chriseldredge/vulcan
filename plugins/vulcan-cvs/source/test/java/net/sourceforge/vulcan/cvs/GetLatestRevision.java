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
package net.sourceforge.vulcan.cvs;

import net.sourceforge.vulcan.cvs.dto.CvsConfigDto;
import net.sourceforge.vulcan.cvs.dto.CvsProjectConfigDto;
import net.sourceforge.vulcan.cvs.dto.CvsRepositoryProfileDto;
import net.sourceforge.vulcan.dto.ProjectConfigDto;
import net.sourceforge.vulcan.dto.RevisionTokenDto;
import net.sourceforge.vulcan.exception.RepositoryException;

public class GetLatestRevision {
	public static void main(String[] args) throws RepositoryException {
		final CvsRepositoryProfileDto profile = new CvsRepositoryProfileDto();
		
		profile.setHost("gtkpod.cvs.sourceforge.net");
		profile.setProtocol("pserver");
		profile.setRepositoryPath("/cvsroot/gtkpod");
		profile.setUsername("anonymous");
		profile.setPassword("");
		
		final CvsProjectConfigDto projectConfig = new CvsProjectConfigDto();
		projectConfig.setModule("gtkpod");
		
		final CvsRepositoryAdaptor repo = new CvsRepositoryAdaptor(new ProjectConfigDto(), new CvsConfigDto(), profile, projectConfig);
		
		RevisionTokenDto rev = repo.getLatestRevision(
				//new RevisionTokenDto(20070416215757l, "2007/06/24 04:44:07"));
				new RevisionTokenDto(20070624044407l, "2007/06/24 04:44:07"));

		System.out.println("Latest modification: " + rev.getLabel());
	}
}
