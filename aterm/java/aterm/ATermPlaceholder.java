package aterm;

/**
 * This interface describes the functionality of an ATermPlaceholder
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 * @version 0.1, Fri Jan 28 13:16:55 MET 2000
 */
public interface ATermPlaceholder extends ATerm {

    /**
     * Gets the type of this placeholder.
     *
     * @return the type of this placeholder.
     *
     */
    public ATerm getPlaceholder();
}
