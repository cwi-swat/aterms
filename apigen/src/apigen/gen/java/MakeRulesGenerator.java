
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
    
    public MakeRulesGenerator(ADT adt, String directory, String api_name, boolean verbose) {
    	super(directory, StringConversions.makeCapitalizedIdentifier(api_name) + "MakeRules","",verbose,false); 
		this.name = StringConversions.makeCapitalizedIdentifier(api_name);
		this.adt = adt;
    }
    
    private static String getClassFileName(String className) {
    	return className + ".java";
    }
    
	protected void generate() {
		String prefix = name + "API";
   
		Iterator types = adt.typeIterator();
		int bucket = 0;
		int i = 0;
		while(types.hasNext()) {
		  Type type = (Type) types.next();
      
		  if (i % MAX_FILES_IN_MAKEFILE_VARIABLE == 0) {
			println(); println();
			print(prefix + bucket++ + "=");
		  }
      
		  print("\\\n" + getClassFileName(JavaGenerator.getClassName(type.getId())));
		  print(" " + getClassFileName(JavaGenerator.getClassImplName(type.getId())));
		  i++;
            
		  Iterator alts = type.alternativeIterator();
		  while(alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();

			if (i % MAX_FILES_IN_MAKEFILE_VARIABLE == 0) {
			  println(); println();
			  print(prefix + bucket++ + "=");
			}
               
			print("\\\n" + getClassFileName(AlternativeImplGenerator.getAltClassImplName(type.getId(),alt.getId())));
			print(" " + getClassFileName(AlternativeGenerator.getAltClassName(type,alt))); 
			i++;
		  }
		}
    
		println();
		println();
    
		print(prefix + "=\\\n" + 
		getClassFileName(FactoryGenerator.getFactoryClassName(name)) + " " + 
		getClassFileName(GenericConstructorGenerator.getConstructorClassName(name)) + "\\\n");
		
		while(--bucket >= 0) {
		  print("${" + prefix + bucket + "} ");
		}
	  }
}
