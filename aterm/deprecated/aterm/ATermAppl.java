package aterm;

import aterm.util.Writer;
import aterm.util.PrintWriter;
import aterm.util.*;
import java.util.*;
import java.io.*;

/**
  * ATermAppl objects represent function application terms.
  *
  */

public class ATermAppl extends ATerm
{
  private String fun;
  private ATermList args;
  private boolean quoted;

  //{ protected static int hashFunction(String fun, ATermList args, iq, ATermList an)

  protected static int hashFunction(String fun, ATermList args, 
				    boolean iq, ATermList annos)
  {
    return fun.hashCode() + args.hashCode();
  }

  //}
  //{ static protected boolean needsQuotes(String fn)

  static protected boolean needsQuotes(String fn)
  {
    if(Character.isLetter(fn.charAt(0)) || fn.charAt(0) == '_') {
      for(int i=1; i<fn.length(); i++) {
				char c = fn.charAt(i);
				if(!Character.isLetterOrDigit(c) && c != '_' && c != '-')
					return true;
      }
      return false;
    }
    return true;
  }

  //}

  //{ protected ATermAppl(World world, String fn, ATermList as, ATerm an, boolean iq)

  /**
    * Create a new function application.
    */

  protected ATermAppl(World world, String fn, ATermList as, ATermList an, boolean iq)
  {
    super(world, an);
    fun = fn.intern();
    args = as;
    quoted = iq;
    hashcode = hashFunction(fn, as, iq, an);
  }

  //}
  //{ protected boolean deepEquality(ATerm peer)

  /**
    * Check deep equality on terms
    */

  protected boolean deepEquality(ATerm peer)
  {
    if(peer.getType() != ATerm.APPL)
      return false;

    ATermAppl appl = (ATermAppl)peer;
    return appl.quoted == quoted && appl.fun.equals(fun) && 
      appl.args.deepEquality(args) && appl.annos.deepEquality(annos);
  }

  //}
  //{ protected ATerm setAnnotations(ATermList annos)

  /**
    * Annotate this term.
    */

  protected ATerm setAnnotations(ATermList annos)
  {
    return world.makeAppl(fun, args, quoted, annos);
  }

  //}
  //{ protected boolean match(ATerm trm, Vector subterms)

  protected boolean match(ATerm trm, Vector subterms)
  {
    if(trm.getType() == ATerm.APPL) {
      ATermAppl ap = (ATermAppl)trm;
      if(ap.getName() == getName())
	 return getArguments().match(ap.getArguments(), subterms);
    }
    return false;
  }

  //}
  //{ private void writeFun(OutputStream o)

  private void writeFun(OutputStream o) 
    throws java.io.IOException
  {
    if(!quoted) {
      for(int i=0; i<fun.length(); i++)
				o.write(fun.charAt(i));
    } else {
      boolean escaped = false;
      o.write('"');
      for(int i=0; i<fun.length(); i++) {
				char c = fun.charAt(i);
				switch(c) {
					case '\n':	o.write('\\');	o.write('n');	break;
					case '\t':	o.write('\\');	o.write('t');	break;
					case '\b':	o.write('\\');	o.write('b');	break;
					case '\r':	o.write('\\');	o.write('r');	break;
					case '\f':	o.write('\\');	o.write('f');	break;
					case '\\':	o.write('\\');	o.write('\\');	break;
					case '\'':	o.write('\\');	o.write('\'');	break;
					case '\"':	o.write('\\');	o.write('\"');	break;
					case '!':	case '@':	case '#':	case '$':
					case '%':	case '^':	case '&':	case '*':
					case '(':	case ')':	case '-':	case '_':
					case '+':	case '=':	case '|':	case '~':
					case '{':	case '}':	case '[':	case ']':
					case ';':	case ':':	case '<':	case '>':
					case ',':	case '.':	case '?': case ' ':
					case '/':
						o.write(c);
						break;
					default:    
						if(Character.isLetterOrDigit(c)) {
							o.write(c);
						} else {
							o.write('\\');
							o.write((char)((int)'0' + (int)c/64));
							c = (char)(c % 64);
							o.write((char)((int)'0' + (int)c/8));
							c = (char)(c % 8);
							o.write((char)((int)'0' + (int)c));
						}
				}
      }
      o.write('"');
    }
  }

