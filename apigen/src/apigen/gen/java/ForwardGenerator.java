
package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Type;
import apigen.gen.StringConversions;

public class ForwardGenerator extends JavaGenerator {
    private ADT adt;
    
	public ForwardGenerator(
	    ADT adt,
		String directory,
		String apiName,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, className(apiName), pkg, standardImports, verbose);
		this.adt = adt;
	}

	public static String className(String apiName) {
		  return StringConversions.capitalize("Fwd");
	}
	 
	protected void genVisits(ADT adt) {
		Iterator types = adt.typeIterator();
		while(types.hasNext()) {
			Type type = (Type) types.next();
			Iterator alts = type.alternativeIterator();
            while (alts.hasNext()) {
                Alternative alt = (Alternative) alts.next();
                genVisit(type, alt);
            }
		}
	}

	private void genVisit(Type type, Alternative alt) {
        String className =  AlternativeGenerator.className(type,alt);
        String classImplName =  AlternativeImplGenerator.className(type,alt);
        
		println("public void visit_" + className + "(" + classImplName + " arg ) throws jjtraveler.VisitFailure {");
        println("  any.visit(arg);");
        println("}");
		
	}

	protected void generate() {
		printPackageDecl();
		
        println("public class Fwd extends Visitor implements jjtraveler.Visitor");
        println("{");
        genConstructor();
		genVisits(adt);
        println("}");
	}

	private void genConstructor() {
       println("private jjtraveler.Visitor any;");
       println("public Fwd (jjtraveler.Visitor v) {");
       println("this.any = v;");
       println("}");
	}

}
