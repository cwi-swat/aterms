package aterm.tool;

import aterm.*;
import java.io.*;

public class ToolBusChannel extends ATermChannel
{
  private InputStream istream;
  private int bytesLeft = 0;

  ToolBusChannel(InputStream istream) { this.istream = istream; }
  
  public int read()
       throws IOException
  {
    if(bytesLeft == 0)
      lastChar = -1;
    else {
      bytesLeft--;
      lastChar = istream.read();
    }
    if(lastChar == 0) {
      if(bytesLeft > 0)
        istream.skip(bytesLeft);
      lastChar = -1;
    }

    return lastChar;
  }

  public void reset() 
       throws IOException
  {
    byte[] lspecBuf = new byte[Tool.LENSPEC];
    istream.read(lspecBuf);
    // jdk 1.1
    // String lspec = new String(lspecBuf);
    // jdk 1.02
    String lspec = new String(lspecBuf, 0);

    bytesLeft = Integer.parseInt(lspec.substring(0, Tool.LENSPEC-1));
    if(bytesLeft < Tool.MIN_MSG_SIZE)
      bytesLeft = Tool.MIN_MSG_SIZE;
    bytesLeft -= Tool.LENSPEC;
  }
}

