package apigen;

import aterm.*;

public class Alternative
{
  String id;
  ATerm  pattern;

  //{{{ public Alternative(String id, ATerm pattern)

  public Alternative(String id, ATerm pattern)
  {
    this.id = id;
    this.pattern = pattern;
  }

  //}}}
  //{{{ public String getId()

  public String getId()
  {
    return id;
  }

  //}}}
  //{{{ public ATerm getPattern()

  public ATerm getPattern()
  {
    return pattern;
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    return "alt[" + id + ", " + pattern + "]";
  }
  
  //}}}
}
