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
import java.util.Vector;

class ATermListImpl
  extends ATermImpl
  implements ATermList
{
  ATerm     first;
  ATermList next;
  int length;

  //{{{ static int hashFunction(ATerm first, ATermList next, ATermList annos)

  static int hashFunction(ATerm first, ATermList next, ATermList annos)
  {
    int hnr;

    if (first == null && next == null) {
      return 42;
    }

    hnr = next.hashCode();
    return Math.abs(first.hashCode() ^ (hnr << 1) ^ (hnr >> 1) + annos.hashCode());
  }

  //}}}
  //{{{ public int hashCode()

  public int hashCode()
  {
    return hashFunction(first, next, annotations);
  }

  //}}}
  //{{{ public int getType()

  public int getType()
  {
    return ATerm.LIST;
  }

  //}}}

  //{{{ protected ATermListImpl(PureFactory factory)

  protected ATermListImpl(PureFactory factory)
  {
    super(factory, null);
    this.annotations = this;
    this.first  = null;
    this.next   = null;
    this.length = 0;
  }

  //}}}
  //{{{ protected ATermListImpl(factory, ATerm first, next, annos)

  protected ATermListImpl(PureFactory factory, ATerm first, ATermList next,
			  ATermList annos)
  {
    super(factory, annos);
    this.first  = first;
    this.next   = next;

    if (first == null && next == null) {
      this.length = 0;
    } else {
      this.length = 1 + next.getLength();
    }
  }

  //}}}

  //{{{ public boolean match(ATerm pattern, List list)

  protected boolean match(ATerm pattern, List list)
  {
    if (pattern.getType() == LIST) {
      ATermList l = (ATermList)pattern;

      if (this == PureFactory.empty) {
	return l == PureFactory.empty;
      }

      if (l == PureFactory.empty) {
	return false;
      }

      // match("[1,2,3],[<list>]")
      if (l.getFirst().getType() == PLACEHOLDER) {
	ATerm ph_type = ((ATermPlaceholder)l.getFirst()).getPlaceholder();
	if (ph_type.getType() == APPL) {
	  ATermAppl appl = (ATermAppl)ph_type;
	  if (appl.getName().equals("list") && appl.getArguments().isEmpty()) {
	    list.add(this);
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

    return super.match(pattern, list);;
  }

  //}}}
  //{{{ public ATerm make(List args)

  public ATerm make(List args)
  {
    if (first == null) {
      return this;
    }

    return factory.makeList(first.make(args), (ATermList)next.make(args));
  }

  //}}}
  
  //{{{ public String toString()

  public String toString()
  {
    StringBuffer result = new StringBuffer();

    if (this == PureFactory.empty) {
      return "[]";
    }

    result.append("[");

    ATermList list = this;
    while (!list.isEmpty()) {
      result.append(list.getFirst().toString());
      list = list.getNext();
      if (!list.isEmpty()) {
	result.append(",");
      }
    }
    
    result.append("]");
    result.append(super.toString());

    return result.toString();
  }

  //}}}
  
  //{{{ public boolean isEmpty()

  public boolean isEmpty()
  {
    return this == PureFactory.empty;
  }


  //}}}
  //{{{ public int getLength()

  public int getLength()
  {
    return length;
  }

  //}}}
  //{{{ public ATerm getFirst()

  public ATerm getFirst()
  {
    return first;
  }

  //}}}
  //{{{ public ATermList getNext()

  public ATermList getNext()
  {
    return next;
  }

  //}}}
  //{{{ public ATerm getLast()

  public ATerm getLast()
  {
    ATermList cur;

    cur = this;
    while (cur.getNext() != PureFactory.empty) {
      cur = cur.getNext();
    }

    return cur.getFirst();
  }

  //}}}
  //{{{ public int indexOf(ATerm el, int start)

  public int indexOf(ATerm el, int start)
  {
    int i;
    ATermList cur;

    if (start < 0) {
      start += length + 1;
    }

    if (start > length) {
      throw new IllegalArgumentException("start (" + start +
					 ") > length of list (" + length + ")");
    }

    cur = this;
    for (i=0; i<start; i++) {
      cur = cur.getNext();
    }

    while (cur != PureFactory.empty && cur.getFirst() != el) {
      cur = cur.getNext();
      ++i;
    }

    return cur == PureFactory.empty ? -1 : i;
  }

  //}}}
  //{{{ public int lastIndexOf(ATerm el, int start)

  public int lastIndexOf(ATerm el, int start)
  {
    int result;

    if (start < 0) {
      start += length + 1;
    }

    if (start > length) {
      throw new IllegalArgumentException("start (" + start +
					 ") > length of list (" + length + ")");
    }

    if (start > 0) {
      result = next.lastIndexOf(el, start-1);
      if (result >= 0) {
	return result+1;
      }
    }

    if (first == el) {
      return 0;
    } else {
      return -1;
    }
  }

  //}}}
  //{{{ public ATermList concat(ATermList rhs)

  public ATermList concat(ATermList rhs)
  {
    if (next == PureFactory.empty) {
      return factory.makeList(first, rhs);
    }

    return factory.makeList(first, next.concat(rhs));
  }

  //}}}
  //{{{ public ATermList append(ATerm el) 

  public ATermList append(ATerm el) 
  {
    return concat(factory.makeList(el, PureFactory.empty));
  }

  //}}}
  //{{{ public ATerm elementAt(int index)

  public ATerm elementAt(int index)
  {
    if (0 > index || index > length) {
      throw new IllegalArgumentException("illegal list index: " + index);
    } 

    ATermList cur = this;
    for (int i=0; i<index; i++) {
      cur = cur.getNext();
    }

    return cur.getFirst();
  }

  //}}}
  //{{{ public ATermList remove(ATerm el)

  public ATermList remove(ATerm el)
  {
    if (first == el) {
      return next;
    }

    ATermList result = next.remove(el);

    if (result == next) {
      return this;
    }

    return factory.makeList(first, result);
  }

  //}}}
  //{{{ public ATermList removeElementAt(int index)

  public ATermList removeElementAt(int index)
  {
    if (0 > index || index > length) {
      throw new IllegalArgumentException("illegal list index: " + index);
    }

    if (index == 0) {
      return next;
    }

    return factory.makeList(first, next.removeElementAt(index-1));
  }

  //}}}
  //{{{ public ATermList removeAll(ATerm el)

  public ATermList removeAll(ATerm el)
  {
    if (first == el) {
      return next.removeAll(el);
    }

    ATermList result = next.removeAll(el);

    if (result == next) {
      return this;
    }

    return factory.makeList(first, result);
  }

  //}}}
  //{{{ public ATermList insert(ATerm el)

  public ATermList insert(ATerm el)
  {
    return factory.makeList(el, this);
  }

  //}}}
  //{{{ public ATermList insertAt(ATerm el, int i)

  public ATermList insertAt(ATerm el, int i)
  {
    if (0 > i || i > length) {
      throw new IllegalArgumentException("illegal list index: " + i);
    }

    if (i == 0) {
      return insert(el);
    }

    return factory.makeList(first, next.insertAt(el, i-1));
  }

  //}}}
  //{{{ public ATermList getPrefix()

  public ATermList getPrefix()
  {
    ATermList cur, next;
    List elems;

    if(this == PureFactory.empty) {
      return this;
    }
    

    cur = this;
    elems = new Vector();
    
    while (true) {
      next = cur.getNext();
      if (next == PureFactory.empty) {
	cur = PureFactory.empty;
	for (int i=elems.size()-1; i>=0; i--) {
	  cur = cur.insert((ATerm)elems.get(i));
	}
	return cur;
      } else {
	elems.add(cur.getFirst());
	cur = cur.getNext();
      }
    }
  }

  //}}}
  //{{{ public ATermList getSlice(int start, int end)

  public ATermList getSlice(int start, int end)
  {
    int i, size = end-start;
    ATermList result = PureFactory.empty;
    ATermList list;

    List buffer = new Vector(size);

    list = this;
    for (i=0; i<start; i++) {
      list = list.getNext();
    }

    for (i=0; i<size; i++) {
      buffer.add(list.getFirst());
      list = list.getNext();
    }

    for (--i; i>=0; i--) {
      result = result.insert((ATerm)buffer.get(i));
    }

    return result;
  }

  //}}}
  //{{{ public ATermList replace(ATerm el, int i)

  public ATermList replace(ATerm el, int i)
  {
    int lcv;
    List buffer;
    ATermList cur;

    if (0 > i || i > length) {
      throw new IllegalArgumentException("illegal list index: " + i);
    }

    buffer = new Vector(i);

    cur = this;
    for (lcv=0; lcv<i; lcv++) {
      buffer.add(cur.getFirst());
      cur = cur.getNext();
    }

    /* Skip the old element */
    cur = cur.getNext();

    /* Add the new element */
    cur = cur.insert(el);

    /* Add the prefix */
    for(--lcv; lcv>=0; lcv--) {
      cur = cur.insert((ATerm)buffer.get(i));
    }

    return cur;
  }

  //}}}
  //{{{ public ATermList reverse()

  public ATermList reverse()
  {
    if (this == PureFactory.empty) {
      return this;
    }

    return next.reverse().insert(first);
  }

  //}}}
  //{{{ public ATerm dictGet(ATerm key)

  public ATerm dictGet(ATerm key)
  {
    if (isEmpty()) {
      return null;
    }

    ATermList pair = (ATermList)first;

    if (key.equals(pair.getFirst())) {
      return pair.getNext().getFirst();
    }

    return next.dictGet(key);
  }

  //}}}
  //{{{ public ATermList dictPut(ATerm key, ATerm value)

  public ATermList dictPut(ATerm key, ATerm value)
  {
    ATermList pair;

    if (isEmpty()) {
      pair = factory.makeList(key, factory.makeList(value));
      return factory.makeList(pair);
    }

    pair = (ATermList)first;

    if (key.equals(pair.getFirst())) {
      pair = factory.makeList(key, factory.makeList(value));
      return factory.makeList(pair, next);
    }

    return factory.makeList(first, next.dictPut(key, value), annotations);
  }

  //}}}
  //{{{ public ATermList dictRemove(ATerm key)

  public ATermList dictRemove(ATerm key)
  {
    ATermList pair;

    if (isEmpty()) {
      return this;
    }

    pair = (ATermList)first;

    if (key.equals(pair.getFirst())) {
      return next;
    }

    return factory.makeList(first, next.dictRemove(key), annotations);
  }

  //}}}
  //{{{ public ATerm setAnnotations(ATermList annos)

  public ATerm setAnnotations(ATermList annos)
  {
    return factory.makeList(first, next, annos);
  }

  //}}}
}
