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
package net.sourceforge.vulcan.metrics.dom;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;

public class DomBuilderTest extends TestCase {
	DomBuilder builder = new DomBuilder();
	
	public void testEmpty() throws Exception {
		assertTrue(builder.getMergedDocument().getRootElement().getChildren().isEmpty());
	}
	
	public void testMergeDocument() throws Exception {
		Document doc = new Document();
		
		doc.setRootElement(new Element("root"));
		
		builder.merge(doc);
		
		assertEquals(1, builder.getMergedDocument().getRootElement().getChildren().size());
	}
	public void testMergeTwoDocuments() throws Exception {
		builder.merge(new Document(new Element("root")));
		builder.merge(new Document(new Element("root")));
		
		assertEquals(2, builder.getMergedDocument().getRootElement().getChildren().size());
	}
}