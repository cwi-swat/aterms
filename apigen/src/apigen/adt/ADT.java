package apigen.adt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import apigen.adt.api.Factory;
import apigen.adt.api.types.Entries;
import apigen.adt.api.types.Entry;
import apigen.adt.api.types.Imports;
import apigen.adt.api.types.Module;
import apigen.adt.api.types.Modules;
import apigen.adt.api.types.Separators;
import apigen.adt.api.types.module.Modulentry;
import apigen.gen.TypeConverter;
import aterm.ATerm;
import aterm.ATermAppl;

public class ADT {
		List modules;
    List types;
    List bottomTypes;
    Map modulesTypes;
    Factory factory;
    private static ADT instance;
    
    public static ADT getInstance() {
    	if(instance == null) {
    		throw new RuntimeException("ADT.getInstance called before initialize");
    	} 
    	return instance;
    }
    
    private ADT(Modules adt) throws ADTException {
    	types = new LinkedList();
      modules = new LinkedList();
      modulesTypes = new HashMap();
      factory = adt.getApiFactory();

      List entries = new LinkedList();

      Modulentry currentModule;
      String moduleName;
      while (!adt.isEmpty()) { //For each module
      		List listModuleType = new LinkedList();
      		currentModule = (Modulentry)adt.getHead();
      		moduleName = currentModule.getModulename().getName();
      		//System.out.println("Processing module "+moduleName);
      		modules.add(currentModule);
      		Entries currentEntries = currentModule.getEntries();
      		while(!currentEntries.isEmpty()) {
      			entries.add(currentEntries.getHead());
      			currentEntries = currentEntries.getTail();
      		}
      		
      		
      		while (!entries.isEmpty()) { //For each entry in the module
            List alts = new LinkedList();
            ListIterator iter = entries.listIterator();
            Entry first = (Entry) iter.next();
            alts.add(first);
            String typeId = ((ATermAppl) first.getSort()).getAFun().getName();

            iter.remove();
            while (iter.hasNext()) {
                try {
                    Entry entry = (Entry) iter.next();
                    if (((ATermAppl) entry.getSort())
                        .getAFun()
                        .getName()
                        .equals(typeId)) {
                        alts.add(entry);
                        iter.remove();
                    }
                } catch (ClassCastException exc) {
                    throw new ADTException(
                        "unexpected entry found:" + iter.previous());
                }
            }

            listModuleType.add(processAlternatives(typeId.toString(), moduleName, alts));
        }
      	modulesTypes.put(moduleName,listModuleType);
      		
        adt = adt.getTail();
      }
      
      computeBottomTypes();
      analyseModuleList();
      
      /*System.out.println("Computed Types");
      Iterator iter = typeIterator();
      while(iter.hasNext()) {
      	System.out.println(iter.next());
      }
      System.out.println("Computed BottomTypes");
      iter = bottomTypeIterator();
      while(iter.hasNext()) {
      	System.out.println(iter.next());
      }*/

    }
    
    public static ADT initialize(Modules adt) throws ADTException {
    	instance = new ADT(adt);
    	return instance;
    }

    /**
	 * Analyse the list of module to see whether there are no cycles and module with same names
	 */
    private void analyseModuleList() {
		
		
    }

