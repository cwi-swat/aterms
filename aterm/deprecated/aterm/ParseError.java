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

/**
  * ParseError is thrown when a an error occurs during the
  * parsing of a term.
  * Note that ParseError is a RuntimeException,
  * so it can be ignored when a parse error can only be caused
  * by a bug in your program.
  * @see java.lang.RuntimeException
  */

public class ParseError extends RuntimeException
{
  private ATermChannel channel;
  private char last;
  private String error;

  protected ParseError(ATermChannel c, int lst, String err) {
    super(explanation(c, (char)lst, err));
    channel = c;
    last = (char)lst;
    error = err;
  }

  protected ParseError(String msg) {
    super(msg);
  }

  static private String explanation(ATermChannel c, char lst, String err) {
    StringBuffer str = new StringBuffer();

    str.append(err);
    if(c == null)
      str.append(" at the end of the input.");
    else {
      str.append("\ninput left (max 40 chars.): ");
      int ch;
      int i = 40;
      str.append('\'');
      if(lst != (char)-1)
        str.append(lst);
      try {
        while((ch = c.read()) != -1 && i-- > 0) {
          str.append((char)ch);
        }
      } catch (IOException e) {
        str.append("*read error*");
      }
      str.append('\'');
    }
    str.append('\n');
    return str.toString();
  }
}
