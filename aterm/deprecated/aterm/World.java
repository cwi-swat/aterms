package aterm;

import java.lang.ref.*;
import java.util.*;
import java.io.*;

public class World
{
  private static int DEFAULT_TABLE_SIZE = 43117;
  private static int DEFAULT_PATTERN_CACHE_SIZE = 4321;
  private static int EMPTY_HASH_NR      = 123;

  private int table_size;
  private HashedWeakRef[] table;
  private int pattern_cache_size;
  private String[] pattern_strings;
  private ATerm[]  pattern_terms;

  public ATermList empty;

  //{ public World()

  /**
    * Construct a world with the default table size
    * and pattern cache size.
    */

  public World()
  {
    this(DEFAULT_TABLE_SIZE, DEFAULT_PATTERN_CACHE_SIZE);
  }

  //}
  //{ public World(int table_size)

  /**
    * Build a new term world with a default pattern cache size.
    * @param table_size The initial size of the term hashtable.
    */

  public World(int table_size)
  {
    this(table_size, DEFAULT_PATTERN_CACHE_SIZE);
  }

  //}
  //{ public World(int table_size, int pattern_cache_size)

  /**
    * Build a new term world
    * @param table_size The initial size of the term hashtable.
    * @param pattern_cache_size The size of the pattern cache
    */

  public World(int table_size, int pattern_cache_size)
  {
    this.table_size = table_size;
    this.pattern_cache_size = pattern_cache_size;
    
    table = new HashedWeakRef[table_size];
    pattern_strings = new String[pattern_cache_size];
    pattern_terms   = new ATerm[pattern_cache_size];

    empty = new ATermList(this, null);
    empty.annos = empty;
  }

  //}

  //{ public ATermAppl makeAppl(String fun)

  /**
    * Create a new ATermAppl with zero arguments.
    * @param fun The function symbol.
    */

  public ATermAppl makeAppl(String fun)
  {
    return makeAppl(fun, empty, ATermAppl.needsQuotes(fun), empty);
  }

  //}
  //{ public ATermAppl makeAppl(String fun, ATermList args)

  /**
    * Create a new ATermAppl.
    * @param fun The function symbol
    * @param args The argument list
    */

  public ATermAppl makeAppl(String fun, ATermList args)
  {
    return makeAppl(fun, args, ATermAppl.needsQuotes(fun), empty);
  }

  //}
  //{ public ATermAppl makeAppl(String fun, ATermList args, boolean iq)

  /**
    * Create a new ATermAppl.
    * @param fun The function symbol
    * @param args The argument list
    * @param isquoted A boolean stating whether or not the function symbol needs quoting
    */

  public ATermAppl makeAppl(String fun, ATermList args, boolean isquoted)
  {
    return makeAppl(fun, args, isquoted, empty);
  }

  //}
  //{ protected ATermAppl makeAppl(String fun, ATermList args, iq, ATermList annos)

  /**
    * Create a new ATermAppl.
    * @param fun The function symbol
    * @param args The argument list
    * @param isquoted A boolean stating whether or not the function symbol needs quoting
    * @param annotations The annotations
    */

  protected ATermAppl makeAppl(String fun, ATermList args, boolean iq, ATermList annos)
  {
    int hnr = ATermAppl.hashFunction(fun, args, iq, annos);
    int idx = Math.abs(hnr) % table_size;
    HashedWeakRef prev = null, cur;
    ATermAppl appl;

    if(args.getWorld() != this)
      args = deepCopy(args);
    if(annos.getWorld() != this)
      annos = deepCopy(annos);
    
    synchronized (table) {
      // Lookup application in the world-global hashtable
      cur = table[idx];
      while(cur != null) {
	ATerm term = (ATerm)cur.get();
	if(term == null) {
	  // Found a ghost reference, remove it to speed up lookups.
	  if(prev == null)
	    table[idx] = cur.next;
	  else
	    prev.next = cur.next;
	} else {
	  if(term.getType() == ATerm.APPL) {
	    appl = (ATermAppl)term;
	    if(appl.getName().equals(fun) && appl.getArguments().equals(args)) {
	      ATermList annos2 = appl.getAnnotations();
	      if(annos2 == annos)
		return appl;
	    }
	  }
	}
	cur = cur.next;
      }
      // Application not found, create a new one
      appl = new ATermAppl(this, fun, args, annos, iq);
      cur = new HashedWeakRef(appl, table[idx]);
      table[idx] = cur;
    }
    return appl;
  }

