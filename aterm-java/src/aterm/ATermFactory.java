/*
 * Java version of the ATerm library
 * Copyright (C) 2002, CWI, LORIA-INRIA
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

package aterm;

import java.io.*;
import java.util.List;

/**
 * An ATermFactory is responsible for making new ATerms.
 * A factory can create a new ATerm by parsing a String, by making
 * it via one of the many "make" methods, or by reading it from an
 * InputStream.
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 */
public interface ATermFactory
{
  static byte START_OF_SHARED_TEXT_FILE = (byte)'!';

  /**
   * Creates a new ATerm by parsing a string.
   *
   * @param trm the string representation of the term
   *
   * @return the parsed term.
   *
   * @see #make(String)
   */
  ATerm parse(String trm);

  /**
   * Equivalent of parse.
   *
   * @param trm the string representation of the term
   *
   * @return the parsed term.
   *
   * @see #parse(String)
   */
  ATerm make(String trm);

  /**
   * Creates a new ATerm given a string pattern and a list of arguments.
   * First the string pattern is parsed into an ATerm.
   * Then the holes in the pattern are filled with arguments taken from
   * the supplied list of arguments.
   *
   * @param pattern the string pattern containing a placeholder for each
   *        argument.
   * @param args the list of arguments to be filled into the placeholders.
   *
   * @return the constructed term.
   */
  ATerm make(String pattern, List args);

  /**
   * Creates a new ATerm given a pattern and a list of arguments.
   * The holes in the pattern are filled with arguments taken from
   * the supplied list of arguments.
   *
   * @param pattern the pattern containing a placeholder for each argument.
   * @param args the list of arguments to be filled into the placeholders.
   *
   * @return the constructed term.
   */
  ATerm make(ATerm pattern, List args);

  /**
   * Creates a new ATerm given a pattern and a single argument.
   * This convenience method creates an ATerm from a pattern and one
   * argument.
   *
   * @param pattern the pattern containing a placeholder for the argument.
   * @param arg1 the argument to be filled into the hole.
   *
   * @return the constructed term.
   */
  ATerm make(String pattern, Object arg1);

  /**
   * Creates a new ATerm given a pattern and a fixed number of arguments.
   * This convenience method creates an ATerm from a pattern and two
   * arguments.
   *
   * @param pattern the pattern containing a placeholder for the arguments.
   * @param arg1 the argument to be filled into the first hole.
   * @param arg2 the argument to be filled into the second hole.
   *
   * @return the constructed term.
   */
  ATerm make(String pattern, Object arg1, Object arg2);

