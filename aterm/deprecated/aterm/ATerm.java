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
package aterm;

// Prefer toolbus.util.PrintWriter above java.io.PrintWriter
import aterm.util.PrintWriter;
import aterm.util.Writer;
import aterm.util.CharArrayWriter;
import aterm.util.*;
import java.util.*;
import java.io.*;

/**
  * The class ATerm is the base class for all ATerm classes,
  * representing unique objects.
  *
  * @author Pieter Olivier
  * @version 0.4, Mon Jun 22 10:20:35 MET DST 1998
  */

public abstract class ATerm
{
  public static final int INT         = 1;
  public static final int REAL		    = 2;
  public static final int APPL		    = 3;
  public static final int LIST		    = 4;
  public static final int PLACEHOLDER	= 5;
	public static final int BLOB        = 6;
  
  public static World the_world = new World();

  protected World world;
  protected int hashcode;
  protected ATermList annos;

  //{ static public ATerm parse(String string)

  /**
    * Parse an aterm from a string
    * @param string The string to parse
    * @exception ParseError When the string does not represent a valid term
    */

  static public ATerm parse(String string)
    throws ParseError
  {
    return the_world.parse(string);
  }

  //}
  //{ static public ATerm readFromTextFile(InputStream i)

  /**
    * Read a term from an input stream.
    * @param stream The stream to read from
    * @exception IOException When error occurs during a stream operation
    * @exception ParseError When the characters read from the stream do no
                            represent a valid term.
    */

  static public ATerm readFromTextFile(InputStream stream)
    throws IOException, ParseError
  {
    return the_world.readFromTextFile(stream);
  }

  //}
  //{ static public ATerm make(String pattern)

  /**
    * Make a new term given a pattern and zero arguments
    * @param pattern The pattern (without placeholders) from which to create
             a new term.
    * @exception ParseError When pattern does not represent a valid
             term.
  */

  static public ATerm make(String pattern)
    throws ParseError
  {
    return the_world.make(pattern);
  }

  //}
  //{ static public ATerm make(String pattern, Object arg)

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

  static public ATerm make(String pattern, Object arg)
    throws ParseError
  {
    return the_world.make(pattern, arg);
  }

  //}
  //{ static public ATerm make(String pattern, Object arg1, arg2)

  /**
    * Make a new term given a pattern and a two arguments.
    * @param pattern The pattern (without placeholders) from which to create
             a new term. The pattern must contain exactly two placeholders.
    * @param arg1,arg2 See 
             <A HREF=placeholders.html>the use of placeholders</A>
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  static public ATerm make(String pattern, Object arg1, Object arg2)
    throws ParseError
  {
    return the_world.make(pattern, arg1, arg2);
  }

  //}
  //{ static public ATerm make(String pattern, Object arg1, arg2, arg3)

  /**
    * Make a new term given a pattern and three arguments.
    * @param pattern The pattern (without placeholders) from which to create
             a new term. The pattern must contain exactly three placeholders.
    * @param arg1,arg2,arg3 See 
             <A HREF=placeholders.html>the use of placeholders</A>
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  static public ATerm make(String pattern, Object arg1, Object arg2, Object arg3)
    throws ParseError
  {
    return the_world.make(pattern, arg1, arg2, arg3);
  }

  //}
  //{ static public ATerm make(String pattern, Object arg1, arg2, arg3, arg4)

  /**
    * Make a new term given a pattern and three arguments.
    * @param pattern The pattern (without placeholders) from which to create
             a new term. The pattern must contain exactly three placeholders.
    * @param arg1,arg2,arg3,arg4 See 
             <A HREF=placeholders.html>the use of placeholders</A>
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  static public ATerm make(String pattern, Object arg1, Object arg2, 
													 Object arg3, Object arg4)
    throws ParseError
  {
    return the_world.make(pattern, arg1, arg2, arg3, arg4);
  }

  //}
  //{ static public ATerm make(String pattern, Object arg1, arg2, arg3, arg4, arg5)

  /**
    * Make a new term given a pattern and three arguments.
    * @param pattern The pattern (without placeholders) from which to create
             a new term. The pattern must contain exactly five placeholders.
    * @param arg1,arg2,arg3,arg4,arg5 See 
             <A HREF=placeholders.html>the use of placeholders</A>
    * @exception ParseError When pattern does not represent a valid
             term.
    */

  static public ATerm make(String pattern, Object arg1, Object arg2, 
													 Object arg3, Object arg4, Object arg5)
    throws ParseError
  {
    return the_world.make(pattern, arg1, arg2, arg3, arg4, arg5);
  }

  //}
  //{ public ATerm make(String pattern, Enumeration e)

	/**
		* Make using a list of arguments
		*/

	static public ATerm make(String pattern, Enumeration e)
    throws ParseError
	{
		return the_world.make(pattern, e);
	}

	//}

  //{ protected ATerm(World world, ATermList an)

  /**
    * Construct a term given a world and a set of annotations.
    * @param w The world in which the new ATerm is living
    * @param an The annotations of the new ATerm.
    */

  protected ATerm(World w, ATermList an)
  {
    world = w;
    annos = an;
  }

  //}

  //{ abstract public int getType()

  /**
    * Retrieve the type of this term (for instance ATerm.LIST)
    */

  abstract public int getType();

  //}
  //{ public Vector match(String pattern)

  /**
    * Match this term against a String pattern.
    * This method returns A Vector containing the subterms matching 
    * with placeholders in the pattern when the match succeeds, 
    * and null when the match fails.
    * See <A HREF=placeholders.html>the use of placeholders</A>
    * @param pattern The string pattern against which to match
    * @exception ParseError When the pattern does not represent a valid
    *            term.
    */

