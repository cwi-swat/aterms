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

import java.util.*;

import shared.SharedObject;

import aterm.*;

class ATermListImpl extends ATermImpl implements ATermList {
  ATerm first;
  ATermList next;
  int length;

  public int getType() {
    return ATerm.LIST;
  }

  protected void init(int hashCode, ATermList annos, ATerm first, ATermList next) {
    super.init(hashCode, annos);
    this.first = first;
    this.next = next;

    if (first == null && next == null) {
      this.length = 0;
    } else {
      this.length = 1 + next.getLength();
    }
  }

  protected void initHashCode(ATermList annos, ATerm first, ATermList next) {
    this.first = first;
    this.next = next;
    this.internSetAnnotations(annos);
    this.setHashCode(this.hashFunction());
      //super.init(hashCode, annos);
    
    if (first == null && next == null) {
      this.length = 0;
    } else {
      this.length = 1 + next.getLength();
    }
  }

  
  public Object clone() {
    ATermListImpl clone = new ATermListImpl();
    clone.init(hashCode(), getAnnotations(), first, next);
    return clone;
  }
  
  public boolean equivalent(SharedObject obj) {
    if (super.equivalent(obj)) {
      ATermList peer = (ATermList) obj;
      if (peer.getLength() == length) {
        return peer.getFirst().equals(first) && peer.getNext().equals(next);
      }
    }
    
    return false;
  }


  protected boolean match(ATerm pattern, List list) {
    if (pattern.getType() == LIST) {
      ATermList l = (ATermList) pattern;


        /*
      if (this == PureFactory.empty) {
        return l == PureFactory.empty;
      }

      if (l == PureFactory.empty) {
        return false;
      }
        */

      if (l == PureFactory.empty) {
        return this == PureFactory.empty;
      }
      
      if (l.getFirst().getType() == PLACEHOLDER) {
        ATerm ph_type = ((ATermPlaceholder) l.getFirst()).getPlaceholder();
        if (ph_type.getType() == APPL) {
          ATermAppl appl = (ATermAppl) ph_type;
          if (appl.getName().equals("list") && appl.getArguments().isEmpty()) {
              //if(this != PureFactory.empty) {
              list.add(this);
                //}
            return true;
          }
        }
      }

      List submatches = first.match(l.getFirst());
      if (submatches == null) {
        return false;
      }

      list.addAll(submatches);

      submatches = next.match(l.getNext());

      if (submatches == null) {
        return false;
      }

      list.addAll(submatches);
      return true;
    }

    return super.match(pattern, list);
  }

  public ATerm make(List args) {
    if (first == null) {
      return this;
    }

    return getPureFactory().makeList(first.make(args), (ATermList) next.make(args));
  }

  public boolean isEmpty() {
    return this == PureFactory.empty;
  }

  public int getLength() {
    return length;
  }

  public ATerm getFirst() {
    return first;
  }

  public ATermList getNext() {
    return next;
  }

  public ATerm getLast() {
    ATermList cur;

    cur = this;
    while (cur.getNext() != PureFactory.empty) {
      cur = cur.getNext();
    }

    return cur.getFirst();
  }

  public int indexOf(ATerm el, int start) {
    int i;
    ATermList cur;

    if (start < 0) {
      start += length + 1;
    }

    if (start > length) {
      throw new IllegalArgumentException("start (" + start + ") > length of list (" + length + ")");
    }

    cur = this;
    for (i = 0; i < start; i++) {
      cur = cur.getNext();
    }

    while (cur != PureFactory.empty && cur.getFirst() != el) {
      cur = cur.getNext();
      ++i;
    }

    return cur == PureFactory.empty ? -1 : i;
  }

  public int lastIndexOf(ATerm el, int start) {
    int result;

    if (start < 0) {
      start += length + 1;
    }

    if (start > length) {
      throw new IllegalArgumentException("start (" + start + ") > length of list (" + length + ")");
    }

    if (start > 0) {
      result = next.lastIndexOf(el, start - 1);
      if (result >= 0) {
        return result + 1;
      }
    }

    if (first == el) {
      return 0;
    } else {
      return -1;
    }
  }

  public ATermList concat(ATermList rhs) {
    if (isEmpty()) {
      return rhs;
    }

    if (next == PureFactory.empty) {
      return getPureFactory().makeList(first, rhs);
    }

    return getPureFactory().makeList(first, next.concat(rhs));
  }

  public ATermList append(ATerm el) {
    return concat(getPureFactory().makeList(el, PureFactory.empty));
  }

  public ATerm elementAt(int index) {
    if (0 > index || index > length) {
      throw new IllegalArgumentException("illegal list index: " + index);
    }

    ATermList cur = this;
    for (int i = 0; i < index; i++) {
      cur = cur.getNext();
    }

    return cur.getFirst();
  }

  public ATermList remove(ATerm el) {
    if (first == el) {
      return next;
    }

    ATermList result = next.remove(el);

    if (result == next) {
      return this;
    }

    return getPureFactory().makeList(first, result);
  }

