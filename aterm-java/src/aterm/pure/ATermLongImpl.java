/*
 * Copyright (c) 2002-2007, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package aterm.pure;

import java.util.List;

import jjtraveler.VisitFailure;

import shared.SharedObject;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermLong;
import aterm.ATermList;
import aterm.ATermPlaceholder;
import aterm.Visitor;

public class ATermLongImpl extends ATermImpl implements ATermLong {
  private long value;

  protected ATermLongImpl(PureFactory factory) {
    super(factory);
  }

  public int getType() {
    return ATerm.LONG;
  }

  protected void init(int hashCode, ATermList annos, long value) {
    super.init(hashCode, annos);
    this.value = value;
  }

  protected void initHashCode(ATermList annos, long value) {
    this.value = value;
    this.internSetAnnotations(annos);
    this.setHashCode(this.hashFunction());
  }

  public SharedObject duplicate() {
    ATermLongImpl clone = new ATermLongImpl(factory);
    clone.init(hashCode(), getAnnotations(), value);
    return clone;
  }

  public boolean equivalent(SharedObject obj) {
    if (super.equivalent(obj)) {
      ATermLong peer = (ATermLong) obj;
      return peer.getLong() == value;
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
        if (afun.getName().equals("long") && afun.getArity() == 0 && !afun.isQuoted()) {
          list.add(new Long(value));
          return true;
        }
      }
    }

    return super.match(pattern, list);
  }

  public long getLong() {
    return value;
  }

  public ATerm setAnnotations(ATermList annos) {
    return getPureFactory().makeLong(value, annos);
  }

  public aterm.Visitable accept(Visitor v) throws VisitFailure {
    return v.visitLong(this);
  }

  private int hashFunction() {
    /* Set up the internal state */
    int a = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    int b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    int c = 2; /* the previous hash value */

    /*------------------------------------- handle the last 11 bytes */
    a += (getAnnotations().hashCode() << 8);
    a += (value);

    a -= b;
    a -= c;
    a ^= (c >> 13);
    b -= c;
    b -= a;
    b ^= (a << 8);
    c -= a;
    c -= b;
    c ^= (b >> 13);
    a -= b;
    a -= c;
    a ^= (c >> 12);
    b -= c;
    b -= a;
    b ^= (a << 16);
    c -= a;
    c -= b;
    c ^= (b >> 5);
    a -= b;
    a -= c;
    a ^= (c >> 3);
    b -= c;
    b -= a;
    b ^= (a << 10);
    c -= a;
    c -= b;
    c ^= (b >> 15);

    /*-------------------------------------------- report the result */
    return c;
  }

}
