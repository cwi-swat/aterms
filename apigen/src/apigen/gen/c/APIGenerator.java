package apigen.gen.c;

import java.util.*;
import java.io.*;

import apigen.adt.*;
import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;
import aterm.*;

public class APIGenerator extends CGenerator {
	private ADT adt;
	private String apiName;

	private boolean make_term_compatibility = false;

	private String prefix;
	private String prologue;
	private String macro;

	public AFunRegister afunRegister;

	public APIGenerator(
		ADT adt,
		String apiName,
		String prefix,
		String prologue,
		boolean verbose,
		boolean folding,
		boolean make_term_compatibility) {
		super(".", apiName, verbose, folding);
		this.adt = adt;
		this.apiName = apiName;
		this.prefix = prefix;
		this.prologue = prologue;
		this.make_term_compatibility = make_term_compatibility;

		afunRegister = new AFunRegister();
	}

	protected void generate() {
		info("generating " + apiName + ".[ch]");

		genPrologue();
		genTypes(adt);
		genInitFunction();
		genTermConversions(adt);
		genListsApi(adt);
		genConstructors(adt);
		genIsEquals(adt);
		genAccessors(adt);
		genSortVisitors(adt);
		genEpilogue();
	}
    
	private void genListsApi(ADT adt) {
      Iterator types = adt.typeIterator();
      
      bothPrintFoldOpen("list functions");  
      while (types.hasNext()) {
	    Type type = (Type) types.next();
	   
	    if (type instanceof ListType) {
	   	  genListApi((ListType) type);
	    }	
	  }
	  bothPrintFoldClose();
	}

	private void genListApi(ListType type) {
	   String typeName = buildTypeName(type.getId());
	   String elementTypeName = buildTypeName(type.getElementType());
	   genGetLength(typeName);
	   genReverse(typeName);
	   genAppend(typeName, elementTypeName);
	   genConcat(typeName);
	   genSlice(typeName);
	   genGetElementAt(typeName, elementTypeName);
	   genReplaceElementAt(typeName, elementTypeName);
	   genListMakes(typeName, elementTypeName);
	}

	private void genListMakes(String typeName, String elementTypeName) {
       for (int arity = 1; arity <= 6; arity++) {
       	 genListMake(arity, typeName, elementTypeName);
       }
	}

	private void genListMake(int arity, String typeName, String elementTypeName) {
		String decl = typeName + " " + prefix + "make" + typeName + arity + "(";
		for (int i = 1; i < arity; i++) {
			decl = decl + elementTypeName + " elem" + i + ", ";
		}
		decl = decl + elementTypeName + " elem" + arity + ")";
		hprintln(decl + ";");
		println(decl + " {");
		print("  return (" + typeName + ") ATmakeList" + arity + "(");
		
		for (int i = 1; i < arity; i++) {
			print("(ATerm) elem" + i + ", ");
		}
		println("(ATerm) elem" + arity + ");");
		println("}");
	}

	private void genGetElementAt(String typeName, String elementTypeName) {
		String decl = elementTypeName + " " + prefix + "get" + typeName + elementTypeName + "At(" + typeName + " arg, int index)";
		hprintln(decl + ";");
		println(decl +" {");
		println(" return (" + elementTypeName + ") ATelementAt((ATermList) arg, index);");
		println("}");
	}
	
	private void genReplaceElementAt(String typeName, String elementTypeName) {
		String decl = typeName + " " + prefix + "replace" + typeName + elementTypeName + "At(" + typeName + " arg, " + elementTypeName + " elem, int index)";
		hprintln(decl + ";");
		println(decl +" {");
		println(" return (" + typeName + ") ATreplace((ATermList) arg, (ATerm) elem, index);");
		println("}");
	}

	private void genReverse(String typeName) {
		String decl = typeName + " " + prefix + "reverse" + typeName + "(" +
		              typeName + " arg)";
		hprintln(decl + ";");
		println(decl + " {");
		println("  return (" + typeName + ") ATreverse((ATermList) arg);");
		println("}");
	}

	private void genAppend(String typeName, String elementTypeName) {
		String decl = typeName + " " + prefix + "append" + typeName + "(" + typeName + " arg, " + elementTypeName + " elem)";
		hprintln(decl + ";");
		println(decl + " {");
		println("  return (" + typeName + ") ATappend((ATermList) arg, (ATerm) elem);");
		println("}");
	}
    
