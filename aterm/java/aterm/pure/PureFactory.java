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

public class PureFactory
  implements ATermFactory
{
  private static int DEFAULT_TERM_TABLE_SIZE = 43117;
  private static int DEFAULT_AFUN_TABLE_SIZE = 2003;;
  private static int DEFAULT_PATTERN_CACHE_SIZE = 4321;

  public static ATermFactory factory;

  private int term_table_size;
  private HashedWeakRef[] term_table;

  private int afun_table_size;
  private HashedWeakRef[] afun_table;

  //{ public PureFactory()

  public PureFactory()
  {
    this(DEFAULT_TERM_TABLE_SIZE, DEFAULT_AFUN_TABLE_SIZE);
  }

  //}
  //{ public PureFactory(int term_table_size, int afun_table_size)

  public PureFactory(int term_table_size, int afun_table_size)
  {
    this.term_table_size = term_table_size;
    this.term_table = new HashedWeakRef[term_table_size];

    this.afun_table_size = afun_table_size;
    this.afun_table = new HashedWeakRef[afun_table_size];
  }

  //}
  
  //{ ATermInt makeInt(int val)

  synchronized ATermInt makeInt(int val)
  {
    ATerm term;
    int hnr = ATermIntImpl.hashFunction(val);
    int idx = hnr % table_size;

    HashedWeakRef prev, cur;
    prev = null;
    cur  = term_table[idx];
    while (cur != null) {
      term = (ATerm)cur.get();
      if (term == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  table[idx] = cur.next;
	} else {
	  prev.next = cur.next;
	}
      } else {
	if (term.getType() == ATerm.INT) {
	  if (((ATermInt)term).getInt() == val) {
	    return (ATermInt)term;
	  }
	}
      }
      cur = cur.next;
    }
    
    // No integer term with 'val' found, so let's create one!
    term = new ATermInt(val);
    cur = new HashedWeakRef(term, table[idx]);
    table[idx] = cur;

    return (ATermInt)term;
  }

  //}
  //{ ATermInt makeReal(double val)

  synchronized ATermReal makeReal(int val)
  {
    ATerm term;
    int hnr = ATermRealImpl.hashFunction(val);
    int idx = hnr % table_size;

    HashedWeakRef prev, cur;
    prev = null;
    cur  = term_table[idx];
    while (cur != null) {
      term = (ATerm)cur.get();
      if (term == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  table[idx] = cur.next;
	} else {
	  prev.next = cur.next;
	}
      } else {
	if (term.getType() == ATerm.REAL) {
	  if (((ATermReal)term).getValue() == val) {
	    return (ATermReal)term;
	  }
	}
      }
      cur = cur.next;
    }
    
    // No real term with 'val' found, so let's create one!
    term = new ATermReal(val);
    cur = new HashedWeakRef(term, table[idx]);
    table[idx] = cur;

    return (ATermReal)term;
  }

  //}
  //{ ATermInt makeList(ATerm first, ATermList next)

  synchronized ATermList makeList(ATerm first, ATermList next)
  {
    ATerm term;
    int hnr = ATermListImpl.hashFunction(first, next);
    int idx = hnr % table_size;

    HashedWeakRef prev, cur;
    prev = null;
    cur  = term_table[idx];
    while (cur != null) {
      term = (ATerm)cur.get();
      if (term == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  table[idx] = cur.next;
	} else {
	  prev.next = cur.next;
	}
      } else {
	if (term.getType() == ATerm.LIST) {
	  ATermList list = (ATermList)term;
	  if (list.getFirst() == first && list.getNext() == next) {
	    return list;
	  }
	}
      }
      cur = cur.next;
    }
    
    // No existing term found, so let's create one!
    term = new ATermList(first, next);
    cur = new HashedWeakRef(term, table[idx]);
    table[idx] = cur;

    return (ATermList)term;
  }

  //}

  //{ ATerm parsePattern(String pattern)

  ATerm parsePattern(String pattern)
    throws ParseError
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}

  //{ protected boolean isDeepEqual(ATermImpl t1, ATerm t2)

  protected boolean isDeepEqual(ATermImpl t1, ATerm t2)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}

}

//{ class HashedWeakRef

class HashedWeakRef extends WeakReference
{
  protected HashedWeakRef next;

  public HashedWeakRef(ATerm term, HashedWeakRef next)
  {
    super(term);
    this.next = next;
  }
}

//}