  //}

  //{ public ATermList getEmpty()

  /**
    * Retrieve the empty list.
    */

  public ATermList getEmpty()
  {
    return empty;
  }

  //}
  //{ public ATermList makeList(ATerm el)

  /**
    * Build a list consisting of one element.
    * @param element The single element of the new list
    */

  public ATermList makeList(ATerm element)
  {
    return makeList(element, empty, empty);
  }

  //}
  //{ public ATermList makeList(ATerm first, ATermList next)

  /**
    * Build a list consisting of a head and a tail.
    * @param first The first element of the new list
    * @param next The tail of the new list
    */

  public ATermList makeList(ATerm first, ATermList next)
  {
    return makeList(first, next, empty);
  }

  //}
  //{ protected ATermList makeList(ATerm first, ATermList next, ATermList annos)

  /**
    * Build a list consisting of a head and a tail and a list of annotations.
    * @param first The first element of the new list
    * @param next The tail of the new list
    * @param annos The annotations of the new list
    */

  protected ATermList makeList(ATerm first, ATermList next, ATermList annos)
  {
    int hnr = ATermList.hashFunction(first, next, annos);
    int idx = Math.abs(hnr) % table_size;
    HashedWeakRef prev = null, cur;
    ATermList list;

    if(first.getWorld() != this)
      first = deepCopy(first);
    if(next.getWorld() != this)
      next = deepCopy(next);
    if(annos.getWorld() != this)
      annos = deepCopy(annos);
    
    synchronized (table) {
      // Lookup list in the world-global hashtable
      cur = table[idx];
      while(cur != null) {
				ATerm term = (ATerm)cur.get();
				if(term == null) {
					// Found a ghost reference, remove it to speed up lookups.
					if(prev == null)
						table[idx] = cur.next;
					else
						prev.next = cur.next;
				} else {
					if(term.getType() == ATerm.LIST) {
						list = (ATermList)term;
						if(list.getFirst() == first && list.getNext() == next &&
							 list.getAnnotations() == annos) {
							return list;
						}
					}
				}
				cur = cur.next;
      }
      // Terms not found, create a new one
      list = new ATermList(this, first, next, annos);
      cur = new HashedWeakRef(list, table[idx]);
      table[idx] = cur;
    }
    return list;
  }

  //}

  //{ public ATermInt makeInt(int i)

  /**
    * Create a new integer term
    * @param i The integer value of the new term
    */

  public ATermInt makeInt(int i)
  {
    return makeInt(i, empty);
  }

  //}
  //{ protected ATermInt makeInt(int i, ATermList annos)

  /**
    * Create a new integer term
    * @param i The integer value of the new term
    * @param annos The annotations of the new term
    */

  protected ATermInt makeInt(int i, ATermList annos)
  {
    int hnr = ATermInt.hashFunction(i, annos);
    int idx = Math.abs(hnr) % table_size;
    HashedWeakRef prev = null, cur;
    ATermInt iterm;
		
    if(annos.getWorld() != this)
      annos = deepCopy(annos);
    
    synchronized (table) {
      // Lookup int in the world-global hashtable
      cur = table[idx];
      while(cur != null) {
				ATerm term = (ATerm)cur.get();
				if(term == null) {
					// Found a ghost reference, remove it to speed up lookups.
					if(prev == null)
						table[idx] = cur.next;
					else
						prev.next = cur.next;
				} else {
					if(term.getType() == ATerm.INT) {
						iterm = (ATermInt)term;
						if(iterm.getInt() == i && iterm.getAnnotations() == annos)
							return iterm;
					}
				}
				cur = cur.next;
      }
      // Term not found, create a new one
      iterm = new ATermInt(this, i, annos);
      cur = new HashedWeakRef(iterm, table[idx]);
      table[idx] = cur;
    }
    return iterm;
  }

  //}

  //{ public ATermReal makeReal(double d)

  /**
    * Create a new real-valued term
    * @param d The real value of the new term
    */

  public ATermReal makeReal(double d)
  {
    return makeReal(d, empty);
  }

  //}
  //{ protected ATermReal makeReal(double d, ATermList annos)

