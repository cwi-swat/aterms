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
	List<Module> modules;
	List<Type> types;
    List<String> bottomTypes;
    Map<String, List<Type>> modulesTypes;
    Factory factory;
    private static ADT instance;
    
    public static ADT getInstance() {
    	if(instance == null) {
    		throw new RuntimeException("ADT.getInstance called before initialize");
    	} 
    	return instance;
    }
    
    private ADT(Modules adt) throws ADTException {
    	types = new LinkedList<Type>();
      modules = new LinkedList<Module>();
      modulesTypes = new HashMap<String, List<Type>>();
      factory = adt.getApiFactory();

      List<Entry> entries = new LinkedList<Entry>();

      Modulentry currentModule;
      String moduleName;
      while (!adt.isEmpty()) { //For each module
      		List<Type> listModuleType = new LinkedList<Type>();
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
            List<Entry> alts = new LinkedList<Entry>();
            ListIterator<Entry> iter = entries.listIterator();
            Entry first = iter.next();
            alts.add(first);
            String typeId = ((ATermAppl) first.getSort()).getAFun().getName();

            iter.remove();
            while (iter.hasNext()) {
                try {
                    Entry entry = iter.next();
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
        bottomTypes = new LinkedList<String>();
        Iterator<Type> types = typeIterator();

        // TODO: do not include builtin types as bottomTypes
        while (types.hasNext()) {
            Type type = types.next();
            Iterator<Field> fields = type.fieldIterator();

            while (fields.hasNext()) {
                Field field = fields.next();
                Iterator<Type> definedTypes = typeIterator();
                boolean defined = false;

                while (definedTypes.hasNext()) {
                    Type definedType = definedTypes.next();

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

    private Type processAlternatives(String typeId, String moduleName, List<Entry> alts) {
        Entry first = alts.get(0);

        if (first.isList() || first.isSeparatedList() || first.isNamedList()) {
            if (alts.size() > 1) {
                throw new RuntimeException("Multiple definitions of same list: " + alts);
            }

            if (first.isSeparatedList()) {
                return processSeparatedList(typeId, moduleName, first);
            } else if (first.isNamedList()) {
                return processNamedList(typeId, moduleName, first);
            } else {
                return processList(typeId, moduleName, first);
            }
        }
        return processConstructors(typeId, moduleName, alts);
    }

    private Type processSeparatedList(String typeId, String moduleName, Entry entry) {
        String elementType = ((ATermAppl) entry.getElemSort()).getAFun().getName();
        Separators separators = entry.getSeparators();
        SeparatedListType type = new SeparatedListType(typeId, moduleName, elementType, separators, factory);
        type.addAlternatives();
        types.add(type);
        return type;
    }

    private Type processNamedList(String typeId, String moduleName, Entry entry) {
        String elementType = ((ATermAppl) entry.getElemSort()).getAFun().getName();
        String opName = ((ATermAppl) entry.getOpname()).getAFun().getName();
        NamedListType type = new NamedListType(typeId, moduleName, elementType, opName, factory);
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

    private Type processConstructors(String typeId, String moduleName, List<Entry> alts) {
        Type type = new Type(typeId, moduleName);
        ListIterator<Entry> iter = alts.listIterator();

        while (iter.hasNext()) {
            Entry entry = iter.next();

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

    public Iterator<Type> typeIterator() {
        return types.iterator();
    }

    public Iterator<Type> typeIterator(Module module) {
    	return (modulesTypes.get(module.getModulename().getName())).iterator();
    }

    public Iterator<Type> typeIterator(String moduleName) {
    	return (modulesTypes.get(moduleName)).iterator();
    }

    public Iterator<String> bottomTypeIterator() {
        return bottomTypes.iterator();
    }
    
    public Iterator<Module> moduleIterator() {
      return modules.iterator();
    }
    
    public String getModuleName(TypeConverter conv, String typename) {
    		String modulename= "";
    		//System.out.println("Test : "+ modulesTypes);
    		if (conv.isReserved(typename)) {
    			return modulename;
    		}
    		Iterator<Type> it = typeIterator();
    		while (it.hasNext()) {
    			Type current = it.next();
    			if (current.getId().equals(typename)) {
    				return current.getModuleName();
    			}
    		}
    		it = typeIterator();
    		while (it.hasNext()) {
    			Type current = it.next();
    			Iterator<Alternative> altit = current.alternativeIterator();
    			while (altit.hasNext()) {
    			    Alternative alt = altit.next();
    			    	if (alt.getId().equals(typename)) {
    				return current.getModuleName();
    			}
    			}
    		}
    		throw new RuntimeException("The type " + typename + " is unknown");
    }
    
    public Set<String> getImportsClosureForModule(String moduleName) {
    		Set<String> result = new HashSet<String>();
    	    computeImportsClosureForModule(result,moduleName);
    		return result;
    }
    
    private void computeImportsClosureForModule(Set<String> result,String moduleName) {
    	    //	System.out.println("moduleName = " + moduleName);
    	    Module currentModule = getModuleFromName(moduleName);
    	    if(currentModule==null) {
    	    		return;
    	    }
    	
    	    String currentModuleName = currentModule.getModulename().getName();
    	    result.add(currentModuleName);
    	    
    	    Imports imported = currentModule.getImports();
    	    while(!imported.isEmpty()) {
    	    		String name = imported.getHead().getName();
    	    		if(!result.contains(name)) {
    	    			computeImportsClosureForModule(result,name);
    	    		}
    	    		imported = imported.getTail();
    	    }
    	
    //	System.out.println("result = " + result);
    }
    
    private Module getModuleFromName(String moduleName) {
        	Iterator<Module> modulesIt = moduleIterator();
    	    	while(modulesIt.hasNext()) {
    	    		Module currentModule = modulesIt.next();
    	    		String currentModuleName = currentModule.getModulename().getName();
    	    		if(moduleName.equals(currentModuleName)) {
    	    			return currentModule;
    	    		}
    	    	}
    	    	return null;
    	}
  
  public Set<String> getModuleNameSet() {
    Set<String> modulenames = new HashSet<String>();
    Iterator<Module> it = moduleIterator();
    while (it.hasNext()) {
      Module mod = it.next();
      String modname = mod.getModulename().getName();
      modulenames.add(modname);
    }
    return modulenames;
  }

}
