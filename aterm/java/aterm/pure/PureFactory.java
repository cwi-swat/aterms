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


  //{ private ATerm parseNumber(ATermReader reader)

  private ATerm parseNumber(ATermReader reader)
  {
    StringBuffer str = new StringBuffer();
    ATerm result;

    do {
      str.append(reader.getLastChar());
    } while(Character.isDigit((char)reader.read()));

    if(reader.getLastChar() != '.' && 
       reader.getLastChar() != 'e' && reader.getLastChar() != 'E') {
      int val;
      try {
	val = Integer.parseInt(str.toString());
      } catch (NumberFormatException e) {
	throw new ParseError("malformed int");
      }
      result = makeInt(val);
    } else {
      if(reader.getLastChar() == '.') {
	str.append('.');
	reader.read();
	if(!Character.isDigit((char)reader.getLastChar()))
	  throw new ParseError("digit expected");
	do {
	  str.append(reader.getLastChar());
	} while(Character.isDigit((char)reader.read()));
      }
      if(reader.getLastChar() == 'e' || reader.getLastChar() == 'E') {
	str.append(reader.getLastChar());
	reader.read();
	if(reader.getLastChar() == '-' || reader.getLastChar() == '+') {
	  str.append(reader.getLastChar());
	  reader.read();
	}
	if(!Character.isDigit((char)reader.getLastChar()))
	  throw new ParseError("digit expected!");
	do {
	  str.append(reader.getLastChar());
	} while(Character.isDigit((char)reader.read()));
      }
      double val;
      try {
	val = Double.valueOf(str.toString()).doubleValue();
      } catch (NumberFormatException e) {
	throw new ParseError("malformed real");
      }
      result = makeReal(val);    
    }
    return result;
  }

  //}
  //{ private String parseId(ATermReader reader)

  private String parseId(ATermReader reader)
  {
    int c = reader.getLastChar();
    StringBuffer buf = new StringBuffer(32);

    do {
      buf.append((char)c);
      c = reader.read();
    } while (Character.isLetter((char)c) || c == '_');

    return buf.toString();
  }

  //}
  //{ private String parseString(ATermReader reader)

  private String parseString(ATermReader reader)
  {
    boolean escaped;
    StringBuffer str = new StringBuffer();
    
    do {
      escaped = false;
      if(reader.read() == '\\') {
        reader.read();
	escaped = true;
      }

      if(escaped) {
	switch(reader.getLastChar()) {
	case 'n':	str.append('\n');	break;
	case 't':	str.append('\t');	break;
	case 'b':	str.append('\b');	break;
	case 'r':	str.append('\r');	break;
	case 'f':	str.append('\f');	break;
	case '\\':	str.append('\\');	break;
	case '\'':	str.append('\'');	break;
	case '\"':	str.append('\"');	break;
	case '0':	case '1':	case '2':	case '3':
	case '4':	case '5':	case '6':	case '7':
	  str.append(reader.readOct());
	  break;
	default:	str.append('\\').append(reader.getLastChar());
	} 
      } else if(reader.getLastChar() != '\"')
	str.append(reader.getLastChar());
    } while(escaped || reader.getLastChar() != '"');

    return str.toString();
  }

  //}
  //{ private ATermList parseATermList(ATermReader reader)

  private ATermList parseATermList(ATermReader reader)
  {
    ATerm[] terms = parseATermsArray(reader);
    ATermList result = ATermListImpl.empty;
    for (int i=terms.length-1; i>=0; i--) {
      result = makeList(terms[i], result);
    }

    return result;
  }

  //}
  //{ private ATerm[] parseATermsArray(ATermReader reader)

  private ATerm[] parseATermsArray(ATermReader reader)
  {
    List list = new Vector();
    ATerm term;

    do {
      term = parseFromReader(reader);
      list.add(list);
    } while (reader.getLastChar() == ',');

    ATerm[] array = new ATerm[list.size()];
    ListIterator iter = list.listIterator();
    int index = 0;
    while (iter.hasNext()) {
      array[index++] = (ATerm)iter.next();
    }
    return array;
  }

  //}
  //{ private ATerm parseFromReader(ATermReader reader)

  synchronized private ATerm parseFromReader(ATermReader reader)
  {    
    ATerm[] list;
    ATerm result;
    int c;
    String funname;

    switch(reader.readSkippingWS()) {
      case -1:
	throw new ParseError("permature EOF encountered.");

      case '[':
	//{ Read a list

	c = reader.readSkippingWS();
	if (c == -1) {
	  throw new ParseError("premature EOF encountered.");
	}
	
	if(c == ']') {
	  c = reader.readSkippingWS();
	  if (c == -1) {
	    throw new ParseError("premature EOF encountered.");
	  }
	  
	  result = (ATerm)ATermListImpl.empty;
	} else {
	  list = parseATermsArray(reader);
	  if(reader.getLastChar() != ']') {
	    throw new ParseError("expected ']' but got '" + (char)reader.getLastChar() + "'");
	  }
	}

	//}
	break;

      case '<':
	//{ Read a placeholder

	c = reader.readSkippingWS();
	ATerm ph = parseFromReader(reader);
	
	if (reader.getLastChar() != '>') {
	  throw new ParseError("expected '>' but got '" + (char)reader.getLastChar() + "'");
	}

	c = reader.readSkippingWS();

	result = makePlaceholder(ph);

	//}
	break;

      case '"':
	//{ Read a quoted function

	funname = parseString(reader);
	
	c = reader.readSkippingWS();
	if (reader.getLastChar() == '(') {
	  c = reader.readSkippingWS();
	  if (c == -1) {
	    throw new ParseError("premature EOF encountered.");
	  }
	  if (reader.getLastChar() == ')') {
	    result = makeAppl(makeAFun(funname, 0, true));
	  } else {
	    list = parseATermsArray(reader);

	    if(reader.getLastChar() != ')') {
	      throw new ParseError("expected ')' but got '" + reader.getLastChar() + "'");
	    }
	    result = makeAppl(makeAFun(funname, list.length, true));
	  }
	  c = reader.readSkippingWS();
	  if (c == -1) {
	    throw new ParseError("premature EOF encountered.");
	  }
	} else {
	  result = makeAppl(makeAFun(funname, 0, true));
	}


	//}
	break;

      case '-':
      case '0':	case '1': case '2': case '3': case '4':
      case '5':	case '6': case '7': case '8': case '9':
        result = parseNumber(reader);
	break;

      default:
	c = reader.getLastChar();
	if (Character.isLetter((char)c)) {
	  //{ Parse an unquoted function
					 
	  funname = parseId(reader);
	  if (reader.getLastChar() == '(') {
	    c = reader.readSkippingWS();
	    if (c == -1) {
	      throw new ParseError("premature EOF encountered.");
	    }
	    if (reader.getLastChar() == ')') {
	      result = makeAppl(makeAFun(funname, 0, false));
	    } else {
	      list = parseATermsArray(reader);

	      if(reader.getLastChar() != ')') {
		throw new ParseError("expected ')' but got '" + reader.getLastChar() + "'");
	      }
	      result = makeAppl(makeAFun(funname, list.length, false));
	    }
	    c = reader.readSkippingWS();
	    if (c == -1) {
	      throw new ParseError("premature EOF encountered.");
	    }
	  } else {
	    result = makeAppl(makeAFun(funname, 0, false));
	  }
	  
	  //}
	} else {
	  throw new ParseError("illegal character: " + reader.getLastChar());
	}
    }
	
    if(reader.getLastChar() == '{') {
      //{ Parse annotation

      ATermList annos;
      // Parse annotation
      if(reader.readSkippingWS() == '}') {
	reader.readSkippingWS();
	annos = ATermListImpl.empty;
      } else {
	annos = parseATermList(reader);
	if(reader.getLastChar() != '}') {
	  throw new ParseError("'}' expected");
	}
	reader.readSkippingWS();
      }
      result = result.setAnnotations(annos);	

      //}
    }

    /* Parse some ToolBus anomalies for backwards compatibility */
    if(reader.getLastChar() == ':') {
      reader.read();
      ATerm anno = parseFromReader(reader);
      result = result.setAnnotation(ATerm.parse("type"), anno);
    }

    if(reader.getLastChar() == '?') {
      reader.readSkippingWS();
      result = result.setAnnotation(ATerm.parse("result"),ATerm.parse("true"));
    }


    return result;    
  }

  //}

  //{ public ATerm parse(String trm)

  public ATerm parse(String trm)
  {
    return parseFromReader(new ATermReader(new StringReader(trm)));
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
    return parseFromReader(new ATermReader(new InputStreamReader(stream)));
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
//{ class ATermReader

class ATermReader
{
  private Reader reader;
  private int last_char;

  public ATermReader(Reader reader)
  {
    this.reader = reader;
    last_char = -1;
  }

  public int read()
  {
    last_char = reader.read();
    return last_char;
  }

  public int readSkippingWS()
  {
    do {
      last_char = reader.read();
    } while (Character.isWhitespace(last_char));

    return last_char;

  }

  public int readOct()
  {
    int val = Character.digit((char)lastChar, 8);
    val += Character.digit((char)read(), 8);

    if(val < 0) {
      throw new ParseError("octal must have 3 octdigits.");
    }

    val += Character.digit((char)read(), 8);

    if(val < 0) {
      throw new ParseError("octal must have 3 octdigits");
    }

    return val;
  }
  
  public int getLastChar()
  {
    return last_char;
  }
}

//}
