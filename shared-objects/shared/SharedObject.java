/* Copyright (c) 2003, CWI, LORIA-INRIA All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation; either version 2, or 
 * (at your option) any later version.
 */

package shared;

public interface SharedObject {

  /**
   * This method should ONLY be used by a SharedObjectFactory
   * 
   * Makes a clone of a prototype. Just like Object.clone(),
   * but it returns a SharedObject instead of an Object.
   * Use this method to duplicate a Prototype object (an object
   * that is allocated only once and gets updated with new information).
   * Using the Prototype design pattern will lead to an efficient 
   * ObjectFactory that minimizes on object allocation time.
   * 
   * @return An exact duplicate of the current object
   */
  SharedObject duplicate();
  
  /**
   * This method should ONLY be used by a SharedObjectFactory
   * 
   * Checks whether an object is really equivalent. The types should be 
   * equal, the fields should be equivalent too. So this is complete
   * recursive equivalence.
   * 
   * @param  The object to compare to
   * @return true if the object is really equivalent, or false otherwise
   */
  boolean equivalent(SharedObject o);
  
  /**
   * This method is typically used by a SharedObjectFactory
   * 
   * Returns the hash code of an object. It is a good idea to 
   * compute this code once, and store it locally in a field to let
   * this hashCode() method return it. Because a SharedObject should be 
   * immutable, the hashCode has to be computed only once. Note that a hashCode()
   * of 0 should also lead to a correct implementation, but it will be very slow.
   * A good uniform hash leads to the fastest implementations. 
   * @return a proper hash code for this object
   */
  int hashCode();
}
