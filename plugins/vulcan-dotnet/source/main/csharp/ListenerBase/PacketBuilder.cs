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
namespace SourceForge.Vulcan.DotNet
{
	public class PacketBuilder {
		System.Text.ASCIIEncoding enc = new System.Text.ASCIIEncoding();
		System.IO.MemoryStream ms = new System.IO.MemoryStream();
		
		public void Write(string s) {
			if (s == null) {
				ms.WriteByte(unchecked((byte)-1));
				ms.WriteByte(0);
				return;
			}
			
			int length = s.Length;
			
			ms.WriteByte((byte) (length & 0x00FF));
			ms.WriteByte((byte) ((length & 0xFF00) >> 8));
			ms.Write(enc.GetBytes(s), 0, length);
		}
		
		public byte[] ToArray() {
			return ms.ToArray();
		}
		
		public void Reset() {
			ms = new System.IO.MemoryStream();
		}
	}
}