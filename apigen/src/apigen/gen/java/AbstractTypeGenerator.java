package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Type;
import apigen.adt.api.types.Module;
import apigen.gen.GenerationParameters;
import apigen.gen.StringConversions;
import apigen.gen.java.FactoryGenerator;

public class AbstractTypeGenerator extends JavaGenerator {
    private boolean visitable;
    private String apiName;
    private String factoryClassName;
    private Module module;
    private ADT adt;

    public AbstractTypeGenerator(ADT adt, JavaGenerationParameters params, Module module) {
        super(params);
        this.adt = adt;
        this.apiName = params.getApiExtName(module);
        this.module = module;
        this.factoryClassName = FactoryGenerator.qualifiedClassName(getJavaGenerationParameters(), module.getModulename().getName());
        this.visitable = params.isVisitable();
    }

    public String getClassName() {
        return className();
    }

    public String className() {
    	return AbstractTypeGenerator.className(module.getModulename().getName());
    }
    
    public static String className(String moduleName) {
        return moduleName + "AbstractType";
    }

    public String qualifiedClassName(JavaGenerationParameters params) {
    	return AbstractTypeGenerator.qualifiedClassName(params,module.getModulename().getName());
    }
    
    public static String qualifiedClassName(JavaGenerationParameters params, String moduleName) {
        StringBuffer buf = new StringBuffer();
        String pkg = params.getPackageName();

        if (pkg != null) {
            buf.append(pkg);
            buf.append('.');
        }
        buf.append(params.getApiExtName(moduleName).toLowerCase());
        buf.append('.');
        buf.append(AbstractTypeGenerator.className(moduleName));
        return buf.toString();
    }

    protected void generate() {
        printPackageDecl();
        genAbstractTypeClass();
    }

    private void genAbstractTypeClass() {
        println(
            "abstract public class "
                + getClassName()
                + " extends aterm.pure.ATermApplImpl {");
        genClassVariables();
        genConstructor();
        genInitMethod();
        genInitHashcodeMethod();
        genToTermMethod();
        genToStringMethod();
        genSetTermMethod();
        genGetFactoryMethod();
        genDefaultTypePredicates();

        if (visitable) {
            genAccept();
        }
        println("}");
    }

    private void genClassVariables() {
        println("  protected aterm.ATerm term;");
        println();
        println("  private " + factoryClassName + " abstractTypeFactory;");
        println();
    }

    private void genConstructor() {
        println("  public " + getClassName() + "(" + factoryClassName + " abstractTypeFactory) {");
        println("    super(abstractTypeFactory.getPureFactory());");
        println("    this.abstractTypeFactory = abstractTypeFactory;");
        println("  }");
        println();
    }

    private void genInitMethod() {
        println("  public void init(int hashCode, aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {");
        println("    super.init(hashCode, annos, fun, args);");
        println("  }");
        println();
    }

    private void genInitHashcodeMethod() {
        println("  public void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {");
        println("    super.initHashCode(annos, fun, args);");
        println("  }");
        println();
    }

    private void genGetFactoryMethod() {
        GenerationParameters params = getGenerationParameters();
        println(
            "  public " + factoryClassName + ' ' + getFactoryMethodName(params,module.getModulename().getName()) + "() {");
        println("    return abstractTypeFactory;");
        println("  }");
        println();
    }

    private void genSetTermMethod() {
        println("  protected void setTerm(aterm.ATerm term) {");
        println("    this.term = term;");
        println("  }");
        println();
    }

    private void genToStringMethod() {
        println("  public String toString() {");
        println("    return toTerm().toString();");
        println("  }");
        println();
    }

    private void genToTermMethod() {
        println("  abstract public aterm.ATerm toTerm();");
        println();
    }

    private void genAccept() {
        String visitorPackage =
            VisitorGenerator.qualifiedClassName(getJavaGenerationParameters());
        println(
            "  abstract public void accept("
                + visitorPackage
                + ".Visitor v) throws jjtraveler.VisitFailure;");
        println();
    }

    private void genDefaultTypePredicates() {
        Iterator types = adt.typeIterator();
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

    public String getPackageName() {
        return apiName.toLowerCase();
    }

    public String getQualifiedClassName() {
        return getClassName();
    }

    public static String getFactoryMethodName(GenerationParameters params, String moduleName) {
        String apiName = params.getApiName();
        if (!apiName.equals("")) {
            apiName = StringConversions.capitalize(params.getApiName());
        }
        return "get" + apiName + FactoryGenerator.className(moduleName);
    }
}
