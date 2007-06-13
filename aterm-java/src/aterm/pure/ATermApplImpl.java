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

public class ATermApplImpl extends ATermImpl implements ATermAppl {
  private AFun fun;

  private ATerm[] args;

  protected ATermApplImpl(PureFactory factory) {
    super(factory);
  }

  public int getType() {
    return ATerm.APPL;
  }

  protected void init(int hashCode, ATermList annos, AFun fun, ATerm[] i_args) {
    super.init(hashCode, annos);
    this.fun = fun;
    this.args = new ATerm[i_args.length];

    System.arraycopy(i_args, 0, args, 0, args.length);
  }

  protected void initHashCode(ATermList annos, AFun fun, ATerm[] i_args) {
    this.fun = fun;
    this.args = i_args;
    this.internSetAnnotations(annos);
    this.setHashCode(this.hashFunction());
  }

  public SharedObject duplicate() {
    ATermApplImpl clone = new ATermApplImpl(factory);
    clone.init(hashCode(), getAnnotations(), fun, args);
    return clone;
  }

  protected ATermAppl make(AFun fun, ATerm[] i_args, ATermList annos) {
    return getPureFactory().makeAppl(fun, i_args, annos);
  }

  protected ATermAppl make(AFun fun, ATerm[] i_args) {
    return make(fun, i_args, getPureFactory().makeList());
  }

