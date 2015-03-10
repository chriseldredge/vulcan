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
package net.sourceforge.vulcan.git;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import net.sourceforge.vulcan.dto.ChangeLogDto;
import net.sourceforge.vulcan.dto.ChangeSetDto;
import net.sourceforge.vulcan.dto.ModifiedPathDto;
import net.sourceforge.vulcan.dto.PathModification;

import org.apache.commons.lang.StringUtils;

public class CommitLogParser {
	final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
	final static int dateLength = 25;
		
	public ChangeLogDto parse(String text) throws ParseException {
		final ChangeLogDto log = new ChangeLogDto();
		final String[] entries = text.split("\u001E");
		final int entriesLength = entries.length;
		ChangeSetDto change;
		String[] items;
		
		if (entriesLength == 0)
			return log;
		
		for(int i = 1; i < entriesLength; i++) {			
			change = new ChangeSetDto();
			
			if (entries[i].length() > 0) {
				items = entries[i].split("\u001F");
				
				if (items.length >= 7) {
					change.setRevisionLabel(items[0] + ":" + items[1]);
					change.setAuthorName(items[2]);
					change.setAuthorEmail(nullForMissing(items[3]));
					change.setTimestamp(commitDate(items[4]));
					change.setMessage(chomp(items[6]));
					change.setModifiedPaths(processFiles(items[7]));
				}
			}

			log.addChangeSet(change);
		}

		return log;
	}

	public static Date commitDate(String text) throws ParseException {
		if (StringUtils.isBlank(text) || text.length() != dateLength) {
			return null;
		}

		return dateFormat.parse(text);
	}
	
	public List<ModifiedPathDto> processFiles(String text) {
		final List<ModifiedPathDto> paths = new ArrayList<ModifiedPathDto>();
		final String[] lines = text.split("\n");
		final int lineLength = lines.length;
		ModifiedPathDto path;
		String[] pieces;
		char code;
		PathModification pathAction = PathModification.Add;
		
		for(int i = 0; i < lineLength; i++) {
			if (lines[i].length() == 0)
				continue;
			
			pieces = lines[i].split("\t");
			code = pieces[0].charAt(0);
			
			switch(code) {
				case 'A':
					pathAction = PathModification.Add;
					break;
				case 'M':
					pathAction = PathModification.Modify;
					break;
				case 'D':
					pathAction = PathModification.Remove;
					break;
			}
						
			path = new ModifiedPathDto(pieces[1], pathAction);
			
			paths.add(path);
		}
		
		return paths;
	}
	
	private String nullForMissing(String text) {
		if (StringUtils.isBlank(text) || text.length() == 0) {
			return null;
		}
		return text;
	}
	
	private String chomp(String text) {
		if (StringUtils.isBlank(text) || text.length() == 0) {
			return text;
		}
		return text.substring(0, text.length() - 1);
	}
}