  /**
    * Create a new real-valued term
    * @param d The real value of the new term
    * @param annos The annotations of the new term
    */

  protected ATermReal makeReal(double d, ATermList annos)
  {
    int hnr = ATermReal.hashFunction(d, annos);
    int idx = Math.abs(hnr) % table_size;
    HashedWeakRef prev = null, cur;
    ATermReal rterm;

    if(annos.getWorld() != this)
      annos = deepCopy(annos);
    
    synchronized (table) {
      // Lookup real in the world-global hashtable
      cur = table[idx];
      while(cur != null) {
	ATerm term = (ATerm)cur.get();
	if(term == null) {
	  // Found a ghost reference, remove it to speed up lookups.
	  if(prev == null)
	    table[idx] = cur.next;
	  else
	    prev.next = cur.next;
	} else {
	  if(term.getType() == ATerm.REAL) {
	    rterm = (ATermReal)term;
	    if(rterm.getReal() == d && annos == rterm.getAnnotations())
	      return rterm;
	  }
	}
	cur = cur.next;
      }
      // Real not found, create a new one
      rterm = new ATermReal(this, d, annos);
      cur = new HashedWeakRef(rterm, table[idx]);
      table[idx] = cur;
    }
    return rterm;
  }

  //}

  //{ public ATermPlaceholder makePlaceholder(ATerm type)

  /**
    * Create a new placeholder term
    * @param type The type of the new placeholder term
    */

  public ATermPlaceholder makePlaceholder(ATerm type)
  {
    return makePlaceholder(type, empty);
  }

  //}
  //{ protected ATermPlaceholder makePlaceholder(ATerm type, ATermList annos)

  /**
    * Create a new placeholder term
    * @param type The type of the new placeholder term
    * @param annos The annotations of the new placeholder term
    */

  protected ATermPlaceholder makePlaceholder(ATerm type, ATermList annos)
  {
    int hnr = ATermPlaceholder.hashFunction(type, annos);
    int idx = Math.abs(hnr) % table_size;
    HashedWeakRef prev = null, cur;
    ATermPlaceholder placeholder;

    if(type.getWorld() != this)
      type = deepCopy(type);
    if(annos.getWorld() != this)
      annos = deepCopy(annos);
    
    synchronized (table) {
      // Lookup int in the world-global hashtable
      cur = table[idx];
      while(cur != null) {
				ATerm term = (ATerm)cur.get();
				if(term == null) {
					// Found a ghost reference, remove it to speed up lookups.
					if(prev == null)
						table[idx] = cur.next;
					else
						prev.next = cur.next;
				} else {
					if(term.getType() == ATerm.PLACEHOLDER) {
						placeholder = (ATermPlaceholder)term;
						if(placeholder.getPlaceholderType() == type
							 && annos == placeholder.getAnnotations())
							return placeholder;
					}
				}
				cur = cur.next;
      }
      // Term not found, create a new one
      placeholder = new ATermPlaceholder(this, type, annos);
      cur = new HashedWeakRef(placeholder, table[idx]);
      table[idx] = cur;
    }
    return placeholder;
  }

  //}

	//{ public ATermBlob makeBlob(byte[] data)

	/**
		* Create a BLOB
		*/

	public ATermBlob makeBlob(byte[] data)
	{
		return makeBlob(data, empty);
	}

	//}
	//{ protected ATermBlob makeBlob(byte[] data, ATermList annos)

	/**
		* Create a BLOB
		*/

	protected ATermBlob makeBlob(byte[] data, ATermList annos)
	{
    int hnr = ATermBlob.hashFunction(data, annos);
    int idx = Math.abs(hnr) % table_size;
    HashedWeakRef prev = null, cur;
    ATermBlob blob;

    if(annos.getWorld() != this)
      annos = deepCopy(annos);
    
    synchronized (table) {
      // Lookup int in the world-global hashtable
      cur = table[idx];
      while(cur != null) {
				ATerm term = (ATerm)cur.get();
				if(term == null) {
					// Found a ghost reference, remove it to speed up lookups.
					if(prev == null)
						table[idx] = cur.next;
					else
						prev.next = cur.next;
				} else {
					if(term.getType() == ATerm.BLOB) {
						blob = (ATermBlob)term;
						if(blob.getData() == data
							 && annos == blob.getAnnotations())
							return blob;
					}
				}
				cur = cur.next;
      }
      // Term not found, create a new one
      blob = new ATermBlob(this, data, annos);
      cur = new HashedWeakRef(blob, table[idx]);
      table[idx] = cur;
    }
    return blob;
	}

