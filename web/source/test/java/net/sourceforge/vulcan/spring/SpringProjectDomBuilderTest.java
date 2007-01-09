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
package net.sourceforge.vulcan.spring;

import java.util.Collections;

import javax.xml.transform.TransformerFactory;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import net.sourceforge.vulcan.exception.NoSuchTransformFormatException;
import net.sourceforge.vulcan.metadata.SvnRevision;
import net.sourceforge.vulcan.web.MockWebApplicationContext;
import junit.framework.TestCase;

@SvnRevision(id="$Id", url="$HeadURL$")
public class SpringProjectDomBuilderTest extends TestCase {
	MockWebApplicationContext ctx = new MockWebApplicationContext() {
		@Override
		public Resource getResource(String location) {
			assertEquals("foo.xsl", location);
			return new ByteArrayResource("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\"/>"
					.getBytes()); 
		}
	};
	TransformerFactory tf = TransformerFactory.newInstance();
	SpringProjectDomBuilder builder = new SpringProjectDomBuilder();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ctx.refresh();
		builder.setApplicationContext(ctx);
		builder.setTransformerFactory(tf);
	}
	
	public void testCreate() throws Exception {
		builder.setTransformResources(Collections.singletonMap("foo", "foo.xsl"));
		
		assertNotNull(builder.createTransformer("foo"));
	}

	public void testCreateMissing() throws Exception {
		try {
			builder.createTransformer("foo");
			fail("expected exception");
		} catch (NoSuchTransformFormatException e) {
		}
	}
	
	public void testFormatMessage() throws Exception {
		assertEquals("hello", builder.formatMessage("hello", null, null));
	}
}
