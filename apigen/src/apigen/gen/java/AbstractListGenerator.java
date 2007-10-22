package apigen.gen.java;

import java.util.Iterator;
import java.util.Set;

import apigen.adt.ADT;
import apigen.adt.Type;
import apigen.adt.api.types.Module;
import apigen.gen.GenerationParameters;
import apigen.gen.StringConversions;
import apigen.gen.java.FactoryGenerator;

public class AbstractListGenerator extends JavaGenerator {
  private boolean visitable;
  private String apiName;
  private String factoryClassName;
  private Module module;
  private boolean hasGlobalName;
  private ADT adt;
  private String traveler;
    
    public AbstractListGenerator(ADT adt, JavaGenerationParameters params, Module module) {
        super(params);
        this.adt = adt;
        this.apiName = params.getApiExtName(module);
        this.hasGlobalName = !(params.getApiName()).equals("");
        this.module = module;
        this.factoryClassName = FactoryGenerator.qualifiedClassName(getJavaGenerationParameters(), module.getModulename().getName());
        this.visitable = params.isVisitable();
        this.traveler = params.getTravelerName();
    }

    public String getClassName() {
        return className();
    }

    public String className() {
    		return AbstractListGenerator.className(module.getModulename().getName());
    }
    
    public static String className(String moduleName) {
        return moduleName + "AbstractList";
    }

    public String qualifiedClassName(JavaGenerationParameters params) {
    		return AbstractListGenerator.qualifiedClassName(params,module.getModulename().getName());
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
        buf.append(AbstractListGenerator.className(moduleName));
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
                + " extends aterm.pure.ATermListImpl {");
        genClassVariables();
        genConstructor();
        genToTermMethod();
        genToStringMethod();
        genGetFactoryMethod();
        genDefaultTypePredicates();
        genPredicates();

        if (visitable) {
            genAccept();
        }
        println("}");
    }

    private void genClassVariables() {
        println("  private " + factoryClassName + " abstractTypeFactory;");
        println();
    }

    private void genConstructor() {
        println("  public " + getClassName() + "(" + factoryClassName + " abstractTypeFactory, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {");
        println("    super(abstractTypeFactory.getPureFactory(), annos, first, next);");
        println("    this.abstractTypeFactory = abstractTypeFactory;");
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
        Iterator moduleIt;
        if (hasGlobalName) {
          moduleIt = adt.getModuleNameSet().iterator();
        } else {
          Set moduleToGen = adt.getImportsClosureForModule(module.getModulename().getName());
          moduleIt = moduleToGen.iterator();
        }

       	while(moduleIt.hasNext()) {
       	    String moduleName = (String) moduleIt.next();
       	    String visitorPackage = VisitorGenerator.qualifiedClassName(getJavaGenerationParameters(),moduleName);
       	    println(
            "  abstract public " + getClassName() + " accept("
                + visitorPackage
                + " v) throws " + traveler + ".VisitFailure;");
       	    println();
       	}
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

	private void genPredicates() {
		genIsAlternativeMethods();
		genHasPredicates();
	}

	private void genIsAlternativeMethods() {
		genIsEmpty();
		genIsMany();
		genIsSingle();
	}

	private void genIsEmpty() {
		println("  public boolean isEmpty() {");
		println("    return getFirst()==getEmpty().getFirst() && getNext()==getEmpty().getNext();");
		println("  }");
		println();
	}

	private void genIsMany() {
		println("  public boolean isMany() {");
		println("    return !isEmpty();");
		println("  }");
		println();
	}

	private void genIsSingle() {
		println("  public boolean isSingle() {");
		println("    return !isEmpty() && getNext().isEmpty();");
		println("  }");
		println();
	}

	private void genHasPredicates() {
		genHasHeadMethod();
		genHasTailMethod();
	}

	private void genHasTailMethod() {
		println("  public boolean hasTail() {");
		println("    return !isEmpty();");
		println("  }");
		println();
	}

	private void genHasHeadMethod() {
		println("  public boolean hasHead() {");
		println("    return !isEmpty();");
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
