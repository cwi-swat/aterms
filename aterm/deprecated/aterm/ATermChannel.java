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
package aterm;

import java.io.*;

abstract public class ATermChannel
{
  protected int lastChar;

  abstract public int read()
    throws IOException;

  public char last()
  { 
    return (char)lastChar; 
  }

  public char readNext() 
	throws IOException
  {
    do {
      read();
    } while(lastChar == ' ' || lastChar == '\t' || lastChar == '\n');
    return (char)lastChar;
  }

  public void skipWhitespace() 
	throws IOException
  {
    while(lastChar == ' ' || lastChar == '\t' || lastChar == '\n')
      read();
  }

  public int readOct()
	throws IOException, ParseError
  {
    int val = Character.digit((char)lastChar, 8);
    val += Character.digit((char)read(), 8);
    if(val < 0)
      throw new ParseError(this, lastChar, "octal must have 3 digits.");
    val += Character.digit((char)read(), 8);
    if(val < 0)
      throw new ParseError(this, lastChar, "octal must have 3 digits");
    return val;
  }

  // reset is called before a new term is being read.
  public void reset() throws IOException
  { 
  }
}


