package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.ListType;
import apigen.adt.NormalListType;
import apigen.adt.SeparatedListType;
import apigen.adt.Type;
import apigen.adt.api.Separators;
import apigen.gen.StringConversions;

public class FactoryGenerator extends JavaGenerator {
	private ADT adt;
	private String apiName;
	private String className;

	public FactoryGenerator(ADT adt, JavaGenerationParameters params) {
		super(params);
		this.adt = adt;
		this.apiName = params.getApiName();
		this.className = className();
	}

	public String getClassName() {
		return className;
	}

	public static String className() {
		return "Factory";
	}

	public static String qualifiedClassName(JavaGenerationParameters params) {
		StringBuffer buf = new StringBuffer();
		buf.append(params.getPackageName());
		buf.append('.');
		buf.append(params.getApiName().toLowerCase());
		buf.append('.');
		buf.append(className());
		return buf.toString();
	}

	protected void generate() {
		printPackageDecl();
		printImports();
		genFactoryClass(adt);
	}

	private void genFactoryClass(ADT api) {
		println("public class " + getClassName() + " {");
		genFactoryPrivateMembers(api);
		genFactoryEmptyLists(api);
		genFactoryConstructor();
		genFactoryPureFactoryGetter();
		genFactoryInitialize(api);
		genAlternativeMethods(api);
		genFactoryMakeLists(api);
		genTypeFromTermMethods(api);
		genTypeFromMethods(api);
		println("}");
	}

	private void genFactoryPureFactoryGetter() {
		println("  public aterm.pure.PureFactory getPureFactory() {");
		println("    return factory;");
		println("  }");
		println();
	}

	private void genTypeFromTermMethods(ADT api) {
		Iterator types = api.typeIterator();

		while (types.hasNext()) {
			Type type = (Type) types.next();

			if (type instanceof NormalListType) {
				genFactoryListTypeFromTerm((NormalListType) type);
			}
			else if (type instanceof SeparatedListType) {
				genFactorySeparatedListTypeFromTermMethod((SeparatedListType) type);
			}
			else {
				genTypeFromTermMethod(type);
			}
		}
	}

	private void genTypeFromMethods(ADT api) {
		Iterator types = api.typeIterator();

		while (types.hasNext()) {
			Type type = (Type) types.next();

			genTypeFromStringMethod(type);
			genTypeFromFileMethod(type);
		}
	}

	private void genFactoryInitialize(ADT api) {
		println("  private void initialize() {");
		genFactoryInitialization(api);
		println("  }");
		println();
	}

	private void genFactoryConstructor() {
		println("  public " + getClassName() + "(aterm.pure.PureFactory factory) {");
		println("    this.factory = factory;");
		println("    initialize();");
		println("  }");
		println();
	}

