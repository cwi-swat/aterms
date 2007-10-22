package apigen.adt;

import java.util.*;

public class Location
  implements Cloneable
{
  String altId;
  List<Step> path;

  //{{{ public Location(String altId)

  public Location(String altId)
  {
    this.altId = altId;

    path = new LinkedList<Step>();
  }

  //}}}

  //{{{ public String getAltId()

  public String getAltId()
  {
    return altId;
  }

  //}}}

  //{{{ public void addStep(Step step)

  public void addStep(Step step)
  {
    path.add(step);
  }

  //}}}
  //{{{ public void makeTail()

  // Turn the last step into a TAIL step

  public void makeTail()
  {
    Step step;

    step = path.get(path.size()-1);
    step.makeTail();
  }

  //}}}
  //{{{ public Iterator stepIterator()

  public Iterator<Step> stepIterator()
  {
    return path.iterator();
  }

  //}}}

  //{{{ public Object clone()

  public Object clone()
  {
    Location copy = new Location(altId);
    copy.path = (List<Step>)((LinkedList<Step>)path).clone();

    return copy;
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    return "loc[" + altId + ", " + path + "]";
  }

  //}}}

}
