package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.*;
import apigen.gen.StringConversions;

public class FactoryGenerator extends JavaGenerator {
	private String className;
	private ADT adt;

	public FactoryGenerator(
		ADT adt,
		String directory,
		String apiName,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, className(apiName), pkg, standardImports, verbose);
		this.className = className(apiName);
		this.adt = adt;
	}

	public static String className(String apiName) {
		return StringConversions.makeCapitalizedIdentifier(apiName) + "Factory";
	}

	protected void generate() {
		printPackageDecl();

		imports.add("aterm.pure.PureFactory");
		printImports();

		genFactoryClass(adt);
	}

	private void genFactoryClass(ADT api) {
		println("public class " + className + " extends PureFactory");
		println("{");
		genFactoryPrivateMembers(api);
		genFactoryEmptyLists(api);
		genFactoryConstructor(api);
		genFactoryConstructorWithSize(api);
		genFactoryInitialize(api);
		genFactoryMakeMethods(api);
		genFactoryMakeLists(api);
		println("}");
	}

	private void genFactoryInitialize(ADT api) {
		println("  private void initialize()");
		println("  {");
		genFactoryInitialization(api);
		println("  }");
	}

	private void genFactoryConstructorWithSize(ADT api) {
		println("  public " + className + "(int logSize)");
		println("  {");
		println("     super(logSize);");
		genFactoryConstructorBody(api);
		println("  }");
	}

	private void genFactoryConstructorBody(ADT api) {
		println("     initialize();");
	}

	private void genFactoryConstructor(ADT api) {
		println("  public " + className + "()");
		println("  {");
		println("     super();");
		genFactoryConstructorBody(api);
		println("  }");
	}

	private void genFactoryEmptyLists(ADT api) {
		Iterator types = api.typeIterator();

		while (types.hasNext()) {
			Type type = (Type) types.next();
			if (type instanceof ListType) {
				String className = ListTypeGenerator.className(type);
				println("  static protected " + className + " empty" + className + ";");
			}
		}

	}

	private void genFactoryMakeLists(ADT api) {
		Iterator types = api.typeIterator();

		while (types.hasNext()) {
			Type type = (Type) types.next();
			if (type instanceof ListType) {
				String className = ListTypeGenerator.className(type);
				String elementClassName = TypeGenerator.className(((ListType) type).getElementType());
				String empty = "empty" + className;
				
				println("  public " + className + " make" + className + "() {");
				println("    return " + empty + ";");
				println("  }");
				println("  public " + className + " make" + className + "(" + elementClassName + " elem ) {");
				println("    return (" + className + ") make" + className + "(elem, " + empty + ");");
				println("  }");
				println("  public " + className + " make" + className + "(" + elementClassName + " head, " + className + " tail) {");
				println("    return (" + className + ") make" + className + "((aterm.ATerm) head, (aterm.ATermList) tail, empty);");
				println("  }");
				println("  public " + className + " make" + className + "(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {");
				println("    synchronized (proto" + className +") {");
				println("      proto" + className + ".initHashCode(annos,head,tail);");
				println("      return (" + className +") build(proto" + className + ");");
				println("    }");
				println("  }");
			}
		}

	}

	private void genFactoryPrivateMembers(ADT api) {

		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			String typeClassName = TypeGenerator.className(type.getId());
			Iterator alts = type.alternativeIterator();

			if (type instanceof ListType) {
				String protoVar = "proto" + typeClassName;
				println("  private " + typeClassName + " " + protoVar + ";");
			} else {
				while (alts.hasNext()) {
					Alternative alt = (Alternative) alts.next();
					String altClassName = AlternativeGenerator.className(type, alt);

					String protoVar = "proto" + altClassName;
					String funVar = "fun" + altClassName;

					println("  private aterm.AFun " + funVar + ";");
					println("  private " + typeClassName + " " + protoVar + ";");
				}
			}
		}
	}

	private void genFactoryMakeMethods(ADT api) {
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();

			if (type instanceof ListType) {
				continue;
			}

			Iterator alts = type.alternativeIterator();
			while (alts.hasNext()) {
				Alternative alt = (Alternative) alts.next();
				String altClassName = AlternativeGenerator.className(type, alt);
				String protoVar = "proto" + altClassName;
				String funVar = "fun" + altClassName;

				println(
					"  protected "
						+ altClassName
						+ " make"
						+ altClassName
						+ "(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {");
				println("    synchronized (" + protoVar + ") {");
				println("      " + protoVar + ".initHashCode(annos,fun,args);");
				println("      return (" + altClassName + ") build(" + protoVar + ");");
				println("    }");
				println("  }");
				println();
				print("  public " + altClassName + " make" + altClassName + "(");
				printFormalTypedAltArgumentList(type, alt);
				println(") {");
				print("    aterm.ATerm[] args = new aterm.ATerm[] {");
				printActualTypedArgumentList(type, alt);
				println("};");
				println("    return make" + altClassName + "( " + funVar + ", args, empty);");
				println("  }");
				println();
			}

		}
	}

	private void genFactoryInitialization(ADT api) {
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			int listTypesCount = 0;

			println("    " + TypeGenerator.className(type) + ".initialize(this);");

			if (type instanceof ListType) {
				String emptyHashCode = new Integer(42 * (2 + listTypesCount)).toString();
				String className = ListTypeGenerator.className(type);
				println("    proto" + className + " = new " + className + "();");
				println("    proto" + className + ".init(" + emptyHashCode + ", null, null, null);");
				println("    empty" + className + " = (" + className + ") build(proto" + className + ");");
				println("    empty" + className + ".init(" + emptyHashCode + ", null, null, null);");
				listTypesCount++;
			} else {
				Iterator alts = type.alternativeIterator();
				while (alts.hasNext()) {
					Alternative alt = (Alternative) alts.next();
					String altClassName = AlternativeGenerator.className(type, alt);
					String protoVar = "proto" + altClassName;
					String funVar = "fun" + altClassName;
					String afunName = type.getId() + "_" + alt.getId();

					println();
					println("    " + altClassName + ".initializePattern();");
					println(
						"    "
							+ funVar
							+ " = makeAFun(\""
							+ "_"
							+ afunName
							+ "\", "
							+ type.getAltArity(alt)
							+ ", false);");
					println("    " + protoVar + " = new " + altClassName + "();");
				}
			}
			println();
		}

		Iterator bottoms = api.bottomTypeIterator();

		while (bottoms.hasNext()) {
			String type = (String) bottoms.next();

			if (!converter.isReserved(type)) {
				println("    " + StringConversions.makeCapitalizedIdentifier(type) + ".initialize(this);");
			}
		}
	}
}
