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
  static    ATermList empty;
  ATerm     first;
  ATermList next;
  int length;

  //{ static int hashFunction(ATerm first, ATermList next)

  static int hashFunction(ATerm first, ATermList next)
  {
    int hnr;

    hnr = next.hashCode();
    return first.hashCode() ^ (hnr << 1) ^ (hnr >> 1);
  }

  //}
  //{ public int hashCode()

  public int hashCode()
  {
    return hashFunction(first, next);
  }

  //}
  //{ public int getType()

  public int getType()
  {
    return ATerm.LIST;
  }

  //}

  //{ protected ATermListImpl(PureFactory factory, ATerm first, ATermList next)

  protected ATermListImpl(PureFactory factory, ATerm first, ATermList next)
  {
    super(factory);
    this.first  = first;
    this.next   = next;
    this.length = 1 + next.getLength();
  }

  //}

  //{ public boolean match(ATerm pattern, List list)

  protected boolean match(ATerm pattern, List list)
  {
    if (pattern.getType() == LIST) {
      ATermList l = (ATermList)pattern;

      if (this == empty) {
	return l == empty;
      }

      if (l == empty) {
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

  //}
  //{ public String toString()

  public String toString()
  {
    String result;

    if (this == empty) {
      return "";
    }

    result = first.toString();

    if (first.getType() == LIST) {
      result = "[" + result + "]";
    }

    if (next == empty) {
      return result;
    } else {
      return result + "," + next.toString();
    }
  }

  //}
  
  //{ public boolean isEmpty()

  public boolean isEmpty()
  {
    return this == empty;
  }


  //}
  //{ public int getLength()

  public int getLength()
  {
    return length;
  }

  //}
  //{ public ATerm getFirst()

  public ATerm getFirst()
  {
    return first;
  }

  //}
  //{ public ATermList getNext()

  public ATermList getNext()
  {
    return next;
  }

  //}
  //{ public ATerm getLast()

  public ATerm getLast()
  {
    ATermList cur;

    cur = this;
    while (cur.getNext() != empty) {
      cur = cur.getNext();
    }

    return cur.getFirst();
  }

  //}
  //{ public int indexOf(ATerm el, int start)

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

    while (cur != empty && cur.getFirst() != el) {
      cur = cur.getNext();
      ++i;
    }

    return cur == empty ? -1 : i;
  }

  //}
  //{ public int lastIndexOf(ATerm el, int start)

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

  //}
  //{ public ATermList concat(ATermList rhs)

  public ATermList concat(ATermList rhs)
  {
    if (next == empty) {
      return factory.makeList(first, rhs);
    }

    return factory.makeList(first, next.concat(rhs));
  }

  //}
  //{ public ATermList append(ATerm el) 

  public ATermList append(ATerm el) 
  {
    return concat(factory.makeList(el, empty));
  }

  //}
  //{ public ATerm elementAt(int index)

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

  //}
  //{ public ATermList remove(ATerm el)

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

  //}
  //{ public ATermList removeElementAt(int index)

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

  //}
  //{ public ATermList removeAll(ATerm el)

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

  //}
  //{ public ATermList insert(ATerm el)

  public ATermList insert(ATerm el)
  {
    return factory.makeList(el, this);
  }

  //}
  //{ public ATermList insertAt(ATerm el, int i)

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

  //}
  //{ public ATermList getPrefix()

  public ATermList getPrefix()
  {
    ATermList cur, next;
    List elems;

    if(this == empty) {
      return this;
    }
    

    cur = this;
    elems = new Vector();
    
    while (true) {
      next = cur.getNext();
      if (next == empty) {
	cur = empty;
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

  //}
  //{ public ATermList getSlice(int start, int end)

  public ATermList getSlice(int start, int end)
  {
    int i, size = end-start;
    ATermList result = empty;
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

  //}
  //{ public ATermList replace(ATerm el, int i)

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

  //}
  //{ public ATermList reverse()

  public ATermList reverse()
  {
    if (this == empty) {
      return this;
    }

    return next.reverse().insert(first);
  }

  //}  
}
