
package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Type;
import apigen.gen.Generator;
import apigen.gen.StringConversions;

public class MakeRulesGenerator extends  Generator {
    private ADT adt;
    private String name;
    
    public MakeRulesGenerator(ADT adt, String directory, String api_name, boolean verbose) {
    	super(directory, StringConversions.makeCapitalizedIdentifier(api_name) + "MakeRules","",verbose,false); 
		name = StringConversions.makeCapitalizedIdentifier(api_name);
    }
    
    private static String getClassFileName(String className) {
    	return className + ".java";
    }
    
	protected void generate() {
		String prefix = name + "API";
    
		info("generating " + name + "MakeRules");
		Iterator types = adt.typeIterator();
		int bucket = 0;
		int i = 0;
		while(types.hasNext()) {
		  Type type = (Type) types.next();
      
		  if (i % 50 == 0) {
			println(); println();
			print(prefix + bucket++ + "=");
		  }
      
		  print("\\\n" + getClassFileName(JavaGenerator.getClassName(type.getId())));
		  print(getClassFileName(JavaGenerator.getClassImplName(type.getId())));
		  i++;
            
		  Iterator alts = type.alternativeIterator();
		  while(alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();

			if (i % 50 == 0) {
			  println(); println();
			  print(prefix + bucket++ + "=");
			}
               
			print("\\\n" + getClassFileName(AlternativeImplGenerator.getAltClassImplName(type.getId(),alt.getId())));
			print(getClassFileName(AlternativeGenerator.getAltClassName(type.getId(),alt.getId()))); 
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
