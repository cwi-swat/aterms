package apigen.gen.java;

import java.util.Set;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.adt.api.types.Module;

public class ForwardVisitableGenerator extends JavaGenerator {
	private static final String CLASS_NAME = "VisitableFwd";

	private ADT adt;
	private String apiName;
    private Module module;
    private String traveler;
    
	public ForwardVisitableGenerator(ADT adt, JavaGenerationParameters params, Module module) {
		super(params);
		this.adt = adt;
		this.module = module;
		this.apiName = params.getApiExtName(module);
		this.traveler = params.getTravelerName();
	}

	public String getClassName() {
		return module.getModulename().getName() + CLASS_NAME;
	}

	public String getVisitorName() {
	    return module.getModulename().getName() + VisitorGenerator.CLASS_NAME;
	}
	
	protected void generate() {
		printPackageDecl();

		println("public class " + getClassName() + " extends " + module.getModulename().getName() + "Fwd implements " + traveler + ".reflective.VisitableVisitor {");
		println("public int getChildCount() {");
		println("    return 1;");
		println("  }");
		println();
		println("  public " + traveler + ".Visitable getChildAt(int i) {");
		println("    switch (i) {");
		println("    case 0: return (" + traveler + ".Visitable) any;");
		println("    default: throw new IndexOutOfBoundsException();");
		println("    }");
		println("  }");
		println();
		println("  public " + traveler + ".Visitable setChildAt(int i, " + traveler + ".Visitable child) {");
		println("    switch (i) {");
		println("    case 0: any = (" + traveler + ".reflective.VisitableVisitor) child; return this;");
		println("    default: throw new IndexOutOfBoundsException();");
		println("    }");
		println("  }");
		println();
		println("  public " + getClassName() + "(" + traveler + ".reflective.VisitableVisitor any) {");
		println("    super(any);");
		println("  }");
		println("}");
	}


	public String getPackageName() {
		return apiName.toLowerCase();
	}

	public String getQualifiedClassName() {
		return getClassName();
	}
}