	  private void computeBottomTypes() {
        bottomTypes = new LinkedList();
        Iterator types = typeIterator();

        // TODO: do not include builtin types as bottomTypes
        while (types.hasNext()) {
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

    private Type processAlternatives(String typeId, String moduleName, List alts) {
        Entry first = (Entry) alts.get(0);

        if (first.isList() || first.isSeparatedList()) {
            if (alts.size() > 1) {
                throw new RuntimeException("Multiple definitions of same list: " + alts);
            }

            if (first.isSeparatedList()) {
                return processSeparatedList(typeId, moduleName, first);
            } else {
                return processList(typeId, moduleName, first);
            }
        } else {
            return processConstructors(typeId, moduleName, alts);
        }
    }

    private Type processSeparatedList(String typeId, String moduleName, Entry entry) {
        String elementType = ((ATermAppl) entry.getElemSort()).getAFun().getName();
        Separators separators = entry.getSeparators();
        SeparatedListType type = new SeparatedListType(typeId, moduleName, elementType, separators, factory);
        type.addAlternatives();
        types.add(type);
        return type;
    }

    private Type processList(String typeId, String moduleName, Entry first) {
        String elementType = ((ATermAppl) first.getElemSort()).getAFun().getName();
        ListType type = new ListType(typeId, moduleName, elementType, factory);
        type.addAlternatives();
        types.add(type);
        return type;
    }

    private Type processConstructors(String typeId, String moduleName, List alts) {
        Type type = new Type(typeId, moduleName);
        ListIterator iter = alts.listIterator();

        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();

            if (entry.isConstructor()) {
                String altId = ((ATermAppl) entry.getAlternative()).getAFun().getName();
                ATerm pattern = entry.getTermPattern();

                Alternative alt = new Alternative(altId, pattern);

                addAlternative(typeId, type, altId, alt);
            } else {
                throw new RuntimeException("Unexpected alternative");
            }
        }

        types.add(type);
        return type;
    }

    private void addAlternative(
        String typeId,
        Type type,
        String altId,
        Alternative alt) {
        if (!type.hasAlternative(altId)) {
            type.addAlternative(alt);
        } else {
            throw new RuntimeException(
                altId + " is defined more than once for " + typeId);
        }
    }

    public Iterator typeIterator() {
        return types.iterator();
    }

    public Iterator typeIterator(Module module) {
    	return ((List)modulesTypes.get(module.getModulename().getName())).iterator();
    }

    public Iterator typeIterator(String moduleName) {
    	return ((List)modulesTypes.get(moduleName)).iterator();
    }

    public Iterator bottomTypeIterator() {
        return bottomTypes.iterator();
    }
    
    public Iterator moduleIterator() {
      return modules.iterator();
    }
    
    public String getModuleName(TypeConverter conv, String typename) {
    		String modulename= "";
    		//System.out.println("Test : "+ modulesTypes);
    		if (conv.isReserved(typename)) {
    			return modulename;
    		}
    		Iterator it = typeIterator();
    		while (it.hasNext()) {
    			Type current = (Type) it.next();
    			if (current.getId().equals(typename)) {
    				return current.getModuleName();
    			}
    		}
    		it = typeIterator();
    		while (it.hasNext()) {
    			Type current = (Type) it.next();
    			Iterator altit = current.alternativeIterator();
    			while (altit.hasNext()) {
    			    Alternative alt = (Alternative) altit.next();
    			    	if (alt.getId().equals(typename)) {
    				return current.getModuleName();
    			}
    			}
    		}
    		throw new RuntimeException("The type " + typename + " is unknown");
    }
    
    // Warning: if there are cycle, tchou tchou TODO
    public Set getImportsClosureForModule(String moduleName) {
    	Set result = new HashSet();
    	Iterator modulesIt = moduleIterator();
    	
    //	System.out.println("getImportsClosureForModule for "+moduleName);
    	
    	while(modulesIt.hasNext()) {
    		Module currentModule = (Module)modulesIt.next();
    		String currentModuleName = currentModule.getModulename().getName();
    		if(moduleName.equals(currentModuleName)) {
    			Imports imported = currentModule.getImports();
    			//System.out.println(currentModuleName+" has imports:"+imported);
    			result.add(currentModuleName);
    			//System.out.println("getImportsClosureForModule adding "+currentModuleName);
    			while(!imported.isEmpty()) {
    				result.addAll(getImportsClosureForModule(imported.getHead().getName()));
    				imported = imported.getTail();
    			}
    			break;
    		}
    	}
    	return result;
    }
    
}
