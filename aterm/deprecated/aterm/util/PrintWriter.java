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
package aterm.util;

import java.io.*;

public class PrintWriter extends Writer
{
  public PrintWriter(Writer w) { super(w.getPrintStream()); }
  public PrintWriter(OutputStream o) { super(new PrintStream(o)); }
  public void print(int val)   { getPrintStream().print(""+val); }
  public void println(int val)   { getPrintStream().println(""+val); }
  public void print(char val)   { getPrintStream().print(""+val); }
  public void println(char val)   { getPrintStream().println(""+val); }
  public void print(Double val){ getPrintStream().print(""+val); }
  public void println(Double val){ getPrintStream().println(""+val); }
  public void print(String s)  { getPrintStream().print(s); }
  public void println(String s)  { getPrintStream().println(s); }
  public void flush() { try { super.flush(); } catch (IOException e) {} }
}






