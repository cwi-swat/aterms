package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;
import apigen.gen.StringConversions;

public class AlternativeGenerator extends JavaGenerator {
	private boolean visitable;
	private Type type;
	private Alternative alt;
	private String apiName;

	private String typeId;
	private String altId;
	private String className;
	private String superClassName;

	protected AlternativeGenerator(GenerationParameters params, Type type, Alternative alt) {
		super(params);
		this.apiName = params.getApiName();
		this.visitable = params.isVisitable();
		this.type = type;
		this.alt = alt;
		this.typeId = type.getId();
		this.altId = alt.getId();
		this.className = buildClassName(typeId, altId);
		this.superClassName = TypeGenerator.className(type);
	}

	public String getClassName() {
		return className;
	}

	private static String buildClassName(String type, String alt) {
		if (getConverter().isReserved(alt)) {
			return alt;
		}
		else {
			return StringConversions.makeCapitalizedIdentifier(alt);
		}
	}

	public static String className(String type, String alt) {
		return buildClassName(type, alt);
	}

	public static String className(Type type, Alternative alt) {
		return className(type.getId(), alt.getId());
	}

	protected void generate() {
		printPackageDecl();
		genAlternativeClassImpl(type, alt);
	}

	private void genAlternativeClassImpl(Type type, Alternative alt) {
		print("public class " + className + " extends " + superClassName);
		if (visitable) {
			print(" implements jjtraveler.Visitable");
		}
		println(" {");

		genInitMethod();
		genInitHashcodeMethod();
		genAltConstructor();
		genAltFieldIndexMembers(type, alt);
		genAltDuplicateMethod(type, alt);
		genAltEquivalentMethod(type, alt);
		genAltMake(type, alt);
		genAltToTerm();
		genOverrideProperties(type, alt);
		genAltGetAndSetMethods(type, alt);
		genAltHashFunction(type, alt);

		if (visitable) {
			genAltVisitableInterface(type, alt);
		}

		println("}");
	}

	private void genInitMethod() {
		println("  protected void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {");
		println("    super.init(hashCode, annos, fun, args);");
		println("  }");
	}

	private void genInitHashcodeMethod() {
		println("  protected void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] i_args) {");
		println("  	super.initHashCode(annos, fun, i_args);");
		println("  }");
	}

	private void genAltConstructor() {
		println("  protected " + className + "(" + FactoryGenerator.className(apiName) + " factory) {");
		println("    super(factory);");
		println("  }");
	}

	private void genAltHashFunction(Type type, Alternative alt) {
		if (!hasReservedTypeFields(type, alt)) {
			int arity = type.getAltArity(alt);
			String goldenratio = "0x9e3779b9";
			int changingArg = guessChangingArgument(type, alt);
			String initval;

			if (changingArg > 0) {
				initval = "getArgument(" + changingArg + ").hashCode()";
			}
			else {
				initval = "0";
			}

			println("  protected int hashFunction() {");
			println("    int c = " + initval + " + (getAnnotations().hashCode()<<8);");
			println("    int a = " + goldenratio + ";");
			println("    int b = (getAFun().hashCode()<<8);");

			/*
			 * This piece of code generates a number of shifts following the
			 * algorithm of the Doobs hash functions (Google: Doobs), you can
			 * find a general implementation in shared.HashFunctions of the
			 * shared-objects library. This code specializes the algorithm for
			 * a fixed number of arguments.
			 */
			for (int i = arity - 1; i >= 0; i--) {
				int shift = (i % 4) * 8;
				println(
					"    "
						+ "aaaabbbbcccc".toCharArray()[i % 12]
						+ " += (getArgument("
						+ i
						+ ").hashCode() << "
						+ shift
						+ ");");
			}

			println();
			println("    a -= b; a -= c; a ^= (c >> 13);");
			println("    b -= c; b -= a; b ^= (a << 8);");
			println("    c -= a; c -= b; c ^= (b >> 13);");
			println("    a -= b; a -= c; a ^= (c >> 12);");
			println("    b -= c; b -= a; b ^= (a << 16);");
			println("    c -= a; c -= b; c ^= (b >> 5);");
			println("    a -= b; a -= c; a ^= (c >> 3);");
			println("    b -= c; b -= a; b ^= (a << 10);");
			println("    c -= a; c -= b; c ^= (b >> 15);");
			println();
			println("    return c;");
			println("  }");
		}
	}

