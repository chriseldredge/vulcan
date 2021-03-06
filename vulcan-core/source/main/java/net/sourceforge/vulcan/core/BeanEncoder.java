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
package net.sourceforge.vulcan.core;

import java.util.List;

import java.io.IOException;
import java.io.Writer;


public interface BeanEncoder {
	public static interface FactoryExpert {
		public boolean needsFactory(Object bean);
		public String getFactoryBeanName(Object bean);
		public String getFactoryMethod(Object bean);
		public List<String> getConstructorArgs(Object bean);
		public void registerPlugin(ClassLoader classLoader, String id);
	}

	public void addBean(String beanName, Object bean);

	public void write(Writer writer) throws IOException;

	public void reset();
}