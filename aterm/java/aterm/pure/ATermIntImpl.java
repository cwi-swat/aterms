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

import java.util.List;

class ATermIntImpl
  extends ATermImpl
  implements ATermInt
{
  int value;

  //{ int getType()

  int getType()
  {
    return ATerm.INT;
  }

  //}
  //{ int hashCode()

  int hashCode()
  {
    return value;
  }

  //}

  //{ protected ATermIntImpl(int value)

  protected ATermIntImpl(int value)
  {
    this.value = value;
  }

  //}

  //{ public boolean match(ATerm pattern, List list)

  protected boolean match(ATerm pattern, List list)
  {
    if (this.equals(pattern)) {
      return true; 
    }

    if (pattern.getType() == ATerm.PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder)pattern).getPlaceholderType();
      if (type.getType() == ATerm.APPL) {
	ATerm appl = (ATermAppl)type;
	AFun  afun = appl.getAFun();
	if(afun.getName().equals("int") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(new Integer(value));
	  return true;
	}
      }
    }

    return false;
  }

  //}
  //{ public String toString()

  public String toString()
  {
    return String.valueOf(value);
  }

  //}
  //{ public int getInt()

  public int getInt()
  {
    return value;
  }

  //}
}
