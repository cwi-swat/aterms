package aterm;

/**
 * This interface describes the functionality of an ATermReal
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 * @version 0.1, Fri Jan 28 10:19:58 MET 2000
 */
public interface ATermReal extends ATerm {

    /**
     * Gets the real value from this term as a double.
     *
     * @return the real value from this term.
     *
     */
    public double getReal();
}
