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
 * This interface describes the functionality of an ATermList
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 */
public interface ATermList extends ATerm {

    /**
     * Checks if this list is the empty list.
     *
     * @return true if this list is empty, false otherwise.
     */
    public boolean isEmpty();

    /**
     * Gets the length (number of elements) of this list.
     *
     * @return the length of this list.
     */
    public int getLength();

    /**
     * Gets the first element of this list.
     *
     * @return the first element of this list.
     */
    public ATerm getFirst();

    /**
     * Gets the last element of this list.
     *
     * @return the last element of this list.
     */
    public ATerm getLast();

	/**
	 * Gets the empty list associated to this list.
	 *
	 * @return the empty list.
	 */
	public ATermList getEmpty();

    /**
     * Gets the tail (all but the first element) of this list.
     *
     * @return the tail of this list.
     */
    public ATermList getNext();

    /**
     * Gets the index of the first occurance of a term in this list.
     * Lookup starts at a given index (0 being the first element).
     *
     * @param el the element to look for.
     * @param start the starting position of the lookup. Negative start
     *        implies searching backwards from the tail of the list.
     *
     * @return the index of the first occurance of el in this list,
     * or -1 if el does not occur.
     *
     * @throws IllegalArgumentException when start &gt; length of list ||
     *         start &lt; -length
     *
     * @see #lastIndexOf
     */
    public int indexOf(ATerm el, int start);

    /**
     * Gets the last occurance of a term in this list.
     * Lookup starts at a given index (0 being the first element).
     *
     * @param el the element to look for.
     * @param start the starting position of the lookup.
     *
     * @return the index of the last occurance of el in this list,
     * or -1 if el does not occur.
     *
     * @see #indexOf
     */
    public int lastIndexOf(ATerm el, int start);

    /**
     * Concatenates a list to this list.
     *
     * @param rhs the list to concatenate to this list.
     *
     * @return the concatenation of this list and rhs
     */
    public ATermList concat(ATermList rhs);

    /**
     * Appends an element to this list.
     *
     * @param el the element to append to this list.
     *
     * @return a list with el appended to it.
     */
    public ATermList append(ATerm el);

    /**
     * Gets the element at a specific index of this list.
     *
     * @param i the index of the required element.
     *
     * @return the ith element of this list.
     *
     * @throws IndexOutOfBoundsException if i does not refer
     * to a position in this list.
     */
    public ATerm elementAt(int i);

    /**
     * Removes one occurance of an element from this list.
     *
     * @param el the element to be removed.
     *
     * @return this list with one occurance of el removed.
     */
    public ATermList remove(ATerm el);

    /**
     * Removes the element at a specific index in this list.
     *
     * @param i the index of the element to be removed.
     *
     * @return a list with the ith element removed.
     *
     * @throws IndexOutOfBoundsException if i does not refer
     * to a position in this list.
     */
    public ATermList removeElementAt(int i);

    /**
     * Removes all occurances of an element in this list.
     *
     * @param el the element to be removed.
     *
     * @return this list with all occurances of el removed.
     */
    public ATermList removeAll(ATerm el);

    /**
     * Inserts a term in front of this list.
     *
     * @param el the element to be inserted.
     *
     * @return a list with el inserted.
     */
    public ATermList insert(ATerm el);

    /**
     * Inserts an element at a specific position in this list.
     *
     * @param el the element to be inserted.
     * @param i the index at which to insert.
     *
     * @return a list with el inserted as ith element.
     */
    public ATermList insertAt(ATerm el, int i);

    /**
     * Gets the prefix (all but the last element) of this list.
     *
     * @return the prefix of this list.
     */
    public ATermList getPrefix();

    /**
     * Gets a portion (slice) of this list.
     *
     * @param start the start of the slice (included).
     * @param end the end of the slice (excluded).
     *
     * @return the portion of this list between start and end.
     */
    public ATermList getSlice(int start, int end);

    /**
     * Replaces a specific term in this list with another.
     *
     * @param el the element to be put into this list.
     * @param i the index of the element in this list to be replaced.
     *
     * @return this list with the ith element replaced by el.
     *
     * @throws IndexOutOfBoundsException if i does not refer
     * to a position in this list.
     */
    public ATermList replace(ATerm el, int i);

    /**
     * Reverses the elements of this list.
     *
     * @return a reverse order copy of this list.
     */
    public ATermList reverse();

    /**
      * Retrieves an element from a dictionary list.
      * A dictionary list is a list of [key,value] pairs.
      *
      * @param key the key to look for
      *
      * @return the value associated with key, or null when key is not present.
      */
    public ATerm dictGet(ATerm key);

    /**
      * Sets the value for an element in a dictionary list.
      * A dictionary list is a list of [key,value] pairs.
      *
      * @param key the key to set
      * @param value the value to associate with key
      *
      * @return the new dictionary list
      */
    public ATermList dictPut(ATerm key, ATerm value);

    /**
      * Removes an element from a dictionary list.
      * A dictionary list is a list of [key,value] pairs.
      *
      * @param key the key to remove
      *
      * @return the new dictionary list
      */
    public ATermList dictRemove(ATerm key);
}
