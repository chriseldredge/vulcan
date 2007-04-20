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
package net.sourceforge.vulcan.web;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;
import net.sourceforge.vulcan.metadata.SvnRevision;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;

import servletunit.HttpServletRequestSimulator;
import servletunit.ServletContextSimulator;

@SvnRevision(id="$Id$", url="$HeadURL$")
public class JstlFunctionsTest extends TestCase {
	final HttpServletRequestSimulator request = new HttpServletRequestSimulator(new ServletContextSimulator());
	
	public void testMangleSpace() throws Exception {
		assertEquals("full_throttle", JstlFunctions.mangle("full throttle"));
	}
	public void testManglePlus() throws Exception {
		assertEquals("full___throttle", JstlFunctions.mangle("full + throttle"));
	}
	public void testBrackets() throws Exception {
		assertEquals("fullThrottle_0_", JstlFunctions.mangle("fullThrottle[0]"));
	}
	public void testGetPropList() throws Exception {
		ActionErrors errors = new ActionErrors();
		errors.add("a", new ActionMessage("foo"));
		errors.add("a", new ActionMessage("bar"));
		errors.add("b", new ActionMessage("baz"));
		
		request.setAttribute(Globals.ERROR_KEY, errors);
		
		assertEquals(Arrays.asList("a", "b"), JstlFunctions.getActionErrorPropertyList(request));
	}
	public void testGetPropListNull() throws Exception {
		assertEquals(Collections.emptyList(), JstlFunctions.getActionErrorPropertyList(request));
	}
}