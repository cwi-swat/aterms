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

/**
 * A ParseError is thrown when an error occurs during the
 * parsing of a term.
 * Note that ParseError is a RuntimeException, so it can
 * be ignored if a parse error can only occur by a bug in
 * your program.
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 */
public class ParseError extends RuntimeException {
	static final long serialVersionUID = 3638731523325353017L;

  /**
   * Constructs a ParseError given a description of the error
   *
   * @param msg the error message describing the parse error.
   */
  public ParseError(String msg) {
    super(msg);
  }

}
