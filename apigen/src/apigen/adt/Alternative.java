package apigen.adt;

import aterm.*;

public class Alternative
{
  private final static String[] RESERVED_TYPES = 
    { "int", "real", "str", "term" };

  String id;
  ATerm  pattern;

  //{{{ private static boolean containsPlaceholder(ATerm term)

  private static boolean containsPlaceholder(ATerm term)
  {
    switch (term.getType()) {
      case ATerm.PLACEHOLDER:
	return true;

      case ATerm.LIST:
	{
	  ATermList list = (ATermList)term;
	  if (list.isEmpty()) {
	    return false;
	  }
	  return containsPlaceholder(list.getFirst())
	    || containsPlaceholder(list.getNext());
	}

      case ATerm.APPL:
	{
	  ATermAppl appl = (ATermAppl)term;
	  int arity = appl.getArity();
	  for (int i=0; i<arity; i++) {
	    if (containsPlaceholder(appl.getArgument(i))) {
	      return true;
	    }
	  }
	  return false;
	}

      default: 
	return false;
    }
  }

  //}}}

  //{{{ public Alternative(String id, ATerm pattern)

  public Alternative(String id, ATerm pattern)
  {
    this.id = id;
    this.pattern = pattern;
  }

  //}}}
  //{{{ public String getId()

  public String getId()
  {
    return id;
  }

  //}}}
  //{{{ public ATerm getPattern()

  public ATerm getPattern()
  {
    return pattern;
  }

  //}}}
  //{{{ public int getPatternType()

  public int getPatternType()
  {
    ATerm match_pattern = buildMatchPattern();

    if (match_pattern.getType() == ATerm.PLACEHOLDER) {
      ATerm ph = ((ATermPlaceholder)match_pattern).getPlaceholder();
      if (ph.match("int") != null) {
	return ATerm.INT;
      } else if (ph.match("real") != null) {
	return ATerm.REAL;
      } else if (ph.match("str") != null) {
	return ATerm.APPL;
      } else if (ph.match("list") != null) {
	return ATerm.LIST;
      } else if (ph.match("term") != null) {
	throw new RuntimeException("multiple alts with <term> pattern?");
      } else {
	throw new RuntimeException("strange root pattern: " + match_pattern);
      }
    } else {
      return match_pattern.getType();
    }
  }

  //}}}
  //{{{ public boolean containsPlaceholder()

  public boolean containsPlaceholder()
  {
    return containsPlaceholder(pattern);
  }

  //}}}
  //{{{ public ATerm buildMatchPattern()

  public ATerm buildMatchPattern()
  {
    return buildMatchPattern(pattern);
  }

  //}}}
  //{{{ private ATerm buildMatchPattern(ATerm t)

  private ATerm buildMatchPattern(ATerm t)
  {
    switch (t.getType()) {
      case ATerm.APPL:
	{
	  ATermAppl appl = (ATermAppl)t;
	  AFun fun = appl.getAFun();
	  ATerm[] newargs = new ATerm[fun.getArity()];
	  for (int i=0; i<fun.getArity(); i++) {
	    newargs[i] = buildMatchPattern(appl.getArgument(i));
	  }
	  return pattern.getFactory().makeAppl(fun, newargs);
	}
	
      case ATerm.LIST:
	{
	  ATermList list = (ATermList)t;
	  ATerm[] elems = new ATerm[list.getLength()];
	  int i = 0;
	  while (!list.isEmpty()) {
	    elems[i++] = buildMatchPattern(list.getFirst());
	    list = list.getNext();
	  }
	  for (i=elems.length-1; i>=0; i--) {
	    list = list.insert(elems[i]);
	  }
	  return list;
	}

      case ATerm.PLACEHOLDER:
	{
	  ATerm ph = ((ATermPlaceholder)t).getPlaceholder();
	  if (ph.getType() == ATerm.LIST) {
	    return pattern.getFactory().parse("<list>");
	  } else { 
	    ATerm type = ((ATermAppl)ph).getArgument(0);
	    if (isReservedType(type.toString())) {
	      return pattern.getFactory().makePlaceholder(type);
	    } else {
	      return pattern.getFactory().parse("<term>");
	    }
	  }
	}

      default:
	return t;
    }
  }

  //}}}
  //{{{ public boolean isReservedType(ATerm t)

  static public boolean isReservedType(String type)
  {
    for (int i=0; i<RESERVED_TYPES.length; i++) {
      if (RESERVED_TYPES[i].equals(type)) {
	return true;
      }
    }

    return false;
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    return "alt[" + id + ", " + pattern + "]";
  }
  
  //}}}
}
