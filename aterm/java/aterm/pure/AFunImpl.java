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

package aterm.pure;

import aterm.*;

class AFunImpl
  implements AFun
{
  String  name;
  int     arity;
  boolean isQuoted;

  //{ static int hashFunction(String name, int arity, boolean isQuoted)

  static int hashFunction(String name, int arity, boolean isQuoted)
  {
    return Math.abs(name.hashCode() ^ arity ^ (isQuoted ? 8 : 7));
  }

  //}
  
  //{ protected AFunImpl(String name, int arity, boolean isQuoted)

  protected AFunImpl(String name, int arity, boolean isQuoted)
  {
    this.name     = name;
    this.arity    = arity;
    this.isQuoted = isQuoted;
  }

  //}
  //{ public String getName()

  public String getName()
  {
    return name;
  }

  //}
  //{ public int getArity()

  public int getArity()
  {
    return arity;
  }

  //}
  //{ public boolean isQuoted()

  public boolean isQuoted()
  {
    return isQuoted;
  }

  //}

  //{ public String toString()

  public String toString()
  {
    StringBuffer result = new StringBuffer(name.length());

    if (isQuoted) {
      result.append('"');
    }

    for(int i=0; i<name.length(); i++) {
      char c = name.charAt(i);
      switch(c) {
        case '\n':	result.append('\\');	result.append('n');	break;
        case '\t':	result.append('\\');	result.append('t');	break;
        case '\b':	result.append('\\');	result.append('b');	break;
        case '\r':	result.append('\\');	result.append('r');	break;
        case '\f':	result.append('\\');	result.append('f');	break;
        case '\\':	result.append('\\');	result.append('\\');	break;
        case '\'':	result.append('\\');	result.append('\'');	break;
        case '\"':	result.append('\\');	result.append('\"');	break;

        case '!':	case '@':	case '#':	case '$':
        case '%':	case '^':	case '&':	case '*':
        case '(':	case ')':	case '-':	case '_':
        case '+':	case '=':	case '|':	case '~':
        case '{':	case '}':	case '[':	case ']':
        case ';':	case ':':	case '<':	case '>':
        case ',':	case '.':	case '?':       case ' ':
        case '/':
	  result.append(c);
	  break;

        default:
	  if(Character.isLetterOrDigit(c)) {
	    result.append(c);
	  } else {
	    result.append('\\');
	    result.append((char)((int)'0' + (int)c/64));
	    c = (char)(c % 64);
	    result.append((char)((int)'0' + (int)c/8));
	    c = (char)(c % 8);
	    result.append((char)((int)'0' + (int)c));
	  }
      }
    }

    if (isQuoted) {
      result.append('"');
    }

    return result.toString();
  }

  //}
}
