package apigen.gen.java;

import java.util.Iterator;
import java.util.Set;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.adt.api.types.Module;

public class ForwardGenerator extends JavaGenerator {
	private static final String CLASS_NAME = "Fwd";

	private ADT adt;
	private String apiName;
    private Module module;

	public ForwardGenerator(ADT adt, JavaGenerationParameters params, Module module) {
		super(params);
		this.adt = adt;
		this.module = module;
		this.apiName = params.getApiExtName(module);
	}

	public String getClassName() {
		return module.getModulename().getName() + CLASS_NAME;
	}
	
	public String getVisitorName() {
	    return module.getModulename().getName() + VisitorGenerator.CLASS_NAME;
	}

	protected void genVisits(ADT adt) {
		Iterator types = adt.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			Iterator alts = type.alternativeIterator();

			if (type instanceof ListType) {
				genListVisit(type);
			} else {
				genTypeVisit(type);
				while (alts.hasNext()) {
					Alternative alt = (Alternative) alts.next();
					genVisit(type, alt);
				}
			}
		}
	}

	private void genListVisit(Type type) {
		genTypeVisit(type);
	}

	private void genVisit(Type type, Alternative alt) {
		String methodName = FactoryGenerator.concatTypeAlt(type, alt);
		String paramType = AlternativeGenerator.qualifiedClassName(getJavaGenerationParameters(), type, alt);
		String returnType = TypeGenerator.qualifiedClassName(getJavaGenerationParameters(), TypeGenerator.className(type));
		String typeMethodName = TypeGenerator.className(type);
		
		println("  public " + returnType + " visit_" + methodName + "(" + paramType + " arg) throws jjtraveler.VisitFailure {");
		println("    return visit_" + typeMethodName +"(arg);");
		println("  }");
		println();
		//genVisitMethod(methodName, paramType, returnType);
	}

	private void genTypeVisit(Type type) {
		String methodName = TypeGenerator.className(type);
		String typeName = TypeGenerator.qualifiedClassName(getJavaGenerationParameters(), TypeGenerator.className(type));
		
		println("  public " + typeName + " visit_" + methodName + "(" + typeName + " arg) throws jjtraveler.VisitFailure {");
		println("    return (" + typeName + ") any.visit(arg);");
		println("  }");
		println();
		
	}
	
	protected void generate() {
		printPackageDecl();

		println("public class " + getClassName() + " implements " + getVisitorName() + ", jjtraveler.Visitor {");
		genConstructor();
		genDefaultVisit();
		genVisits(adt);
		println("}");
	}

	private void genConstructor() {
		println("  protected jjtraveler.Visitor any;");
		println();
		println("  public " + getClassName() + "(jjtraveler.Visitor v) {");
		println("    this.any = v;");
		println("  }");
		println();
	}

	private void genDefaultVisit() {
		
		println("  public jjtraveler.Visitable visit(jjtraveler.Visitable v) throws jjtraveler.VisitFailure {");
		String prefixIf = "";
		Set moduleToGen = adt.getImportsClosureForModule(module.getModulename().getName());
       	Iterator moduleIt = moduleToGen.iterator();
       	while(moduleIt.hasNext()) {
       	    String moduleName = (String) moduleIt.next();
       	    String abstractTypePackage = AbstractTypeGenerator.qualifiedClassName(getJavaGenerationParameters(),moduleName);
       	
		println("    " + prefixIf + "if (v instanceof " + abstractTypePackage + ") {");
		println("      return ((" + abstractTypePackage + ") v).accept(this);");
			prefixIf = "} else ";
		}
		println("    } else {");
		println("      return any.visit(v);");
		println("    }");
		println("  }");
		println();
		
	}

	public String getPackageName() {
		return apiName.toLowerCase();
	}

	public String getQualifiedClassName() {
		return getClassName();
	}
}