  //}
  //{ private void printFun(PrintWriter w)

  /**
    * Print a function symbol.
    */

  private void printFun(PrintWriter o) 
  {
    if(!quoted) {
      for(int i=0; i<fun.length(); i++)
	o.print(fun.charAt(i));
    } else {
      boolean escaped = false;
      o.print('"');
      for(int i=0; i<fun.length(); i++) {
	char c = fun.charAt(i);
	switch(c) {
	  case '\n':	o.print('\\');	o.print('n');	break;
	  case '\t':	o.print('\\');	o.print('t');	break;
	  case '\b':	o.print('\\');	o.print('b');	break;
	  case '\r':	o.print('\\');	o.print('r');	break;
	  case '\f':	o.print('\\');	o.print('f');	break;
	  case '\\':	o.print('\\');	o.print('\\');	break;
	  case '\'':	o.print('\\');	o.print('\'');	break;
	  case '\"':	o.print('\\');	o.print('\"');	break;
	  case '!':	case '@':	case '#':	case '$':
	  case '%':	case '^':	case '&':	case '*':
	  case '(':	case ')':	case '-':	case '_':
	  case '+':	case '=':	case '|':	case '~':
	  case '{':	case '}':	case '[':	case ']':
	  case ';':	case ':':	case '<':	case '>':
	  case ',':	case '.':	case '?':       case ' ':
	    o.print(c);
	    break;
	  default:    
	    if(Character.isLetterOrDigit(c)) {
	      o.print(c);
	    } else {
	      o.print('\\');
	      o.print((char)((int)'0' + (int)c/64));
	      c = (char)(c % 64);
	      o.print((char)((int)'0' + (int)c/8));
	      c = (char)(c % 8);
	      o.print((char)((int)'0' + (int)c));
	    }
	}
      }
      o.print('"');
    }
  }

  //}
  //{ private int printSizeFun()

  private int printSizeFun() 
  {
    int size = 0;
    
    if(!quoted)
      size = fun.length();
    else {
      boolean escaped = false;
      size++;
      for(int i=0; i<fun.length(); i++) {
	char c = fun.charAt(i);
	switch(c) {
	  case '\n':
	  case '\t':
	  case '\b':
	  case '\r':
	  case '\f':
	  case '\\':
	  case '\'':
	  case '\"':	size += 2;
	    break;
	    
	  case '!':	case '@':	case '#':	case '$':
	  case '%':	case '^':	case '&':	case '*':
	  case '(':	case ')':	case '-':	case '_':
	  case '+':	case '=':	case '|':	case '~':
	  case '{':	case '}':	case '[':	case ']':
	  case ';':	case ':':	case '<':	case '>':
	  case ',':	case '.':	case '?':       case ' ':
	    size++;
	    break;
	  default:	
	    if(Character.isLetterOrDigit(c))
	      size++;	// A simple letter or digit
	    else
	      size += 4;	// An octal number
	}
      }
      size++;
    }
    return size;
  }

  //}

  //{ public int getType()

  public int getType()
  {
    return ATerm.APPL;
  }

  //}
  //{ public void write(OutputStream o) 

	public void write(OutputStream o)
		throws java.io.IOException
	{
		_write(o);
    super.write(o);
	}

  protected void _write(OutputStream o) 
    throws java.io.IOException
  {
    writeFun(o);
    if(!args.isEmpty()) {
      o.write('(');
      args._write(o);
      o.write(')');
    }
  }

  //}

  //{ public String getName()

  public String getName() {
    return fun;
  }

  //}
  //{ public ATermList getArguments()

  public ATermList getArguments()
  {
    return args;
  }

  //}
	//{ public ATerm getArgument(int idx)

	/**
		* Retrieve a specific argument
		*/

	public ATerm getArgument(int idx)
	{
		return args.elementAt(idx);
	}

	//}
  //{ public boolean isQuoted()

  public boolean isQuoted()
  {
    return quoted;
  }

  //}
	//{ public int getArity()

	/**
		* What is the arity of this function?
		*/

	public int getArity()
	{
		return args.getLength();
	}

	//}

}


