package aterm;

/**
 * This interface describes the functionality of an ATermList
 * 
 * 
 * @author Pieter Olivier (olivierp@cwi.nl)
 * @author Hayco de Jong (jong@cwi.nl)
 * @version 0.1, Fri Jan 28 10:19:58 MET 2000
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
     * @param start the starting position of the lookup.
     *
     * @return the index of the first occurance of el in this list,
     * or -1 if el does not occur.
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
     * @return the list with el appended to it.
     */
    public ATermList append(ATerm el);

    /**
     * Gets the element at a specific index of this list.
     *
     * @param i the index of the required element.
     *
     * @return the ith element of this list.
     *
     * @throws IndexOutOfBoundsException if i does not signify
     * to a position in this list.
     */
    public ATerm elementAt(int i);
}
