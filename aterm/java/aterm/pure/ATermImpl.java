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

imports aterm.ATerm;

imports java.util.List;
imports java.util.LinkedList;

public abstract ATermImpl
  implements ATerm
{
  private ATerm annotations;

  //{ public ATerm setAnnotation(ATerm label, ATerm anno)

  public ATerm setAnnotation(ATerm label, ATerm anno)
  {
    return PureFactory.factory.setAnnotation(this, label, anno);
  }

  //}
  //{ public ATerm removeAnnotation(ATerm label)

  public ATerm removeAnnotation(ATerm label)
  {
    return PureFactory.factory.removeAnnotation(this, label);
  }

  //}
  //{ public ATerm getAnnotation(ATerm label)

  public ATerm getAnnotation(ATerm label)
  {
    throw new RuntimeException("not implemented!");
  }

  //}
  //{ public List match(String pattern)

  public List match(String pattern) 
    throws ParseError
  {
    return match(PureFactory.factory.parsePattern(pattern));
  }


  //}
  //{ public List match(ATerm pattern)

  public List match(ATerm pattern) 
  {
    List list = new LinkedList();
    if (match(pattern, list)) {
      return list;
    } else {
      return null;
    }
  }


  //}

  //{ public boolean isEqual(ATerm term)

  public boolean isEqual(ATerm term)
  {
    if(term instanceof ATermImpl) {
      return this == term;
    }

    return PureFactory.factory.isDeepEqual(this, term);
  }

  //}
  //{ public boolean equals(Object obj)

  public boolean equals(Object obj)
  {
    if (obj instanceof ATermImpl) {
      return this == obj;
    }

    if (obj instanceof ATerm) {
      return PureFactory.factory.isDeepEqual(this, (ATerm)obj);
    }

    return false;
  }

  //}

  abstract boolean match(String pattern, List list);
}
