    package apigen.adt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import apigen.adt.api.Entries;
import apigen.adt.api.Entry;
import aterm.ATerm;
import aterm.ATermAppl;

public class ADT {
	List types;
  List bottomTypes;

	public ADT(Entries adt) {
		types = new LinkedList();

		List entries = new LinkedList();

		while (!adt.isEmpty()) {
			entries.add(adt.getHead());
			adt = adt.getTail();
		}

		while (!entries.isEmpty()) {
			List alts = new LinkedList();
			ListIterator iter = entries.listIterator();
			Entry first = (Entry) iter.next();
			alts.add(first);
			String typeId = ((ATermAppl) first.getSort()).getAFun().getName();

			iter.remove();
			while (iter.hasNext()) {
        try {
				  Entry entry = (Entry) iter.next();
				  if (((ATermAppl) entry.getSort()).getAFun().getName().equals(typeId)) {
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

	private void processAlternatives(String typeId, List alts) {
		Type type = new Type(typeId);
		ListIterator iter = alts.listIterator();
        
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			String altId = ((ATermAppl) entry.getAlternative()).getAFun().getName();
			ATerm pattern = entry.getTermPattern();

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

	public Iterator typeIterator() {
		return types.iterator();
	}

  public Iterator bottomTypeIterator() {
    return bottomTypes.iterator();
  }
}
