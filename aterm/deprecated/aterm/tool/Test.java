package aterm.tool;

import java.net.*;
import java.io.*;

public class Test
{
  public static void main(String[] args) {
    try {
      TestTool tool = new TestTool(args);
      tool.setVerbose(true);
      tool.run();
    } catch (UnknownHostException e) {
      System.err.println("cannot get hostname!");
    }
  }
}
