package apigen.adt;

import java.util.*;

public class Field
{
  String id;
  String type;

  List locations;

  //{{{ public Field(String id, String type)

  public Field(String id, String type)
  {
    this.id     = id;
    this.type   = type;

    locations = new Vector();
  }

  //}}}

  //{{{ public String getId()

  public String getId()
  {
    return id;
  }

  //}}}
  //{{{ public String getType()

  public String getType()
  {
    return type;
  }

  //}}}

  //{{{ public void addLocation(Location loc)

  public void addLocation(Location loc)
  {
    locations.add(loc);
  }

  //}}}

  //{{{ public boolean hasAltId(String altId)

  public boolean hasAltId(String altId)
  {
    return getLocation(altId) != null;
  }

  //}}}
  //{{{ public Location getLocation(String altId)

  public Location getLocation(String altId)
  {
    Iterator locs = locations.iterator();
    while (locs.hasNext()) {
      Location loc = (Location)locs.next();
      if (loc.getAltId().equals(altId)) {
	return loc;
      }
    }

    return null;
  }

  //}}}
  //{{{ public Iterator locationIterator()

  public Iterator locationIterator()
  {
    return locations.iterator();
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    return "field[" + id + ", " + type + "," + locations + "]";
  }
  
  //}}}
}