	private void genFactoryEmptyLists(ADT api) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			if (type instanceof ListType) {
				String typeName = TypeGenerator.qualifiedClassName(params, type);
				String emptyName = emptyListVariable(type);
				println("  private " + typeName + ' ' + emptyName + ";");
			}
		}
		println();
	}

	private void genFactoryMakeLists(ADT api) {
		Iterator types = api.typeIterator();

		while (types.hasNext()) {
			Type type = (Type) types.next();
			if (type instanceof ListType) {
				ListType listType = (ListType) type;
				JavaGenerationParameters params = getJavaGenerationParameters();
				String returnTypeName = TypeGenerator.qualifiedClassName(params, type);
				String methodName = "make" + TypeGenerator.className(type);
				String paramTypeName = TypeGenerator.qualifiedClassName(params, listType.getElementType());
				String empty = emptyListVariable(type);
				String proto = protoListVariable(type);

				if (type instanceof NormalListType) {
					genFactoryMakeEmptyList(returnTypeName, methodName, empty);
					genFactoryMakeSingletonList(returnTypeName, methodName, paramTypeName, empty);
					genFactoryMakeManyList(returnTypeName, methodName, paramTypeName);
					genFactoryMakeManyTermList(returnTypeName, methodName, proto);
				}
				else if (type instanceof SeparatedListType) {
					SeparatedListType lType = (SeparatedListType) type;
					genFactoryMakeEmptyList(returnTypeName, methodName, empty);
					genFactoryMakeSingletonSeparatedList(returnTypeName, methodName, paramTypeName, lType, empty);
					genFactoryMakeManySeparatedList(returnTypeName, methodName, paramTypeName, lType);
					genFactoryMakeManySeparatedTermList(returnTypeName, methodName, proto, lType);
					genFactoryReverseSeparated(lType, methodName);
					genFactoryConcatSeparated(lType, methodName);
				}
			}
		}
	}

	private void genFactoryConcatSeparated(SeparatedListType type, String makeMethodName) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String className = TypeGenerator.qualifiedClassName(params, type);
		String sepArgs = buildOptionalSeparatorArguments(type);

		String formalSepArgs = buildFormalTypedArgumentList(type.separatorFieldIterator());
		if (formalSepArgs.length() > 0) {
			formalSepArgs += ", ";
		}

		println("  public " + className + " concat(" + className + " arg0, " + formalSepArgs + className + " arg1) {");
		println("    if (arg0.isEmpty()) {");
		println("      return arg1;");
		println("    }");
		println("    " + className + " list = reverse(arg0);");
		println("    " + className + " result = arg1;");
		println("");
		println("    while(!list.isSingle()) {");
		println("      result = " + makeMethodName + "(list.getHead(), " + sepArgs + "result);");

		Iterator seps = type.separatorFieldIterator();
		while (seps.hasNext()) {
			Field sep = (Field) seps.next();
			String fieldId = JavaGenerator.getFieldId(sep.getId());
			String fieldGet = "get" + StringConversions.makeCapitalizedIdentifier(sep.getId());
			println("      " + fieldId + " = list." + fieldGet + "();");

		}
		println("      list = list.getTail();");
		println("    }");
		println();
		println("    return " + makeMethodName + "(list.getHead(), " + sepArgs + "result);");
		println("  }");
		println();
	}

	private void genFactoryReverseSeparated(SeparatedListType type, String makeMethodName) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String className = TypeGenerator.qualifiedClassName(params, type);

		println("  public " + className + " reverse(" + className + " arg) {");
		println("    if (arg.isEmpty() || arg.isSingle()) {");
		println("         return arg;");
		println("    }");
		println();
		println("    int length = arg.getLength();");
		println("    " + className + "[] nodes = new " + className + "[length];");
		println();
		println("    for (int i = 0; i < length; i++) {");
		println("      nodes[i] = arg;");
		println("      arg = arg.getTail();");
		println("    }");
		println();
		println("    " + className + " result = " + makeMethodName + "(nodes[0].getHead());");
		println();
		println("    for (int i = 1; i < length; i++) {");
		String elementType = TypeGenerator.qualifiedClassName(params, type.getElementType());
		println("      " + elementType + " _head = nodes[i].getHead();");

		Iterator separators = type.separatorFieldIterator();
		while (separators.hasNext()) {
			Field separator = (Field) separators.next();
			String fieldId = separator.getId();
			String fieldName = JavaGenerator.getFieldId(fieldId);
			String fieldType = TypeGenerator.qualifiedClassName(params, separator.getType());
			String capitalizedFieldId = StringConversions.makeCapitalizedIdentifier(fieldId);
			String fieldGetter = "get" + capitalizedFieldId + "()";
			println("      " + fieldType + ' ' + fieldName + " = nodes[i-1]." + fieldGetter + ";");
		}

		String seps = buildOptionalSeparatorArguments(type);
		println("      result = " + makeMethodName + "(_head, " + seps + "result);");
		println("    }");
		println();
		println("    return result;");
		println("  }");
		println();
	}

	private void genFactoryMakeManyTermList(String returnTypeName, String methodName, String protoName) {
		println(
			"  protected "
				+ returnTypeName
				+ ' '
				+ methodName
				+ "(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {");
		println("    synchronized (" + protoName + ") {");
		println("      " + protoName + ".initHashCode(annos, head, tail);");
		println("      return (" + returnTypeName + ") factory.build(" + protoName + ");");
		println("    }");
		println("  }");
		println();
	}

	private void genFactoryMakeManyList(String returnTypeName, String methodName, String paramTypeName) {
		println(
			"  public "
				+ returnTypeName
				+ ' '
				+ methodName
				+ '('
				+ paramTypeName
				+ " head, "
				+ returnTypeName
				+ " tail) {");
		println(
			"    return ("
				+ returnTypeName
				+ ") "
				+ methodName
				+ "((aterm.ATerm) head, (aterm.ATermList) tail, factory.getEmpty());");
		println("  }");
		println();
	}

	private void genFactoryMakeManySeparatedList(
		String returnTypeName,
		String methodName,
		String paramTypeName,
		SeparatedListType type) {
		String formalSeps = buildFormalTypedArgumentList(type.separatorFieldIterator());
		String actualSeps = buildActualTypedAltArgumentList(type.separatorFieldIterator());
		if (formalSeps.length() > 0) {
			formalSeps += ", ";
			actualSeps += ", ";
		}

		println(
			"  public "
				+ returnTypeName
				+ ' '
				+ methodName
				+ "("
				+ paramTypeName
				+ " head, "
				+ formalSeps
				+ returnTypeName
				+ " tail) {");
		println(
			"    return ("
				+ returnTypeName
				+ ") "
				+ methodName
				+ "((aterm.ATerm) head, "
				+ actualSeps
				+ "(aterm.ATermList) tail, factory.getEmpty());");
		println("  }");
		println();
	}

	private void genFactoryMakeManySeparatedTermList(
		String returnTypeName,
		String methodName,
		String proto,
		SeparatedListType type) {
		String formalSeps = buildFormalTypedArgumentList(type.separatorFieldIterator());
		String actualSeps = buildActualTypedAltArgumentList(type.separatorFieldIterator());

		if (formalSeps.length() > 0) {
			formalSeps += ", ";
			actualSeps += ", ";
		}

		println(
			"  protected "
				+ returnTypeName
				+ ' '
				+ methodName
				+ "(aterm.ATerm head, "
				+ formalSeps
				+ "aterm.ATermList tail, aterm.ATermList annos) {");
		println("    synchronized (" + proto + ") {");
		println("      " + proto + ".initHashCode(annos, head, " + actualSeps + "tail);");
		println("      return (" + returnTypeName + ") factory.build(" + proto + ");");
		println("    }");
		println("  }");
		println();
	}

	private void genFactoryMakeSingletonList(
		String returnTypeName,
		String methodName,
		String paramTypeName,
		String empty) {

		println("  public " + returnTypeName + ' ' + methodName + '(' + paramTypeName + " elem) {");
		println("    return (" + returnTypeName + ") " + methodName + "(elem, " + empty + ");");
		println("  }");
		println();
	}

	private void genFactoryMakeSingletonSeparatedList(
		String returnTypeName,
		String methodName,
		String paramTypeName,
		SeparatedListType type,
		String empty) {
		String separators = buildActualNullArgumentList(type.separatorFieldIterator());
		if (separators.length() > 0) {
			separators += ", ";
		}

		println("  public " + returnTypeName + ' ' + methodName + "(" + paramTypeName + " elem ) {");
		println("    return (" + returnTypeName + ") " + methodName + "(elem, " + separators + empty + ");");
		println("  }");
		println();
	}

	private void genFactoryMakeEmptyList(String returnTypeName, String methodName, String empty) {
		println("  public " + returnTypeName + ' ' + methodName + "() {");
		println("    return " + empty + ";");
		println("  }");
		println();
	}

	private void genFactoryPrivateMembers(ADT api) {
		JavaGenerationParameters params = getJavaGenerationParameters();

		// TODO: maybe ATermFactory is enough instead of PureFactory
		println("  private aterm.pure.PureFactory factory;");
		println();

		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			String typeClassName = TypeGenerator.qualifiedClassName(params, type);
			Iterator alts = type.alternativeIterator();

			if (type instanceof ListType) {
				String protoVar = protoListVariable(type);
				println("  private " + typeClassName + ' ' + protoVar + ';');
				println("  private aterm.ATerm " + patternListVariable(type) + ';');
				println();
			}
			else {
				while (alts.hasNext()) {
					Alternative alt = (Alternative) alts.next();
					String funVar = funVariable(type, alt);
					String protoVar = prototypeVariable(type, alt);
					String patternVar = patternVariable(type, alt);

					println("  private aterm.AFun " + funVar + ';');
					println("  private " + typeClassName + " " + protoVar + ';');
					println("  private aterm.ATerm " + patternVar + ';');
					println();
				}
			}
		}
	}

	private void genAlternativeMethods(ADT api) {
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();

			if (type instanceof ListType) {
				if (type instanceof SeparatedListType) {
					genFactorySeparatedListToTerm((SeparatedListType) type);
				}
			}
			else {
				Iterator alts = type.alternativeIterator();
				while (alts.hasNext()) {
					Alternative alt = (Alternative) alts.next();

					genInternalMakeMethod(type, alt);
					genMakeMethod(type, alt);
					genAltFromTerm(type, alt);
					genAltToTerm(type, alt);
				}
			}
		}
	}

	private void genMakeMethod(Type type, Alternative alt) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String altClassName = AlternativeGenerator.qualifiedClassName(params, type, alt);
		String makeMethodName = "make" + concatTypeAlt(type, alt);
		String funVar = funVariable(type, alt);

		print("  public " + altClassName + ' ' + makeMethodName + "(");
		printFormalTypedAltArgumentList(type, alt);
		println(") {");
		print("    aterm.ATerm[] args = new aterm.ATerm[] {");
		printActualTypedArgumentList(type, alt);
		println("};");
		println("    return " + makeMethodName + "(" + funVar + ", args, factory.getEmpty());");
		println("  }");
		println();
	}

	private void genInternalMakeMethod(Type type, Alternative alt) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String altClassName = AlternativeGenerator.qualifiedClassName(params, type, alt);
		String protoVar = prototypeVariable(type, alt);

		print("  public " + altClassName + " make" + concatTypeAlt(type, alt));
		println("(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {");
		println("    synchronized (" + protoVar + ") {");
		println("      " + protoVar + ".initHashCode(annos, fun, args);");
		println("      return (" + altClassName + ") factory.build(" + protoVar + ");");
		println("    }");
		println("  }");
		println();
	}

	private void genAltToTerm(Type type, Alternative alt) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String className = AlternativeGenerator.qualifiedClassName(params, type, alt);

		println("  public aterm.ATerm toTerm(" + className + " arg) {");
		println("    java.util.List args = new java.util.LinkedList();");

		Iterator fields = type.altFieldIterator(alt.getId());
		genAddFieldsToListCalls(fields);
		println("    return factory.make(" + patternVariable(type, alt) + ", args);");
		println("  }");
		println();
	}

	private void genAddFieldsToListCalls(Iterator fields) {
		for (int i = 0; fields.hasNext(); i++) {
			Field field = (Field) fields.next();
			String field_type = field.getType();
			String getArgumentCall = "arg.get" + StringConversions.makeCapitalizedIdentifier(field.getId()) + "()";

			if (field_type.equals("str")) {
				println("    args.add(" + getArgumentCall + ");");
			}
			else if (field_type.equals("int")) {
				println("    args.add(new Integer(" + getArgumentCall + "));");
			}
			else if (field_type.equals("real")) {
				println("    args.add(new Double (" + getArgumentCall + "));");
			}
			else if (field_type.equals("term")) {
				println("    args.add((aterm.ATerm)" + getArgumentCall + ");");
			}
			else if (field_type.equals("list")) {
				println("    args.add((aterm.ATermList)" + getArgumentCall + ");");
			}
			else {
				println("    args.add(" + getArgumentCall + ".toTerm());");
			}
		}
	}

	private void genFactoryInitialization(ADT api) {
		Iterator types = api.typeIterator();
		int listTypeCount = 0;

		while (types.hasNext()) {
			Type type = (Type) types.next();

			if (type instanceof NormalListType) {
				genNormalListTypeInitialization(type, listTypeCount);
				listTypeCount++;
			}
			else if (type instanceof SeparatedListType) {
				genSeparatedListTypeInitialization(listTypeCount, (SeparatedListType) type);
				listTypeCount++;
			}
			else {
				JavaGenerationParameters params = getJavaGenerationParameters();
				Iterator alts = type.alternativeIterator();
				while (alts.hasNext()) {
					Alternative alt = (Alternative) alts.next();
					String protoVar = prototypeVariable(type, alt);
					String funVar = funVariable(type, alt);
					String afunName = type.getId() + "_" + alt.getId();

					println(
						"    "
							+ patternVariable(type, alt)
							+ " = factory.parse(\""
							+ StringConversions.escapeQuotes(alt.buildMatchPattern().toString())
							+ "\");");
					println(
						"    "
							+ funVar
							+ " = factory.makeAFun(\""
							+ "_"
							+ afunName
							+ "\", "
							+ type.getAltArity(alt)
							+ ", false);");
					println(
						"    "
							+ protoVar
							+ " = new "
							+ AlternativeGenerator.qualifiedClassName(params, type, alt)
							+ "(this);");
					println();
				}
			}
		}

		Iterator bottoms = api.bottomTypeIterator();

		while (bottoms.hasNext()) {
			String type = (String) bottoms.next();

			if (!getConverter().isReserved(type)) {
				println("    " + StringConversions.makeCapitalizedIdentifier(type) + ".initialize(this);");
			}
		}
	}

	private void genNormalListTypeInitialization(Type type, int listTypeCount) {
		genInitializePrototype(type);
		genInitializeEmptyList(type, listTypeCount);
	}

	private void genSeparatedListTypeInitialization(int listTypeCount, SeparatedListType type) {
		genInitializePrototype(type);
		genInitializeEmptySeparatedList(type, listTypeCount);
		genInitializeManyPattern(type);
	}

	private void genInitializeManyPattern(SeparatedListType type) {
		Alternative alt = type.getManyAlternative();
		println(
			"    "
				+ patternListVariable(type)
				+ " = factory.parse(\""
				+ StringConversions.escapeQuotes(alt.buildMatchPattern().toString())
				+ "\");");
	}

	private void genInitializeEmptySeparatedList(SeparatedListType type, int listTypeCount) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String className = TypeGenerator.qualifiedClassName(params, type);
		String protoName = protoListVariable(type);
		String emptyName = emptyListVariable(type);
		String emptyHashCode = buildInitialEmptyListHashcode(listTypeCount).toString();
		println(
			"    "
				+ protoName
				+ ".init("
				+ emptyHashCode
				+ ", null, null, "
				+ buildAmountOfSeparatorsNullExpressions(type)
				+ "null);");
		println("    " + emptyName + " = (" + className + ") factory.build(" + protoName + ");");
		println(
			"    "
				+ emptyName
				+ ".init("
				+ emptyHashCode
				+ ", "
				+ emptyName
				+ ", null, "
				+ buildAmountOfSeparatorsNullExpressions(type)
				+ "null);");
	}

	private String buildAmountOfSeparatorsNullExpressions(SeparatedListType type) {
		String result = "";
		Iterator fields = type.separatorFieldIterator();
		while (fields.hasNext()) {
			fields.next();
			result += "null, ";
		}
		return result;
	}

	private void genInitializeEmptyList(Type type, int listTypeCount) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String className = TypeGenerator.qualifiedClassName(params, type);
		String protoName = protoListVariable(type);
		String emptyName = emptyListVariable(type);
		String emptyHashCode = buildInitialEmptyListHashcode(listTypeCount).toString();
		println("    " + protoName + ".init(" + emptyHashCode + ", null, null, null);");
		println("    " + emptyName + " = (" + className + ") factory.build(" + protoName + ");");
		println("    " + emptyName + ".init(" + emptyHashCode + ", " + emptyName + ", null, null);");
	}

	private void genInitializePrototype(Type type) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String protoName = protoListVariable(type);
		String className = TypeGenerator.qualifiedClassName(params, type);
		println("    " + protoName + " = new " + className + "(this);");
	}

	private Integer buildInitialEmptyListHashcode(int listTypeCount) {
		return new Integer(42 * (2 + listTypeCount));
	}

	private void genAltFromTerm(Type type, Alternative alt) {
		JavaGenerationParameters params = getJavaGenerationParameters();

		String returnType = TypeGenerator.qualifiedClassName(params, type);
		String methodName = concatTypeAlt(type, alt) + "FromTerm";

		println("  protected " + returnType + " " + methodName + "(aterm.ATerm trm) {");
		println("    java.util.List children = trm.match(" + patternVariable(type, alt) + ");");
		println();
		println("    if (children != null) {");

		methodName = "make" + concatTypeAlt(type, alt);
		println("      return " + methodName + "(");

		Iterator fields = type.altFieldIterator(alt.getId());
		int argnr = 0;
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			print("        " + buildFieldMatchResultRetriever(argnr, field));

			if (fields.hasNext()) {
				print(",");
			}
			println();
			argnr++;
		}
		println("      );");
		println("    }"); // endif
		println("    else {");
		println("      return null;");
		println("    }");
		println("  }");
		println();
	}

	private String buildFieldMatchResultRetriever(int argnr, Field field) {
		String fieldType = field.getType();
		String fieldClass = TypeGenerator.className(fieldType);
		String result = "";

		if (fieldType.equals("str")) {
			result = "(String) children.get(" + argnr + ")";
		}
		else if (fieldType.equals("int")) {
			result = "((Integer) children.get(" + argnr + ")).intValue()";
		}
		else if (fieldType.equals("real")) {
			result = "((Double) children.get(" + argnr + ")).doubleValue()";
		}
		else if (fieldType.equals("term")) {
			result = "(aterm.ATerm) children.get(" + argnr + ")";
		}
		else if (fieldType.equals("list")) {
			result = "(aterm.ATermList) children.get(" + argnr + ")";
		}
		else {
			result = fieldClass + "FromTerm((aterm.ATerm) children.get(" + argnr + "))";
		}

		return result;
	}

	private void genTypeFromTermMethod(Type type) {
		JavaGenerationParameters params = getJavaGenerationParameters();

		String returnType = TypeGenerator.qualifiedClassName(params, type);
		String methodName = TypeGenerator.className(type) + "FromTerm";

		println("  public " + returnType + " " + methodName + "(aterm.ATerm trm) {");
		println("    " + returnType + " tmp;");
		genAltFromTermCalls(type);
		println(
			"    throw new IllegalArgumentException(\"This is not a " + TypeGenerator.className(type) + ": \" + trm);");
		println("  }");
		println();
	}

	protected void genAltFromTermCalls(Type type) {
		Iterator alts = type.alternativeIterator();
		while (alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();
			String methodName = concatTypeAlt(type, alt) + "FromTerm";
			println("    tmp = " + methodName + "(trm);");
			println("    if (tmp != null) {");
			println("      return tmp;");
			println("    }");
			println();
		}
	}

	protected void genTypeFromStringMethod(Type type) {
		JavaGenerationParameters params = getJavaGenerationParameters();

		String returnType = TypeGenerator.qualifiedClassName(params, type);
		String fromString = TypeGenerator.className(type.getId()) + "FromString";
		String fromTerm = TypeGenerator.className(type.getId()) + "FromTerm";

		println("  public " + returnType + " " + fromString + "(String str) {");
		println("    return " + fromTerm + "(factory.parse(str));");
		println("  }");
		println();
	}

	protected void genTypeFromFileMethod(Type type) {
		JavaGenerationParameters params = getJavaGenerationParameters();

		String returnType = TypeGenerator.qualifiedClassName(params, type);
		String fromFile = TypeGenerator.className(type.getId()) + "FromFile";
		String fromTerm = TypeGenerator.className(type.getId()) + "FromTerm";

		print("  public " + returnType + ' ' + fromFile + "(java.io.InputStream stream)");
		println(" throws java.io.IOException {");
		println("    return " + fromTerm + "(factory.readFromFile(stream));");
		println("  }");
		println();
	}

	private void genFactoryListTypeFromTerm(NormalListType type) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String returnTypeName = TypeGenerator.qualifiedClassName(params, type);
		String className = TypeGenerator.className(type);
		String elementType = type.getElementType();
		String elementTypeName = TypeGenerator.qualifiedClassName(params, elementType);
		String fromTermMethodName = TypeGenerator.className(elementType) + "FromTerm";

		if (getConverter().isReserved(elementType)) {
			// TODO: implement lists of builtins
			throw new RuntimeException("List of " + elementType + " not yet implemented, sorry!");
		}

		println("  public " + returnTypeName + " " + className + "FromTerm(aterm.ATerm trm) {");
		println("     if (trm instanceof aterm.ATermList) {");
		println("        aterm.ATermList list = ((aterm.ATermList) trm).reverse();");
		println("        " + returnTypeName + " result = make" + className + "();");
		println("        for (; !list.isEmpty(); list = list.getNext()) {");
		println("          " + elementTypeName + " elem = " + fromTermMethodName + "(list.getFirst());");
		println("           if (elem != null) {");
		println("             result = make" + className + "(elem, result);");
		println("           }");
		println("           else {");
		println("             throw new RuntimeException(\"Invalid element in " + className + ": \" + elem);");
		println("           }");
		println("        }");
		println("        return result;");
		println("     }");
		println("     else {");
		println("       throw new RuntimeException(\"This is not a " + className + ": \" + trm);");
		println("     }");
		println("  }");
		println();
	}

	private void genFactorySeparatedListTypeFromTermMethod(SeparatedListType type) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String returnTypeName = TypeGenerator.qualifiedClassName(params, type);
		String className = TypeGenerator.className(type);
		String makeClassName = "make" + className;
		String manyPattern = patternListVariable(type);
		String elementClass = TypeGenerator.className(type.getElementType());
		String elementClassFromTerm = elementClass + "FromTerm";
		Separators separators = type.getSeparators();
		int headLength = 1 + separators.getLength(); // on the ATerm level
		int tailIndex = 1 + type.countSeparatorFields(); // on the abstract
		// level

		println("  public " + returnTypeName + " " + className + "FromTerm(aterm.ATerm trm) {");
		println("    if (!(trm instanceof aterm.ATermList)) {");
		println("      throw new IllegalArgumentException(\"This is not a " + className + ": \" + trm);");
		println("    }");
		println();
		println("    aterm.ATermList list = (aterm.ATermList) trm;");
		println("    if (list.isEmpty()) {");
		println("      return " + makeClassName + "();");
		println("    }");
		println();
		println("    if (list.getLength() == 1) {");
		println("      return " + makeClassName + "(" + elementClassFromTerm + "(list.getFirst()));");
		println("    }");
		println();
		println("    int length = (list.getLength() / " + headLength + ") + 1;");
		println("    java.util.List[] nodes = new java.util.List[length-1];");
		println();
		println("    for (int i = 0; i < length - 1; i++) {");
		println("      java.util.List args = list.match(" + manyPattern + ");");
		println("      if (args == null) {");
		println("        throw new IllegalArgumentException(\"This is not a " + className + ": \" + trm);");
		println("      }");
		println("      nodes[i] = args;");
		println("      list = (aterm.ATermList) args.get(" + tailIndex + ");");
		println("    }");
		println();
		println("    if (list.getLength() != 1) {");
		println("      throw new IllegalArgumentException(\"This is not a " + className + ": \" + trm);");
		println("    }");
		println();
		println(
			"    "
				+ returnTypeName
				+ " result = "
				+ makeClassName
				+ "("
				+ elementClassFromTerm
				+ "(list.getFirst()));");
		println();
		println("    for (int i = length - 2; i >= 0; i--) {");
		println("      java.util.List children = nodes[i];");
		String elementTypeName = TypeGenerator.qualifiedClassName(params, type.getElementType());
		println("      " + elementTypeName + " head = " + elementClassFromTerm + "((aterm.ATerm) children.get(0));");

		genFromTermSeparatorFieldAssigments(type);

		String separatorArgs = buildOptionalSeparatorArguments(type);

		println("      result = " + makeClassName + "(head, " + separatorArgs + "result);");
		println("    }");
		println();
		println("    return result;");
		println("  }");
		println();
	}

	private String buildOptionalSeparatorArguments(SeparatedListType type) {
		Iterator separatorFields = type.separatorFieldIterator();
		String separatorArgs = buildActualTypedAltArgumentList(separatorFields);
		if (separatorArgs.length() > 0) {
			separatorArgs += ", ";
		}
		return separatorArgs;
	}

	private void genFactorySeparatedListToTerm(SeparatedListType type) {
		String paramTypeName = TypeGenerator.qualifiedClassName(getJavaGenerationParameters(), type);
		String manyPattern = patternListVariable(type);

		println("  public aterm.ATerm toTerm(" + paramTypeName + " arg) {");
		println("    if (arg.isEmpty()) {");
		println("      return factory.getEmpty();");
		println("    }");
		println();
		println("    if (arg.isSingle()) {");
		println("      return factory.makeList(arg.getHead().toTerm());");
		println("    }");
		println();
		println("    int length = arg.getLength();");
		println("    " + paramTypeName + "[] nodes = new " + paramTypeName + "[length];");
		println();
		println("    for (int i = 0; i < length; i++) {");
		println("      nodes[length-i-1] = arg;");
		println("      arg = arg.getTail();");
		println("    }");
		println();
		println("    aterm.ATermList result = factory.makeList(nodes[0].getHead().toTerm());");
		println("    for (int i = 1; i < length; i++) {");
		println("      java.util.List args = new java.util.LinkedList();");
		println("      args.add(nodes[i].getHead().toTerm());");

		Iterator separators = type.separatorFieldIterator();
		while (separators.hasNext()) {
			Field separator = (Field) separators.next();
			String fieldId = separator.getId();
			String capitalizedFieldId = StringConversions.makeCapitalizedIdentifier(fieldId);
			String fieldGetter = "get" + capitalizedFieldId + "()";
			println("      args.add(nodes[i]." + fieldGetter + ".toTerm());");
		}

		println("      args.add(result);");
		println("      result = (aterm.ATermList) " + manyPattern + ".make(args);");
		println("    }");
		println();
		println("    return result;");
		println("  }");
		println();
	}

	private int genFromTermSeparatorFieldAssigments(SeparatedListType type) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		Iterator fields = type.separatorFieldIterator();
		int i;
		for (i = 1; fields.hasNext(); i++) {
			Field field = (Field) fields.next();
			String fieldId = JavaGenerator.getFieldId(field.getId());
			String fieldType = TypeGenerator.qualifiedClassName(params, field.getType());
			println("      " + fieldType + " " + fieldId + " = " + buildFieldMatchResultRetriever(i, field) + ";");
		}
		return i;
	}

	public String getPackageName() {
		return apiName.toLowerCase();
	}

	public String getQualifiedClassName() {
		return getClassName();
	}

	public static String concatTypeAlt(Type type, Alternative alt) {
		return TypeGenerator.className(type) + "_" + AlternativeGenerator.className(alt);
	}

	private static String alternativeVariable(String pre, Type type, Alternative alt) {
		return pre + '_' + concatTypeAlt(type, alt);
	}

	private static String patternVariable(Type type, Alternative alt) {
		return alternativeVariable("pattern", type, alt);
	}

	private static String prototypeVariable(Type type, Alternative alt) {
		return alternativeVariable("proto", type, alt);
	}

	private static String funVariable(Type type, Alternative alt) {
		return alternativeVariable("fun", type, alt);
	}

	private static String listVariable(String pre, Type type) {
		return pre + '_' + TypeGenerator.className(type);
	}

	private static String emptyListVariable(Type type) {
		return listVariable("empty", type);
	}

	private static String protoListVariable(Type type) {
		return listVariable("proto", type);
	}

	private static String patternListVariable(Type type) {
		return listVariable("pattern", type);
	}
}
