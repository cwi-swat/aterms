package aterm.tool;

import aterm.*;

public class ToolException extends Exception
{
  private ATerm term = null;
  private Tool tool;
  private String msg;

  public ToolException(Tool t, String m) { tool = t; msg = m; }
  public ToolException(Tool t, String m, ATerm trm) 
  { 
    tool = t; 
    msg = m;
    term = trm;
  }
  public String toString() {
    if(term == null)
      return tool.getName() + ": " + msg;
    return tool.getName() + ": " + msg + ": \"" + term.toString() + "\"";
  }
}
