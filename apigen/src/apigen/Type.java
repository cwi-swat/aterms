package apigen;

import java.util.*;

import aterm.*;

public class Type
{
  String id;
  List   alts;
  Map    fields;

  //{{{ public Type(String id)

  public Type(String id)
  {
    this.id = id;

    alts   = new LinkedList();
    fields = new HashMap();
  }

  //}}}
  //{{{ public String getId()

  public String getId()
  {
    return id;
  }

  //}}}
  //{{{ public void addAlternative(Alternative alt)

  public void addAlternative(Alternative alt)
  {
    alts.add(alt);

    extractFields(alt.getPattern(), new Location(alt.getId()));
  }

  //}}}
  //{{{ private void extractFields(ATerm t, Location loc)

  private void extractFields(ATerm t, Location loc)
  {
    AFun fun;
    ATermAppl appl;
    ATermList list;

    switch (t.getType()) {
      case ATerm.APPL:
	//{{{ Call 'extractFields' for every argument

	appl = (ATermAppl)t;
	fun  = appl.getAFun();
	for (int i=0; i<fun.getArity(); i++) {
	  Location newloc = (Location)loc.clone();
	  newloc.addStep(new Step(Step.ARG, i));
	  extractFields(appl.getArgument(i), newloc);
	}

	//}}}
	break;

      case ATerm.LIST:
	//{{{ Call 'extractFields' for every element

	list = (ATermList)t;
	for (int i=0; !list.isEmpty(); i++) {
	  Location newloc = (Location)loc.clone();
	  newloc.addStep(new Step(Step.ELEM, i));
	  extractFields(list.getFirst(), newloc);
	  list = list.getNext();
	}

	//}}}
	break;

      case ATerm.PLACEHOLDER:
	//{{{ Add a new field based on this placeholder

	ATerm ph = ((ATermPlaceholder)t).getPlaceholder();
	
	if (ph.getType() == ATerm.LIST) {
	  list = (ATermList)ph;
	  appl = (ATermAppl)list.elementAt(0);
	  String fieldId = appl.getAFun().getName();
	  appl = (ATermAppl)appl.getArgument(0);
	  String fieldType = appl.getAFun().getName();
	  loc.makeTail();
	  addField(fieldId, fieldType, loc);
	} else if (ph.getType() == ATerm.APPL) {
	  appl = (ATermAppl)ph;
	  String fieldId = appl.getAFun().getName();
	  appl = (ATermAppl)appl.getArgument(0);
	  String fieldType = appl.getAFun().getName();
	  addField(fieldId, fieldType, loc);
	} else {
	  throw new RuntimeException("illegal field spec: " + t);
	}

	//}}}
	break;

      default:
	break;
    }
  }

  //}}}
  //{{{ private void addField(id, type, location)

  private void addField(String id, String type, Location location)
  {
    Field field;

    field = (Field)fields.get(id);
    if (field == null) {
      field = new Field(id, type);
      fields.put(id, field);
    }

    field.addLocation(location);
  }

  //}}}

  //{{{ public Iterator alternativeIterator()

  public Iterator alternativeIterator()
  {
    return alts.iterator();
  }

  //}}}
  //{{{ public Iterator fieldIterator()

  public Iterator fieldIterator()
  {
    return fields.values().iterator();
  }

  //}}}
  //{{{ public Iterator altFieldIterator(final String altId)

  public Iterator altFieldIterator(final String altId)
  {
    return new Iterator() {
      private Iterator iter = fields.values().iterator();
      private Field next = findNext();

      //{{{ private Field findNext()

      private Field findNext() {
	while (iter.hasNext()) {
	  Field next = (Field)iter.next();
	  if (next.hasAltId(altId)) {
	    return next;
	  }
	}
	return null;
      }

      //}}}
      //{{{ public boolean hasNext()

      public boolean hasNext()
      {
	return next != null;
      }

      //}}}
      //{{{ public Object next()

      public Object next()
      {
	Field result = next;
	next = findNext();
	return result;
      }

      //}}}
      //{{{ public void remove()

      public void remove()
      {
	throw new UnsupportedOperationException();
      }

      //}}}
    };
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    return "type[" + id + ", " + alts.toString() + 
      ",\n" + fields.toString() + "]";
  }

  //}}}
}
