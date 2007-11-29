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
package net.sourceforge.vulcan.filesystem;

import java.io.File;

import junit.framework.TestCase;
import net.sourceforge.vulcan.filesystem.dto.FileSystemProjectConfigDto;

import org.apache.commons.io.FileUtils;

public class FileSystemRepositoryAdaptorTest extends TestCase {
	File dir;
	FileSystemProjectConfigDto config = new FileSystemProjectConfigDto();
	FileSystemRepositoryAdaptor ra;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		dir = new File(System.getProperty("java.io.tmpdir"), "vulcan-filesystem-junit");
		ra = new FileSystemRepositoryAdaptor(config);
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (dir.exists()) {
			FileUtils.deleteDirectory(dir);
		}
		super.tearDown();
	}
	
	public void testNotWorkingCopy() throws Exception {
		assertTrue(dir.mkdir());
		
		assertEquals(false, ra.isWorkingCopy(dir));
	}
	
	public void testIsWorkingCopyWhenMarkerPresent() throws Exception {
		assertTrue(dir.mkdir());
		
		FileUtils.touch(new File(dir, FileSystemRepositoryAdaptor.WORKING_COPY_MARKER));
		
		assertEquals(true, ra.isWorkingCopy(dir));
	}
}