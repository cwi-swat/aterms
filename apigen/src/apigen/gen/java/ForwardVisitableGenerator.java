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
	private String constructor;

    private Module module;

	public ForwardVisitableGenerator(ADT adt, JavaGenerationParameters params, Module module) {
		super(params);
		this.adt = adt;
		this.module = module;
		this.constructor = AbstractTypeGenerator.qualifiedClassName(params,module.getModulename().getName());
	}

	public String getClassName() {
		return module.getModulename().getName() + CLASS_NAME;
	}
	
	public String getVisitorName() {
	    return module.getModulename().getName() + VisitorGenerator.CLASS_NAME;
	}
	
	protected void generate() {
		printPackageDecl();

		println("public class " + getClassName() + " extends Fwd implements jjtraveler.reflective.VisitableVisitor {");
		println("public int getChildCount() {");
		println("    return 1;");
		println("  }");
		println();
		println("  public jjtraveler.Visitable getChildAt(int i) {");
		println("    switch (i) {");
		println("    case 0: return (jjtraveler.Visitable) any;");
		println("    default: throw new IndexOutOfBoundsException();");
		println("    }");
		println("  }");
		println();
		println("  public jjtraveler.Visitable setChildAt(int i, jjtraveler.Visitable child) {");
		println("    switch (i) {");
		println("    case 0: any = (jjtraveler.reflective.VisitableVisitor) child; return this;");
		println("    default: throw new IndexOutOfBoundsException();");
		println("    }");
		println("  }");
		println();
		println("  public VisitableFwd(jjtraveler.reflective.VisitableVisitor any) {");
		println("    super(any);");
		println("  }");
		println("}");
	}


	public String getPackageName() {
		return getGenerationParameters().getApiName().toLowerCase();
	}

	public String getQualifiedClassName() {
		return getClassName();
	}
}