	//}

  //{ public ATerm make(String pattern)

  /**
    * Make a new term given a pattern and zero arguments
    * @param pattern The pattern (without placeholders) from which to create
             a new term.
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  public ATerm make(String pattern)
    throws ParseError
  {
    return parsePattern(pattern);
  }

  //}
  //{ public ATerm make(String pattern, Object arg)

  /**
    * Make a new term given a pattern and a single argument.
    * @param pattern The pattern (without placeholders) from which to create
             a new term. The pattern must contain exactly one placeholder.
    * @param arg An object that is used to create the subterm that will
             be used in place of the placeholder.
             We are creating a second document that explains
	     <A HREF=placeholders.html>the use of placeholders</A> in 
	     the make and match functions.
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  public ATerm make(String pattern, Object arg)
    throws ParseError
  {
    Vector v = new Vector();
    v.addElement(arg);
    return makeUsingString(pattern, v.elements());
  }

  //}
  //{ public ATerm make(String pattern, Object arg1, arg2)

  /**
    * Make a new term given a pattern and a two arguments.
    * @param pattern The pattern (without placeholders) from which to create
             a new term. The pattern must contain exactly two placeholders.
    * @param arg1,arg2 See 
             <A HREF=placeholders.html>the use of placeholders</A>
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  public ATerm make(String pattern, Object arg1, Object arg2)
    throws ParseError
  {
    Vector v = new Vector(2);
    v.addElement(arg1);
    v.addElement(arg2);
    return makeUsingString(pattern, v.elements());
  }

  //}
  //{ public ATerm make(String pattern, Object arg1, arg2, arg3)

  /**
    * Make a new term given a pattern and three arguments.
    * @param pattern The pattern (without placeholders) from which to create
             a new term. The pattern must contain exactly three placeholders.
    * @param arg1,arg2,arg3 See 
             <A HREF=placeholders.html>the use of placeholders</A>
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  public ATerm make(String pattern, Object arg1, Object arg2, Object arg3)
    throws ParseError
  {
    Vector v = new Vector(3);
    v.addElement(arg1);
    v.addElement(arg2);
    v.addElement(arg3);
    return makeUsingString(pattern, v.elements());
  }

  //}
  //{ public ATerm make(String pattern, Object arg1, arg2, arg3, arg4)

  /**
    * Make a new term given a pattern and three arguments.
    * @param pattern The pattern (without placeholders) from which to create
             a new term. The pattern must contain exactly three placeholders.
    * @param arg1,arg2,arg3,arg4 See 
             <A HREF=placeholders.html>the use of placeholders</A>
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  public ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
										Object arg4)
    throws ParseError
  {
    Vector v = new Vector(4);
    v.addElement(arg1);
    v.addElement(arg2);
    v.addElement(arg3);
    v.addElement(arg4);
    return makeUsingString(pattern, v.elements());
  }

  //}
  //{ public ATerm make(String pattern, Object arg1, arg2, arg3, arg4, arg5)

  /**
    * Make a new term given a pattern and three arguments.
    * @param pattern The pattern (without placeholders) from which to create
             a new term. The pattern must contain exactly three placeholders.
    * @param arg1,arg2,arg3,arg4,arg5 See 
             <A HREF=placeholders.html>the use of placeholders</A>
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  public ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
										Object arg4, Object arg5)
    throws ParseError
  {
    Vector v = new Vector(5);
    v.addElement(arg1);
    v.addElement(arg2);
    v.addElement(arg3);
    v.addElement(arg4);
    v.addElement(arg5);
    return makeUsingString(pattern, v.elements());
  }

  //}
	//{ public ATerm make(String pattern, Enumeration e)

	/**
		* Make using a list of arguments
		*/

	public ATerm make(String pattern, Enumeration e)
    throws ParseError
	{
		return makeUsingString(pattern, e);
	}

	//}
  //{ protected ATerm parsePattern(String pattern)

  /**
    * Retrieve the parsed version of a pattern.
    * @param pattern The pattern to parse
    * @exception ParseError when pattern does not represent a valid term.
    */

