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
import java.util.List;

class ATermRealImpl
  extends ATermImpl
  implements ATermReal
{
  double value;

  //{ static int hashFunction(double value)

  static int hashFunction(double value)
  {
    return (new Double(value)).hashCode();
  }

  //}

  //{ public int hashCode()

  public int hashCode()
  {
    return hashFunction(value);
  }

  //}
  //{ public int getType()

  public int getType()
  {
    return ATerm.REAL;
  }

  //}

  //{ protected ATermRealImpl(PureFactory factory, double value)

  protected ATermRealImpl(PureFactory factory, double value)
  {
    super(factory);
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
      ATerm type = ((ATermPlaceholder)pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
	ATermAppl appl = (ATermAppl)type;
	AFun  afun = appl.getAFun();
	if(afun.getName().equals("real") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(new Double(value));
	  return true;
	}
      }
    }

    return super.match(pattern, list);
  }

  //}
  //{ public String toString()

  public String toString()
  {
    return String.valueOf(value);
  }

  //}
  //{ public int getInt()

  public double getReal()
  {
    return value;
  }

  //}  
}
