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
 * An AFun represents a function symbol
 *
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 */
public interface AFun extends ATerm
{
  /**
   * Gets the name of the function symbol
   *
   * @return the name of this function symbol.
   */
  String getName();

  /**
   * Gets the arity of this application. Arity is the number
   * of arguments of a function application.
   *
   * @return the number of arguments that applications of this
   * function symbol have.
   */
  public int getArity();

  /**
   * Checks if this application is quoted. A quoted application looks
   * like this: "foo", whereas an unquoted looks like this: foo.
   *
   * @return true if this application is quoted, false otherwise.
   */
  public boolean isQuoted();
}
