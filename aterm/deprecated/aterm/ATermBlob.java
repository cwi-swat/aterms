package aterm;

// Prefer toolbus.util.PrintWriter above java.io.PrintWriter
import aterm.util.PrintWriter;
import aterm.util.*;
import java.io.*;

public class ATermBlob extends ATerm
{
	private byte[] data;

	//{ protected static int hashFunction(byte[] data, ATerm annos)

	/**
		* Calculate the hash value of this blob.
		*/

	protected static int hashFunction(byte[] data, ATerm annos)
	{
		return data.hashCode();
	}

	//}
	//{ protected ATermBlob(World world, byte[] data, ATermList annos)

	/**
		* Create a new ATermBlob object.
		*/

	protected ATermBlob(World world, byte[] data, ATermList annos)
	{
		super(world, annos);
		this.data = data;
		hashcode = hashFunction(data, annos);
	}

	//}
  //{ protected boolean deepEquality(ATerm peer)

  /**
    * Check deep equality on terms
    */

  protected boolean deepEquality(ATerm peer)
  {
    if(peer.getType() != BLOB)
      return false;

    return data == ((ATermBlob)peer).data && annos.deepEquality(peer.annos);
  }

  //}
  //{ protected ATerm setAnnotations(ATermList annos)

  /**
    * Annotate this term.
    */

  protected ATerm setAnnotations(ATermList annos)
  {
    return world.makeBlob(data, annos);
  }

  //}

  //{ public int getType()

  /**
    * Return the type of this ATermReal (ATerm.REAL).
    */

  public int getType()
  {
    return BLOB;
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
    String str = "00000000" + data.length + ":";
		str = str.substring(str.length()-9);
    for(int i=0; i<str.length(); i++)
      stream.write(str.charAt(i));
		for(int i=0; i<data.length; i++)
			stream.write(data[i]);
  }

  //}
  //{ public byte[] getData()

  /**
    * Retrieve the data
    */

  public byte[] getData()
  {
    return data;
  }

  //}
}
