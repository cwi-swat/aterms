package aterm;

// Prefer toolbus.util.PrintWriter above java.io.PrintWriter
import aterm.util.PrintWriter;
import aterm.util.*;
import java.util.*;
import java.io.*;

/**
  * ATermPlaceholder objects represent placeholder terms of the form <term>.
  */

public class ATermPlaceholder extends ATerm
{
  private ATerm type;

  //{ protected static int hashFunction(ATerm type, ATermList annos)

  /**
    * Calculate the hashcode of a placeholder.
    **/

  protected static int hashFunction(ATerm type, ATermList annos)
  {
    return type.hashCode() + 123;
  }
    

  //}
  //{ protected ATermPlaceholder(World world, ATerm t, ATermList annos)

  /**
    * Create a new ATermPlaceholder object.
    */

  protected ATermPlaceholder(World world, ATerm t, ATermList annos)
  {
    super(world, annos);
    type = t;
    hashcode = hashFunction(t, annos);
  }

  //}
  //{ protected boolean deepEquality(ATerm peer)

  /**
    * Check deep equality on terms
    */

  protected boolean deepEquality(ATerm peer)
  {
    if(peer.getType() != ATerm.PLACEHOLDER)
      return false;

    ATermPlaceholder place = (ATermPlaceholder)peer;
    return type.deepEquality(place.type) && annos.deepEquality(place.annos);
  }

  //}
  //{ protected ATerm setAnnotations(ATermList annos)

  /**
    * Annotate this term.
    * @param annos The new set of annotations.
    */

  protected ATerm setAnnotations(ATermList annos)
  {
    return world.makePlaceholder(type, annos);
  }

  //}
  //{ protected boolean match(ATerm trm, Vector subterms)

  protected boolean match(ATerm term, Vector subterms)
  {
    ATermList termargs;
    String termfun;

    if(type.getType() != APPL)
      return false;
  
    String fun = ((ATermAppl)type).getName();
    ATermList args = ((ATermAppl)type).getArguments();
    if(fun.equals("term")) {
			subterms.addElement(term);
      return true;
    }
    switch(term.getType()) {
      //{ case INT
			
      case INT:
				if(fun.equals("int")) {
					subterms.addElement(new Integer(((ATermInt)term).getInt()));
					return true;
				}
				break;
				
			//}
			//{ case REAL
				
      case REAL:
				if(fun.equals("real")) {
					subterms.addElement(new Double(((ATermReal)term).getReal()));
					return true;
				}
				break;
				
			//}
			//{ case LIST
				
      case LIST:
				if(fun.equals("list")) {
					subterms.addElement((ATermList)term);
					return true;
				}
				break;

			//}
      //{ case PLACEHOLDER

      case PLACEHOLDER:
				if(fun.equals("placeholder")) {
					subterms.addElement((ATermPlaceholder)term);
					return true;
				}
				break;
				
			//}
      //{ case APPL

      case APPL:
				termargs = ((ATermAppl)term).getArguments();
				termfun  = ((ATermAppl)term).getName();
				if(fun.equals("appl")) {
					subterms.addElement((ATermAppl)term);
					return true;
				}
	
				if(fun.equals("str")) {
					if(!((ATermAppl)term).isQuoted())
						return false;
				}
	
				if(fun.equals("fun") || fun.equals("str")) {
					if(!args.isEmpty()) {
						if(termargs.isEmpty())
							return false;
						subterms.addElement(termfun);
						return args.match(termargs, subterms);
					}
					if(!termargs.isEmpty())
						return false;
					subterms.addElement(termfun);
					return true;
				}
	
				if(fun.equals("bool")) {
					if(termargs.isEmpty()) {
						if(termfun.equals("true")) {
							subterms.addElement(new Boolean(true));
							return true;
						} else if(termfun.equals("false")) {
							subterms.addElement(new Boolean(false));
							return true;
						}
					}
					return false;
				}

				// Another application, make sure it matches with the
				// function symbol we are looking for.
				if(fun.equals(termfun)) {
					subterms.addElement((ATermAppl)term);
					if(args.isEmpty())
						return true;
					return args.match(termargs, subterms);
				}
				break;
				
				//}
		}
		return false;
	}
		
	//}

  //{ public int getType()

  /**
    * Return the type of this placeholder term (ATerm.PLACEHOLDER).
    */

  public int getType()
  { 
    return ATerm.PLACEHOLDER;
  }

  //}
  //{ public void write(OutputStream stream)

  /**
    * Write this term to an OutputStream.
    * @param stream
    * @exception java.io.IOException When something goes wrong during a
                 stream operation.
    */

  public void write(OutputStream stream)
    throws IOException
  {
		_write(stream);
		super.write(stream);
	}

  protected void _write(OutputStream stream)
    throws IOException
  {
    stream.write('<');
    type.write(stream);
    stream.write('>');
  }

  //}
  //{ public ATerm getPlaceholderType()

  /**
    * Return the placeholder type stored in this placeholder.
    */

  public ATerm getPlaceholderType()
  {
    return type;
  }

  //}
}