  public boolean equivalent(SharedObject obj) {
    if (super.equivalent(obj)) {
      ATermAppl peer = (ATermAppl) obj;
      if (peer.getAFun().equals(fun)) {
        for (int i = 0; i < args.length; i++) {
          if (!peer.getArgument(i).equals(args[i])) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  protected boolean match(ATerm pattern, List list) {
    if (pattern.getType() == APPL) {
      ATermAppl appl = (ATermAppl) pattern;
      if (fun.equals(appl.getAFun())) {
        return matchArguments(appl.getArgumentArray(), list);
      }
      return false;
    }

    if (pattern.getType() == PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
      if (type.getType() == APPL) {
        ATermAppl appl = (ATermAppl) type;
        AFun afun = appl.getAFun();
        if (afun.getName().equals("appl") && !afun.isQuoted()) {
          list.add(fun.getName());
          return matchArguments(appl.getArgumentArray(), list);
        } else if (afun.getName().equals("str") && !afun.isQuoted()) {
          if (fun.isQuoted()) {
            list.add(fun.getName());
            return matchArguments(appl.getArgumentArray(), list);
          }
        } else if (afun.getName().equals("fun") && !afun.isQuoted()) {
          if (!fun.isQuoted()) {
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

  boolean matchArguments(ATerm[] pattern_args, List list) {
    for (int i = 0; i < args.length; i++) {
      if (i >= pattern_args.length) {
        return false;
      }

      ATerm arg = args[i];
      ATerm pattern_arg = pattern_args[i];

      if (pattern_arg.getType() == PLACEHOLDER) {
        ATerm ph_type = ((ATermPlaceholder) pattern_arg)
          .getPlaceholder();
        if (ph_type.getType() == APPL) {
          ATermAppl appl = (ATermAppl) ph_type;
          if (appl.getName().equals("list") && appl.getArguments().isEmpty()) {
            ATermList result = ((PureFactory) getFactory()).getEmpty();
            for (int j = args.length - 1; j >= i; j--) {
              result = result.insert(args[j]);
            }
            list.add(result);
            return true;
          }
        }
      }

      List submatches = arg.match(pattern_arg);
      if (submatches == null) {
        return false;
      }
      list.addAll(submatches);
    }

    return args.length == pattern_args.length;
  }

  public ATerm[] getArgumentArray() {
    return args;
  }

  public AFun getAFun() {
    return fun;
  }

  public ATermList getArguments() {
    ATermList result = ((PureFactory) getFactory()).getEmpty();

    for (int i = args.length - 1; i >= 0; i--) {
      result = result.insert(args[i]);
    }

    return result;
  }

  public ATerm getArgument(int index) {
    return args[index];
  }

  public ATermAppl setArgument(ATerm newarg, int index) {
    ATerm[] newargs = (ATerm[]) args.clone();
    newargs[index] = newarg;

    return make(fun, newargs, getAnnotations());
  }

  public boolean isQuoted() {
    return fun.isQuoted();
  }

  public String getName() {
    return fun.getName();
  }

  public int getArity() {
    return args.length;
  }

  public ATerm make(List arguments) {
    ATerm[] newargs = new ATerm[this.args.length];
    for(int i = 0; i < args.length; i++){
    	newargs[i] = args[i].make(arguments);
    }
    
    PureFactory pf = getPureFactory();
    ATermList empty = pf.getEmpty();
    
    ATermList annos = getAnnotations();
    ATermList tempAnnos = empty;
    while(annos != empty){
    	tempAnnos = pf.makeList(annos.getFirst().make(arguments), tempAnnos);
    	annos = annos.getNext();
    }
    ATermList newAnnos = tempAnnos.reverse();
    
    return getPureFactory().makeAppl(fun, newargs, newAnnos);
  }

  public ATerm setAnnotations(ATermList annos) {
    return make(fun, args, annos);
  }

  public aterm.Visitable accept(Visitor v) throws VisitFailure {
    return v.visitAppl(this);
  }

  public int getNrSubTerms() {
    return args.length;
  }

  public ATerm getSubTerm(int index) {
    return args[index];
  }

  public ATerm setSubTerm(int index, ATerm t) {
    return setArgument(t, index);
  }

  protected Object[] serialize() {
    int arity = getArity();
    Object[] o = new Object[arity + 2];
    for (int i = 0; i < arity; i++) {
      o[i] = getArgument(i);
    }
    o[arity] = getAnnotations();
    o[arity + 1] = getAFun();
    return o;
  }

  protected int hashFunction() {
    int initval = 0; /* the previous hash value */
    int a, b, c, len;

    /* Set up the internal state */
    len = getArity();
    a = b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    c = initval; /* the previous hash value */
    /*---------------------------------------- handle most of the key */
    if (len >= 12) {
      return staticDoobs_hashFuntion(serialize());
      // return PureFactory.doobs_hashFunction(serialize());
    }

    /*------------------------------------- handle the last 11 bytes */
    c += len;
    c += (getAnnotations().hashCode() << 8);
    b += (getAFun().hashCode() << 8);

    switch (len) {
      case 11:
        c += (getArgument(10).hashCode() << 24);
      case 10:
        c += (getArgument(9).hashCode() << 16);
      case 9:
        c += (getArgument(8).hashCode() << 8);
      case 8:
        b += (getArgument(7).hashCode() << 24);
      case 7:
        b += (getArgument(6).hashCode() << 16);
      case 6:
        b += (getArgument(5).hashCode() << 8);
      case 5:
        b += (getArgument(4).hashCode());
      case 4:
        a += (getArgument(3).hashCode() << 24);
      case 3:
        a += (getArgument(2).hashCode() << 16);
      case 2:
        a += (getArgument(1).hashCode() << 8);
      case 1:
        a += (getArgument(0).hashCode());
        /* case 0: nothing left to add */
    }
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

  static private int staticDoobs_hashFuntion(Object[] o) {
    // System.out.println("static doobs_hashFuntion");

    int initval = 0; /* the previous hash value */
    int a, b, c, len;

    /* Set up the internal state */
    len = o.length;
    a = b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    c = initval; /* the previous hash value */

    /*---------------------------------------- handle most of the key */
    int k = 0;
    while (len >= 12) {
      a += (o[k + 0].hashCode() + (o[k + 1].hashCode() << 8)
          + (o[k + 2].hashCode() << 16) + (o[k + 3].hashCode() << 24));
      b += (o[k + 4].hashCode() + (o[k + 5].hashCode() << 8)
          + (o[k + 6].hashCode() << 16) + (o[k + 7].hashCode() << 24));
      c += (o[k + 8].hashCode() + (o[k + 9].hashCode() << 8)
          + (o[k + 10].hashCode() << 16) + (o[k + 11].hashCode() << 24));
      // mix(a,b,c);
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

      k += 12;
      len -= 12;
    }

    /*------------------------------------- handle the last 11 bytes */
    c += o.length;
    switch (len) /* all the case statements fall through */
    {
      case 11:
        c += (o[k + 10].hashCode() << 24);
      case 10:
        c += (o[k + 9].hashCode() << 16);
      case 9:
        c += (o[k + 8].hashCode() << 8);
        /* the first byte of c is reserved for the length */
      case 8:
        b += (o[k + 7].hashCode() << 24);
      case 7:
        b += (o[k + 6].hashCode() << 16);
      case 6:
        b += (o[k + 5].hashCode() << 8);
      case 5:
        b += o[k + 4].hashCode();
      case 4:
        a += (o[k + 3].hashCode() << 24);
      case 3:
        a += (o[k + 2].hashCode() << 16);
      case 2:
        a += (o[k + 1].hashCode() << 8);
      case 1:
        a += o[k + 0].hashCode();
        /* case 0: nothing left to add */
    }
    // mix(a,b,c);
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