	private void genConcat(String typeName) {
	   String decl = typeName + " " + prefix + "concat" + typeName + "(" + typeName + " arg0, " + typeName + " arg1)";
	   hprintln(decl + ";");
	   println(decl + " {");
	   println("  return (" + typeName + ") ATconcat((ATermList) arg0, (ATermList) arg1);");
	   println("}");
	}
	
	private void genSlice(String typeName) {
		String decl = typeName + " " + prefix + "slice" + typeName + "(" + typeName + " arg, int start, int end)";
		hprintln(decl + ";");
		println(decl + " {");
		println("  return (" + typeName + ") ATgetSlice((ATermList) arg, start, end);");
		println("}");
	}
		
	private void genGetLength(String typeName) {
		String decl = "int " + prefix + "get" + typeName + "Length (" + typeName + " arg)";
		
		hprintln(decl + ";");
		println(decl + " {");
		println("  return ATgetLength((ATermList) arg);");
		println("}");
	}
	
	private void genPrologue() {
		genHeaderIfDefWrapper();
		genHeaderIncludes();
		copyPrologueFileToHeader();
		genSourceIncludes();
	}

	private void genSourceIncludes() {

		println("#include <assert.h>");
		println();
		println("#include <aterm2.h>");
		println("#include <deprecated.h>");
		println("#include \"" + apiName + ".h\"");
		println();
	}

	private void copyPrologueFileToHeader() {
		if (prologue != null) {
			hprintFoldOpen("prologue");
			InputStream stream;
			try {
				stream = new FileInputStream(prologue);
				while (true) {
					int b = stream.read();
					if (b == -1) {
						break;
					}
					hwrite(b);
				}
			} catch (FileNotFoundException e) {
				System.out.println("Could not find prologue file");
				System.exit(1);
			} catch (IOException e) {
				System.out.println("Could not read from prologue file");
				System.exit(1);
			}

			hprintFoldClose();
		}
	}

	private void genHeaderIncludes() {
		hprintln();
		hprintFoldOpen("includes");
		hprintln("#include <aterm1.h>");
		hprintln("#include \"" + apiName + "_dict.h\"");
		hprintFoldClose();
		hprintln();
	}

	private void genHeaderIfDefWrapper() {
		macro = "_" + apiName.toUpperCase() + "_H";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < macro.length(); i++) {
			if (Character.isLetterOrDigit(macro.charAt(i))) {
				buf.append(macro.charAt(i));
			} else {
				buf.append('_');
			}
		}
		macro = buf.toString();

