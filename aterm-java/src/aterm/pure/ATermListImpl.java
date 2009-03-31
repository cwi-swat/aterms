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

import java.util.ArrayList;
import java.util.List;

import jjtraveler.VisitFailure;
import shared.SharedObject;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;
import aterm.ATermPlaceholder;
import aterm.Visitable;
import aterm.Visitor;

public class ATermListImpl extends ATermImpl implements ATermList {
  private ATerm first;

  private ATermList next;

  private int length;
  
  protected ATermListImpl(PureFactory factory) {
    super(factory);
  }
  
  protected ATermListImpl(PureFactory factory, ATermList annos, ATerm first, ATermList next) {
    super(factory, annos);
    
    this.first = first;
    this.next = next;
    
    if (first == null && next == null) {
      this.length = 0;
    } else {
      this.length = 1 + next.getLength();
    }
    
    setHashCode(hashFunction());
  }

  public int getType() {
    return ATerm.LIST;
  }

  /**
   * @depricated Use the new constructor instead.
   * @param hashCode
   * @param annos
   * @param first
   * @param next
   */
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

  /**
   * @depricated Use the new constructor instead.
   * @param annos
   * @param first
   * @param next
   */
  protected void initHashCode(ATermList annos, ATerm first, ATermList next) {
    this.first = first;
    this.next = next;
    this.internSetAnnotations(annos);
    this.setHashCode(this.hashFunction());
    // super.init(hashCode, annos);
    if (first == null && next == null) {
      this.length = 0;
    } else {
      this.length = 1 + next.getLength();
    }
  }

  public SharedObject duplicate() {
    return this;
  }

	public boolean equivalent(SharedObject obj){
		if(obj instanceof ATermList){
			ATermList peer = (ATermList) obj;
			if(peer.getType() != getType()) return false;
			
			return (peer.getFirst() == first && peer.getNext() == next && peer.getAnnotations().equals(getAnnotations()));
		}
		
		return false;
  }

  public ATermList insert(ATerm el){
	  ATermList tail = (!this.hasAnnotations()) ? this : ((ATermList) this.removeAnnotations());
	  
	  return getPureFactory().makeList(el, tail);
  }

  protected ATermList make(ATerm head, ATermList tail) {
    return make(head, tail, getPureFactory().makeList());
  }

  protected ATermList make(ATerm head, ATermList tail, ATermList annos) {
    return getPureFactory().makeList(head, tail, annos);
  }

  public ATermList getEmpty() {
    return getPureFactory().makeList();
  }

  public ATerm setAnnotations(ATermList annos) {
    return make(first, next, annos);
  }

