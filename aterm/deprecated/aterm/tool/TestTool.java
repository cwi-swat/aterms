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
package aterm.tool;

import aterm.*;
import java.net.*;
import java.io.*;

public class TestTool extends Tool
{
  public TestTool() throws UnknownHostException { 
    super("testing");
  }

  public TestTool(String[] args) throws UnknownHostException {
    super(args);
  }

  public void run() {
    try {
      System.out.println("connecting to ToolBus...");
      connect();
      System.out.println("connected!");
      super.run();
    } catch (IOException e) {
      System.err.println("IOException caught: " + e.toString());
    } catch (ToolException e) {
      System.err.println("ToolException caught: " + e.toString());
    }
  }

  protected void idle() {
    System.out.println("idle called");
    super.idle();
  }

  protected ATerm handler(ATerm term) 
	{
    System.out.println("handler called: ");
    term.print(System.out);
    System.out.println("");
    return null;
  }

  protected void checkInputSignature(ATermList sig) {
    System.out.println("checkInputSignature called: ");
    sig.print(System.out);
    System.out.println("");
  }
}

