package apigen.gen.java;

import java.util.Iterator;
import java.util.Set;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.StringConversions;

public class ListTypeGenerator extends TypeGenerator {
	private ADT adt;
	private ListType type;
	private String typeName;
	private String elementTypeName;
	private String factory;
	private String elementType;
	private String abstractListPackage;
	private String traveler;
	private boolean hasGlobalName;
	
	public ListTypeGenerator(ADT adt, JavaGenerationParameters params, ListType type) {
		super(params, type);
		this.adt = adt;
		this.type = type;
		this.typeName = TypeGenerator.className(type);
		this.elementType = type.getElementType();
		this.elementTypeName = TypeGenerator.qualifiedClassName(params, elementType);
		this.factory = FactoryGenerator.qualifiedClassName(params,type.getModuleName());
		this.abstractListPackage = AbstractListGenerator.qualifiedClassName(getJavaGenerationParameters(),type.getModuleName());
		this.traveler = params.getTravelerName(); 
		this.hasGlobalName = !(params.getApiName()).equals("");
	}

	public String getTypeName() {
		return typeName;
	}

	public String getFactory() {
		return factory;
	}

	protected void generate() {
		printPackageDecl();
		printImports();
		genListTypeClass();
	}

	public static String className(Type type) {
		return TypeGenerator.className(type);
	}

	private void genListTypeClass() {
		boolean visitable = getJavaGenerationParameters().isVisitable();
        print("public class " + typeName + " extends " + abstractListPackage);
        if (visitable) {
            print(" implements " + traveler + ".Visitable");
        }
        println(" {");
        
        genTermField();
		genConstructor(className(type));
		genSharedObjectInterface();
		genToTerm();
		genIsTypeMethod(type);
		genGetters();
		genInsertMethods();
		genReverseMethods();
		genConcatMethods();
		genAppendMethods();
		genElementAtMethod();
    if (visitable) {
      genVisitableInterface();
    }
		println("}");
	}

	private void genTermField() {
		println("  private aterm.ATerm term = null;");
	}

	protected void genConstructor(String className) {
		println("  public " + className + "(" + factory + " factory) {");
		println("     super(factory);");
		println("  }");
		println();
	}

	private void genSharedObjectInterface() {
		genEquivalentMethod();
		genDuplicateMethod();
	}
	
	private void genEquivalentMethod() {
		String className = TypeGenerator.className(type);

		println("  public boolean equivalent(shared.SharedObject peer) {");
		println("    if (peer instanceof " + className + ") {");
		println("      return super.equivalent(peer);");
		println("    } else {");
		println("      return false;");
		println("    }");
		println("  }");
		println();
	}

	private void genDuplicateMethod() {
		String className = TypeGenerator.className(type);
		println("  public shared.SharedObject duplicate() {");
		println("    " + className + " clone = new " + className + "(" + buildFactoryGetter() + ");");
		println("    clone.init(hashCode(), getAnnotations(), getFirst(), getNext());");
		println("    return clone;");
		println("  }");
		println();
	}

	private void genToTerm() {
		String getFactoryMethodName = buildFactoryGetter();
		String className = TypeGenerator.className(type);

		println("  public aterm.ATerm toTerm() {");
		println("    aterm.ATermFactory atermFactory = " + getFactoryMethodName + ".getPureFactory();");
		println("    if (this.term == null) {");
		println("      " + className + " reversed = (" + className + ")this.reverse();");
		println("      aterm.ATermList tmp = atermFactory.makeList();");
		println("      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {");

		String head = "reversed.getHead()";
		String termHead;
		if (!getConverter().isReserved(elementType)) {
			termHead = head + ".toTerm()";
		}
		else {
			termHead = getConverter().makeBuiltinToATermConversion(elementType, head);
		}
		println("        aterm.ATerm elem = " + termHead + ";");

		println("        tmp = atermFactory.makeList(elem, tmp);");
		println("      }");
		println("      this.term = tmp;");
		println("    }");
		println("    return this.term;");
		println("  }");
		println();
	}


	private void genGetters() {
		genGetHead();
		genGetTail();
		genGetEmptyMethod();
	}

	private void genGetHead() {
		println("  public " + elementTypeName + " getHead() {");
		String convertedValue = getConverter().makeATermToBuiltinConversion(elementType, "getFirst()");
		println("    return (" + elementTypeName + ")" + convertedValue + ";");
		println("  }");
		println();
	}

	private void genGetTail() {
		String className = TypeGenerator.className(type);

		println("  public " + className + " getTail() {");
		println("    return (" + className + ") getNext();");
		println("  }");
		println();
	}

