package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;

public class VisitorGenerator extends JavaGenerator {
	private ADT adt;
    private String className;

	public VisitorGenerator(
		ADT adt,
		String directory,
		String pkg,
		List standardImports,
		boolean verbose) {
		super(directory, className(), pkg, standardImports, verbose);
		this.adt = adt;
        className = className();
        
	}

	public static String className() {
		return  "Visitor";
	}

	protected void generate() {
		printPackageDecl();
	
		println("public abstract class " + className + " extends jjtraveler.VoidVisitor");
		println("{");
		genVisits(adt);
		println("}");
	}
    
	protected void genVisits(ADT adt) {
		Iterator types = adt.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			
			if (type instanceof ListType) {
			  genListVisit(type);	
			}
			else {
			  Iterator alts = type.alternativeIterator();
			  while (alts.hasNext()) {
				  Alternative alt = (Alternative) alts.next();
			  	  genVisit(type, alt);
			  }
			}
		}
	}

	private void genListVisit(Type type) {
		String className = TypeGenerator.className(type);
		String classImplName = ListTypeGenerator.className(type);
		genVisitDecl(classImplName, className);
	}

	private void genVisit(Type type, Alternative alt) {
		String classImplName = AlternativeGenerator.className(type, alt);
        String className = AlternativeGenerator.className(type, alt);
		genVisitDecl(classImplName, className);
	}

	private void genVisitDecl(String classImplName, String className) {
		println("public abstract void visit_" + className + "(" + classImplName + " arg ) throws jjtraveler.VisitFailure;");
	}

}