  public ATermList removeElementAt(int index) {
    if (0 > index || index > length) {
      throw new IllegalArgumentException("illegal list index: " + index);
    }

    if (index == 0) {
      return next;
    }

    return getPureFactory().makeList(first, next.removeElementAt(index - 1));
  }

  public ATermList removeAll(ATerm el) {
    if (first == el) {
      return next.removeAll(el);
    }

    ATermList result = next.removeAll(el);

    if (result == next) {
      return this;
    }

    return getPureFactory().makeList(first, result);
  }

  public ATermList insert(ATerm el) {
    return getPureFactory().makeList(el, this);
  }

  public ATermList insertAt(ATerm el, int i) {
    if (0 > i || i > length) {
      throw new IllegalArgumentException("illegal list index: " + i);
    }

    if (i == 0) {
      return insert(el);
    }

    return getPureFactory().makeList(first, next.insertAt(el, i - 1));
  }

  public ATermList getPrefix() {
    ATermList cur, next;
    List elems;

    if (this == PureFactory.empty) {
      return this;
    }

    cur = this;
    elems = new Vector();

    while (true) {
      next = cur.getNext();
      if (next == PureFactory.empty) {
        cur = PureFactory.empty;
        for (int i = elems.size() - 1; i >= 0; i--) {
          cur = cur.insert((ATerm) elems.get(i));
        }
        return cur;
      } else {
        elems.add(cur.getFirst());
        cur = cur.getNext();
      }
    }
  }

  public ATermList getSlice(int start, int end) {
    int i, size = end - start;
    ATermList result = PureFactory.empty;
    ATermList list;

    List buffer = new Vector(size);

    list = this;
    for (i = 0; i < start; i++) {
      list = list.getNext();
    }

    for (i = 0; i < size; i++) {
      buffer.add(list.getFirst());
      list = list.getNext();
    }

    for (--i; i >= 0; i--) {
      result = result.insert((ATerm) buffer.get(i));
    }

    return result;
  }

  public ATermList replace(ATerm el, int i) {
    int lcv;
    List buffer;
    ATermList cur;

    if (0 > i || i > length) {
      throw new IllegalArgumentException("illegal list index: " + i);
    }

    buffer = new Vector(i);

    cur = this;
    for (lcv = 0; lcv < i; lcv++) {
      buffer.add(cur.getFirst());
      cur = cur.getNext();
    }

    /* Skip the old element */
    cur = cur.getNext();

    /* Add the new element */
    cur = cur.insert(el);

    /* Add the prefix */
    for (--lcv; lcv >= 0; lcv--) {
      cur = cur.insert((ATerm) buffer.get(lcv));
    }

    return cur;
  }

  public ATermList reverse() {
    if (this == PureFactory.empty) {
      return this;
    }

    return next.reverse().insert(first);
  }

  public ATerm dictGet(ATerm key) {
    if (isEmpty()) {
      return null;
    }

    ATermList pair = (ATermList) first;

    if (key.equals(pair.getFirst())) {
      return pair.getNext().getFirst();
    }

    return next.dictGet(key);
  }

  public ATermList dictPut(ATerm key, ATerm value) {
    ATermList pair;

    if (isEmpty()) {
      pair = getPureFactory().makeList(key, getPureFactory().makeList(value));
      return getPureFactory().makeList(pair);
    }

    pair = (ATermList) first;

    if (key.equals(pair.getFirst())) {
      pair = getPureFactory().makeList(key, getPureFactory().makeList(value));
      return getPureFactory().makeList(pair, next);
    }

    return getPureFactory().makeList(first, next.dictPut(key, value), getAnnotations());
  }

  public ATermList dictRemove(ATerm key) {
    ATermList pair;

    if (isEmpty()) {
      return this;
    }

    pair = (ATermList) first;

    if (key.equals(pair.getFirst())) {
      return next;
    }

    return getPureFactory().makeList(first, next.dictRemove(key), getAnnotations());
  }

  public ATerm setAnnotations(ATermList annos) {
    return getPureFactory().makeList(first, next, annos);
  }

  public void accept(ATermVisitor v) throws ATermVisitFailure {
    v.visitList(this);
  }

  public int getNrSubTerms() {
    return length;
  }

  public ATerm getSubTerm(int index) {
    return elementAt(index);
  }

  public ATerm setSubTerm(int index, ATerm t) {
    return replace(t, index);
  }

  private int hashFunction() {
    /* Set up the internal state */
    int a = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    int b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    int c = 3;          /* the previous hash value */

    /*------------------------------------- handle the last 11 bytes */
    a += (getAnnotations().hashCode()<<16);
    a += (next.hashCode()<<8);
    a += (first.hashCode());
    
    a -= b; a -= c; a ^= (c >> 13);
    b -= c; b -= a; b ^= (a << 8);
    c -= a; c -= b; c ^= (b >> 13);
    a -= b; a -= c; a ^= (c >> 12);
    b -= c; b -= a; b ^= (a << 16);
    c -= a; c -= b; c ^= (b >> 5);
    a -= b; a -= c; a ^= (c >> 3);
    b -= c; b -= a; b ^= (a << 10);
    c -= a; c -= b; c ^= (b >> 15);

    /*-------------------------------------------- report the result */
    return c;
  }

  
}
