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

class ATermBlobImpl
  extends ATermImpl
  implements ATermBlob
{
  byte[] data;

  //{ static int hashFunction(byte[] data)

  static int hashFunction(byte[] data)
  {
    return Math.abs(data.hashCode());
  }

  //}

  //{ public int hashCode()

  public int hashCode()
  {
    return hashFunction(data);
  }

  //}
  //{ public int getType()

  public int getType()
  {
    return ATerm.BLOB;
  }

  //}

  //{ protected ATermBlobImpl(PureFactory factory, byte[] data)

  protected ATermBlobImpl(PureFactory factory, byte[] data)
  {
    super(factory);
    this.data = data;
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
	if(afun.getName().equals("blob") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(data);
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
    return String.valueOf(data.length) + "#" + String.valueOf(hashCode());
  }

  //}
  //{ public byte[] getBlobData()

  public byte[] getBlobData()
  {
    return data;
  }

  //}
  //{ public int getBlobSize()

  public int getBlobSize()
  {
    return data.length;
  }

  //}
}