  protected ATerm parsePattern(String pattern)
    throws ParseError
  {
    ATerm result;
    int idx = Math.abs(pattern.hashCode()) % pattern_cache_size;
    synchronized (pattern_strings) {
      if(pattern_strings[idx] != null && 
				 pattern_strings[idx].equals(pattern)) {
				return pattern_terms[idx];
      }
      result = read(pattern);
      pattern_strings[idx] = pattern;
      pattern_terms[idx] = result;
    }
    return result;
  }

  //}
  //{ protected ATerm makeUsingString(String pattern, Enumeration args)

  /**
    * Create a new new term by filling the holes in pattern with arguments
    * taken from args.
    */

  protected ATerm makeUsingString(String pattern, Enumeration args)
    throws ParseError
  {
    ATerm pat = parsePattern(pattern);
    return makeUsingTerm(pat, args);
  }

  //}
  //{ protected ATerm makeUsingTerm(pattern, Enumeration args)

  /**
    * Make a term, using subterms given by an Enumeration.
    */

  protected ATerm makeUsingTerm(ATerm pattern, Enumeration e)
  {
    ATermList terms;

    switch(pattern.getType())
      {
				case ATerm.INT:     return (ATermInt)pattern;
				case ATerm.REAL:    return (ATermReal)pattern;
				case ATerm.LIST:    return makeUsingList((ATermList)pattern, e);
					
				case ATerm.APPL:
					String fun = ((ATermAppl)pattern).getName();
					ATermList args = ((ATermAppl)pattern).getArguments();
					return makeAppl(fun, makeUsingList(args, e));
					
				default:	// Must be a placeholder
					return makeUsingPlaceholder(((ATermPlaceholder)pattern).getPlaceholderType(), e);
      }
  }
	
  //}
  //{ protected ATermList makeUsingList(ATermList list, Enumeration e)

  protected ATermList makeUsingList(ATermList list, Enumeration e)
  {
    Vector result = new Vector();
    
    while(!list.isEmpty()) {
      if(list.getFirst().getType() == ATerm.PLACEHOLDER) {
				ATerm type = ((ATermPlaceholder)list.getFirst()).getPlaceholderType();
				if(type.getType() == ATerm.APPL) {
					if(((ATermAppl)type).getName().equals("terms") &&
						 ((ATermAppl)type).getArguments().isEmpty()) {
						ATermList els = (ATermList)e.nextElement();
						while(!els.isEmpty()) {
							result.addElement(els.getFirst());
							els = els.getNext();
						}  
						list = list.getNext();
						continue;
					}
				}
      }
      result.addElement(makeUsingTerm(list.getFirst(), e));
      list = list.getNext();
    }

    ATermList R = empty;
    
    for(int i=result.size()-1; i>=0; i--)
      R = makeList((ATerm)result.elementAt(i), R);
    
    return R;
  }

  //}
  //{ protected ATerm makeUsingPlaceholder(ATerm type, Enumeration e)

  protected ATerm makeUsingPlaceholder(ATerm type, Enumeration e)
  {
    if(type.getType() == ATerm.APPL) {
      ATermAppl appl = (ATermAppl)type;
      String fun = appl.getName();
      ATermList args = appl.getArguments();
      
      if(fun.equals("int") && args.isEmpty()) {
				Integer Int = (Integer)e.nextElement();
				return makeInt(Int.intValue());
      }
      if(fun.equals("real") && args.isEmpty()) {
				Double D = (Double)e.nextElement();
				return makeReal(D.doubleValue());
      }
      if(fun.equals("appl") && args.isEmpty())
				return (ATermAppl)e.nextElement();
      if(fun.equals("term") && args.isEmpty())
				return (ATerm)e.nextElement();
      if(fun.equals("list") && args.isEmpty())
				return (ATermList)e.nextElement();
      if(fun.equals("str") && args.isEmpty())
				return makeAppl((String)e.nextElement(), empty, true);
      if(fun.equals("fun")) {
				if(args == null)
					return makeAppl((String)e.nextElement());
				else
					return makeAppl((String)e.nextElement(), makeUsingList(args, e));
      }
      if(fun.equals("placeholder")) {
				return makePlaceholder((ATerm)e.nextElement());
      }
    }
    throw new IllegalArgumentException("illegal placeholder: " + type.toString());
  }

//}

