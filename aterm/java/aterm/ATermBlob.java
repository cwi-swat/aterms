package aterm;

/**
 * This interface describes the functionality of an ATermBlob
 * (Binary Large OBject).
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 * @version 0.1, Fri Jan 28 10:19:58 MET 2000
 */
public interface ATermBlob extends ATerm {

  /**
   * Gets the size (in bytes) of the data in this blob.
   *
   * @return the size of the data in this blob.
   */
  public int getBlobSize();

  /**
   * Gets the data in this blob.
   *
   * @return the data in this blob.
   *
   */
  public byte[] getBlobData();
}
