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

class ATermPlaceholderImpl
  extends ATermImpl
  implements ATermPlaceholder
{ 
  ATerm type;

  //{{{ static int hashFunction(ATerm type)

  static int hashFunction(ATerm type)
  {
    return Math.abs(type.hashCode()+1);
  }

  //}}}

  //{{{ public int hashCode()

  public int hashCode()
  {
    return hashFunction(type);
  }

  //}}}
  //{{{ public int getType()

  public int getType()
  {
    return ATerm.PLACEHOLDER;
  }

  //}}}

  //{{{ protected ATermPlaceholderImpl(PureFactory factory, ATerm type)

  protected ATermPlaceholderImpl(PureFactory factory, ATerm type)
  {
    super(factory);
    this.type = type;
  }

  //}}}

  //{{{ public boolean match(ATerm pattern, List list)

  public boolean match(ATerm pattern, List list)
  {
    if (pattern.getType() == ATerm.PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder)pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
	ATermAppl appl = (ATermAppl)type;
	AFun  afun = appl.getAFun();
	if(afun.getName().equals("placeholder") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(type);
	  return true;
	}
      }
    }


    return super.match(pattern, list);
  }

  //}}}
  //{{{ public ATerm make(List args)

  public ATerm make(List args)
  { 
    ATermAppl appl;
    AFun fun;
    String name;

    appl = (ATermAppl)type;
    fun  = appl.getAFun();
    name = fun.getName();
    if (!fun.isQuoted()) {
      if (fun.getArity() == 0) {
	if (name.equals("term")) {
	  ATerm t = (ATerm)args.get(0);
	  args.remove(0);

	  return t;
	} else if (name.equals("list")) {
	  ATermList l = (ATermList)args.get(0);
	  args.remove(0);

	  return l;
	} else if (name.equals("int")) {
	  Integer i = (Integer)args.get(0);
	  args.remove(0);

	  return factory.makeInt(i.intValue());
	} else if (name.equals("real")) {
	  Double d = (Double)args.get(0);
	  args.remove(0);

	  return factory.makeReal(d.doubleValue());
	} else if (name.equals("placeholder")) {
	  ATerm type = (ATerm)args.get(0);
	  args.remove(0);
	  return factory.makePlaceholder(type);
	}
      }
      if (name.equals("appl")) {
	ATermList oldargs = appl.getArguments();
	String newname = (String)args.get(0);
	args.remove(0);
	ATermList newargs = (ATermList)oldargs.make(args);
	AFun newfun = factory.makeAFun(newname, newargs.getLength(), false);
	return factory.makeAppl(newfun, newargs);
      }
    }
    throw new RuntimeException("illegal pattern: " + this);
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    String result;

    result = type.toString();
    if (type.getType() == LIST) {
      result = "[" + result + "]";
    }

    return "<" + result + ">";
  }

  //}}}
  //{{{ public ATerm getPlaceholder()

  public ATerm getPlaceholder()
  {
    return type;
  }

  //}}}
}