  public Vector match(String pat)
    throws ParseError
  {
    Vector subterms = new Vector();
    ATerm pattern = world.parsePattern(pat);
    if(pattern.match(this, subterms))
      return subterms;
    return null;
  }

  //}
  //{ public Vector match(ATerm pattern)

  /**
    * Match this term against a term pattern.
    * This method returns A Vector containing the subterms matching 
    * with placeholders in the pattern when the match succeeds, 
    * and null when the match fails.
    * See <A HREF=placeholders.html>the use of placeholders</A>
    * @param pattern The string pattern against which to match
    * @exception ParseError When the pattern does not represent a valid
    *            term.
    */

  public Vector match(ATerm pattern)
    throws ParseError
  {
    Vector subterms = new Vector();
    if(pattern.match(this, subterms))
      return subterms;
    return null;
  }

  //}
  //{ public void writeToTextFile(OutputStream stream)


  /**
    * Write the text representation of a term to an OutputStream.
    * @param stream The OutputStream to use
    * @exception java.io.IOException Something went wrong when writing to 
                 the stream
    */

	public void writeToTextFile(OutputStream stream)
		throws java.io.IOException
	{
		write(stream);
	}

	//}
  //{ public void write(OutputStream o)

  /**
    * Write the text representation of a term to an OutputStream.
    * @param stream The OutputStream to use
    * @exception java.io.IOException Something went wrong when writing to 
                 the stream
    */

	public void write(OutputStream stream)
		throws java.io.IOException
	{
    if(!annos.isEmpty()) {
      stream.write('{');
      annos._write(stream);
      stream.write('}');
    }
	}

  abstract protected void _write(OutputStream stream) 
		throws java.io.IOException;


	//}
  //{ public void print(PrintWriter writer)

	/**
		* Print a term to a stream.
		*/

	public void print(PrintWriter writer)
	{
		try {
			write(writer.getPrintStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//}
  //{ public void println(PrintWriter writer)

	/**
		* Print a term to a stream.
		*/

	public void println(PrintWriter writer)
	{
		try {
			print(writer);
			writer.write('\n');
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//}
  //{ public void print(OutputStream stream)

	/**
		* Print a term to a stream.
		*/

	public void print(OutputStream stream)
	{
		try {
			write(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//}
  //{ public void println(OutputStream stream)

	/**
		* Print a term to a stream.
		*/

	public void println(OutputStream stream)
	{
		try {
			print(stream);
			stream.write('\n');
			stream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//}
  
  //{ abstract protected ATerm setAnnotations(ATermList annos)

  /**
    * Return a new version of this term with other annotations.
    * @param annos The annotations of the new term.
    */

  abstract protected ATerm setAnnotations(ATermList annos);

  //}
  //{ protected ATermList getAnnotations()

  /**
    * Retrieve the annotations of this term .
    */

  protected ATermList getAnnotations()
  {
    return annos;
  }

  //}
  //{ public ATerm getAnnotation(ATerm label)

  /**
    * Retrieve a specific annotation, or null if an annotation with
    * the specified label does not exist.
    * @param label The label of the desired annotation.
    */

  public ATerm getAnnotation(ATerm label)
	{
		return world.dictGet(annos, label);
  }

  //}
  //{ public ATerm setAnnotation(ATerm label, ATerm anno)

  /**
    * Return a new version of this term with a changed annotation.
    * The function symbol of the
    * annotation is used as a label to later retrieve the annotation.
    * @param anno The new annotation.
    */

  public ATerm setAnnotation(ATerm label, ATerm anno)
  {
    return setAnnotations((ATermList)world.dictPut(annos, label, anno));
  }

  //}
  //{ public ATerm removeAnnotation(ATerm label)

  /**
    * Return a new version of this term, that does not longer have
    * an annotation with the specified label.
    * @param label The label of the annotation that is to be removed.
    */

  public ATerm removeAnnotation(ATerm label)
  {
    return setAnnotations((ATermList)world.dictRemove(annos, label));
  }

  //}

  //{ public World getWorld()

  /**
    * Retrieve the world this term lives in.
    */

  public World getWorld()
  {
    return world;
  }

  //}

  //{ public boolean isEqual(ATerm term)

	/**
		* Check the equality of two ATerms
		*/

	public boolean isEqual(ATerm term)
	{
		return equals(term);
	}

	//}
  //{ public boolean equals(Object obj)

  /**
    * Equality test on terms.
    * @param obj The object to test.
    */

  public boolean equals(Object obj)
  { 
    if(obj == this)
      return true;
    if(((ATerm)obj).world == world)
      return false;
    
    /**
      * Now we need deep equality test
      */

    return deepEquality((ATerm)obj);
  }

  //}

  //{ abstract protected boolean deepEquality(ATerm peer)

  /**
    * Check deep equality between terms in different worlds.
    */

  abstract protected boolean deepEquality(ATerm peer);

  //}
  //{ public int hashCode()

  /**
    * Calculate the hashcode of this object.
    */

  public int hashCode()
  {
    return hashcode;
  }

  //}
  //{ public String writeToString()

	/**
		* Write this ATerm to a String.
		*/

	public String writeToString()
	{
		return toString();
	}

	//}
  //{ public String toString()

  /**
    * Return the string representation of this term.
    */

  public String toString()
  {
    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      write(stream);
      return stream.toString();
    } catch (IOException e) {
      return "IOException: " + e.getMessage();
    }
  }

  //}

  //{ protected boolean match(ATerm trm, Vector subterms) 

  /**
    * Match trm against the pattern represented by this.
    */

  protected boolean match(ATerm trm, Vector subterms) 
  { 
    return equals(trm); 
  }

  //}
}


