package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.StringConversions;
import aterm.ATermFactory;

public class ListTypeImplGenerator extends TypeImplGenerator {
	protected String typeId;
	protected String typeName;
	protected String elementTypeId;
	protected String elementTypeName;

	public ListTypeImplGenerator(
		ListType type,
		String directory,
		String pkg,
		String apiName,
		List standardImports,
		boolean verbose) {
		super(type, directory, pkg, apiName, standardImports, verbose);

		this.typeName = className(type);
		this.typeId = StringConversions.makeCapitalizedIdentifier(type.getId());
		this.elementTypeName = TypeGenerator.className(type.getElementType());
		this.elementTypeId = StringConversions.makeCapitalizedIdentifier(type.getElementType());
	}

	protected void generate() {
		printPackageDecl();
		imports.add("java.io.InputStream");
		imports.add("java.io.IOException");

		printImports();
		println();
		genListTypeClassImpl();
	}

	public static String className(Type type) {
		return TypeImplGenerator.className(type);
	}

	protected String classModifier() {
		return "abstract public";
	}

	private void genListTypeClassImpl() {
		println(classModifier() + " class " + typeName + " extends aterm.pure.ATermListImpl");
		println("{");
		genTermField();
		genFromString(typeName);
		genFromTextFile(typeName);
		genFromTerm();
		genToTerm();
		genToString();
		genGetters();
		genPredicates();
		genGetStaticFactory();
		genSharedObjectInterface();
		genInsertMethod();
		genReverseMethod();
		println("}");
	}
	
	private void genReverseMethod() {
		String className = ListTypeGenerator.className(type);
		
	    println("  public aterm.ATermList reverse() {");
		println("  	 " + typeName + " cur = this;");
		println("  	 " + typeName + " reverse = ("+ typeName +") " + staticFactoryGetter() +".make" + className + "();");
		println("  	 while(!cur.isEmpty()){");
		println("  	   reverse = (" + typeName + ")reverse.insert((aterm.ATerm) cur.getHead());");
		println("  	   cur = cur.getTail();");
		println("  	 }");
		println("  	 return reverse;");
		println("  }");
	}

	private void genInsertMethod() {
		String className = ListTypeGenerator.className(type);
		println("  public aterm.ATermList insert(aterm.ATerm head) {");
		println("    return (aterm.ATermList)" + staticFactoryGetter() + ".make" + className + "((" + elementTypeName + ") head, (" + className + ") this);");
		//println("  public " + className + " insert(" + elementTypeName + " head) {");
		//println("    return " + staticFactoryGetter() + ".make" + className + "(head, (" + className + ") this);");
		println("  }");
		
	}

	private void genPredicates() {
		genIsTypeMethod(type);
		genIsAlternativeMethods();
		genHasPredicates();
	}

	private void genHasPredicates() {
		genHasHeadMethod();
		genHasTailMethod();
	}
	
	private void genHasTailMethod() {
		println("  public boolean hasTail() {");
		println("    return !isEmpty();");
		println("  }");
	}

	private void genHasHeadMethod() {
		println("  public boolean hasHead() {");
		println("    return !isEmpty();");
		println("  }");
	}


	private void genSharedObjectInterface() {
		genEquivalentMethod();
		genDuplicateMethod();
	}

	private void genDuplicateMethod() {
		String className = ListTypeGenerator.className(type);
		println("  public shared.SharedObject duplicate() {");
		println("	 " + className + " clone = new " + className + "();");
		println("	 clone.init(hashCode(), getAnnotations(), getFirst(), getNext());");
		println("	 return clone;");
		println("  }");
	}

	private void genEquivalentMethod() {
		String className = ListTypeGenerator.className(type);
		
		println("  public boolean equivalent(shared.SharedObject peer) {");
		println("	 if (peer instanceof " + className + ") {");
		println("	 	return super.equivalent(peer);");
		println("	 }");
		println("	 else {");
		println("      return false;");
		println("	 }");
		println("  }");
	}

	private void genGetStaticFactory() {
		String factoryName = FactoryGenerator.className(apiName);
		
		println("  public " + factoryName + " get" + factoryName + "() {");
		println("    return (" + factoryName + ") getFactory();");
		println("  }");
		println();
		println("  static protected " + factoryName + " getStatic" + factoryName + "() {");
		println("    return (" + factoryName + ") getStaticFactory();");
		println("  }");
	}
	
	private String staticFactoryGetter() {
		return "getStatic" + FactoryGenerator.className(apiName) + "()";
	}

	private void genGetters() {
		genGetHead();
		genGetTail();
	}

	private void genGetTail() {
		String className = ListTypeGenerator.className(type);
		
		println("  public " + className + " getTail() {");
		println("    return (" + className + ") getNext();");
		println("  }");
	}

	private void genGetHead() {
		println("  public " + elementTypeName + " getHead() {");
		println("    return (" + elementTypeName + ") getFirst();");
		println("  }");
	}

	private void genIsAlternativeMethods() {
		String className = ListTypeGenerator.className(type);
		println("  public boolean isEmpty() {");
		println("    return this == " + FactoryGenerator.className(apiName) + ".empty" + className + ";");
		println("  }");
		println("  public boolean isMany() {");
		println("    return !isEmpty();");
		println("  }");
	}

	private void genFromTerm() {
		String get_factory = staticFactoryGetter();
		String className = ListTypeGenerator.className(type);

		println("  public static " + className + " fromTerm(aterm.ATerm trm)");
		println("  {");
		println("     if (trm instanceof aterm.ATermList) {");
		println("        aterm.ATermList list = ((aterm.ATermList) trm).reverse();");
		println("        " + className + " tmp = (" + className + ") " + get_factory + ".make" + className + "();");
		println("        for (; !list.isEmpty(); list = list.getNext()) {");
		println("          " + elementTypeName + " elem = " + elementTypeName + ".fromTerm(list.getFirst());");
		println("           if (elem != null) {");
		println("             tmp = " + get_factory + ".make" + className + "(elem, tmp);");
		println("           }");
		println("           else {");
		println("             throw new RuntimeException(\"Invalid element in " + className + ":\" + tmp);");
		println("           }");
		println("        }");
		println("        tmp.setTerm(trm);");
		println("        return tmp;");
		println("     }");
		println("     else {");
		println("       throw new RuntimeException(\"This is not a " + className + ":\" + trm);");
		println("     }");
		println("  }");
		
	}

	private void genToTerm() {
		String get_factory = staticFactoryGetter();
		String className = ListTypeGenerator.className(type);

		println("  public aterm.ATerm toTerm()");
		println("  {");
		println("    if (this.term == null) {");
		println("      " + className + " reversed = ("+ className +")this.reverse();");
		println("      aterm.ATermList tmp = " + get_factory + ".makeList();");
		println("      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {");
		println("         aterm.ATerm elem = reversed.getHead().toTerm();");
		println("         tmp = " + get_factory + ".makeList(elem, tmp);");
		println("      }");
		println("      this.term = tmp;");
		println("    }");
		println("    return this.term;");
		println("  }");
	}

    private void genTermField() {
    	println("  protected aterm.ATerm term = null;");
    	println("  protected void setTerm(aterm.ATerm term) {");
    	println("    this.term = term;");
    	println("  }");
    }
    
	private void genToString() {
		println("  public String toString() {");
		println("    return toTerm().toString();");
		println("  }");
	}

}
