package apigen;

import java.util.*;

import aterm.*;
import aterm.pure.*;

public class API
{
  List types;

  //{ public final static void main(String[] args)

  public final static void main(String[] args)
    throws java.io.IOException
  {
    ATermFactory factory = new PureFactory();

    ATerm adt = factory.readFromFile(System.in);

    API api = new API(adt);
  }

  //}

  //{ public API(ATerm adt)

  public API(ATerm adt)
  {
    types = new LinkedList();

    ATermList list = (ATermList)adt;
    List entries = new LinkedList();

    while (!list.isEmpty()) {
      entries.add(list.getFirst());
      list = list.getNext();
    }

    while (!entries.isEmpty()) {
      List alts = new LinkedList();
      ListIterator iter = entries.listIterator();
      ATermList first = (ATermList)iter.next();
      alts.add(first);
      ATerm typeId = first.getFirst();

      iter.remove();
      while (iter.hasNext()) {
	ATermList entry = (ATermList)iter.next();
	if (entry.getFirst().equals(typeId)) {
	  alts.add(entry);
	  iter.remove();
	}
      }
      
      processAlternatives(typeId.toString(), alts);
      System.out.println("alts: " + alts.toString());
    }

    System.out.println("TYPES: " + types);
  }

  //}

  //{ private void processAlternatives(String typeId, List alts)

  private void processAlternatives(String typeId, List alts)
  {
    Type type = new Type(typeId);
    ListIterator iter  = alts.listIterator();
    while (iter.hasNext()) {
      ATermList entry = (ATermList)iter.next();
      List matches    = entry.match("[<appl>,<appl>,<term>]");
      String altId    = (String)matches.get(1);
      ATerm pattern   = (ATerm)matches.get(2);
      
      Alternative alt = new Alternative(altId, pattern);
      type.addAlternative(alt);
    }

    types.add(type);
  }

  //}

}