	private void genGetEmptyMethod() {
		String className = TypeGenerator.className(type);
		println("  public aterm.ATermList getEmpty() {");
		println("    return (aterm.ATermList)" + buildFactoryGetter() + ".make" + className + "();");
		println("  }");
		println();
	}

	private void genInsertMethods() {
		if (!type.getElementType().equals("term")) {
			genInsertMethod();
		}
		genOverrideInsertMethod();
	}

	private void genInsertMethod() {
		println("  public " + typeName + " insert(" + elementTypeName + " head) {");
		println("    return " + buildFactoryGetter() + ".make" + typeName + "(head, (" + typeName + ") this);");
		println("  }");
		println();
	}

	private void genOverrideInsertMethod() {
		String head = getConverter().makeATermToBuiltinConversion(elementType, "head");
		println("  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {");
		println("    return " + buildFactoryGetter() + ".make" + typeName + "(head, tail, annos);");
		println("  }");
		println();
		println("  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail) {");
		println("    return make(head, tail, " + buildFactoryGetter() + ".getPureFactory().getEmpty());");
		println("  }");
		println();
		println("  public aterm.ATermList insert(aterm.ATerm head) {");
		println("    return make(head, this);");
		println("  }");
		println();
	}
	
	private void genAppendMethods() {
		if (!type.getElementType().equals("term")) {
			genAppendMethod();
		}
		genOverrideAppendMethod();
	}

	private void genOverrideAppendMethod() {
		String elemName = "elem";
		String elem = getConverter().makeATermToBuiltinConversion(elementType, elemName);
		println("  public aterm.ATermList append(aterm.ATerm " + elemName + ") {");
		println("    return append((" + elementTypeName + ") " + elem + ");");
		println("  }");
		println();
	}

	private void genAppendMethod() {
		println("  public " + typeName + " append(" + elementTypeName + " elem) {");
		println("    return " + buildFactoryGetter() + ".append(this, elem);");
		println("  }");
		println();
	}

	private void genConcatMethods() {
		genConcatMethod();
		genOverrideConcatMethod();
	}

	private void genOverrideConcatMethod() {
		println("  public aterm.ATermList concat(aterm.ATermList peer) {");
		println("    return concat((" + typeName + ") peer);");
		println("  }");
		println();
	}

	private void genConcatMethod() {
		println("  public " + typeName + " concat(" + typeName + " peer) {");
		println("    return " + buildFactoryGetter() + ".concat(this, peer);");
		println("  }");
		println();
	}

	private void genReverseMethods() {
		genReverseMethod();
		genOverrideReverseMethod();
	}

	private void genOverrideReverseMethod() {
		println("  public aterm.ATermList reverse() {");
		println("    return reverse" + typeName + "();");
		println("  }");
		println();
	}

	private void genReverseMethod() {
		println("  public " + typeName + " reverse" + typeName + "() {");
		println("    return " + buildFactoryGetter() + ".reverse(this);");
		println("  }");
		println();
	}
	
	private void genElementAtMethod() {
		String elementName = StringConversions.makeCapitalizedIdentifier(elementType);
		String converted = getConverter().makeATermToBuiltinConversion(elementType, "elementAt(index)");
		println("  public " + elementTypeName + " get" + elementName + "At(int index) {");
		println("    return (" + elementTypeName + ") " + converted + ";");
		println("  }");
		println();
	}

	private void genVisitableInterface() {
		Iterator moduleIt;
        if (hasGlobalName) {
          moduleIt = adt.getModuleNameSet().iterator();
        } else {
          Set moduleToGen = adt.getImportsClosureForModule(type.getModuleName());
          moduleIt = moduleToGen.iterator();
        }

      	while(moduleIt.hasNext()) {
       	    String moduleName = (String) moduleIt.next();
       	    String visitorPackage = VisitorGenerator.qualifiedClassName(getJavaGenerationParameters(),moduleName);
       	    String className = TypeGenerator.className(type);
       	    String abstractListPackage = AbstractListGenerator.qualifiedClassName(getJavaGenerationParameters(),type.getModuleName());
       	 	
       	    println("  public " + abstractListPackage + " accept("
       	            + visitorPackage + " v) throws " + traveler + ".VisitFailure {");
       	    println("    return v.visit_" + className + "(this);");
       	    println("  }");
 		
       	    //String className = TypeGenerator.className(type);
       	    //String visitorPackage = VisitorGenerator.qualifiedClassName(getJavaGenerationParameters(),type.getModuleName());
       	    println();
      	}
	}
	
	private String buildFactoryGetter() {
		return AbstractListGenerator.getFactoryMethodName(getGenerationParameters(),type.getModuleName()) + "()";
	}
	
}