  protected boolean match(ATerm pattern, List<Object> list) {
    if (pattern.getType() == LIST) {
      ATermList l = (ATermList) pattern;

      if (l.isEmpty()) {
        return this.isEmpty();
      }

      if (l.getFirst().getType() == PLACEHOLDER) {
        ATerm ph_type = ((ATermPlaceholder) l.getFirst()).getPlaceholder();
        if (ph_type.getType() == APPL) {
          ATermAppl appl = (ATermAppl) ph_type;
          if (appl.getName().equals("list")
              && appl.getArguments().isEmpty()) {
            list.add(this);
            return true;
              }
        }
      }

      if (!isEmpty()) {
        List<Object> submatches = first.match(l.getFirst());
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
      return l.isEmpty();
    }

    return super.match(pattern, list);
  }

  public ATerm make(List<Object> args) {
    if (first == null) {
      return this;
    }

    ATerm head = first.make(args);
    ATermList tail = (ATermList) next.make(args);
    if (isListPlaceHolder(first)) {
      /*
       * this is to solve the make([<list>],[]) problem the result should
       * be [] and not [[]] to be compatible with the C version
       */
      return head;
    }
    return tail.insert(head);

  }

  private boolean isListPlaceHolder(ATerm pattern) {
    if (pattern.getType() == ATerm.PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
        ATermAppl appl = (ATermAppl) type;
        AFun afun = appl.getAFun();
        if (afun.getName().equals("list") && afun.getArity() == 0
            && !afun.isQuoted()) {
          return true;
            }
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return (first == null && next == null);
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
    while (!cur.getNext().isEmpty()) {
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

    while (!cur.isEmpty() && cur.getFirst() != el) {
      cur = cur.getNext();
      ++i;
    }

    return cur.isEmpty() ? -1 : i;
  }

  public int lastIndexOf(ATerm el, int start) {
    int result;

    if (start < 0) {
      start += length + 1;
    }

    if (start > length) {
      throw new IllegalArgumentException("start (" + start
          + ") > length of list (" + length + ")");
    }

    if (start > 0) {
      result = next.lastIndexOf(el, start - 1);
      if (result >= 0) {
        return result + 1;
      }
    }

    if (first == el) {
      return 0;
    }
    return -1;
  }

  public ATermList concat(ATermList rhs) {
    if (isEmpty()) {
      return rhs;
    }

    if (next.isEmpty()) {
      return rhs.insert(first);
    }

    return next.concat(rhs).insert(first);
  }

  public ATermList append(ATerm el) {
    return this.concat(getEmpty().insert(el));
  }

  public ATerm elementAt(int index) {
    if (0 > index || index >= length) {
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

    return result.insert(first);
  }

  public ATermList removeElementAt(int index) {
    if (0 > index || index > length) {
      throw new IllegalArgumentException("illegal list index: " + index);
    }

    if (index == 0) {
      return next;
    }

    return next.removeElementAt(index - 1).insert(first);
  }

  public ATermList removeAll(ATerm el) {
    if (first == el) {
      return next.removeAll(el);
    }

    ATermList result = next.removeAll(el);

    if (result == next) {
      return this;
    }

    return result.insert(first);
  }

  public ATermList insertAt(ATerm el, int i) {
    if (0 > i || i > length) {
      throw new IllegalArgumentException("illegal list index: " + i);
    }

    if (i == 0) {
      return insert(el);
    }

    return next.insertAt(el, i - 1).insert(first);
  }

  public ATermList getPrefix() {
    if (isEmpty()) {
      return this;
    }

    ATermList cur = this;
    List<ATerm> elems = new ArrayList<ATerm>();

    while (true) {
      if (cur.getNext().isEmpty()) {
        cur = getPureFactory().getEmpty();
        for (int i = elems.size() - 1; i >= 0; i--) {
          cur = cur.insert(elems.get(i));
        }
        return cur;
      }
      elems.add(cur.getFirst());
      cur = cur.getNext();
    }
  }

  public ATermList getSlice(int start, int end) {
    int i, size = end - start;
    
    ATermList list = this;
    for (i = 0; i < start; i++) {
      list = list.getNext();
    }

    List<ATerm> buffer = new ArrayList<ATerm>(size);
    for (i = 0; i < size; i++) {
      buffer.add(list.getFirst());
      list = list.getNext();
    }
    
    ATermList result = getPureFactory().getEmpty();
    for (--i; i >= 0; i--) {
      result = result.insert(buffer.get(i));
    }

    return result;
  }

  public ATermList replace(ATerm el, int i) {
    int lcv;

    if (0 > i || i > length) {
      throw new IllegalArgumentException("illegal list index: " + i);
    }

    List<ATerm> buffer = new ArrayList<ATerm>(i);
    ATermList cur = this;
    
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
      cur = cur.insert(buffer.get(lcv));
    }

    return cur;
  }

  public ATermList reverse() {
    ATermList cur = this;
    ATermList reverse = this.getEmpty();
    while (!cur.isEmpty()) {
      reverse = reverse.insert(cur.getFirst());
      cur = cur.getNext();
    }
    return reverse;
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
    if (isEmpty()) {
      ATermList pair = getEmpty().insert(value).insert(key);
      return getEmpty().insert(pair);
    }

    ATermList pair = (ATermList) first;
    if (key.equals(pair.getFirst())) {
      pair = getEmpty().insert(value).insert(pair);
      return next.insert(pair);

    }

    return (ATermList) next.dictPut(key, value).insert(first).setAnnotations(getAnnotations());
  }

  public ATermList dictRemove(ATerm key) {
    if (isEmpty()) {
      return this;
    }

    ATermList pair = (ATermList) first;

    if (key.equals(pair.getFirst())) {
      return next;
    }

    return (ATermList) next.dictRemove(key).insert(first).setAnnotations(getAnnotations());
  }

  public Visitable accept(Visitor v) throws VisitFailure {
	return v.visitList(this);
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

  protected int findEmptyHashCode() {
    int magic = 0;
    for(int x = Integer.MIN_VALUE ; x < Integer.MAX_VALUE ; x++) {
      /* Set up the internal state */
      int a = 0x9e3779b9; /* the golden ratio; an arbitrary value */
      int b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
      int c = 3; /* the previous hash value */

      /*------------------------------------- handle the last 11 bytes */
      a += (x << 16);

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

      if(c==x) {
        System.out.println("magic x = " + x);
        magic = x;
        //return x;
      }

      if(x%100000000 ==0) {
        System.out.println("x = " + x);
      }
    }

    return magic;
  }

  
  private int hashFunction() {
    /* Set up the internal state */
    int a = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    int b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    int c = 3; /* the previous hash value */

    /*------------------------------------- handle the last 11 bytes */
    a += (getAnnotations().hashCode() << 16);
    if (next != null && first != null) {
      a += (next.hashCode() << 8);
      a += (first.hashCode());
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

}
