package apigen.adt;

import java.util.*;

import aterm.*;

public class Type
{
  String id;
  AlternativeList alts;
  Map    fields;
  List   field_list;

  //{{{ public Type(String id)

  public Type(String id)
  {
    this.id = id;

    alts   = new AlternativeList();
    fields = new HashMap();
    field_list = new Vector();
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
      field_list.add(field);
    }

    field.addLocation(location);
  }

  //}}}

  //{{{ public List getAlternatives()

  public AlternativeList getAlternatives()
  {
    return (AlternativeList)alts.clone();
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
    return field_list.iterator();
  }

  //}}}
  //{{{ public Iterator altFieldIterator(final String altId)

  public Iterator altFieldIterator(final String altId)
  {
    Comparator comp;

    //{{{ comp = new Comparator() { ... }

    comp = new Comparator() {
      public int compare(Object o1, Object o2) {
	Field field1 = (Field)o1;
	Field field2 = (Field)o2;

	Iterator path1 = field1.getLocation(altId).stepIterator();
	Iterator path2 = field2.getLocation(altId).stepIterator();

	while (path1.hasNext()) {
	  if (!path2.hasNext()) {
	    throw new RuntimeException("incompatible paths: "
				       + field1 + "," + field2);
	  }
	  Step step1 = (Step)path1.next();
	  Step step2 = (Step)path2.next();
	  int type1 = step1.getType();
	  int type2 = step2.getType();

	  if (type1 == Step.TAIL && type2 == Step.ELEM) {
	    return 1;
	  }
	      
	  if (type1 == Step.ELEM && type2 == Step.TAIL) {
	    return -1;
	  }
	      
	  if (type1 != type2) {
	    throw new RuntimeException("incompatible paths: "
				       + field1 + "," + field2);
	  }

	  if (step1.getIndex() < step2.getIndex()) {
	    return -1;
	  }

	  if (step1.getIndex() > step2.getIndex()) {
	    return 1;
	  }
	}
	if (path2.hasNext()) {
	  throw new RuntimeException("incompatible paths: " 
				     + field1 + "," + field2);
	}
	if (o1 != o2) {
	  throw new RuntimeException("asjemenou?");
	}
	return 0;
      }
    };

    //}}}

    SortedSet sortedAltFields = new TreeSet(comp);

    Iterator iter = fields.values().iterator();
    while (iter.hasNext()) {
      Field field = (Field)iter.next();
      if (field.hasAltId(altId)) {
	sortedAltFields.add(field);
      }
    }

    return sortedAltFields.iterator();
  }

  //}}}
  //{{{ public int getAlternativeCount()

  public int getAlternativeCount()
  {
    return alts.size();
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

