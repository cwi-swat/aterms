
package apigen.gen.java;

import java.util.LinkedList;

import apigen.gen.Generator;
import apigen.gen.StringConversions;


public class GenericConstructorGenerator extends JavaGenerator {
    private String className;
    private String factoryName;
    
	public GenericConstructorGenerator(String directory, String apiName, String pkg, boolean verbose, boolean folding) {
		
		super(directory,getConstructorClassName(apiName),pkg,new LinkedList(),verbose,folding); 
		
		className = getConstructorClassName(apiName);
		factoryName = FactoryGenerator.getFactoryClassName(apiName);
	}
	
	public static String getConstructorClassName(String apiName) {
			return StringConversions.makeCapitalizedIdentifier(apiName) + "Factory";
	}
    
	protected void generate() {
		printPackageDecl();
		genGenericConstructorClass();
	}
  
	  private void genGenericConstructorClass()
	  { 
		println("abstract public class " + className);
		println("extends aterm.pure.ATermApplImpl");
		println("implements aterm.ATerm");
		println("{");
		println("  protected aterm.ATerm term = null;");
		println();
		println("  abstract protected aterm.ATerm getPattern();");
		println();
		println("  public aterm.ATerm toTerm() {");
		println("    if(term == null) {");
		println("      java.util.List args = new java.util.LinkedList();");
		println("      for(int i = 0; i<getArity() ; i++) {");
		println("        args.add(((" + className + ") getArgument(i)).toTerm());");
		println("      }");
		println("      setTerm(getFactory().make(getPattern(), args));");
		println("    }");
		println("    return term;");
		println("  }");
		println();
		println("  public String toString() {");
		println("    return toTerm().toString();");
		println("  }");
		println();
		println("  protected void setTerm(aterm.ATerm term) {");
		println("   this.term = term;");
		println("  }");
		println();
		println("  protected " + factoryName + " get" + factoryName + "() {");
		println("    return (" + factoryName + ") getFactory();");
		println("  }");
		println();
		println("  static protected " + factoryName + " getStatic" + factoryName + "() {");
		println("    return (" + factoryName + ") getStaticFactory();");
		println("  }");
		println();
		println("}");
	  }
}
