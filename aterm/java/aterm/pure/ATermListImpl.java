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

import java.util.List;

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
    return first.hashCode() ^ next.hashCode();
  }

  //}
  //{ int hashCode()

  int hashCode()
  {
    return first.hashCode() ^ next.hashCode();
  }

  //}
  //{ int getType()

  int getType()
  {
    return ATerm.LIST;
  }

  //}

  //{ protected ATermListImpl(ATerm first, ATermList next)

  protected ATermListImpl(ATerm first, ATermList next)
  {
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

      if (this == emtpy) {
	return l == empty;
      }

      if (l == empty) {
	return false;
      }

      // match("[1,2,3],[<list>]")
      if (l.first.getType() == PLACEHOLDER) {
	ATerm ph_type = ((ATermPlaceholder)l.first).getPlaceholderType();
	if (ph_type.getType() == APPL) {
	  ATermAppl appl = (ATermAppl)ph_type;
	  if (appl.getName().equals("list") && appl.getArguments().isEmpty()) {
	    list.add(this);
	    return true;
	  }
	} 
      }

      if (!first.match(l.first, list)) {
	return false;
      }

      return next.match(l.next, list);
    }

    return super.match(pattern, list);;
  }

  //}
  //{ public String toString()

  public String toString()
  {
    if (this == empty) {
      return "";
    }

    if (next == empty) {
      return first.toString();
    } else {
      return first.toString() + "," + next.toString();
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
    while (cur.next != empty) {
      cur = cur.next;
    }

    return cur.first;
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
      cur = cur.next;
    }

    while (cur != empty && cur.first != el) {
      cur = cur.next;
      ++i;
    }

    return list == empty ? -1 : i;
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
      return PureFactory.factory.makeList(first, rhs);
    }

    return PureFactory.factory.makeList(first, next.concat(rhs));
  }

  //}
  //{ public ATermList append(ATerm el) 

  public ATermList append(ATerm el) 
  {
    return concat(PureFactory.factory.makeList(el, empty));
  }

  //}
  //{ public ATerm elementAt(int index)

  public ATerm elementAt(int index)
  {
    if (0 > index || index > length) {
      return new IllegalArgumentException("illegal list index: " + index);
    } 

    ATermList cur = this;
    for (int i=0; i<index; i++) {
      cur = cur.next;
    }

    return cur.first;
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

    return PureFactory.factory.makeList(first, result);
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

    return PureFactory.factory.makeList(first, next.removeElementAt(index-1));
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

    return PureFactory.factory.makeList(first, result);
  }

  //}
  //{ public ATermList insert(ATerm el)

  public ATermList insert(ATerm el)
  {
    return PureFactory.factory.makeList(el, this);
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

    return PureFactory.factory.makeList(first, next.insertAt(el, i-1));
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
      next = cur.next;
      if (next == empty) {
	cur = empty;
	for (int i=elems.size()-1; i>=0; i--) {
	  cur = cur.insert((ATerm)elems.get(i));
	}
	return cur;
      } else {
	elems.add(cur.first);
	cur = cur.next;
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
      list = list.next;
    }

    for (i=0; i<size; i++) {
      buffer.add(list.first);
      list = list.next;
    }

    for (--i; i>=0; i--) {
      result = result.insert(buffer.get(i));
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
      buffer.add(cur.first);
      cur = cur.next;
    }

    /* Skip the old element */
    cur = cur.next;

    /* Add the new element */
    cur = cur.insert(el);

    /* Add the prefix */
    for(--lcv; lcv>=0; lcv--) {
      cur = cur.insert(buffer.get(i));
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
