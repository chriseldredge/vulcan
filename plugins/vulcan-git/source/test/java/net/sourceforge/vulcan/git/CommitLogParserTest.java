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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import net.sourceforge.vulcan.dto.ChangeLogDto;
import net.sourceforge.vulcan.dto.ChangeSetDto;
import net.sourceforge.vulcan.dto.ModifiedPathDto;
import net.sourceforge.vulcan.dto.PathModification;
import net.sourceforge.vulcan.git.CommitLogParser;

import org.apache.commons.lang.time.DateUtils;

public class CommitLogParserTest extends TestCase {

	static final String changeLog =
			"\u001Ef672e139e966\u001Ff672e139e966eaee45aca866c53560d52c6ee6ee\u001FExample One\u001Fone@localhost\u001F2010-12-09 12:44:03 -0500\u001F(HEAD, master, origin/master)\u001Fsample message 1\n" +
			"\u001F\n" +
			"\n" +
			"A 	plugins/GitConfig.java\n" +
			"M 	plugins/ProcessInvokerTest.java\n" +
			"\u001Ef0bb3a6e49c3\u001Ff0bb3a6e49c3f26797df9ad6d187ba7e6e552a17\u001FSample User Two\u001F\u001F2010-12-14 19:31:44 -0500\u001F\u001Fsample message 2\n" +
			"\u001F\n" +
			"\n" +
			"M 	file three\n" +
			"D 	4th.txt\n" +
			"";
	
	public void testParseRoot() throws Exception {
		final ChangeLogDto result = new CommitLogParser().parse("");
		
		assertNotNull("return value", result);
	}
	
	public void testAddsChildren() throws Exception {
		final ChangeLogDto result = new CommitLogParser().parse("\u001E\n\u001E\n");
		
		assertNotNull("return value", result);
		assertNotNull("list", result.getChangeSets());
		assertEquals(2, result.getChangeSets().size());
	}
	
	public void testParseAuthor() throws Exception {
		final List<ChangeSetDto> actual = new CommitLogParser().parse(changeLog).getChangeSets();
		
		assertEquals("Example One", actual.get(0).getAuthorName());
		assertEquals("one@localhost", actual.get(0).getAuthorEmail());
		assertEquals("Sample User Two", actual.get(1).getAuthorName());
		assertEquals(null, actual.get(1).getAuthorEmail());
	}
	
	public void testParseMessage() throws Exception {
		final List<ChangeSetDto> actual = new CommitLogParser().parse(changeLog).getChangeSets();
		
		assertEquals("sample message 1", actual.get(0).getMessage());
		assertEquals("sample message 2", actual.get(1).getMessage());
	}
	
	public void testParseModifiedPaths() throws Exception {
		final List<ChangeSetDto> actual = new CommitLogParser().parse(changeLog).getChangeSets();
		
		assertEquals(Arrays.asList(new ModifiedPathDto("plugins/GitConfig.java", PathModification.Add), new ModifiedPathDto("plugins/ProcessInvokerTest.java", PathModification.Modify)), actual.get(0).getModifiedPaths());
		assertEquals(Arrays.asList(new ModifiedPathDto("file three", PathModification.Modify), new ModifiedPathDto("4th.txt", PathModification.Remove)), actual.get(1).getModifiedPaths());
	}
	
	public void testParseRevision() throws Exception {
		final List<ChangeSetDto> actual = new CommitLogParser().parse(changeLog).getChangeSets();
		
		assertEquals("f672e139e966:f672e139e966eaee45aca866c53560d52c6ee6ee", actual.get(0).getRevisionLabel());
		assertEquals("f0bb3a6e49c3:f0bb3a6e49c3f26797df9ad6d187ba7e6e552a17", actual.get(1).getRevisionLabel());
	}
	
	public void testParseTimestamp() throws Exception {
		final List<ChangeSetDto> actual = new CommitLogParser().parse(changeLog).getChangeSets();
		
		final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		
		assertEquals(fmt.parse("2010-12-09 12:44:03 -0500"), actual.get(0).getTimestamp());
		assertEquals(fmt.parse("2010-12-14 19:31:44 -0500"), actual.get(1).getTimestamp());
	}
	
	public void testParseTimestampInDifferentTimeZone() throws Exception {
		final Date d1 = CommitLogParser.commitDate("2012-01-31 23:59:42 -0830");
		final Date d2 = CommitLogParser.commitDate("2012-01-31 23:59:42 -0500");
		
		final long timeZoneDelta = (DateUtils.MILLIS_PER_HOUR * 3) + DateUtils.MILLIS_PER_MINUTE * 30;
		
		assertEquals(Long.toString(timeZoneDelta), Long.toString(d1.getTime() - d2.getTime()));
		
		assertFalse(d1.equals(d2));
	}
}