  //{ private ATerm read(String s)

  /**
    * Create an ATerm by parsing a String.
    * @param s The String to be parsed.
    * @exception ParseError When s does not represent a valid term.
    */

  private ATerm read(String s)
    throws ParseError
  {
    byte[] array = new byte[s.length()]; // jdk 1.02
    s.getBytes(0, s.length(), array, 0);

    // byte[] array = s.getBytes(); // jdk 1.1

    ByteArrayInputStream stream = new ByteArrayInputStream(array);
    try {
      return read(stream);
    } catch (IOException e) {
      throw new ParseError(null, -1, "unexpected end of string");
    }
  }

  //}
  //{ private ATerm read(InputStream i)

  /**
    * Parse an ATerm coming from an InputStream.
    * @param istream The input stream from which to read the term.
    * @exception IOException When a read error is encountered 
    * @exception ParseError When the input read from inputstream
    *            does not constitute a valid term.
    */

  private ATerm read(InputStream istream)
    throws IOException, ParseError
  {
    InputChannel channel = new InputChannel(istream);
    
    channel.reset();
    channel.read();
    return parseATerm(channel);
  }


  //}

	//{ public ATerm parse(String s)

	/**
		* Parse an ATerm
		* @param s The string to parse
		* @exception ParseError When s does not represent a valid term.
		*/

	public ATerm parse(String s)
		throws ParseError
	{
		return read(s);
	}

	//}
	//{ public ATerm readFromString(String s)

	/**
		* Parse an ATerm
		* @param s The string to parse
		* @exception ParseError When s does not represent a valid term.
		*/

	public ATerm readFromString(String s)
		throws ParseError
	{
		return read(s);
	}

	//}
	//{ public ATerm readFromTextFile(InputStream i)

	/**
		* Read a term from a text file
    * @param istream The input stream from which to read the term.
    * @exception IOException When a read error is encountered 
    * @exception ParseError When the input read from inputstream
    *            does not constitute a valid term.
    */

	public ATerm readFromTextFile(InputStream i)
		throws IOException, ParseError
	{
		return read(i);
	}

	//}

  //{ public ATerm importTerm(ATerm term)

  /**
    * Import a term from another term world
    * @param term The term that is to be imported in this world.
    */

  public ATerm importTerm(ATerm term)
  {
    if(term.getWorld() == this)
      return term;
    
    return deepCopy(term);
  }

  //}
  //{ private ATerm deepCopy(ATerm term)

  /**
    * Make a deep copy of a term
    * @param term The term to be copied.
    */

  private ATerm deepCopy(ATerm term)
  {
    switch(term.getType()) {
      case ATerm.APPL:
				ATermAppl appl = (ATermAppl)term;
				return makeAppl(appl.getName(), (ATermList)deepCopy(appl.getArguments()),
												appl.isQuoted(), 
												deepCopy(appl.getAnnotations()));
				
      case ATerm.LIST:
				ATermList list = (ATermList)term;
				return makeList(deepCopy(list.getFirst()),
												deepCopy(list.getNext()),
												deepCopy(list.getAnnotations()));

      case ATerm.INT:
				return makeInt(((ATermInt)term).getInt(), 
											 deepCopy(term.getAnnotations()));
				
      case ATerm.REAL:
				return makeReal(((ATermReal)term).getReal(),
												deepCopy(term.getAnnotations()));
				
      case ATerm.PLACEHOLDER:
				ATermPlaceholder place = (ATermPlaceholder)term;
				return makePlaceholder(deepCopy(place.getPlaceholderType()),
															 deepCopy(place.getAnnotations()));
    }
    throw new RuntimeException("Illegal term type: " + term.getType());
  }

  //}
  //{ private ATermList deepCopy(ATermList list)

  /**
    * Make a deep copy of a term
    */

  private ATermList deepCopy(ATermList list)
  {
    if(list.isEmpty()) {
      // The empty list needs special consideration
      if(list.getAnnotations().isEmpty())
				return empty; // No annotations
      else
				return (ATermList)empty.setAnnotations(deepCopy(list.getAnnotations()));
    }
    return makeList(deepCopy(list.getFirst()), 
										(ATermList)deepCopy(list.getNext()),
										(ATermList)deepCopy(list.getAnnotations()));
  }

