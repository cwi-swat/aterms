package aterm;

/**
 * This interface describes the functionality of an ATermPlaceholder
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
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
