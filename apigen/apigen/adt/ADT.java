package apigen.adt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import aterm.ATerm;
import aterm.ATermFactory;
import aterm.ATermList;
import aterm.pure.PureFactory;

public class ADT {
	List types;
  List bottomTypes;

	//{{{ public final static void main(String[] args)

	public final static void main(String[] args) throws java.io.IOException {
		ATermFactory factory = new PureFactory();

		ATerm adt = factory.readFromFile(System.in);

		ADT api = new ADT(adt);
	}

	//}}}

	//{{{ public API(ATerm adt)

	public ADT(ATerm adt) {
		types = new LinkedList();

		ATermList list = (ATermList) adt;
		List entries = new LinkedList();

		while (!list.isEmpty()) {
			entries.add(list.getFirst());
			list = list.getNext();
		}

		while (!entries.isEmpty()) {
			List alts = new LinkedList();
			ListIterator iter = entries.listIterator();
			ATermList first = (ATermList) iter.next();
			alts.add(first);
			ATerm typeId = first.getFirst();

			iter.remove();
			while (iter.hasNext()) {
        try {
				  ATermList entry = (ATermList) iter.next();
				  if (entry.getFirst().equals(typeId)) {
				  	alts.add(entry);
				  	iter.remove();
				  }
        }
        catch (ClassCastException exc) {
          System.err.println("unexpected entry found:" + iter.previous());
          System.exit(1);
        }
			}

			processAlternatives(typeId.toString(), alts);
		}
    
    computeBottomTypes();
	}

	//}}}

  private void computeBottomTypes() {
    bottomTypes = new LinkedList();
    Iterator types = typeIterator();
    
    while(types.hasNext()) {
      Type type = (Type) types.next();
      Iterator fields = type.fieldIterator(); 
    
      while (fields.hasNext()) {
        Field field = (Field) fields.next();
        Iterator definedTypes = typeIterator();
        boolean defined = false;

        while (definedTypes.hasNext()) {
          Type definedType = (Type) definedTypes.next();

          if (field.getType().equals(definedType.getId())) {
            defined = true;
            break;
          }
        }
        
        if (!defined) {
          if (!bottomTypes.contains(field.getType())) {
            bottomTypes.add(field.getType());
          }
        } 
      } 
    }
  }
	//{{{ private void processAlternatives(String typeId, List alts)

	private void processAlternatives(String typeId, List alts) {
		Type type = new Type(typeId);
		ListIterator iter = alts.listIterator();
		while (iter.hasNext()) {
			ATermList entry = (ATermList) iter.next();
			List matches = entry.match("[<appl>,<appl>,<term>]");
			String altId = (String) matches.get(1);
			ATerm pattern = (ATerm) matches.get(2);

			Alternative alt = new Alternative(altId, pattern);
      
      if (!type.hasAlternative(altId)) {
			  type.addAlternative(alt);
      }
      else {
        throw new RuntimeException(altId + " is defined more than once for " +
                                   typeId);
      }
		}

		types.add(type);
	}

	//}}}

	//{{{ public Iterator typeIterator()

	public Iterator typeIterator() {
		return types.iterator();
	}

  public Iterator bottomTypeIterator() {
    return bottomTypes.iterator();
  }
  
	//}}}

}
