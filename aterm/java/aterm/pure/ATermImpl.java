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
import java.util.LinkedList;
import java.io.*;

public abstract class ATermImpl
  implements ATerm
{
  private ATerm annotations;
  PureFactory factory;

  //{{{ public ATermImpl(PureFactory factory)

  public ATermImpl(PureFactory factory)
  {
    this.factory = factory;
  }

  //}}}
  //{{{ public ATermFactory getFactory()

  public ATermFactory getFactory()
  {
    return factory;
  }

  //}}}

  //{{{ public ATerm setAnnotation(ATerm label, ATerm anno)

  public ATerm setAnnotation(ATerm label, ATerm anno)
  {
    return factory.setAnnotation(this, label, anno);
  }

  //}}}
  //{{{ public ATerm removeAnnotation(ATerm label)

  public ATerm removeAnnotation(ATerm label)
  {
    return factory.removeAnnotation(this, label);
  }

  //}}}
  //{{{ public ATerm getAnnotation(ATerm label)

  public ATerm getAnnotation(ATerm label)
  {
    return factory.getAnnotation(this, label);
  }

  //}}}

  //{{{ public ATerm setAnnotations(ATerm annos)

  public ATerm setAnnotations(ATerm annos)
  {
    return factory.setAnnotations(this, annos);
  }

  //}}}
  //{{{ public ATerm removeAnnotations()

  public ATerm removeAnnotations()
  {
    return factory.removeAnnotations(this);
  }

  //}}}
  //{{{ public ATerm getAnnotations()

  public ATerm getAnnotations()
  {
    return annotations;
  }

  //}}}

  //{{{ public List match(String pattern)

  public List match(String pattern) 
    throws ParseError
  {
    return match(factory.parsePattern(pattern));
  }


  //}}}
  //{{{ public List match(ATerm pattern)

  public List match(ATerm pattern) 
  {
    List list = new LinkedList();
    if (match(pattern, list)) {
      return list;
    } else {
      return null;
    }
  }


  //}}}

  //{{{ public boolean isEqual(ATerm term)

  public boolean isEqual(ATerm term)
  {
    if(term instanceof ATermImpl) {
      return this == term;
    }

    return factory.isDeepEqual(this, term);
  }

  //}}}
  //{{{ public boolean equals(Object obj)

  public boolean equals(Object obj)
  {
    if (obj instanceof ATermImpl) {
      return this == obj;
    }

    if (obj instanceof ATerm) {
      return factory.isDeepEqual(this, (ATerm)obj);
    }

    return false;
  }

  //}}}

  //{{{ boolean match(ATerm pattern, List list)

  boolean match(ATerm pattern, List list)
  {
    if (pattern.getType() == PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder)pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
	ATermAppl appl = (ATermAppl)type;
	AFun  afun = appl.getAFun();
	if(afun.getName().equals("term") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(this);
	  return true;
	}
      }
    }
    
    return false;
  }

  //}}}

  //{{{ public ATerm make(List list)

  public ATerm make(List list)
  {
    return this;
  }

  //}}}

  //{{{ public void writeToTextFile(OutputStream stream)

  public void writeToTextFile(OutputStream stream)
    throws IOException
  {
    PrintStream ps = new PrintStream(stream);
    ps.print(this.toString());
  }

  //}}}
}