  //}

  //{ public ATerm parseATerm(ATermChannel channel)

  /**
    * Use a generic ATermChannel to read a term.
    * ATermChannels can be used to read a term from any source that
    * can provide the characters of a text representation of a
    * term one at a time.
    * @param channel The channel from which to read the term
    * @exception IOException Generated by the channel when something goes
                             wrong.
    * @exception ParseError When the characters read from the channel
                 do not represent a valid term.
    */

  public ATerm parseATerm(ATermChannel channel)
    throws IOException, ParseError
  {
    ATerm result, type;
    ATermList list;
    String fun;

    switch(channel.last()) {
      case '[':	
				//{ Read a list
				
				if(channel.readNext() == ']') {
					channel.readNext();
					result = empty;
				} else {
					result = parseATermList(channel);
					if(channel.last() != ']')
						throw new ParseError(channel, channel.last(), "']' expected");
					channel.readNext();
				}

				//}
				break;
      case '<':
				//{ Read a placeholder

				channel.readNext();
				type = parseATerm(channel);
				if(channel.last() != '>')
					throw new ParseError(channel, channel.last(), "'>' expected");
				result = makePlaceholder(type);
				channel.readNext();
				
				//}
				break;
      case '"':
				//{ Read a quoted function
				
				fun = parseQuotedId(channel);
				if(channel.last() == '(') {
					channel.readNext();
					list = parseATermList(channel);
					if(channel.last() != ')')
						throw new ParseError(channel, channel.last(), "')' expected");
					result = makeAppl(fun, list, true);
					channel.readNext();
				} else
					result = makeAppl(fun, empty, true);

				//}
				break;
      case '-':
      case '0':	case '1':	case '2': 	case '3': 	case '4':
      case '5':	case '6':	case '7':	case '8':	case '9':
				result = parseNumber(channel);
				break;
      default:
				if(Character.isLetter((char)channel.last()) ||
					 //{ Parse an unquoted function
					 
					 channel.last() == '_') {
					fun = parseId(channel);
					if(channel.last() == '(') {
						channel.readNext();
						list = parseATermList(channel);
						if(channel.last() != ')')
							throw new ParseError(channel, channel.last(), "')' expected");
						result = makeAppl(fun, list);
						channel.readNext();
					} else
						result = makeAppl(fun);

					//}
				} else {
					throw new ParseError(channel, channel.last(), "illegal character");
				}
    }

		if(channel.last() == '{') {
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
		}

    return result;
  }

  //}
  //{ private ATermList parseATermList(ATermChannel channel)
	
  private ATermList parseATermList(ATermChannel channel)
    throws IOException, ParseError
  {
    Stack stack = new Stack();
    
    stack.push(parseATerm(channel));
    while(channel.last() == ',') {
      channel.readNext();
      stack.push(parseATerm(channel));
    }
    ATermList result = empty;
    while(!stack.empty())
      result = makeList((ATerm)stack.pop(), result);
    return result;
  }

  //}
  //{ private String parseId(ATermChannel channel)
	
  private String parseId(ATermChannel channel)
    throws IOException
  {
    StringBuffer str = new StringBuffer();
    
    do {
      str.append((char)channel.last());
      channel.read();    
    } while(Character.isLetterOrDigit((char)channel.last()) || 
						channel.last() == '-' || channel.last() == '_');
    channel.skipWhitespace();
    return str.toString();
  }

  //}
  //{ private String parseQuotedId(ATermChannel channel)
  
  private String parseQuotedId(ATermChannel channel)
    throws IOException, ParseError
  {
    boolean escaped;
    StringBuffer str = new StringBuffer();
    
    do {
      escaped = false;
      if(channel.read() == '\\') {
				channel.read();
				escaped = true;
      }

      if(escaped) {
				switch(channel.last()) {
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
						str.append(channel.readOct());
						break;
					default:	str.append('\\').append(channel.last());
				} 
      } else if(channel.last() != '\"')
				str.append(channel.last());
    } while(escaped || channel.last() != '"');
    channel.readNext();
    return str.toString();
  }

  //}
  //{ private ATerm parseNumber(ATermChannel channel)

	/**
		* Parse a number. This function can eiter return an ATermInt,
		* or an ATermReal.
		*/
	
