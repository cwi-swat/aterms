package aterm;

/**
 * This interface describes the functionality of an ATermInt
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 * @version 0.1, Fri Jan 28 10:19:58 MET 2000
 */
public interface ATermInt extends ATerm {

    /**
     * Gets the integer value from this term.
     *
     * @return the integer value from this term.
     *
     */
    public int getInt();
}
