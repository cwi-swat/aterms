
package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Type;
import apigen.gen.Generator;
import apigen.gen.StringConversions;

public class MakeRulesGenerator extends  Generator {
	private static final int MAX_FILES_IN_MAKEFILE_VARIABLE = 50;
    private ADT adt;
    private String name;

    private int bucket;
    
	private String prefix;
	public MakeRulesGenerator(ADT adt, String directory, String api_name, boolean verbose) {
    	super(directory, StringConversions.makeCapitalizedIdentifier(api_name) + "MakeRules","",verbose,false); 
		this.name = StringConversions.makeCapitalizedIdentifier(api_name);
		this.adt = adt;
		prefix = name + "API";
    }
    
    private static String getClassFileName(String className) {
    	return className + ".java";
    }
    
	protected void generate() {
		Iterator types = adt.typeIterator();
		bucket = 0;
		int i = 0;
		
		while(types.hasNext()) {
		  Type type = (Type) types.next();
      
		  makeNewBucket(i);
      
		  printTypeClassFiles(type);
		  i++;
            
		  Iterator alts = type.alternativeIterator();
		  while(alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();

            makeNewBucket(i);         
			printAlternativeClassFiles(type, alt);
			i++;
		  }
		}
    
		println();
		println();
    
		printAccumulatedVariable();
	  }

	protected void printAccumulatedVariable() {
		print(prefix + "=\\\n" + 
		getClassFileName(FactoryGenerator.className(name)) + " " + 
		getClassFileName(GenericConstructorGenerator.getConstructorClassName(name)) + "\\\n");
		
		while(--bucket >= 0) {
		  print("${" + prefix + bucket + "} ");
		}
	}

	protected void printAlternativeClassFiles(Type type, Alternative alt) {
		print("\\\n" + getClassFileName(AlternativeImplGenerator.className(type.getId(),alt.getId())));
		print(" " + getClassFileName(AlternativeGenerator.className(type,alt))); 
	}

	protected void printTypeClassFiles(Type type) {
			print("\\\n" + getClassFileName(TypeGenerator.className(type.getId())));
			  print(" " + getClassFileName(TypeImplGenerator.className(type)));
	}
		
	protected void makeNewBucket(int i) {
		if (i % MAX_FILES_IN_MAKEFILE_VARIABLE == 0) {
			println(); println();
			print(prefix + bucket + "=");
			bucket++;
		  }
	}

	
}
