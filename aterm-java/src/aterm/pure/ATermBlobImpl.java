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

import aterm.*;

class ATermBlobImpl extends ATermImpl implements ATermBlob {
  byte[] data;

  protected ATermBlobImpl(PureFactory factory) {
    super(factory);
  }

  public int getType() {
    return ATerm.BLOB;
  }

  protected void init(int hashCode, ATermList annos, byte[] data) {
    super.init(hashCode, annos);
    this.data = data;
  }

  public SharedObject duplicate() {
    ATermBlobImpl clone = new ATermBlobImpl(factory);
    clone.init(hashCode(), getAnnotations(), data);
    return clone;
  }

  public boolean equivalent(SharedObject obj) {
    if (super.equivalent(obj)) {
      ATermBlob peer = (ATermBlob) obj;
      return peer.getBlobData() == data;
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
        if (afun.getName().equals("blob")
            && afun.getArity() == 0
            && !afun.isQuoted()) {
          list.add(data);
          return true;
            }
      }
    }

    return super.match(pattern, list);
  }

  public byte[] getBlobData() {
    return data;
  }

  public int getBlobSize() {
    return data.length;
  }

  public ATerm setAnnotations(ATermList annos) {
    return getPureFactory().makeBlob(data, annos);
  }

  public aterm.Visitable accept(Visitor v) throws VisitFailure {
    return v.visitBlob(this);
  }

}