		hprintln("#ifndef " + macro);
		hprintln("#define " + macro);
	}

	//}}}
	//{{{ private void genTypes(API api)

	private void genTypes(ADT api) {
		Iterator types = api.typeIterator();
		bothPrintFoldOpen("typedefs");
		while (types.hasNext()) {
			Type type = (Type) types.next();
			String id = buildTypeName(type);
			hprintln("typedef struct _" + id + " *" + id + ";");
			println("typedef struct ATerm _" + id + ";");
		}
		bothPrintFoldClose();
		bothPrintln();
	}

	//}}}
	//	{{{ private void genInitFunction(API api)

	private void genInitFunction() {
		String decl = "void " + prefix + "init" + StringConversions.makeCapitalizedIdentifier(apiName) + "Api(void)";
		hprintln(decl + ";");

		printFoldOpen(decl);
		println(decl);
		println("{");
		String dictInitfunc = buildDictInitFunc(apiName);
		println("  init_" + dictInitfunc + "_dict();");
		println("}");
		printFoldClose();
		println();
		hprintln();
	}

	//}}}
	//	{{{ private String buildDictInitFunc(String name)

	private String buildDictInitFunc(String name) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				buf.append(c);
			} else {
				buf.append('_');
			}
		}
		return buf.toString();
	}

	//}}}
	//{{{ private void genTermConversions(API api)

	private void genTermConversions(ADT api) {
		Iterator types = api.typeIterator();
		bothPrintFoldOpen("term conversion functions");
		while (types.hasNext()) {
			Type type = (Type) types.next();
			String type_id = StringConversions.makeIdentifier(type.getId());
			String type_name = buildTypeName(type);

			genFromTerm(type_id, type_name);
			genToTerm(type_id, type_name);
		}
		bothPrintFoldClose();
	}

	private void genToTerm(String type_id, String type_name) {
		String decl;

		if (make_term_compatibility) {
			String old_macro =
				"#define " + prefix + "makeTermFrom" + type_id + "(t)" + " (" + prefix + type_id + "ToTerm(t))";
			hprintln(old_macro);
		}
		decl = "ATerm " + prefix + type_id + "ToTerm(" + type_name + " arg)";
		hprintln(decl + ";");
		printFoldOpen(decl);
		println(decl);
		println("{");
		println("  return (ATerm)arg;");
		println("}");
		printFoldClose();
	}

	private void genFromTerm(String type_id, String type_name) {
		if (make_term_compatibility) {
			String old_macro =
				"#define " + prefix + "make" + type_id + "FromTerm(t)" + " (" + prefix + type_id + "FromTerm(t))";
			hprintln(old_macro);
		}
		String decl = type_name + " " + prefix + type_id + "FromTerm(ATerm t)";
		hprintln(decl + ";");
		printFoldOpen(decl);
		println(decl);
		println("{");
		println("  return (" + type_name + ")t;");
		println("}");
		printFoldClose();
	}

	//}}}
	//{{{ private void genIsEquals(API api)

	private void genIsEquals(ADT api) {
		Iterator types = api.typeIterator();
		bothPrintFoldOpen("equality functions");

		while (types.hasNext()) {
			Type type = (Type) types.next();
			String type_id = StringConversions.makeIdentifier(type.getId());
			String type_name = buildTypeName(type);

			String decl = "ATbool " + prefix + "isEqual" + type_id + "(" + type_name + " arg0, " + type_name + " arg1)";

			hprintln(decl + ";");

			println(decl);
			println("{");
			println("  return ATisEqual((ATerm)arg0, (ATerm)arg1);");
			println("}");
			if (types.hasNext()) {
				println();
			}
		}

		bothPrintFoldClose();
	}

	//}}}
	//{{{ private String genConstructorImpl(ATerm pattern)

	private String genConstructorImpl(ATerm pattern) {
		String result = null;

		switch (pattern.getType()) {
			case ATerm.REAL :
				result = buildRealConstructorImpl(pattern);
				break;
			case ATerm.INT :
				result = buildIntConstructorImpl(pattern);
				break;
			case ATerm.BLOB :
				throw new RuntimeException("blobs are not supported");
			case ATerm.LIST :
				result = buildListConstructorImpl(pattern);
				break;
			case ATerm.APPL :
				result = buildApplConstructorImpl(pattern);
				break;
			case ATerm.PLACEHOLDER :
				result = buildPlaceholderSubstitution(pattern);
				break;
		}

		return result;
	}

	private String buildPlaceholderSubstitution(ATerm pattern) {
		String result;
		ATermAppl hole = (ATermAppl) ((ATermPlaceholder) pattern).getPlaceholder();
		String name = StringConversions.makeIdentifier(hole.getName());
		String type = hole.getArgument(0).toString();
		if (type.equals("int")) {
			result = "(ATerm)ATmakeInt(" + name + ")";
		} else if (type.equals("real")) {
			result = "(ATerm)ATmakeReal(" + name + ")";
		} else if (type.equals("str")) {
			result = "(ATerm)ATmakeAppl0(ATmakeAFun(" + name + ", 0, ATtrue))";
		} else {
			result = "(ATerm)" + name;
		}
		return result;
	}

	private String buildApplConstructorImpl(ATerm pattern) {
		String result;
		ATermAppl appl = (ATermAppl) pattern;
		int arity = appl.getArity();
		if (arity > 0) {
			ATerm last = appl.getArgument(arity - 1);
			if (last.getType() == ATerm.PLACEHOLDER) {
				ATerm ph = ((ATermPlaceholder) last).getPlaceholder();
				if (ph.getType() == ATerm.LIST) {
					throw new RuntimeException(
						"list placeholder not supported in" + " argument list of function application");
				}
			}
		}
		result = "(ATerm)ATmakeAppl" + (arity <= 6 ? String.valueOf(arity) : "") + "(";
		result += prefix + afunRegister.lookup(appl.getAFun());
		for (int i = 0; i < arity; i++) {
			ATerm arg = appl.getArgument(i);
			result += ", " + genConstructorImpl(arg);
		}
		result += ")";
		return result;
	}

	private String buildListConstructorImpl(ATerm pattern) {
		String result = null;
		ATermList list = (ATermList) pattern;
		int length = list.getLength();
		if (length == 0) {
			result = "ATempty";
		} else {
			ATerm last = list.elementAt(length - 1);
			if (last.getType() == ATerm.PLACEHOLDER) {
				ATerm ph = ((ATermPlaceholder) last).getPlaceholder();
				if (ph.getType() == ATerm.LIST) {
					ATermAppl field = (ATermAppl) (((ATermList) ph).getFirst());
					result = "(ATermList)" + StringConversions.makeIdentifier(field.getName());
				}
			}
			if (result == null || result.length()==0) {
				result = "ATmakeList1(" + genConstructorImpl(last) + ")";
			}
			for (int i = length - 2; i >= 0; i--) {
				ATerm elem = list.elementAt(i);
				result = "ATinsert(" + result + ", " + genConstructorImpl(elem) + ")";
			}
		}
		result = "(ATerm)" + result;
		return result;
	}

	private String buildIntConstructorImpl(ATerm pattern) {
		return "(ATerm)ATmakeInt(" + ((ATermInt) pattern).getInt() + ")";
	}

	private String buildRealConstructorImpl(ATerm pattern) {
		return "(ATerm)ATmakeReal(" + ((ATermReal) pattern).getReal() + ")";
	}

	//}}}
	//{{{ private void genConstructors(API api)

	private void genConstructors(ADT api) {
		Iterator types = api.typeIterator();
		bothPrintFoldOpen("constructors");

		while (types.hasNext()) {
			Type type = (Type) types.next();
			String type_name = buildTypeName(type);

			Iterator alts = type.alternativeIterator();
			while (alts.hasNext()) {
				Alternative alt = (Alternative) alts.next();
				String decl = buildConstructorDecl(type, alt);
				hprintln(decl + ";");

				printFoldOpen(decl.toString());
				println(decl);
				println("{");

				println("  return (" + type_name + ")" + genConstructorImpl(alt.getPattern()) + ";");

				println("}");
                printFoldClose();
			}
		}
		bothPrintFoldClose();
	}

	//}}}
	//	{{{ private void genAccessors(API api)

	private void genAccessors(ADT api) {
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			String type_name = buildTypeName(type);
			bothPrintFoldOpen(type_name + " accessors");

			genTypeIsValid(type);

			Iterator alts = type.alternativeIterator();
			while (alts.hasNext()) {
				Alternative alt = (Alternative) alts.next();
				genIsAlt(type, alt);
			}

			Iterator fields = type.fieldIterator();
			while (fields.hasNext()) {
				Field field = (Field) fields.next();
				genHasField(type, field);
				genGetField(type, field);
				genSetField(type, field);
			}

			bothPrintFoldClose();
		}
	}

	//}}}

	//{{{ private void genSortVisitors(API api)

	private void genSortVisitors(ADT api) {
		Iterator types = api.typeIterator();
		bothPrintFoldOpen("sort visitors");

		while (types.hasNext()) {
			Type type = (Type) types.next();
			String type_id = StringConversions.makeIdentifier(type.getId());
			String type_name = buildTypeName(type);

			StringBuffer decl_buf = new StringBuffer();
			String visitor_name = prefix + "visit" + type_id;
			decl_buf.append(type_name);
			decl_buf.append(" ");
			decl_buf.append(visitor_name);
			decl_buf.append("(");
			decl_buf.append(type_name);
			decl_buf.append(" arg");
			Iterator fields = type.fieldIterator();
			while (fields.hasNext()) {
				Field field = (Field) fields.next();
				if (!field.getType().equals(type.getId())) {
					decl_buf.append(", ");
					decl_buf.append(genAcceptor(field));
				}
			}
			decl_buf.append(")");
			String decl = decl_buf.toString();

			hprintln(decl + ";");

			printFoldOpen(decl);
			println(decl);
			println("{");

			Iterator alts = type.alternativeIterator();
			while (alts.hasNext()) {
				Alternative alt = (Alternative) alts.next();
				genSortVisitorAltImpl(type, alt);
			}

			println("  ATabort(\"not a " + type_id + ": %t\\n\", arg);");
			println("  return (" + type_name + ")NULL;");

			println("}");
			printFoldClose();
		}

		bothPrintFoldClose();
	}

	//}}}
	//{{{ private String genAcceptor(Field field)

	private String genAcceptor(Field field) {
		String type = buildTypeName(field.getType());
		String name = "accept" + StringConversions.makeCapitalizedIdentifier(field.getId());

		return type + " (*" + name + ")(" + type + ")";
	}

	//}}}
	//{{{ private void genSortVisitorAltImpl(String type, Alternative alt)

	private void genSortVisitorAltImpl(Type type, Alternative alt) {
		String type_id = StringConversions.makeIdentifier(type.getId());
		String cons_name = buildConstructorName(type, alt);

		println("  if (" + buildIsAltName(type, alt) + "(arg)) {");
		print("    return " + cons_name + "(");
		Iterator fields = type.altFieldIterator(alt.getId());
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			println("");
			print("        ");
			String getter_name = buildGetterName(type, field);
			if (field.getType().equals(type.getId())) {
				String visitor_name = prefix + "visit" + type_id;
				print(visitor_name + "(" + getter_name + "(arg)");
				Iterator params = type.fieldIterator();
				while (params.hasNext()) {
					Field param = (Field) params.next();
					if (!param.getType().equals(type.getId())) {
						print(", ");
						print("accept" + StringConversions.makeCapitalizedIdentifier(param.getId()));
					}
				}
				print(")");
			} else {
				String acceptor_name = "accept" + StringConversions.makeCapitalizedIdentifier(field.getId());
				print(acceptor_name + " ? " + acceptor_name + "(" + getter_name + "(arg))");
				print(" : " + getter_name + "(arg)");
			}
			if (fields.hasNext()) {
				print(",");
			}
		}
		println(");");
		println("  }");
	}

	//}}}

	//{{{ private void genGetField(Type type, Field field)

	private void genGetField(Type type, Field field) {
		String type_name = buildTypeName(type);
		String field_type_name = buildTypeName(field.getType());
		String decl = field_type_name + " " + buildGetterName(type, field) + "(" + type_name + " arg)";

		hprintln(decl + ";");

		printFoldOpen(decl);
		println(decl);
		println("{");
		Iterator locs = field.locationIterator();
		boolean first = true;
		while (locs.hasNext()) {
			Location loc = (Location) locs.next();
			print("  ");
			if (first) {
				first = false;
			} else {
				print("else ");
			}
			if (locs.hasNext()) {
				println("if (" + buildIsAltName(type, loc.getAltId()) + "(arg)) {");
			} else {
				println("");
			}
			print("    return (" + field_type_name + ")");
			Iterator steps = loc.stepIterator();
			String[] type_getter = genReservedTypeGetter(field.getType());
			print(type_getter[0]);
			genGetterSteps(steps, "arg");
			print(type_getter[1]);
			println(";");
			if (locs.hasNext()) {
				println("  }");
			}
		}

		println("}");
		printFoldClose();
	}

	//}}}
	//	{{{ private void genEpilogue(API api)

	private void genEpilogue() {
		hprintln();
		hprintln("#endif /* " + macro + " */");
	}

	//}}}

	//{{{ private void genSetField(Type type, Field field)

	private void genSetField(Type type, Field field) {
		String type_id = StringConversions.makeIdentifier(type.getId());
		String type_name = buildTypeName(type);
		String field_id = StringConversions.makeIdentifier(field.getId());
		String field_type_name = buildTypeName(field.getType());
		String decl =
			type_name
				+ " "
				+ prefix
				+ "set"
				+ type_id
				+ StringConversions.capitalize(field_id)
				+ "("
				+ type_name
				+ " arg, "
				+ field_type_name
				+ " "
				+ field_id
				+ ")";

		hprintln(decl + ";");

		printFoldOpen(decl);
		println(decl);
		println("{");
		Iterator locs = field.locationIterator();
		boolean first = true;
		while (locs.hasNext()) {
			Location loc = (Location) locs.next();
			print("  ");
			if (first) {
				first = false;
			} else {
				print("else ");
			}
			println("if (" + buildIsAltName(type, loc.getAltId()) + "(arg)) {");
			print("    return (" + type_name + ")");
			Iterator steps = loc.stepIterator();
			String arg = genReservedTypeSetterArg(field.getType(), StringConversions.makeIdentifier(field.getId()));
			genSetterSteps(steps, new LinkedList(), arg);
			println(";");
			println("  }");
		}
		println();
		println("  ATabort(\"" + type_id + " has no " + StringConversions.capitalize(field_id) + ": %t\\n\", arg);");
		println("  return (" + type_name + ")NULL;");

		println("}");
		printFoldClose();
	}

	//}}}
	//{{{ private void genSetterSteps(Iterator steps, List parentPath, String arg)

	private void genSetterSteps(Iterator steps, List parentPath, String arg) {
		if (steps.hasNext()) {
			Step step = (Step) steps.next();
			switch (step.getType()) {
				case Step.ARG :
					print("ATsetArgument((ATermAppl)");
					break;
				case Step.ELEM :
					print("ATreplace((ATermList)");
					break;
				case Step.TAIL :
					print("ATreplaceTail((ATermList)");
					break;
			}
			genGetterSteps(parentPath.iterator(), "arg");
			if (step.getType() == Step.TAIL) {
				print(", (ATermList)");
			} else {
				print(", (ATerm)");
			}
			parentPath.add(step);
			genSetterSteps(steps, parentPath, arg);
			print(", " + step.getIndex() + ")");
		} else {
			print(arg);
		}
	}

	//}}}

	//	{{{ private void genGetterSteps(Iterator steps, String arg)

	private void genGetterSteps(Iterator steps, String arg) {
		if (steps.hasNext()) {
			Step step = (Step) steps.next();
			int index = step.getIndex();
			switch (step.getType()) {
				case Step.ARG :
					genGetterSteps(steps, "ATgetArgument((ATermAppl)" + arg + ", " + step.getIndex() + ")");
					break;
				case Step.ELEM :
					if (index == 0) {
						genGetterSteps(steps, "ATgetFirst((ATermList)" + arg + ")");
					} else {
						genGetterSteps(steps, "ATelementAt((ATermList)" + arg + ", " + step.getIndex() + ")");
					}
					break;
				case Step.TAIL :
					if (index == 0) {
						genGetterSteps(steps, arg);
					} else if (index == 1) {
						genGetterSteps(steps, "ATgetNext((ATermList)" + arg + ")");
					} else {
						genGetterSteps(steps, "ATgetTail((ATermList)" + arg + ", " + step.getIndex() + ")");
					}
					break;
			}
		} else {
			print(arg);
		}
	}

	//}}}

	//	{{{ private void genHasField(Type type, Field field)

	private void genHasField(Type type, Field field) {
		String type_id = StringConversions.makeIdentifier(type.getId());
		String type_name = buildTypeName(type);
		String decl =
			"ATbool "
				+ prefix
				+ "has"
				+ type_id
				+ StringConversions.makeCapitalizedIdentifier(field.getId())
				+ "("
				+ type_name
				+ " arg)";

		hprintln(decl + ";");

		printFoldOpen(decl);
		println(decl);
		println("{");
		Iterator locs = field.locationIterator();
		boolean first = true;
		while (locs.hasNext()) {
			Location loc = (Location) locs.next();
			print("  ");
			if (first) {
				first = false;
			} else {
				print("else ");
			}
			println("if (" + buildIsAltName(type, loc.getAltId()) + "(arg)) {");
			println("    return ATtrue;");
			println("  }");
		}
		println("  return ATfalse;");
		println("}");
		printFoldClose();
	}

	//}}}

	//{{{ private String buildConstructorName(Type type, Alternative alt)

	private String buildConstructorName(Type type, Alternative alt) {
		String type_id = StringConversions.makeIdentifier(type.getId());
		String alt_id = StringConversions.makeCapitalizedIdentifier(alt.getId());
		return prefix + "make" + type_id + alt_id;
	}

	//}}}
	//{{{ private String buildConstructorDecl(Type type, Alternative alt)

	private String buildConstructorDecl(Type type, Alternative alt) {
		String type_name = buildTypeName(type);

		StringBuffer decl = new StringBuffer();
		decl.append(type_name + " " + buildConstructorName(type, alt) + "(");
		Iterator fields = type.altFieldIterator(alt.getId());
		boolean first = true;
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			if (first) {
				first = false;
			} else {
				decl.append(", ");
			}
			decl.append(buildTypeName(field.getType()) + " " + StringConversions.makeIdentifier(field.getId()));
		}
		decl.append(")");

		return decl.toString();
	}

	//}}}

	//{{{ private void genTypeIsValid(Type type)

	private void genTypeIsValid(Type type) {
		String type_id = StringConversions.makeIdentifier(type.getId());
		String type_name = buildTypeName(type);
		String decl = "ATbool " + prefix + "isValid" + type_id + "(" + type_name + " arg)";

		printFoldOpen(decl);

		hprintln(decl + ";");
		println(decl);
		println("{");
		Iterator alts = type.alternativeIterator();
		boolean first = true;
		while (alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();
			print("  ");
			if (first) {
				first = false;
			} else {
				print("else ");
			}
			println("if (" + buildIsAltName(type, alt) + "(arg)) {");
			println("    return ATtrue;");
			println("  }");
		}
		println("  return ATfalse;");
		println("}");

		printFoldClose();
	}

	//}}}
	//{{{ private void genIsAlt(Type type, Alternative alt)

	private void genIsAlt(Type type, Alternative alt) {
		String type_name = buildTypeName(type);
		String decl = "inline ATbool " + buildIsAltName(type, alt) + "(" + type_name + " arg)";
		String pattern;
		StringBuffer match_code = new StringBuffer();
		boolean contains_placeholder = alt.containsPlaceholder();
		int alt_count = type.getAlternativeCount();
		boolean inverted = false;

		//{{{ Create match_code

		pattern = prefix + "pattern" + StringConversions.makeIdentifier(type.getId()) + StringConversions.makeCapitalizedIdentifier(alt.getId());
	
			match_code.append("ATmatchTerm((ATerm)arg, " + pattern);
			Iterator fields = type.altFieldIterator(alt.getId());
			while (fields.hasNext()) {
				fields.next();
				match_code.append(", NULL");
			}
			match_code.append(")");
		
		//}}}

		hprintln(decl + ";");

		printFoldOpen(decl);
		println(decl);
		println("{");
		if (false && inverted) {
			println("  return !(" + match_code + ");");
		} else if (false && alt_count != 1 && !contains_placeholder) {
			println("  return " + match_code + ";");
		} else {
			AlternativeList alts_left = type.getAlternatives();
			alts_left.remove(alt);

			alt_count = alts_left.size();
			int pat_type = alt.getPatternType();
			alts_left.keepByType(pat_type);
			if (alts_left.size() != alt_count) {
				//{{{ Check term types

				println("  if (ATgetType((ATerm)arg) != " + getATermTypeName(pat_type) + ") {");
				println("    return ATfalse;");
				println("  }");

				//}}}
			}
			if (pat_type == ATerm.APPL) {
				//{{{ Check function symbols
				if (alt instanceof ATermAppl) {
					AFun afun = ((ATermAppl) alt.buildMatchPattern()).getAFun();
					alt_count = alts_left.size();
					alts_left.keepByAFun(afun);
					if (alts_left.size() < alt_count) {
						println("  if (ATgetAFun((ATermAppl)arg) != ATgetAFun(" + pattern + ")) {");
						println("    return ATfalse;");
						println("  }");
					}
				}

				//}}}
			} else if (pat_type == ATerm.LIST) {
				//{{{ Check list length

				ATermList matchPattern = (ATermList) alt.buildMatchPattern();
				if (matchPattern.isEmpty()) {
					alts_left.clear();
					println("  if (!ATisEmpty((ATermList)arg)) {");
					println("    return ATfalse;");
					println("  }");
				} else if (!matchPattern.equals(matchPattern.getFactory().parse("[<list>]"))) {
					alts_left.removeEmptyList();
					println("  if (ATisEmpty((ATermList)arg)) {");
					println("    return ATfalse;");
					println("  }");
				}

				//}}}
			}

			if (alts_left.size() == 0) {
				println("#ifndef DISABLE_DYNAMIC_CHECKING");
				println("  assert(arg != NULL);");
				println("  assert(" + match_code + ");");
				println("#endif");
				println("  return ATtrue;");
			} else {
				println("  {");
				println("    static ATerm last_arg = NULL;");
				println("    static int last_gc = -1;");
				println("    static ATbool last_result;");
				println();
				println("    assert(arg != NULL);");
				println();
				println("    if (last_gc != ATgetGCCount() || " + "(ATerm)arg != last_arg) {");
				println("      last_arg = (ATerm)arg;");
				println("      last_result = " + match_code + ";");
				println("      last_gc = ATgetGCCount();");
				println("    }");
				println();
				println("    return last_result;");
				println("  }");
			}
		}
		println("}");

		printFoldClose();
	}

	//}}}

	//	{{{ private String[] genReservedTypeGetter(String type)

	private String[] genReservedTypeGetter(String type) {
		String pre = "";
		String post = "";

		if (type.equals("int")) {
			pre = "ATgetInt((ATermInt)";
			post = ")";
		} else if (type.equals("real")) {
			pre = "ATgetReal((ATermReal)";
			post = ")";
		} else if (type.equals("str")) {
			pre = "ATgetName(ATgetAFun((ATermAppl)";
			post = "))";
		}

		String[] result = { pre, post };
		return result;
	}

	//}}}
	//{{{ private String genReservedTypeSetterArg(String type, String id)

	private String genReservedTypeSetterArg(String type, String id) {
		if (type.equals("int")) {
			return "ATmakeInt(" + id + ")";
		} else if (type.equals("real")) {
			return "ATmakeReal(" + id + ")";
		} else if (type.equals("str")) {
			return "ATmakeAppl0(ATmakeAFun(" + id + ", 0, ATtrue))";
		} else {
			return id;
		}
	}

	//}}}

	//{{{ private String getATermTypeName(int type)

	private String getATermTypeName(int type) {
		switch (type) {
			case ATerm.INT :
				return "AT_INT";
			case ATerm.REAL :
				return "AT_REAL";
			case ATerm.APPL :
				return "AT_APPL";
			case ATerm.LIST :
				return "AT_LIST";
			case ATerm.PLACEHOLDER :
				return "AT_PLACEHOLDER";
			case ATerm.BLOB :
				return "AT_BLOB";
			default :
				throw new RuntimeException("illegal ATerm type: " + type);
		}
	}

	//}}}

	//{{{ private String buildIsAltName(Type type, Alternative alt)

	private String buildIsAltName(Type type, Alternative alt) {
		return buildIsAltName(type, alt.getId());
	}

	//}}}
	//{{{ private String buildIsAltName(Type type, String altId)

	private String buildIsAltName(Type type, String altId) {
		return prefix
			+ "is"
			+ StringConversions.makeIdentifier(type.getId())
			+ StringConversions.makeCapitalizedIdentifier(altId);
	}

	//}}}

	//{{{ private String buildGetterName(Type type, Field field)

	private String buildGetterName(Type type, Field field) {
		String type_id = StringConversions.makeIdentifier(type.getId());
		String fieldId = StringConversions.makeCapitalizedIdentifier(field.getId());

		return prefix + "get" + type_id + fieldId;
	}

	//}}}

	//{{{ private void printFoldOpen(PrintStream out, String comment)

	private String buildTypeName(Type type) {
		return buildTypeName(type.getId());
	}

	//}}}
	//{{{ private String buildTypeName(String typeId)

	private String buildTypeName(String typeId) {
		TypeConverter conv = new TypeConverter(new CTypeConversions());

		String name = conv.getType(typeId);

		if (conv.isReserved(typeId)) {
			return name;
		} else {
			return prefix + StringConversions.makeCapitalizedIdentifier(name);
		}
	}

	//}}}

	public AFunRegister getAFunRegister() {
		return afunRegister;
	}

}
