package apigen;

import java.util.*;

public class Field
{
  String id;
  String type;

  List locations;

  //{ public Field(String id, String type)

  public Field(String id, String type)
  {
    this.id     = id;
    this.type   = type;

    locations = new Vector();
  }

  //}
  //{ public void addLocation(Location loc)

  public void addLocation(Location loc)
  {
    locations.add(loc);
  }

  //}

  //{ public String toString()

  public String toString()
  {
    return "field[" + id + ", " + type + "," + locations + "]";
  }
  
  //}
}
