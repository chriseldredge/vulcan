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
package net.sourceforge.vulcan.subversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import net.sourceforge.vulcan.dto.ChangeSetDto;
import net.sourceforge.vulcan.dto.ModifiedPathDto;
import net.sourceforge.vulcan.dto.PathModification;
import net.sourceforge.vulcan.subversion.dto.CheckoutDepth;
import net.sourceforge.vulcan.subversion.dto.SparseCheckoutDto;
import net.sourceforge.vulcan.subversion.dto.SubversionProjectConfigDto;

public class SparseChangeLogFilterTest extends TestCase {
	ChangeSetDto change1;
	SubversionProjectConfigDto config = new SubversionProjectConfigDto();
	LineOfDevelopment line = new LineOfDevelopment();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		change1 = new ChangeSetDto();
		change1.setModifiedPaths(Arrays.asList(new ModifiedPathDto("/trunk/Scripts/Build", PathModification.Modify)));
		config.setCheckoutDepth(CheckoutDepth.Infinity);
		config.setPath("/trunk");
		
		line.setPath("/trunk");
		line.setTagFolderNames(Collections.singleton("tags"));
		line.setRepositoryRoot("http://localhost/svn");
	}
	
	public void testIncludesOnInfiniteDepth() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/trunk/File"));
		
		config.setCheckoutDepth(CheckoutDepth.Infinity);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(list, result);
	}
	
	public void testIncludesOnFileDepth() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/trunk/File"));
		
		config.setCheckoutDepth(CheckoutDepth.Files);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(list, result);
	}
	
	public void testIncludesTopLevelOnEmpty() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/trunk"));
		
		config.setCheckoutDepth(CheckoutDepth.Empty);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(list, result);
	}
	
	public void testIncludesTopLevelAlternateTag() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/tags/1.0"));
		line.setAlternateTagName("tags/1.0");
		
		config.setCheckoutDepth(CheckoutDepth.Empty);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(list, result);
	}
	
	public void testExcludesFileWithNearMatchButNotQuite() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/trunk2/File"));
		
		config.setCheckoutDepth(CheckoutDepth.Files);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(Collections.emptyList(), result);
	}
	
	public void testIncludesOnFileDepthRootPathEndsWithSlash() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/trunk/a"));
		
		config.setPath("/trunk/");
		config.setCheckoutDepth(CheckoutDepth.Files);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(list, result);
	}
	
	public void testPrependsSlashToRootPath() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/trunk/a"));
		
		config.setPath("trunk");
		config.setCheckoutDepth(CheckoutDepth.Files);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(list, result);
	}
	
	public void testExcludesSubDirOnImmediates() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/trunk/subdir/file"));
		
		config.setCheckoutDepth(CheckoutDepth.Immediates);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(Collections.emptyList(), result);
	}
	
	public void testExcludesWhenOnMismatchPath() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/branches/1.1/subdir/file"));
		
		config.setCheckoutDepth(CheckoutDepth.Infinity);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(Collections.emptyList(), result);
	}

	public void testExcludesOnEmpty() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/trunk/File"));
		
		config.setCheckoutDepth(CheckoutDepth.Empty);
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(Collections.emptyList(), result);
	}
	
	public void testIncludesSubDirOnSparseFolder() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/branches/2.1/folder/file", "/trunk/folder/file"));
		
		config.setPath("/trunk/");
		config.setCheckoutDepth(CheckoutDepth.Empty);
		config.setFolders(makeFolders(new SparseCheckoutDto("folder", CheckoutDepth.Files)));
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(list, result);
	}
	
	public void testExcludesOnShallowSparseFolder() throws Exception {
		List<ChangeSetDto> list = makeList(makeChangeSet("/trunk/folder/inner/file"));
		
		config.setPath("/trunk/");
		config.setCheckoutDepth(CheckoutDepth.Empty);
		config.setFolders(makeFolders(new SparseCheckoutDto("folder", CheckoutDepth.Files)));
		
		final List<ChangeSetDto> result = new ArrayList<ChangeSetDto>(list);
		new SparseChangeLogFilter(config, line).removeIrrelevantChangeSets(result);
		
		assertEquals(Collections.emptyList(), result);
	}

	private SparseCheckoutDto[] makeFolders(SparseCheckoutDto... dtos) {
		return dtos;
	}

	private ChangeSetDto makeChangeSet(String... paths) {
		final ChangeSetDto dto = new ChangeSetDto();
		
		for (String p : paths) {
			dto.addModifiedPath(p, PathModification.Modify);
		}
		
		return dto;
	}
	
	private List<ChangeSetDto> makeList(ChangeSetDto... dtos) {
		return new ArrayList<ChangeSetDto>(Arrays.asList(dtos));
	}
	
}