	private int guessChangingArgument(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());

		/*
		 * if an argument has the same type as the result type, there exists a
		 * chance of building a tower of this constructor where only this
		 * argument changes. Therefore, this argument must be very important in
		 * the computation of the hash code in order to avoid collissions
		 */
		for (int i = 0; fields.hasNext(); i++) {
			Field field = (Field) fields.next();

			if (field.getType().equals(type.getId())) {
				return i;
			}
		}

		return -1;
	}

	private String buildGetFactoryMethodCall(String factoryName) {
		return "get" + factoryName + "()";
	}

	private void genAltToTerm() {
		String factoryName = FactoryGenerator.className(apiName);

		println("  public aterm.ATerm toTerm() {");
		println("    if (term == null) {");
		println("      term = " + buildGetFactoryMethodCall(factoryName) + ".toTerm(this);");
		println("    }");
		println("    return term;");
		println("  }");
		println();
	}

	private void genAltFieldIndexMembers(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
		int argnr = 0;

		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			String fieldId = getFieldIndex(field.getId());

			println("  private static int " + fieldId + " = " + argnr + ";");
			argnr++;
		}
	}

	private void genAltGetAndSetMethod(Field field) {
		String fieldName = StringConversions.makeCapitalizedIdentifier(field.getId());
		String fieldId = getFieldId(field.getId());
		String fieldType = field.getType();
		String fieldClass = TypeGenerator.className(fieldType);
		String fieldIndex = getFieldIndex(field.getId());

		genFieldGetterMethod(fieldName, fieldType, fieldClass, fieldIndex);
		println();
		genFieldSetterMethod(fieldName, fieldId, fieldType, fieldClass, fieldIndex);
		println();

	}

	private void genFieldSetterMethod(
		String fieldName,
		String fieldId,
		String fieldType,
		String fieldClass,
		String fieldIndex) {
		// setter
		println("  public " + superClassName + " set" + fieldName + "(" + fieldClass + " " + fieldId + ")");
		println("  {");

		print("    return (" + superClassName + ") super.setArgument(");

		if (fieldType.equals("str")) {
			print("getFactory().makeAppl(getFactory().makeAFun(" + fieldId + ", 0, true))");
		}
		else if (fieldType.equals("int")) {
			print("getFactory().makeInt(" + fieldId + ")");
		}
		else if (fieldType.equals("real")) {
			print("getFactory().makeReal(" + fieldId + ")");
		}
		else {
			print(fieldId);
		}

		println(", " + fieldIndex + ");");

		println("  }");
	}

	private void genFieldGetterMethod(String fieldName, String fieldType, String fieldClass, String fieldIndex) {
		// getter
		println("  public " + fieldClass + " get" + fieldName + "()");
		println("  {");

		if (fieldType.equals("str")) {
			println("   return ((aterm.ATermAppl) this.getArgument(" + fieldIndex + ")).getAFun().getName();");
		}
		else if (fieldType.equals("int")) {
			println("   return ((aterm.ATermInt) this.getArgument(" + fieldIndex + ")).getInt();");
		}
		else if (fieldType.equals("real")) {
			println("   return ((aterm.ATermReal) this.getArgument(" + fieldIndex + ")).getReal();");
		}
		else if (fieldType.equals("term")) {
			println("   return this.getArgument(" + fieldIndex + ");");
		}
		else {
			println("    return (" + fieldClass + ") this.getArgument(" + fieldIndex + ") ;");
		}

		println("  }");
	}

	private void genOverrideProperties(Type type, Alternative alt) {
		genOverrideIsMethod(alt);
		genOverrideHasMethods(type, alt);
	}

	private void genOverrideIsMethod(Alternative alt) {
		println("  public boolean is" + StringConversions.makeCapitalizedIdentifier(alt.getId()) + "()");
		println("  {");
		println("    return true;");
		println("  }");
		println();
	}

	private void genOverrideHasMethods(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());

		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			genOverrideHasMethod(field);
		}
	}

	private void genOverrideHasMethod(Field field) {
		println("  public boolean has" + StringConversions.makeCapitalizedIdentifier(field.getId()) + "()");
		println("  {");
		println("    return true;");
		println("  }");
		println();
	}

	private void genAltGetAndSetMethods(Type type, Alternative alt) {

		Iterator fields = type.altFieldIterator(alt.getId());
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			genAltGetAndSetMethod(field);
		}

		genOverrrideSetArgument(type, alt);
	}

	private void genOverrrideSetArgument(Type type, Alternative alt) {
		String alt_classname = className(type, alt);

		println("  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {");
		if (type.getAltArity(alt) > 0) {
			println("    switch(i) {");

			Iterator fields = type.altFieldIterator(alt.getId());
			for (int i = 0; fields.hasNext(); i++) {
				Field field = (Field) fields.next();
				String field_type = field.getType();
				String field_class = TypeGenerator.className(field_type);

				String instance_of;

				if (field_type.equals("str")) {
					instance_of = "aterm.ATermAppl";
				}
				else if (field_type.equals("int")) {
					instance_of = "aterm.ATermInt";
				}
				else if (field_type.equals("real")) {
					instance_of = "aterm.ATermReal";
				}
				else if (field_type.equals("term")) {
					instance_of = "aterm.ATerm";
				}
				else {
					instance_of = field_class;
				}

				println("      case " + i + ":");
				println("        if (! (arg instanceof " + instance_of + ")) { ");
				println(
					"          throw new RuntimeException(\"Argument "
						+ i
						+ " of a "
						+ alt_classname
						+ " should have type "
						+ field_type
						+ "\");");
				println("        }");
				println("        break;");
			}
			println(
				"      default: throw new RuntimeException(\""
					+ alt_classname
					+ " does not have an argument at \" + i );");
			println("    }");
			println("    return super.setArgument(arg, i);");
		}
		else {
			println("      throw new RuntimeException(\"" + alt_classname + " has no arguments\");");
		}
		println("  }");
	}

	private void genAltMake(Type type, Alternative alt) {
		String altClassName = className(type, alt);

		println("  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] i_args," + " aterm.ATermList annos) {");
		println(
			"    return get"
				+ FactoryGenerator.className(apiName)
				+ "().make"
				+ altClassName
				+ "(fun, i_args, annos);");
		println("  }");
	}

	private void genAltDuplicateMethod(Type type, Alternative alt) {
		String altClassName = className(type, alt);

		println("  public shared.SharedObject duplicate() {");
		println("    " + altClassName + " clone = new " + altClassName + "(factory);");
		println("     clone.init(hashCode(), getAnnotations(), getAFun(), " + "getArgumentArray());");
		println("    return clone;");
		println("  }");
		println();
	}

	private void genAltEquivalentMethod(Type type, Alternative alt) {
		println("  public boolean equivalent(shared.SharedObject peer) {");
		println("    if (peer instanceof " + className(type, alt) + ") {");
		println("      return super.equivalent(peer);");
		println("    }");
		println("    return false;");
		println("  }");
	}

	private void genAltVisitableInterface(Type type, Alternative alt) {
		String altClassName = className(type, alt);

		println("  public void accept(Visitor v) throws jjtraveler.VisitFailure");
		println("  {");
		println("    v.visit_" + altClassName + "(this);");
		println("  }");
		println();
	}

	private int computeAltArityNotReserved(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
		int count = 0;
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			if (!getConverter().isReserved(field.getType())) {
				count++;
			}
		}
		return count;
	}

	private boolean hasReservedTypeFields(Type type, Alternative alt) {
		return computeAltArityNotReserved(type, alt) < type.getAltArity(alt);
	}
}
