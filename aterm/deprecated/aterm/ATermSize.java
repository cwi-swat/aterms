package aterm;

import java.io.*;

class ATermSize
{
  public static void main(String[] args)
	throws IOException, ParseError
  {
    World world = new World(131071);
    ATerm T = world.readFromTextFile(System.in);
    System.out.println("This term has " + size(T) + " nodes.");
  }

  public static int size(ATerm t)
  {
    return 0;
  }
};