  /**
   * Creates a new ATerm given a pattern and a fixed number of arguments.
   * This convenience method creates an ATerm from a pattern and three
   * arguments.
   *
   * @param pattern the pattern containing a placeholder for the arguments.
   * @param arg1 the argument to be filled into the first hole.
   * @param arg2 the argument to be filled into the second hole.
   * @param arg3 the argument to be filled into the third hole.
   *
   * @return the constructed term.
   */
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3);

  /**
   * Creates a new ATerm given a pattern and a fixed number of arguments.
   * This convenience method creates an ATerm from a pattern and four
   * arguments.
   *
   * @param pattern the pattern containing a placeholder for the arguments.
   * @param arg1 the argument to be filled into the first hole.
   * @param arg2 the argument to be filled into the second hole.
   * @param arg3 the argument to be filled into the third hole.
   * @param arg4 the argument to be filled into the fourth hole.
   *
   * @return the constructed term.
   */
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4);

  /**
   * Creates a new ATerm given a pattern and a fixed number of arguments.
   * This convenience method creates an ATerm from a pattern and five
   * arguments.
   *
   * @param pattern the pattern containing a placeholder for the arguments.
   * @param arg1 the argument to be filled into the first hole.
   * @param arg2 the argument to be filled into the second hole.
   * @param arg3 the argument to be filled into the third hole.
   * @param arg4 the argument to be filled into the fourth hole.
   * @param arg5 the argument to be filled into the fifth hole.
   *
   * @return the constructed term.
   */
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4, Object arg5);

  /**
   * Creates a new ATerm given a pattern and a fixed number of arguments.
   * This convenience method creates an ATerm from a pattern and six
   * arguments.
   *
   * @param pattern the pattern containing a placeholder for the arguments.
   * @param arg1 the argument to be filled into the first hole.
   * @param arg2 the argument to be filled into the second hole.
   * @param arg3 the argument to be filled into the third hole.
   * @param arg4 the argument to be filled into the fourth hole.
   * @param arg5 the argument to be filled into the fifth hole.
   * @param arg6 the argument to be filled into the sixth hole.
   *
   * @return the constructed term.
   */
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4, Object arg5, Object arg6);

  /**
   * Creates a new ATerm given a pattern and a fixed number of arguments.
   * This convenience method creates an ATerm from a pattern and seven
   * arguments.
   *
   * @param pattern the pattern containing a placeholder for the arguments.
   * @param arg1 the argument to be filled into the first hole.
   * @param arg2 the argument to be filled into the second hole.
   * @param arg3 the argument to be filled into the third hole.
   * @param arg4 the argument to be filled into the fourth hole.
   * @param arg5 the argument to be filled into the fifth hole.
   * @param arg6 the argument to be filled into the sixth hole.
   * @param arg7 the argument to be filled into the seventh hole.
   *
   * @return the constructed term.
   */
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4, Object arg5, Object arg6, Object arg7);

  /**
   * Creates a new ATermInt object
   *
   * @param val the integer value to be stored.
   *
   * @return the constructed ATermInt object.
   */
  ATermInt makeInt(int val);

  /**
   * Creates a new ATermReal object
   *
   * @param val the double value to be stored.
   *
   * @return the constructed ATermReal object.
   */
  ATermReal makeReal(double val);

  /**
   * Creates an empty ATermList object
   *
   * @return the (empty) ATermList.
   */
  ATermList makeList();

  
  /**
   * Creates a singleton ATermList object.
   *
   * @param single the element to be placed in the list.
   *
   * @return the singleton ATermList object.
   */
  ATermList makeList(ATerm single);

  /**
   * Creates a head-tail style ATermList.
   *
   * @param head the head of the list.
   * @param tail the tail of the list.
   *
   * @return the constructed ATermList.
   */
  ATermList makeList(ATerm head, ATermList tail);

  /**
   * Creates an ATermPlaceholder object.
   *
   * @param type the type of the hole in the placeholder.
   *
   * @return the constructed ATermPlaceholder.
   */
  ATermPlaceholder makePlaceholder(ATerm type);

  /**
   * Creates an ATermBlob (Binary Large OBject).
   *
   * @param data the data to be stored in the blob.
   *
   * @return the constructed ATermBlob.
   */
  ATermBlob makeBlob(byte[] data);

  /**
   * Creates an AFun object
   *
   * @param name the name of the function symbol.
   * @param arity the arity of the function symbol.
   * @param isQuoted whether the function symbol is quoted ("foo") or not (foo).
   *
   * @return the constructed AFun.
   */
  AFun makeAFun(String name, int arity, boolean isQuoted);

  /**
   * Creates a function application.
   *
   * @param fun the function symbol of the application.
   *
   * @return the constructed function application.
   */
  ATermAppl makeAppl(AFun fun);

  /**
   * Creates a function application.
   *
   * @param fun the function symbol of the application.
   * @param arg the argument of the application.
   *
   * @return the constructed function application.
   */
  ATermAppl makeAppl(AFun fun, ATerm arg);

  /**
   * Creates a function application.
   *
   * @param fun the function symbol of the application.
   * @param arg1 the first argument of the application.
   * @param arg2 the second argument of the application.
   *
   * @return the constructed function application.
   */
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2);

  /**
   * Creates a function application.
   *
   * @param fun the function symbol of the application.
   * @param arg1 the first argument of the application.
   * @param arg2 the second argument of the application.
   * @param arg3 the third argument of the application.
   *
   * @return the constructed function application.
   */
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3);

  /**
   * Creates a function application.
   *
   * @param fun the function symbol of the application.
   * @param arg1 the first argument of the application.
   * @param arg2 the second argument of the application.
   * @param arg3 the third argument of the application.
   * @param arg4 the fourth argument of the application.
   *
   * @return the constructed function application.
   */
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4);

  /**
   * Creates a function application.
   *
   * @param fun the function symbol of the application.
   * @param arg1 the first argument of the application.
   * @param arg2 the second argument of the application.
   * @param arg3 the third argument of the application.
   * @param arg4 the fourth argument of the application.
   * @param arg5 the fifth argument of the application.
   *
   * @return the constructed function application.
   */
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4,
		     ATerm arg5);

  /**
   * Creates a function application.
   *
   * @param fun the function symbol of the application.
   * @param arg1 the first argument of the application.
   * @param arg2 the second argument of the application.
   * @param arg3 the third argument of the application.
   * @param arg4 the fourth argument of the application.
   * @param arg5 the fifth argument of the application.
   * @param arg6 the sixth argument of the application.
   *
   * @return the constructed function application.
   */
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4,
		     ATerm arg5, ATerm arg6);

  /**
   * Creates a function application.
   *
   * @param fun the function symbol of the application.
   * @param args an array containing the arguments.
   *
   * @return the constructed function application.
   */
  ATermAppl makeAppl(AFun fun, ATerm[] args);

  /**
   * Creates a function application.
   *
   * @param fun the function symbol of the application.
   * @param args an ATermList containing the arguments.
   *
   * @return the constructed function application.
   */
  ATermAppl makeApplList(AFun fun, ATermList args);

  /**
   * Creates an ATerm from a text stream.
   *
   * @param stream the inputstream to read the ATerm from.
   *
   * @return the parsed ATerm.
   * @throws IOException
   */
  ATerm readFromTextFile(InputStream stream) throws IOException;

  /**
   * Creates an ATerm from a shared text stream.
   *
   * @param stream the inputstream to read the ATerm from.
   *
   * @return the parsed ATerm.
   * @throws IOException
   */
  ATerm readFromSharedTextFile(InputStream stream) throws IOException;

  /**
   * Creates an ATerm from a binary stream.
   *
   * @param stream the inputstream to read the ATerm from.
   *
   * @return the parsed ATerm.
   * @throws IOException
   */
  ATerm readFromBinaryFile(InputStream stream) throws IOException;

  /**
   * Creates an ATerm from a stream.
   * This function determines the type of stream (text, shared, binary)
   * and parses the ATerm accordingly.
   *
   * @param stream the inputstream to read the ATerm from.
   *
   * @return the parsed ATerm.
   * @throws IOException
   */
  ATerm readFromFile(InputStream stream) throws IOException;

  /**
   * Creates an ATerm from a given filename.
   *
   * @param file the filename to read the ATerm from.
   *
   * @return the parsed ATerm.
   * @throws IOException
   */
  ATerm readFromFile(String file) throws IOException;

  /**
   * Creates an ATerm by importing it from another ATermFactory.
   *
   * @param term the term (possibly from another ATermFactory) to rebuild in
   * this factory.
   *
   * @return the imported ATerm.
   */
  ATerm importTerm(ATerm term);
}
