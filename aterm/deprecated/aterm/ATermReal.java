/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/
package aterm;

// Prefer toolbus.util.PrintWriter above java.io.PrintWriter
import aterm.util.PrintWriter;
import aterm.util.*;
import java.io.*;

public class ATermReal extends ATerm
{
  private Double val;

  //{ protected static int hashFunction(double v, ATerm annos)

  /**
    * Calculate the hash value of this real.
    */

  protected static int hashFunction(double v, ATerm annos)
  {
    return (new Double(v)).hashCode();
  }

  //}
  //{ protected ATermReal(World world, double v, ATermList an)

  /**
    * Create a new ATermReal object.
    */

  protected ATermReal(World world, double v, ATermList an) 
  { 
    super(world, an); 
    val = new Double(v);
    hashcode = val.hashCode();
  }

  //}
  //{ protected boolean deepEquality(ATerm peer)

  /**
    * Check deep equality on terms
    */

  protected boolean deepEquality(ATerm peer)
  {
    if(peer.getType() != ATerm.REAL)
      return false;

    return val == ((ATermReal)peer).val && annos.deepEquality(peer.annos);
  }

  //}
  //{ protected ATerm setAnnotations(ATermList annos)

  /**
    * Annotate this term.
    */

  protected ATerm setAnnotations(ATermList annos)
  {
    return world.makeReal(val.doubleValue(), annos);
  }

  //}

  //{ public int getType()

  /**
    * Return the type of this ATermReal (ATerm.REAL).
    */

  public int getType()
  {
    return REAL;
  }

  //}
  //{ public void write(OutputStream o)

  /**
    * Write this term to an OutputStream.
    * @param stream
    * @exception java.io.IOException When something goes wrong during a
                 stream operation.
    */

  public void write(OutputStream stream)
    throws java.io.IOException
  {
		_write(stream);
		super.write(stream);
	}

  protected void _write(OutputStream stream)
    throws java.io.IOException
  {
    String str = val.toString();
    for(int i=0; i<str.length(); i++)
      stream.write(str.charAt(i));
  }

  //}
  //{ public double getReal()

  /**
    * Retrieve the double value stored in this term.
    */

  public double getReal()
  {
    return val.doubleValue();
  }

  //}
}

