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

class ATermApplImpl
  extends ATermImpl
  implements ATermAppl
{
  AFun fun;
  ATerm[] args;

  //{{{ static int hashFunction(AFun fun, ATerm[] args)

  static int hashFunction(AFun fun, ATerm[] args)
  {
    int hnr;

    hnr = fun.hashCode();
    for (int i=0; i<args.length; i++) {
      hnr = (hnr << 1) ^ (hnr >> 1) ^ args[i].hashCode();
    }

    return Math.abs(hnr);
  }

  //}}}

  //{{{ public int hashCode()

  public int hashCode()
  {
    return hashFunction(fun, args);
  }

  //}}}
  //{{{ public int getType()

  public int getType()
  {
    return ATerm.APPL;
  }

  //}}}

  //{{{ protected ATermApplImpl(PureFactory factory, AFun fun, ATerm[] args)

  protected ATermApplImpl(PureFactory factory, AFun fun, ATerm[] args)
  {
    super(factory);
    this.fun  = fun;
    this.args = args;
  }

  //}}}
  //{{{ protected boolean match(ATerm pattern, List list)

  protected boolean match(ATerm pattern, List list)
  {
    if (pattern.getType() == APPL) {
      ATermAppl appl = (ATermAppl)pattern;
      if (fun.equals(appl.getAFun())) {
	return matchArguments(appl.getArgumentArray(), list);
      } else {
	return false;
      }
    }

    if (pattern.getType() == PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder)pattern).getPlaceholder();
      if (type.getType() == APPL) {
	ATermAppl appl = (ATermAppl)type;
	AFun  afun = appl.getAFun();
	if(afun.getName().equals("appl") && !afun.isQuoted()) {
	  list.add(fun.getName());
	  return matchArguments(appl.getArgumentArray(), list);
	} else if (afun.getName().equals("str") && !afun.isQuoted()) {
	  if (fun.isQuoted()) {
	    list.add(fun.getName());
	    return matchArguments(appl.getArgumentArray(), list);
	  }
	} else if (afun.getName().equals("id") && !afun.isQuoted()) {
	  if (!fun.isQuoted()) {
	    list.add(fun.getName());
	    return matchArguments(appl.getArgumentArray(), list);
	  }
	}
      }
    }

    return super.match(pattern, list);
  }

  //}}}
  //{{{ boolean matchArguments(ATerm[] args, List list)

  boolean matchArguments(ATerm[] pattern_args, List list)
  {
    for (int i=0; i<args.length; i++) {
      if(i >= pattern_args.length) {
	return false;
      }

      ATerm arg = args[i];
      ATerm pattern_arg = pattern_args[i];

      if(pattern_arg.getType() == PLACEHOLDER) {
	ATerm ph_type = ((ATermPlaceholder)pattern_arg).getPlaceholder();
	if (ph_type.getType() == APPL) {
	  ATermAppl appl = (ATermAppl)ph_type;
	  if (appl.getName().equals("list") && appl.getArguments().isEmpty()) {
	    ATermList result = PureFactory.empty;
	    for (int j=args.length-1; j>=i; j--) {
	      result = factory.makeList(args[j], result);
	    }
	    list.add(result);
	    return true;
	  }
	}
      }

      List submatches = arg.match(pattern_arg);
      if(submatches == null) {
	return false;
      }
      list.addAll(submatches);
    }

    return args.length == pattern_args.length;
  }

  //}}}
  //{{{ public ATerm[] getArgumentArray()

  public ATerm[] getArgumentArray()
  {
    return args;
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    StringBuffer result = new StringBuffer();

    if (args.length == 0) {
      return fun.toString();
    }

    result.append(fun.toString());
    result.append('(');
    for (int i=0; i<args.length; i++) {
      if (i != 0) {
	result.append(',');
      }
      result.append(args[i].toString());
    }

    result.append(')');

    return result.toString();
  }

  //}}}
  //{{{ public AFun getAFun()

  public AFun getAFun()
  {
    return fun;
  }

  //}}}
  //{{{ public ATermList getArguments()

  public ATermList getArguments()
  {
    ATermList result = PureFactory.empty;

    for (int i=args.length-1; i>=0; i--) {
      result = factory.makeList(args[i], result);
    }

    return result;
  }

  //}}}

  //{{{ public ATerm getArgument(int index)

  public ATerm getArgument(int index)
  {
    return args[index];
  }

  //}}}
  //{{{ public ATermAppl setArgument(ATerm newarg, int index)

  public ATermAppl setArgument(ATerm newarg, int index)
  {
    ATerm[] newargs = (ATerm [])args.clone();
    newargs[index] = newarg;

    return factory.makeAppl(fun, newargs);
  }

  //}}}

  //{{{ public boolean isQuoted()

  public boolean isQuoted()
  {
    return fun.isQuoted();
  }

  //}}}
  //{{{ public String getName()

  public String getName()
  {
    return fun.getName();
  }

  //}}}
  //{{{ public int getArity()

  public int getArity()
  {
    return fun.getArity();
  }

  //}}}

  //{{{ public ATerm make(List args)

  public ATerm make(List args)
  {
    ATerm[] newargs = new ATerm[this.args.length];
    for (int i=0; i<this.args.length; i++) {
      newargs[i] = this.args[i].make(args);
    }
    return factory.makeAppl(fun, newargs);
  }

  //}}}
}
