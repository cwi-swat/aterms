/*
 * Java version of the ATerm library
 * Copyright (C) 2002, CWI, LORIA-INRIA
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

package aterm.pure;

import java.util.List;

import jjtraveler.VisitFailure;

import shared.SharedObject;

import aterm.*;

class ATermPlaceholderImpl extends ATermImpl implements ATermPlaceholder {
  ATerm type;

  protected ATermPlaceholderImpl(PureFactory factory) {
    super(factory);
  }

  public int getType() {
    return ATerm.PLACEHOLDER;
  }

  protected void init(int hashCode, ATermList annos, ATerm type) {
    super.init(hashCode, annos);
    this.type = type;
  }

  public SharedObject duplicate() {
    ATermPlaceholderImpl clone = new ATermPlaceholderImpl(factory);
    clone.init(hashCode(), getAnnotations(), type);
    return clone;
  }

  public boolean equivalent(SharedObject obj) {
    if (super.equivalent(obj)) {
      ATermPlaceholder peer = (ATermPlaceholder) obj;
      return peer.getPlaceholder() == type;
    }

    return false;
  }

  public boolean match(ATerm pattern, List list) {
    if (pattern.getType() == ATerm.PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
        ATermAppl appl = (ATermAppl) type;
        AFun afun = appl.getAFun();
        if (afun.getName().equals("placeholder")
            && afun.getArity() == 0 && !afun.isQuoted()) {
          list.add(type);
          return true;
            }
      }
    }

    return super.match(pattern, list);
  }

  public ATerm make(List args) {
    PureFactory factory = getPureFactory();
    ATermAppl appl;
    AFun fun;
    String name;

    appl = (ATermAppl) type;
    fun = appl.getAFun();
    name = fun.getName();
    if (!fun.isQuoted()) {
      if (fun.getArity() == 0) {
        if (name.equals("term")) {
          ATerm t = (ATerm) args.get(0);
          args.remove(0);

          return t;
        } else if (name.equals("list")) {
          ATermList l = (ATermList) args.get(0);
          args.remove(0);

          return l;
        } else if (name.equals("bool")) {
          Boolean b = (Boolean) args.get(0);
          args.remove(0);

          return factory.makeAppl(factory.makeAFun(b.toString(), 0,
                false));
        } else if (name.equals("int")) {
          Integer i = (Integer) args.get(0);
          args.remove(0);

          return factory.makeInt(i.intValue());
        } else if (name.equals("real")) {
          Double d = (Double) args.get(0);
          args.remove(0);

          return factory.makeReal(d.doubleValue());
        } else if (name.equals("placeholder")) {
          ATerm type = (ATerm) args.get(0);
          args.remove(0);
          return factory.makePlaceholder(type);
        } else if (name.equals("str")) {
          String str = (String) args.get(0);
          args.remove(0);
          return factory.makeAppl(factory.makeAFun(str, 0, true));
        } else if (name.equals("id")) {
          String str = (String) args.get(0);
          args.remove(0);
          return factory.makeAppl(factory.makeAFun(str, 0, false));
        } else if (name.equals("fun")) {
          String str = (String) args.get(0);
          args.remove(0);
          return factory.makeAppl(factory.makeAFun(str, 0, false));
        }
      }
      if (name.equals("appl")) {
        ATermList oldargs = appl.getArguments();
        String newname = (String) args.get(0);
        args.remove(0);
        ATermList newargs = (ATermList) oldargs.make(args);
        AFun newfun = factory.makeAFun(newname, newargs.getLength(),
            false);
        return factory.makeApplList(newfun, newargs);
      }
    }
    throw new RuntimeException("illegal pattern: " + this);
  }

  public ATerm getPlaceholder() {
    return type;
  }

  public ATerm setPlaceholder(ATerm newtype) {
    return getPureFactory().makePlaceholder(newtype, getAnnotations());
  }

  public ATerm setAnnotations(ATermList annos) {
    return getPureFactory().makePlaceholder(type, annos);
  }

	public aterm.Visitable accept(Visitor v) throws VisitFailure {
		return v.visitPlaceholder(this);
	}

  public int getNrSubTerms() {
    return 1;
  }

  public ATerm getSubTerm(int index) {
    return type;
  }

  public ATerm setSubTerm(int index, ATerm t) {
    if (index == 1) {
      return setPlaceholder(t);
    }
    throw new RuntimeException("no " + index + "-th child!");
  }

}
