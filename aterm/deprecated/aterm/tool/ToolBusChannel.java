/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/
package aterm.tool;

import aterm.*;
import java.io.*;

public class ToolBusChannel extends ATermChannel
{
  private InputStream istream;
  private int bytesLeft = 0;

  ToolBusChannel(InputStream istream) { this.istream = istream; }
  
  public int read()
       throws IOException
  {
    if(bytesLeft == 0)
      lastChar = -1;
    else {
      bytesLeft--;
      lastChar = istream.read();
    }
    if(lastChar == 0) {
      if(bytesLeft > 0)
        istream.skip(bytesLeft);
      lastChar = -1;
    }

    return lastChar;
  }

  public void reset() 
       throws IOException
  {
    byte[] lspecBuf = new byte[Tool.LENSPEC];
    istream.read(lspecBuf);
    // jdk 1.1
    // String lspec = new String(lspecBuf);
    // jdk 1.02
    String lspec = new String(lspecBuf, 0);

    bytesLeft = Integer.parseInt(lspec.substring(0, Tool.LENSPEC-1));
    if(bytesLeft < Tool.MIN_MSG_SIZE)
      bytesLeft = Tool.MIN_MSG_SIZE;
    bytesLeft -= Tool.LENSPEC;
  }
}

