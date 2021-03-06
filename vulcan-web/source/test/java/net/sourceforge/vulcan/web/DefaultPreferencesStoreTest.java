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

import junit.framework.TestCase;
import net.sourceforge.vulcan.dto.PreferencesDto;

import org.apache.commons.lang.StringUtils;

public class DefaultPreferencesStoreTest extends TestCase {
	DefaultPreferencesStore store = new DefaultPreferencesStore();
	final PreferencesDto prefs = new PreferencesDto();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		prefs.setLabels(new String[] {"x", "y"});
		prefs.setDashboardColumns(new String[] {"name", "status"});
		prefs.setGroupByLabel(true);
		prefs.setReloadInterval(543);
		prefs.setPopupMode("modePopup");
		prefs.setStylesheet("standard");
	}
	
	public void testRoundTrip() throws Exception {
		final String data = store.convertToString(prefs);
		final PreferencesDto out = store.convertFromString(data);
		
		assertEquals(prefs, out);
	}
	
	public void testAscii() throws Exception {
		final String data = store.convertToString(prefs);
		assertTrue(StringUtils.isAsciiPrintable(data));
	}
	
	public void testReturnsDefaultPrefsOnBadData() throws Exception {
		store.setDefaultPreferences(prefs);
		
		assertEquals(prefs, store.convertFromString("bad data"));
	}
	
	public void testSetsDefaultColumnsWhenNoneDefined() throws Exception {
		store.setDefaultPreferences((PreferencesDto) prefs.copy());
		final PreferencesDto expected = (PreferencesDto) prefs.copy();
		
		expected.setPopupMode("not default");
		prefs.setPopupMode("not default");
		prefs.setDashboardColumns(null);
		final String data = store.convertToString(prefs);
		final PreferencesDto out = store.convertFromString(data);

		assertEquals(expected, out);
	}
}