  private ATerm parseNumber(ATermChannel channel)
    throws ParseError, IOException
  {
    StringBuffer str = new StringBuffer();
    ATerm result;

    do {
      str.append(channel.last());
    } while(Character.isDigit((char)channel.read()));
		if(channel.last() == ':') {
			// Must be a blob
			int length = Integer.parseInt(str.toString());
			byte[] data = new byte[length];
			for(int i=0; i<length; i++)
				data[i] = (byte)channel.read();
			result = makeBlob(data, empty);
			channel.read(); // Skip next character
    } else if(channel.last() != '.' && 
       channel.last() != 'e' && channel.last() != 'E') {
      int val;
      try {
				val = Integer.parseInt(str.toString());
      } catch (NumberFormatException e) {
				throw new ParseError(channel, channel.last(), "malformed int");
      }
      result = makeInt(val);
    } else {
      if(channel.last() == '.') {
				str.append('.');
				channel.read();
				if(!Character.isDigit((char)channel.last()))
					throw new ParseError(channel, channel.last(), "digit expected");
				do {
					str.append(channel.last());
				} while(Character.isDigit((char)channel.read()));
      }
      if(channel.last() == 'e' || channel.last() == 'E') {
				str.append(channel.last());
				channel.read();
				if(channel.last() == '-') {
					str.append('-');
					channel.read();
				}
				if(!Character.isDigit((char)channel.last()))
					throw new ParseError(channel, channel.last(), "digit expected!");
				do {
					str.append(channel.last());
				} while(Character.isDigit((char)channel.read()));
      }
      double val;
      try {
				val = Double.valueOf(str.toString()).doubleValue();
      } catch (NumberFormatException e) {
				throw new ParseError(channel, channel.last(), "malformed real");
      }
      result = makeReal(val);    
    }
    return result;
  }

  //}

	//{ public ATerm dictCreate()

	/**
		* Create a new ATerm dictionary
		*/
	
	public ATerm dictCreate()
	{
		return empty;
	}

	//}
	//{ public ATerm dictGet(ATerm dict, ATerm key)

	/**
		* Retrieve an element from an ATerm dictionary
		* @param dict The dictionary
		* @param key The key for which to search in the dictionary
		*/

	public ATerm dictGet(ATerm dict, ATerm key)
	{
		ATermList list = (ATermList)dict;
		while(!list.isEmpty()) {
			ATermList pair = (ATermList)list.getFirst();
			if(pair.getFirst().equals(key))
				return pair.getNext().getFirst();
			list = list.getNext();
		}
		return null;
	}

	//}
	//{ public ATerm dictPut(ATerm dict, ATerm key, ATerm value)

	/**
		* Store a key/value pair in a ATerm dictionary.
		* @param dict The dictionary in which to store the key
		* @param key The key under which to store the value
		* @param value The value to store
		*/

	ATerm dictPut(ATerm dict, ATerm key, ATerm value)
	{
		ATermList list = (ATermList)dict;

		if(list.isEmpty())
			return makeList(makeList(key, makeList(value)));

		ATermList pair = (ATermList)list.getFirst();

		if(pair.getFirst().equals(key)) {
			pair = makeList(key, makeList(value));
			return makeList(pair, list.getNext());
		}

		return makeList(pair, (ATermList)dictPut(list.getNext(), key, value));
	}

	//}
	//{ public ATerm dictRemove(ATerm dict, ATerm key)

	/**
		* Remove an element from a dictionary.
		* @param dict The dictionary from which the key is to be removed.
		* @param key The key of the element that is to be removed.
		*/

	public ATerm dictRemove(ATerm dict, ATerm key)
	{
		ATermList list = (ATermList)dict;

		if(list.isEmpty())
			return list;

		ATermList pair = (ATermList)list.getFirst();

		if(pair.getFirst().equals(key))
			return list.getNext();

		return makeList(pair, (ATermList)dictRemove(list, key));
	}

	//} 
}

//{ class InputChannel

class InputChannel extends ATermChannel
{
  private InputStream Stream;
  
  public InputChannel(java.io.InputStream strm)	{ Stream = strm; }
  public InputStream stream()	{ return Stream; }
  
  public int read()
    throws IOException
  {
    lastChar = (char)Stream.read();
    return lastChar;
  }  
}

//}
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
