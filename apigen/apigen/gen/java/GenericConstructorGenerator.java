
package apigen.gen.java;

import java.util.Iterator;
import java.util.LinkedList;

import apigen.adt.ADT;
import apigen.adt.Type;
import apigen.gen.StringConversions;


public class GenericConstructorGenerator extends JavaGenerator {
    private boolean visitable;
	private String className;
    private String factoryName;
    private ADT api;
    
	public GenericConstructorGenerator(ADT api, String directory, String apiName, String pkg, boolean verbose, boolean visitable, int arg1) {
		
		super(directory,className(apiName),pkg,new LinkedList(),verbose); 
		
        this.visitable = visitable;
		className = className(apiName);
		factoryName = FactoryGenerator.className(apiName);
        this.api = api;
	}
	
	public static String className(String apiName) {
			return StringConversions.makeCapitalizedIdentifier(apiName) + "Constructor";
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
		println("  public " + factoryName + " get" + factoryName + "() {");
		println("    return (" + factoryName + ") getFactory();");
		println("  }");
		println();
		println("  static protected " + factoryName + " getStatic" + factoryName + "() {");
		println("    return (" + factoryName + ") getStaticFactory();");
		println("  }");
		println();
        genDefaultTypePredicates();
        if (visitable) {
          genAccept();
        }
		println("}");
	  }
      
      private void genAccept()  {
          println("  abstract public void accept(Visitor v) throws jjtraveler.VisitFailure;"); 
      }
      
      private void genDefaultTypePredicates() {
          Iterator types = api.typeIterator();
          
          while (types.hasNext()) {
              Type type = (Type) types.next();
              
              genDefaultTypePredicate(type);
          }
      }

	private void genDefaultTypePredicate(Type type) {
		println("  public boolean isSort" + TypeGenerator.className(type) + "() {");
        println("    return false;");
        println("  }");
        println();
	}
}
