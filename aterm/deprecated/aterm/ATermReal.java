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

