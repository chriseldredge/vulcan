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
using Microsoft.Win32;

namespace SourceForge.Vulcan.Tray
{
	public class RegistryPreferenceStore : PreferenceStore
	{
		RegistryKey key;
		
		public RegistryPreferenceStore()
		{
			key = Registry.CurrentUser.OpenSubKey(@"Software\VulcanTray", true);
			
			if (key == null)
			{
				key = Registry.CurrentUser.CreateSubKey(@"Software\VulcanTray");
			}
		}
		
		public Preferences Load()
		{
			Preferences preferences = new Preferences();

			preferences.Url = (string) key.GetValue("Url", "http://vulcan.example.com");
			preferences.SelectedLabels = (string)key.GetValue("SelectedLabels", "");
			preferences.Interval = (int) key.GetValue("Interval", 60000);
			preferences.BubbleFailures = ((int) key.GetValue("BubbleFailures", 1)) > 0;
			preferences.BubbleSuccess = ((int) key.GetValue("BubbleSuccess", 1)) > 0;
			
			return preferences;
		}

		public void Save(Preferences preferences)
		{
			key.SetValue("Url", preferences.Url, RegistryValueKind.String);
			key.SetValue("SelectedLabels", preferences.SelectedLabels, RegistryValueKind.String);
			key.SetValue("Interval", preferences.Interval, RegistryValueKind.DWord);
			key.SetValue("BubbleFailures", preferences.BubbleFailures, RegistryValueKind.DWord);
			key.SetValue("BubbleSuccess", preferences.BubbleSuccess, RegistryValueKind.DWord);
		}
	}
}