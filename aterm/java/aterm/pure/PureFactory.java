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

import java.lang.ref.*;
import java.io.*;
import java.util.*;

public class PureFactory
  implements ATermFactory
{
  private static int DEFAULT_TERM_TABLE_SIZE = 43117;
  private static int DEFAULT_AFUN_TABLE_SIZE = 2003;;
  private static int DEFAULT_PATTERN_CACHE_SIZE = 4321;

  private int term_table_size;
  private HashedWeakRef[] term_table;

  private int afun_table_size;
  private HashedWeakRef[] afun_table;

  private char lookahead;

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
  
  //{ public synchronized ATermInt makeInt(int val)

  public synchronized ATermInt makeInt(int val)
  {
    ATerm term;
    int hnr = ATermIntImpl.hashFunction(val);
    int idx = hnr % term_table_size;

    HashedWeakRef prev, cur;
    prev = null;
    cur  = term_table[idx];
    while (cur != null) {
      term = (ATerm)cur.get();
      if (term == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  term_table[idx] = cur.next;
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
    term = new ATermIntImpl(this, val);
    cur = new HashedWeakRef(term, term_table[idx]);
    term_table[idx] = cur;

    return (ATermInt)term;
  }

  //}
  //{ public synchronized ATermReal makeReal(double val)

  public synchronized ATermReal makeReal(double val)
  {
    ATerm term;
    int hnr = ATermRealImpl.hashFunction(val);
    int idx = hnr % term_table_size;

    HashedWeakRef prev, cur;
    prev = null;
    cur  = term_table[idx];
    while (cur != null) {
      term = (ATerm)cur.get();
      if (term == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  term_table[idx] = cur.next;
	} else {
	  prev.next = cur.next;
	}
      } else {
	if (term.getType() == ATerm.REAL) {
	  if (((ATermReal)term).getReal() == val) {
	    return (ATermReal)term;
	  }
	}
      }
      cur = cur.next;
    }
    
    // No real term with 'val' found, so let's create one!
    term = new ATermRealImpl(this, val);
    cur = new HashedWeakRef(term, term_table[idx]);
    term_table[idx] = cur;

    return (ATermReal)term;
  }

  //}
  //{ public synchronized ATermList makeList(ATerm first, ATermList next)

  public synchronized ATermList makeList(ATerm first, ATermList next)
  {
    ATerm term;
    int hnr = ATermListImpl.hashFunction(first, next);
    int idx = hnr % term_table_size;

    HashedWeakRef prev, cur;
    prev = null;
    cur  = term_table[idx];
    while (cur != null) {
      term = (ATerm)cur.get();
      if (term == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  term_table[idx] = cur.next;
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
    term = new ATermListImpl(this, first, next);
    cur = new HashedWeakRef(term, term_table[idx]);
    term_table[idx] = cur;

    return (ATermList)term;
  }

  //}
  //{ public synchronized ATermPlaceholder makePlaceholder(ATerm type)

  public synchronized ATermPlaceholder makePlaceholder(ATerm type)
  {
    ATerm term;
    int hnr = ATermPlaceholderImpl.hashFunction(type);
    int idx = hnr % term_table_size;

    HashedWeakRef prev, cur;
    prev = null;
    cur  = term_table[idx];
    while (cur != null) {
      term = (ATerm)cur.get();
      if (term == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  term_table[idx] = cur.next;
	} else {
	  prev.next = cur.next;
	}
      } else {
	if (term.getType() == ATerm.PLACEHOLDER) {
	  ATermPlaceholder ph = (ATermPlaceholder)term;
	  if (ph.getPlaceholder() == type) {
	    return ph;
	  }
	}
      }
      cur = cur.next;
    }
    
    // No existing term found, so let's create one!
    term = new ATermPlaceholderImpl(this, type);
    cur = new HashedWeakRef(term, term_table[idx]);
    term_table[idx] = cur;

    return (ATermPlaceholder)term;
  }

  //}
  //{ public synchronized ATermBlob makeBlob(byte[] data)

  public synchronized ATermBlob makeBlob(byte[] data)
  {
    ATerm term;
    int hnr = ATermBlobImpl.hashFunction(data);
    int idx = hnr % term_table_size;

    HashedWeakRef prev, cur;
    prev = null;
    cur  = term_table[idx];
    while (cur != null) {
      term = (ATerm)cur.get();
      if (term == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  term_table[idx] = cur.next;
	} else {
	  prev.next = cur.next;
	}
      } else {
	if (term.getType() == ATerm.BLOB) {
	  ATermBlob blob = (ATermBlob)term;
	  if (blob.getBlobData() == data) {
	    return blob;
	  }
	}
      }
      cur = cur.next;
    }
    
    // No existing term found, so let's create one!
    term = new ATermBlobImpl(this, data);
    cur = new HashedWeakRef(term, term_table[idx]);
    term_table[idx] = cur;

    return (ATermBlob)term;
  }

  //}

  //{ public synchronized AFun makeAFun(String name, int arity, boolean isQuoted)

  public synchronized AFun makeAFun(String name, int arity, boolean isQuoted)
  {
    AFun fun;
    int hnr = AFunImpl.hashFunction(name, arity, isQuoted);
    int idx = hnr % afun_table_size;

    name = name.intern();
    HashedWeakRef prev, cur;
    prev = null;
    cur  = afun_table[idx];
    while (cur != null) {
      fun = (AFun)cur.get();
      if (fun == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  afun_table[idx] = cur.next;
	} else {
	  prev.next = cur.next;
	}
      } else {
	// use == because name is interned.
	if (fun.getName() == name && fun.getArity() == arity &&
	    fun.isQuoted() == isQuoted) {
	  return fun;
	}
      }
      cur = cur.next;
    }
    
    // No similar AFun found, so build a new one
    fun = new AFunImpl(name, arity, isQuoted);
    cur = new HashedWeakRef(fun, afun_table[idx]);
    afun_table[idx] = cur;

    return fun;
  }

  //}
  //{ public ATermAppl makeAppl(AFun fun)

  public ATermAppl makeAppl(AFun fun)
  {
    return makeAppl(fun, new ATerm[0]);
  }

  //}
  //{ public synchronized ATermAppl makeAppl(AFun fun, ATerm[] args)

  public synchronized ATermAppl makeAppl(AFun fun, ATerm[] args)
  {
    ATerm term;
    int hnr = ATermApplImpl.hashFunction(fun, args);
    int idx = hnr % term_table_size;

    HashedWeakRef prev, cur;
    prev = null;
    cur  = term_table[idx];
    while (cur != null) {
      term = (ATerm)cur.get();
      if (term == null) {
	// Found a reference to a garbage collected term, remove it to speed up lookups.
	if (prev == null) {
	  term_table[idx] = cur.next;
	} else {
	  prev.next = cur.next;
	}
      } else {
	if (term.getType() == ATerm.APPL) {
	  ATermAppl appl = (ATermAppl)term;
	  if (appl.getAFun() == fun) {
	    ATerm[] appl_args = appl.getArgumentArray();
	    if (appl_args.length == args.length) {
	      boolean found = true;
	      for (int i=0; i<args.length; i++) {
		if(appl_args[i] != args[i]){
		  found = false;
		  break;
		}
	      }
	      if (found) {
		return appl;
	      }
	    }
	  }
	}
      }
      cur = cur.next;
    }
    
    // No existing term found, so let's create one!
    term = new ATermApplImpl(this, fun, args);
    cur = new HashedWeakRef(term, term_table[idx]);
    term_table[idx] = cur;

    return (ATermAppl)term;
  }

  //}

... termReader ...

  //{ private ATerm parseFromReader(Reader reader)

  synchronized private ATerm parseFromReader(Reader reader)
  {    
    ATerm[] list;

    int c = reader.read();
    if(c == -1) {
      throw new ParseError("premature EOF encountered.");
    }

    lookahead = c;
    switch(lookahead) {
      case '[':
	//{ Read a list

	c = reader.read();
	if (c == -1) {
	  throw new ParseError("premature EOF encountered.");
	}
	
	if(c == ']') {
	  c = reader.read();
	  if (c == -1) {
	    throw new ParseError("premature EOF encountered.");
	  }
	  
	  result = ATermListImpl.empty;
	} else {
	  list = parseATermArray(reader);
	  if(lookahead != ']') {
	    throw new ParseError("expected ']' but got '" + lookahead + "'");
	  }
	}

	//}
	break;

      case '<':
	//{ Read a placeholder

	//}
	break;

      case '"':
	//{ Read a quoted function

	//}
	break;

      case '-':
      case '0':	case '1': case '2': case '3': case '4':
      case '5':	case '6': case '7': case '8': case '9':
        result = parseNumber(channel);
	break;

      default:
	if (Character.isLetter(lookahead) || lookahead == '_') {
	  //{ Parse an unquoted function
					 
	  fun = parseId(reader);
	  if (lookahead == '(') {
	    c = reader.read();
	    if (c == -1) {
	      throw new ParseError("premature EOF encountered.");
	    }
	    if (lookahead == ')') {
	      result = makeAppl(makeAFun(fun, 0, false));
	    } else {
	      list = parseATermArray(reader);

	      if(lookahead != ')') {
		throw new ParseError("expected ')' but got '" + lookahead + "'");
	      }
	      result = makeAppl(makeAFun(fun, list.length, false));
	    }
	    c = reader.read();
	    if (c == -1) {
	      throw new ParseError("premature EOF encountered.");
	    }
	  } else {
	    result = makeAppl(makeAFun(fun, 0, false));
	  }
	  
	  //}
	} else {
	  throw new ParseError("illegal character: " + lookahead);
	}
    }
	
    if(lookahead == '{') {
      //{ Parse annotation

      ATermList annos;
      // Parse annotation
      if(channel.readNext() == '}') {
	channel.readNext();
	annos = empty;
      } else {
	annos = parseATermList(channel);
	if(channel.last() != '}')
	  throw new ParseError(channel, channel.last(), "'}' expected");
	channel.readNext();
      }
      result = result.setAnnotations(annos);	

      //}
    }

    /* Parse some ToolBus anomalies for backwards compatibility */
    if(channel.last() == ':') {
      channel.readNext();
      ATerm anno = parseATerm(channel);
      result = result.setAnnotation(ATerm.parse("type"), anno);
    }

    if(channel.last() == '?') {
      channel.readNext();
      result = result.setAnnotation(ATerm.parse("result"),ATerm.parse("true"));
    }


    return result;    
  }

  //}

  //{ public ATerm parse(String trm)

  public ATerm parse(String trm)
  {
    return parseFromReader(new StringReader(trm));
  }

  //}
  //{ public ATerm make(String pattern, List args)

  public ATerm make(String pattern, List args)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}
  //{ public ATerm make(ATerm pattern, List args)

  public ATerm make(ATerm pattern, List args)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}

  //{ public ATerm setAnnotation(ATerm term, ATerm label, ATerm anno)

  public ATerm setAnnotation(ATerm term, ATerm label, ATerm anno)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}
  //{ public ATerm removeAnnotation(ATerm term, ATerm label)

  public ATerm removeAnnotation(ATerm term, ATerm label)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}

  //{ ATerm parsePattern(String pattern)

  ATerm parsePattern(String pattern)
    throws ParseError
  {
    // <TODO>: cache patterns
    return parse(pattern);
  }

  //}

  //{ protected boolean isDeepEqual(ATermImpl t1, ATerm t2)

  protected boolean isDeepEqual(ATermImpl t1, ATerm t2)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}

  //{ public ATerm readFromTextFile(InputStream stream)

  public ATerm readFromTextFile(InputStream stream)
  {
    return parseFromReader(new InputStreamReader(stream));
  }

  //}
  //{ public ATerm readFromBinaryFile(InputStream stream)

  public ATerm readFromBinaryFile(InputStream stream)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}
  //{ public ATerm readFromFile(InputStream stream)

  public ATerm readFromFile(InputStream stream)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}
  //{ public void writeToTextFile(OutputStream stream)

  public void writeToTextFile(OutputStream stream)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}
  //{ public void writeToBinaryFile(OutputStream stream)

  public void writeToBinaryFile(OutputStream stream)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}

  //{ public ATerm importTerm(ATerm term)

  public ATerm importTerm(ATerm term)
  {
    throw new RuntimeException("not yet implemented!");
  }

  //}

}

//{ class HashedWeakRef

class HashedWeakRef extends WeakReference
{
  protected HashedWeakRef next;

  public HashedWeakRef(Object object, HashedWeakRef next)
  {
    super(object);
    this.next = next;
  }
}

//}
