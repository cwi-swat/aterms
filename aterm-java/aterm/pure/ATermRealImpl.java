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
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;
import aterm.ATermPlaceholder;
import aterm.ATermReal;
import aterm.Visitor;

class ATermRealImpl extends ATermImpl implements ATermReal {
  double value;

  protected ATermRealImpl(PureFactory factory) {
    super(factory);
  }

  public int getType() {
    return ATerm.REAL;
  }

  protected void init(int hashCode, ATermList annos, double value) {
    super.init(hashCode, annos);
    this.value = value;
  }

  public SharedObject duplicate() {
    ATermRealImpl clone = new ATermRealImpl(factory);
    clone.init(hashCode(), getAnnotations(), value);
    return clone;
  }

  public boolean equivalent(SharedObject obj) {
    if (super.equivalent(obj)) {
      ATermReal peer = (ATermReal) obj;
      return peer.getReal() == value;
    }

    return false;
  }

  protected boolean match(ATerm pattern, List list) {
    if (this.equals(pattern)) {
      return true;
    }

    if (pattern.getType() == ATerm.PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
        ATermAppl appl = (ATermAppl) type;
        AFun afun = appl.getAFun();
        if (afun.getName().equals("real") && afun.getArity() == 0 && !afun.isQuoted()) {
          list.add(new Double(value));
          return true;
        }
      }
    }

    return super.match(pattern, list);
  }

  public double getReal() {
    return value;
  }

  public ATerm setAnnotations(ATermList annos) {
    return getPureFactory().makeReal(value, annos);
  }

	public aterm.Visitable accept(Visitor v) throws VisitFailure {
		return v.visitReal(this);
	}

}
