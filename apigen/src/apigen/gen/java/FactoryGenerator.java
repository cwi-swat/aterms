package apigen.gen.java;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.ListType;
import apigen.adt.SeparatedListType;
import apigen.adt.Type;
import apigen.adt.api.types.Module;
import apigen.adt.api.types.Separators;
import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;

public class FactoryGenerator extends JavaGenerator {
    private static final String CLASS_NAME = "Factory";
    private ADT adt;
    private String apiName;
    private Module module;
    private TypeConverter typeConverter = new TypeConverter(new JavaTypeConversions("factory"));
    
    public FactoryGenerator(ADT adt, JavaGenerationParameters params, Module module) {
        super(params);
        this.adt = adt;
        this.apiName = params.getApiExtName(module);
        this.module = module;
      }

    public String getClassName() {
    		return FactoryGenerator.className(module.getModulename().getName());
    }
    
    public static String className(String moduleName) {
    		return moduleName + CLASS_NAME;
    }

    public String qualifiedClassName(JavaGenerationParameters params) {
    		return FactoryGenerator.qualifiedClassName(params,module.getName());
    }

    public static String qualifiedClassName(
    				JavaGenerationParameters params,
				String moduleName) {
      StringBuffer buf = new StringBuffer();
      String pkg = params.getPackageName();

      if (pkg != null) {
          buf.append(pkg);
          buf.append('.');
      }
      buf.append(params.getApiExtName(moduleName).toLowerCase());
      buf.append('.');
      buf.append(FactoryGenerator.className(moduleName));
      return buf.toString();
  }

    protected void generate() {
        printPackageDecl();
        printImports();
        genFactoryClass(adt);
    }

    private void genFactoryClass(ADT api) {
        println("public class " + getClassName() + " {");
        genPrivateMembers(api);
        genEmptyLists(api);
        genConstructor();
        genPureFactoryGetter();
        genInitialize(api);
        println("/*genAlternativeMethods*/");
        genAlternativeMethods(api, false);
        println("/*genMakeLists*/");
        genMakeLists(api, false);
        println("/*genTypeFromTermMethods*/");
        genTypeFromTermMethods(api, false);
        println("/*genTypeFromMethods*/");
        genTypeFromMethods(api, false);
        
        genForwardingMethod(api);
        
        genConversions();
        println("}");
    }

    private void genForwardingMethod(ADT api) {
    	println("/*genForwardingAlternativeMethods*/");
      genAlternativeMethods(api, true);
      println("/*genForwardingMakeLists*/");
      genMakeLists(api, true);
      println("/*genForwardingTypeFromTermMethods*/");
      genTypeFromTermMethods(api, true);
      println("/*TODOgenForwardingTypeFromMethods*/");
      genTypeFromMethods(api, true);
		
    }
    
  	private void genConversions() {
        genCharsToString();
	genCharToByte();
        genStringToChars();
	genByteToChar();
    }

    private void genCharToByte() {
        println("  public static char charToByte(aterm.ATerm arg) {");
        println();
        println("      return((char) ((aterm.ATermInt) arg).getInt());");
        println("  }");
        println();
    }

    private void genCharsToString() {
        println("  public static String charsToString(aterm.ATerm arg) {");
        println("    aterm.ATermList list = (aterm.ATermList) arg;");
        println("    StringBuffer str = new StringBuffer();");
        println();
        println("    for ( ; !list.isEmpty(); list = list.getNext()) {");
        println("      str.append((char) ((aterm.ATermInt) list.getFirst()).getInt());");
        println("    }");
        println();
        println("    return str.toString();");
        println("  }");
        println();
    }

    private void genByteToChar() {
        println("  public aterm.ATerm byteToChar(char ch) {");
        println("      return getPureFactory().makeInt((int) ch);");
        println("  }");
        println();
    }

    private void genStringToChars() {
        println("  public aterm.ATerm stringToChars(String str) {");
        println("    int len = str.length();");
        println("    byte chars[] = str.getBytes();");
        println("    aterm.ATermList result = getPureFactory().makeList();");
        println();
        println("    for (int i = len - 1; i >= 0; i--) {");
        println("      result = result.insert(getPureFactory().makeInt(chars[i]));");
        println("    }");
        println();
        println("    return (aterm.ATerm) result;");
        println("  }");
        println();
    }

    private void genPureFactoryGetter() {
        println("  public aterm.pure.PureFactory getPureFactory() {");
        println("    return factory;");
        println("  }");
        println();
    }

    private void genTypeFromTermMethods(ADT api, boolean forwarding) {
    	Set moduleToGen = new HashSet();
    	if(!forwarding) {
    		moduleToGen.add(module.getModulename().getName());
    	} else {
    		moduleToGen = adt.getImportsClosureForModule(module.getModulename().getName());
    		moduleToGen.remove(module.getModulename().getName());
    	}
    	Iterator moduleIt = moduleToGen.iterator();
    	while(moduleIt.hasNext()) {
    		String moduleName = (String) moduleIt.next();
    		
    		Iterator types = api.typeIterator(moduleName);

        while (types.hasNext()) {
            Type type = (Type) types.next();

            if (type instanceof ListType) {
                if (type instanceof SeparatedListType) {
                    genSeparatedListFromTermMethod((SeparatedListType) type, forwarding, moduleName);
                } else {
                    genListFromTerm((ListType) type, forwarding, moduleName);
                }
            } else if (!typeConverter.isReserved(type.getId())) {
                genTypeFromTermMethod(type, forwarding, moduleName);
            }
        }
       }
    }

    private void genTypeFromMethods(ADT api, boolean forwarding) {
    	Set moduleToGen = new HashSet();
    	if(!forwarding) {
    		moduleToGen.add(module.getModulename().getName());
    	} else {
    		moduleToGen = api.getImportsClosureForModule(module.getModulename().getName());
    		moduleToGen.remove(module.getModulename().getName());
    	}
    	Iterator moduleIt = moduleToGen.iterator();
    	while(moduleIt.hasNext()) {
    		String moduleName = (String) moduleIt.next(); 
    		
    		Iterator types = api.typeIterator(moduleName);

        while (types.hasNext()) {
            Type type = (Type) types.next();
            if (!typeConverter.isReserved(type.getId())) {
              genTypeFromStringMethod(type);
              genTypeFromFileMethod(type);
            }
        }
    	}
    }

    private void genInitialize(ADT api) {
        println("  private void initialize() {");
        genImportedFactoryInitialization();
        genFactoryInitialization(api);
        println("  }");
        println();
    }

    private void genConstructor() {
        println("  private " + getClassName() + "(aterm.pure.PureFactory factory) {");
        println("    this.factory = factory;");
        println("  }");
        println();
        println("  private static " + getClassName() + " instance = null;");
        println();
        println("  public static " + getClassName() + " getInstance(aterm.pure.PureFactory factory) {");
        println("    if (instance == null) {");
        println("        instance = new " + getClassName() + "(factory);");
        println("        instance.initialize();");
        println("    }");
        println("    if (instance.factory != factory) {");
        println("        throw new RuntimeException(\"Dont create two " + getClassName() + " factories with differents PureFactory \");");
        println("    } else {");
        println("        return instance;");
        println("    }");
        println("  }");
        println();
    }

    private void genEmptyLists(ADT api) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        Iterator types = api.typeIterator(module);
        while (types.hasNext()) {
            Type type = (Type) types.next();
            if (type instanceof ListType) {
                String typeName = TypeGenerator.qualifiedClassName(params, type.getId());
                String emptyName = emptyListVariable(type);
                println("  private " + typeName + ' ' + emptyName + ";");
            }
        }
        println();
    }

    private void genMakeLists(ADT api, boolean forwarding) {
    	Set moduleToGen = new HashSet();
    	if(!forwarding) {
    		moduleToGen.add(module.getModulename().getName());
    	} else {
    		moduleToGen = api.getImportsClosureForModule(module.getModulename().getName());
    		moduleToGen.remove(module.getModulename().getName());
    	}
    	Iterator moduleIt = moduleToGen.iterator();
    	while(moduleIt.hasNext()) {
    		String moduleName = (String) moduleIt.next();
    	
        Iterator types = api.typeIterator(moduleName);

        while (types.hasNext()) {
            Type type = (Type) types.next();
            if (type instanceof ListType) {
                ListType listType = (ListType) type;
                JavaGenerationParameters params = getJavaGenerationParameters();
                String returnTypeName = TypeGenerator.qualifiedClassName(params, type.getId());
                String methodName = "make" + TypeGenerator.className(type);
                String paramTypeName =
                		TypeGenerator.qualifiedClassName(params, listType.getElementType());
                String empty = emptyListVariable(type);
                String proto = protoListVariable(type);

                if (type instanceof SeparatedListType) {
                    SeparatedListType sepListType = (SeparatedListType) type;
                    genMakeEmptyList(returnTypeName, methodName, empty, forwarding, moduleName);
                    genMakeSingletonSeparatedList(
                        returnTypeName,
                        methodName,
                        paramTypeName,
                        sepListType,
                        empty,
												forwarding, moduleName);
                    genMakeManySeparatedList(
                        sepListType.getElementType(),
                        returnTypeName,
                        methodName,
                        paramTypeName,
                        sepListType,
												forwarding, moduleName);
                    if(!forwarding) {
                    	genMakeManySeparatedTermList(
                        returnTypeName,
                        methodName,
                        proto,
                        sepListType);
                    }
                    genMakeFixedSizedSeparatedList(
                        returnTypeName,
                        methodName,
                        sepListType,
												forwarding, moduleName);
                    genReverseSeparatedLists(sepListType, methodName, forwarding, moduleName);
                    genConcatSeparatedLists(sepListType, methodName, forwarding, moduleName);
                    genAppendSeparatedLists(sepListType, methodName, forwarding, moduleName);
                } else {
                    genMakeEmptyList(returnTypeName, methodName, empty, forwarding, moduleName);
                    genMakeSingletonList(
                        returnTypeName,
                        methodName,
                        paramTypeName,
                        empty,
												forwarding, moduleName);
                    genMakeManyList(
                        listType.getElementType(),
                        returnTypeName,
                        methodName,
                        paramTypeName,
												forwarding, moduleName);
                    if (!forwarding) {
                    	genMakeManyTermList(returnTypeName, methodName, proto);
                    }
                    genMakeFixedSizedList(returnTypeName, methodName, listType,
                    		forwarding, moduleName);
                    genReverseLists(listType, methodName,
                    		forwarding, moduleName);
                    genConcatLists(listType, methodName, forwarding, moduleName);
                    genAppendLists(listType, methodName, forwarding, moduleName);
                }
            }
        }
    	}
    }
  	
    private void genAppendLists(ListType type, String makeMethodName,boolean forwarding,
    		String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String className = TypeGenerator.qualifiedClassName(params, type.getId());
        String elementTypeName =
            TypeGenerator.qualifiedClassName(params, type.getElementType());

        println(
            "  public "
                + className
                + " append"
                + "("
                + className
                + " list, "
                + elementTypeName
                + " elem) {");
        if(!forwarding) {
        	println("    return concat(list, " + makeMethodName + "(elem));");
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+".append(list, elem);");
        }
        println("  }");
        println();
    }

    private void genConcatLists(ListType type, String makeMethodName,boolean forwarding,
    		String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String className = TypeGenerator.qualifiedClassName(params, type.getId());

        println(
            "  public "
                + className
                + " concat("
                + className
                + " arg0, "
                + className
                + " arg1) {");
        if(!forwarding) {
        	println("    " + className + " result = arg1;");
        	println();
        	println(
            "    for ("
                + className
                + " list = reverse(arg0); !list.isEmpty(); list = list.getTail()) {");
        	println("      result = " + makeMethodName + "(list.getHead(), result);");
        	println("    }");
        	println();
        	println("    return result;");
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+".concat(arg0, arg1);");
        }
        println("  }");
        println();
    }

    private void genReverseLists(ListType type, String makeMethodName, boolean forwarding,
    		String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String className = TypeGenerator.qualifiedClassName(params, type.getId());

        println("  public " + className + " reverse(" + className + " arg) {");
        if(!forwarding) {
        	println("    " + className + " reversed = " + makeMethodName + "();");
        	println("    while (!arg.isEmpty()) {");
        	println("      reversed = " + makeMethodName + "(arg.getHead(), reversed);");
        	println("      arg = arg.getTail();");
        	println("    }");
        	println("    return reversed;");
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+".reverse(arg);");
        }
        println("  }");
        println();
    }

    private void genMakeFixedSizedList(
        String returnTypeName,
        String methodName,
        ListType type,
				boolean forwarding,
				String moduleName) {
        for (int i = 2; i < 7; i++) {
            genMakeFixedSizedList(returnTypeName, methodName, type, i, forwarding, moduleName);
        }
    }

    private void genMakeFixedSizedList(
        String returnTypeName,
        String methodName,
        ListType type,
        int size,
				boolean forwarding,
				String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String qualifiedElementName =
            TypeGenerator.qualifiedClassName(params, type.getElementType());
        String formalElems = buildFormalArgumentList(qualifiedElementName, "elem", size);
        String actualElems = buildActualArgumentList("elem", 1, size);
        println(
            "  public " + returnTypeName + " " + methodName + "(" + formalElems + ") {");
        if(!forwarding) {
        	println(
            "    return "
                + methodName
                + "(elem0, "
                + methodName
                + "("
                + actualElems
                + "));");
    		} else {//forwarding
    			println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+methodName+"(elem0, "+actualElems+");");
    		}
        println("  }");
        println();
    }

    private void genMakeFixedSizedSeparatedList(
        String returnTypeName,
        String methodName,
        SeparatedListType type,
				boolean forwarding,
				String moduleName) {
        for (int i = 2; i < 7; i++) {
            genMakeFixedSizedSeparatedList(returnTypeName, methodName, type, i, forwarding, moduleName);
        }
    }

    private void genMakeFixedSizedSeparatedList(
        String returnTypeName,
        String methodName,
        SeparatedListType type,
        int size,
				boolean forwarding,
				String moduleName) {
        String formalSeps = buildFormalSeparatorArguments(type);
        String actualSeps = buildActualSeparatorArgumentList(type, false);

        if (!formalSeps.equals("")) {
            formalSeps += ", ";
            actualSeps += ", ";
        }

        JavaGenerationParameters params = getJavaGenerationParameters();
        String qualifiedElementName =
            TypeGenerator.qualifiedClassName(params, type.getElementType());
        String formalElems = buildFormalArgumentList(qualifiedElementName, "elem", size);
        String actualElems = buildActualArgumentList("elem", 1, size);
        println(
            "  public "
                + returnTypeName
                + " "
                + methodName
                + "("
                + formalSeps
                + formalElems
                + ") {");

        String recursiveActualSeps = "";
        if (size > 2) {
            // a singleton does not have separators
            recursiveActualSeps = actualSeps;
        }
        if(forwarding) {
        	methodName = FactoryGenerator.className(moduleName).toLowerCase()+"."+methodName;
        }
        println(
            "    return "
                + methodName
                + "(elem0, "
                + actualSeps
                + methodName
                + "("
                + recursiveActualSeps
                + actualElems
                + "));");
        
        println("  }");
        println();
    }

    private String buildActualArgumentList(String arg, int from, int to) {
        StringBuffer buf = new StringBuffer();
        int i = from;

        while (i < to) {
            buf.append(arg);
            buf.append(i);

            i++;
            if (i < to) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

    private String buildFormalArgumentList(String type, String arg, int size) {
        StringBuffer buf = new StringBuffer();
        int i = 0;

        while (i < size) {
            buf.append(type);
            buf.append(' ');
            buf.append(arg);
            buf.append(i);

            i++;
            if (i < size) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

    private void genAppendSeparatedLists(SeparatedListType type, String makeMethodName,
    			boolean forwarding, String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String qualifiedClassName = TypeGenerator.qualifiedClassName(params, type.getId());
        String className = TypeGenerator.className(type);
        String formalSeps = buildFormalSeparatorArguments(type);
        String actualSeps = buildActualSeparatorArgumentList(type, false);
        String elementTypeName =
            TypeGenerator.qualifiedClassName(params, type.getElementType());

        if (formalSeps.length() > 0) {
            formalSeps += ", ";
            actualSeps += ", ";
        }

        println(
            "  public "
                + qualifiedClassName
                + " append"
                + className
                + "("
                + qualifiedClassName
                + " list, "
                + formalSeps
                + elementTypeName
                + " elem) {");
        if(!forwarding) {
        	println("    return concat(list, " + actualSeps + makeMethodName + "(elem));");
        } else {//forwarding
        	println("   return "+FactoryGenerator.className(moduleName).toLowerCase()+".append"+className+"(list, "+actualSeps+" elem);");
        }
        println("  }");
    }

    private String buildActualSeparatorArgumentList(
        SeparatedListType type,
        boolean convert) {
        return buildActualTypedAltArgumentList(type.separatorFieldIterator(), convert);
    }

    private String buildFormalSeparatorArguments(SeparatedListType type) {
        return buildFormalTypedArgumentList(type.separatorFieldIterator());
    }

    private void genConcatSeparatedLists(SeparatedListType type, String makeMethodName,
    			boolean forwarding, String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String className = TypeGenerator.qualifiedClassName(params, type.getId());
        String sepArgs = buildOptionalSeparatorArguments(type);
        String formalSepArgs = buildFormalSeparatorArguments(type);
        if (formalSepArgs.length() > 0) {
            formalSepArgs += ", ";
        }

        println(
            "  public "
                + className
                + " concat("
                + className
                + " arg0, "
                + formalSepArgs
                + className
                + " arg1) {");
        if(!forwarding) {
        	println("    if (arg0.isEmpty()) {");
        	println("      return arg1;");
        	println("    }");
        	println("    " + className + " list = reverse(arg0);");
        	println("    " + className + " result = arg1;");
        	println("");
        	println("    while(!list.isSingle()) {");
        	println(
            "      result = "
                + makeMethodName
                + "(list.getHead(), "
                + sepArgs
                + "result);");

        	Iterator seps = type.separatorFieldIterator();
        	while (seps.hasNext()) {
            Field sep = (Field) seps.next();
            String fieldId = JavaGenerator.getFieldId(sep.getId());
            String fieldGet =
                "get" + StringConversions.makeCapitalizedIdentifier(sep.getId());
            println("      " + fieldId + " = list." + fieldGet + "();");

        	}
        	println("      list = list.getTail();");
        	println("    }");
        	println();
        	println(
            "    return " + makeMethodName + "(list.getHead(), " + sepArgs + "result);");
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+".concat(arg0, "+sepArgs+" arg1);");
        }
        println("  }");
        println();
    }

    private void genReverseSeparatedLists(
        SeparatedListType type,
        String makeMethodName,
				boolean forwarding,
				String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String className = TypeGenerator.qualifiedClassName(params, type.getId());
        
        println("  public " + className + " reverse(" + className + " arg) {");
        if(!forwarding) {
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
        	println(
        			"    " + className + " result = " + makeMethodName + "(nodes[0].getHead());");
        	println();
        	println("    for (int i = 1; i < length; i++) {");
        	String elementType =
        		TypeGenerator.qualifiedClassName(params, type.getElementType());
        	println("      " + elementType + " _head = nodes[i].getHead();");

        	Iterator separators = type.separatorFieldIterator();
        	while (separators.hasNext()) {
            Field separator = (Field) separators.next();
            String fieldId = separator.getId();
            String fieldName = JavaGenerator.getFieldId(fieldId);
            String fieldType =
                TypeGenerator.qualifiedClassName(params, separator.getType());
            String capitalizedFieldId =
                StringConversions.makeCapitalizedIdentifier(fieldId);
            String fieldGetter = "get" + capitalizedFieldId + "()";
            println(
                "      "
                    + fieldType
                    + ' '
                    + fieldName
                    + " = nodes[i-1]."
                    + fieldGetter
                    + ";");
        	}

        	String seps = buildOptionalSeparatorArguments(type);
        	println("      result = " + makeMethodName + "(_head, " + seps + "result);");
        	println("    }");
        	println();
        	println("    return result;");
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+".reverse(arg);");
        }
        println("  }");
        println();
    }

    private void genMakeManyTermList(
        String returnTypeName,
        String methodName,
        String protoName) {
        println(
            "  public "
                + returnTypeName
                + ' '
                + methodName
                + "(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {");
        println("    synchronized (" + protoName + ") {");
        println("      " + protoName + ".initHashCode(annos, head, tail);");
        println(
            "      return (" + returnTypeName + ") factory.build(" + protoName + ");");
        println("    }");
        println("  }");
        println();
    }

    private void genMakeManyList(
        String paramType,
        String returnTypeName,
        String methodName,
        String paramTypeName,
				boolean forwarding,
				String moduleName) {
        String head;

        if (!getConverter().isReserved(paramType)) {
            head = "(aterm.ATerm) head";
        } else if (paramType.equals("int")) {
            head = "factory.makeInt(head)";
        } else if (paramType.equals("real")) {
            head = "factory.makeReal(head)";
        } else if (paramType.equals("str")) {
            head = "factory.makeAppl(factory.makeAFun(head,0,true))";
        } else {
            head = "head";
        }
        
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
        if(!forwarding) {
        	println(
            "    return ("
                + returnTypeName
                + ") "
                + methodName
                + "("
                + head
                + ", (aterm.ATermList) tail, factory.getEmpty());");
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+methodName+"(head, tail);");
        }
        println("  }");
        println();
    }

    private void genMakeManySeparatedList(
        String paramType,
        String returnTypeName,
        String methodName,
        String paramTypeName,
        SeparatedListType type,
				boolean forwarding,
				String moduleName) {
        String formalSeps = buildFormalSeparatorArguments(type);
        String actualSeps =
            buildActualTypedAltArgumentListNoConversion(type.separatorFieldIterator());
        if (formalSeps.length() > 0) {
            formalSeps += ", ";
            actualSeps += ", ";
        }

        String head;
        if (!getConverter().isReserved(paramType)) {
            head = "(aterm.ATerm) head";
        } else if (paramType.equals("int")) {
            head = "factory.makeInt(head)";
        } else if (paramType.equals("real")) {
            head = "factory.makeReal(head)";
        } else if (paramType.equals("str")) {
            head = "factory.makeAppl(factory.makeAFun(head,0,true))";
        } else {
            head = "head";
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
        if(!forwarding) {
        	println(
            "    return ("
                + returnTypeName
                + ") "
                + methodName
                + "("
                + head
                + ", "
                + actualSeps
                + "(aterm.ATermList) tail, factory.getEmpty());");
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+methodName+"(head, tail);");
        }
        println("  }");
        println();
    }

    private void genMakeManySeparatedTermList(
        String returnTypeName,
        String methodName,
        String proto,
        SeparatedListType type) {
        String formalSeps = buildFormalSeparatorArguments(type);
        String actualSeps =
            buildActualTypedAltArgumentListNoConversion(type.separatorFieldIterator());

        if (formalSeps.length() > 0) {
            formalSeps += ", ";
            actualSeps += ", ";
        }

        println(
            "  public "
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

    private void genMakeSingletonList(
        String returnTypeName,
        String methodName,
        String paramTypeName,
        String empty,
				boolean forwarding,
				String moduleName) {
        println(
            "  public "
                + returnTypeName
                + ' '
                + methodName
                + '('
                + paramTypeName
                + " elem) {");
        if(!forwarding) {
        	println(
            "    return ("
                + returnTypeName
                + ") "
                + methodName
                + "(elem, "
                + empty
                + ");");
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+methodName+"(elem);");
        }
        println("  }");
        println();
    }

    private void genMakeSingletonSeparatedList(
        String returnTypeName,
        String methodName,
        String paramTypeName,
        SeparatedListType type,
        String empty,
				boolean forwarding,
				String moduleName) {
        String separators = buildActualNullArgumentList(type.separatorFieldIterator());
        if (separators.length() > 0) {
            separators += ", ";
        }

        println(
            "  public "
                + returnTypeName
                + ' '
                + methodName
                + "("
                + paramTypeName
                + " elem ) {");
        if(!forwarding) {
        	println(
            "    return ("
                + returnTypeName
                + ") "
                + methodName
                + "(elem, "
                + separators
                + empty
                + ");");
        } else { //forwarding 
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+methodName+"(elem);");
        }
        println("  }");
        println();
    }

    private void genMakeEmptyList(
        String returnTypeName,
        String methodName,
        String empty,
				boolean forwarding,
				String moduleName) {
        println("  public " + returnTypeName + ' ' + methodName + "() {");
        if(!forwarding) {
        	println("    return " + empty + ";");
        } else {//forwarding 
        	println("    return " + FactoryGenerator.className(moduleName).toLowerCase() + "."+methodName+"();");
        }
        println("  }");
        println();
    }

    private void genPrivateMembers(ADT api) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        
        // TODO: maybe ATermFactory is enough instead of PureFactory
        println("  private aterm.pure.PureFactory factory;");
        println();
        genFactoryForImportedModules();
        println();
        Iterator types = api.typeIterator(module);
        while (types.hasNext()) {
            Type type = (Type) types.next();
            genTypeMembers(type, params);
            println();
        }
    }

    private void genFactoryForImportedModules() {
    		Set importedModules = new HashSet();
    		importedModules = adt.getImportsClosureForModule(module.getModulename().getName());
    		importedModules.remove(module.getModulename().getName());
    	
    		Iterator it = importedModules.iterator();
    		String moduleName;
    		while(it.hasNext()) {
    			moduleName = (String) it.next();
    			println("  private "+
    					FactoryGenerator.qualifiedClassName(getJavaGenerationParameters(), moduleName)+
							" "+FactoryGenerator.className(moduleName).toLowerCase()+";");
    		}
    }
    
    private void genTypeMembers(Type type, JavaGenerationParameters params) {
        if (type instanceof ListType) {
            genListTypeMembers(type, params);
        } else if (!typeConverter.isReserved(type.getId())) {
            genConstructorTypeMembers(type, params);
        }
    }

    private void genConstructorTypeMembers(Type type, JavaGenerationParameters params) {
        Iterator alts = type.alternativeIterator();

        while (alts.hasNext()) {
            Alternative alt = (Alternative) alts.next();
            genAlternativeMembers(type, alt, params);
        }
    }

    private void genAlternativeMembers(Type type, Alternative alt, JavaGenerationParameters params) {
        String typeClassName = TypeGenerator.qualifiedClassName(params, type.getId());            
        String funVar = funVariable(type, alt);
        String protoVar = prototypeVariable(type, alt);
        String patternVar = patternVariable(type, alt);

        println("  private aterm.AFun " + funVar + ';');
        println("  private " + typeClassName + " " + protoVar + ';');
        println("  private aterm.ATerm " + patternVar + ';');
    }

    private void genListTypeMembers(Type type, JavaGenerationParameters params) {
        String typeClassName = TypeGenerator.qualifiedClassName(params, type.getId());
        String protoVar = protoListVariable(type);
        println("  private " + typeClassName + ' ' + protoVar + ';');
        
        if (type instanceof SeparatedListType) {
          println("  private aterm.ATerm " + patternListVariable(type) + ';');
        }
    }
    
    private void genAlternativeMethods(ADT api, boolean forwarding) {
    	Set moduleToGen = new HashSet();
    	if(!forwarding) {
    		moduleToGen.add(module.getModulename().getName());
    	} else {
    		moduleToGen = api.getImportsClosureForModule(module.getModulename().getName());
    		moduleToGen.remove(module.getModulename().getName());
    	}
    	
    	Iterator moduleIt = moduleToGen.iterator();
    	while(moduleIt.hasNext()) {
    		String moduleName = (String) moduleIt.next();
    		Iterator types = api.typeIterator(moduleName);
    		while (types.hasNext()) {
           Type type = (Type) types.next();
            if (type instanceof ListType) {
               if (type instanceof SeparatedListType) {
                   genSeparatedListToTerm((SeparatedListType) type, forwarding, moduleName);
               }
           } else if (!typeConverter.isReserved(type.getId())) {
             /* no make method for operators with builtin codomain */
               Iterator alts = type.alternativeIterator();
               while (alts.hasNext()) {
                   Alternative alt = (Alternative) alts.next();
                   genInternalMakeMethod(type, alt, forwarding, moduleName);
                   genMakeMethod(type, alt, forwarding, moduleName);
                   if(!forwarding) {
                   	genAltFromTerm(type, alt);
                   }
                   genAltToTerm(type, alt, forwarding, moduleName);
                }
            }
        }
    	}
    }

    private void genMakeMethod(Type type, Alternative alt, boolean forwarding, String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String altClassName = AlternativeGenerator.qualifiedClassName(params, type, alt);
        String makeMethodName = "make" + concatTypeAlt(type, alt);
        String funVar = funVariable(type, alt);

        print("  public " + altClassName + ' ' + makeMethodName + "(");
        printFormalTypedAltArgumentList(type, alt);
        println(") {");
        if(!forwarding) {
        	print("    aterm.ATerm[] args = new aterm.ATerm[] {");
        	printActualTypedArgumentList(type, alt);
        	println("};");
        	println(
            "    return "
                + makeMethodName
                + "("
                + funVar
                + ", args, factory.getEmpty());");
        } else {//forwarding
        Iterator fields = type.altFieldIterator(alt.getId());
        	print("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+makeMethodName+"(");
        	// printFormalTypedAltArgumentList(type, alt);
        	print(buildActualTypedAltArgumentList(fields, false));
        	println(");");
        }
        println("  }");
        println();
    }

    private void genInternalMakeMethod(Type type, Alternative alt, boolean forwarding, String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String altClassName = AlternativeGenerator.qualifiedClassName(params, type, alt);
        String protoVar = prototypeVariable(type, alt);
        String methodName = "make" + concatTypeAlt(type, alt);
        
        print("  public " + altClassName + " "+methodName);
        println("(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {");
        if(!forwarding) {
        	println("    synchronized (" + protoVar + ") {");
        	println("      " + protoVar + ".initHashCode(annos, fun, args);");
        	println("      return (" + altClassName + ") factory.build(" + protoVar + ");");
        	println("    }");
        } else { //forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+methodName+"(fun, args, annos);");
        }
        println("  }");
        println();
    }

    private void genAltToTerm(Type type, Alternative alt, boolean forwarding, String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String className = AlternativeGenerator.qualifiedClassName(params, type, alt);
        
        println("  public aterm.ATerm toTerm(" + className + " arg) {");
        if(!forwarding) {
            println("    java.util.List args = new java.util.LinkedList();");
            
            Iterator fields = type.altFieldIterator(alt.getId());
            genAddFieldsToListCalls(fields);
            println("    return factory.make(" + patternVariable(type, alt) + ", args);");
        } else{
            println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+".toTerm(arg);");
        }
        println("  }");
        println();
    }

    private void genAddFieldsToListCalls(Iterator fields) {
        for (int i = 0; fields.hasNext(); i++) {
            Field field = (Field) fields.next();
            String field_type = field.getType();
            String getArgumentCall =
                "arg.get"
                    + StringConversions.makeCapitalizedIdentifier(field.getId())
                    + "()";

            if (field_type.equals("str")) {
                println("    args.add(" + getArgumentCall + ");");
            } else if (field_type.equals("int")) {
                println("    args.add(new Integer(" + getArgumentCall + "));");
            } else if (field_type.equals("real")) {
                println("    args.add(new Double (" + getArgumentCall + "));");
            } else if (field_type.equals("term")) {
                println("    args.add((aterm.ATerm)" + getArgumentCall + ");");
            } else if (field_type.equals("list")) {
                println("    args.add((aterm.ATermList)" + getArgumentCall + ");");
            } else if (field_type.equals("chars")) {
                println(
                    "    args.add((aterm.ATerm) stringToChars("
                        + getArgumentCall
                        + "));");
            } else if (field_type.equals("char")) {
                println(
                    "    args.add(new Integer((char) "
                        + getArgumentCall
                        + "));");
            } else {
                println("    args.add(" + getArgumentCall + ".toTerm());");
            }
        }
    }

    private void genFactoryInitialization(ADT api) {
        Iterator types = api.typeIterator(module);
        int listTypeCount = 0;

        while (types.hasNext()) {
            Type type = (Type) types.next();

            if (type instanceof ListType) {
                if (type instanceof SeparatedListType) {
                    genSeparatedListInitialization(
                        listTypeCount,
                        (SeparatedListType) type);
                } else {
                    genNormalListTypeInitialization(type, listTypeCount);
                }
                listTypeCount++;
            } else if (!typeConverter.isReserved(type.getId())) {
              /* do not generate prototypes for the builtin sorts */
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
                            + StringConversions.escapeQuotes(
                                alt.buildMatchPattern().toString())
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
                println(
                    "    "
                        + StringConversions.makeCapitalizedIdentifier(type)
                        + ".initialize(this);");
            }
        }
    }

    private void genImportedFactoryInitialization() {
    		println();
    
    		Set importedModules = new HashSet();
    		importedModules = adt.getImportsClosureForModule(module.getModulename().getName());
    		importedModules.remove(module.getModulename().getName());
	
    		Iterator it = importedModules.iterator();
    		String moduleName;
    		while(it.hasNext()) {
    		    moduleName = (String) it.next();
    		    println("    this."+FactoryGenerator.className(moduleName).toLowerCase()+" = "+
    		            FactoryGenerator.qualifiedClassName(getJavaGenerationParameters(), moduleName)+".getInstance(factory);");
    		}
    }
    
    private void genNormalListTypeInitialization(Type type, int listTypeCount) {
        genInitializePrototype(type);
        genInitializeEmptyList(type, listTypeCount);
    }

    private void genSeparatedListInitialization(
        int listTypeCount,
        SeparatedListType type) {
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

    private void genInitializeEmptySeparatedList(
        SeparatedListType type,
        int listTypeCount) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String className = TypeGenerator.qualifiedClassName(params, type.getId());
        String protoName = protoListVariable(type);
        String emptyName = emptyListVariable(type);
        String emptyHashCode = buildInitialEmptyListHashcode(listTypeCount).toString();
        String pureEmptyName = "factory.getEmpty()";
        println(
            "    "
                + protoName
                + ".initHashCode("
                + pureEmptyName
                + ", null, "
                + buildAmountOfSeparatorsNullExpressions(type)
                + "null);");
        println(
            "    "
                + emptyName
                + " = ("
                + className
                + ") factory.build("
                + protoName
                + ");");
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
        String className = TypeGenerator.qualifiedClassName(params, type.getId());
        String protoName = protoListVariable(type);
        String emptyName = emptyListVariable(type);
        String emptyHashCode = buildInitialEmptyListHashcode(listTypeCount).toString();
        String pureEmptyName = "factory.getEmpty()";
        println("    " + protoName + ".initHashCode(" + pureEmptyName + ", null, null);");
        println(
            "    "
                + emptyName
                + " = ("
                + className
                + ") factory.build("
                + protoName
                + ");");
    }

    private void genInitializePrototype(Type type) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String protoName = protoListVariable(type);
        String className = TypeGenerator.qualifiedClassName(params, type.getId());
        println("    " + protoName + " = new " + className + "(this);");
    }

    private Integer buildInitialEmptyListHashcode(int listTypeCount) {
        return new Integer(42 * (2 + listTypeCount));
    }

    private void genAltFromTerm(Type type, Alternative alt) {
        JavaGenerationParameters params = getJavaGenerationParameters();

        String returnType = TypeGenerator.qualifiedClassName(params, type.getId());
        String methodName = concatTypeAlt(type, alt) + "FromTerm";

        println("  protected " + returnType + " " + methodName + "(aterm.ATerm trm) {");
        println(
           "    java.util.List children = trm.match("
               + patternVariable(type, alt)
               + ");");
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
        } else if (fieldType.equals("int")) {
            result = "((Integer) children.get(" + argnr + ")).intValue()";
        } else if (fieldType.equals("real")) {
            result = "((Double) children.get(" + argnr + ")).doubleValue()";
        } else if (fieldType.equals("term")) {
            result = "(aterm.ATerm) children.get(" + argnr + ")";
        } else if (fieldType.equals("list")) {
            result = "(aterm.ATermList) children.get(" + argnr + ")";
        } else if (fieldType.equals("chars")) {
            result = "(String) charsToString((aterm.ATerm) children.get(" + argnr + "))";
        } else if (fieldType.equals("char")) {
            result = "(char) charToByte((aterm.ATerm) children.get(" + argnr + "))";
        } else {
            result = fieldClass + "FromTerm((aterm.ATerm) children.get(" + argnr + "))";
        }

        return result;
    }

    private void genTypeFromTermMethod(Type type, boolean forwarding, String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();

        String returnType = TypeGenerator.qualifiedClassName(params, type.getId());
        String methodName = TypeGenerator.className(type) + "FromTerm";

        println("  public " + returnType + " " + methodName + "(aterm.ATerm trm) {");
        if(!forwarding) {
        	println("    " + returnType + " tmp;");
        	genAltFromTermCalls(type);
        	println(
        			"    throw new IllegalArgumentException(\"This is not a "
        			+ TypeGenerator.className(type)
							+ ": \" + trm);");
        } else {//forwarding 
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+methodName
                + "(trm);");
        }
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

        String returnType = TypeGenerator.qualifiedClassName(params, type.getId());
        String fromString = TypeGenerator.className(type.getId()) + "FromString";
        String fromTerm = TypeGenerator.className(type.getId()) + "FromTerm";

        println("  public " + returnType + " " + fromString + "(String str) {");
        println("    return " + fromTerm + "(factory.parse(str));");
        println("  }");
        println();
    }

    protected void genTypeFromFileMethod(Type type) {
        JavaGenerationParameters params = getJavaGenerationParameters();

        String returnType = TypeGenerator.qualifiedClassName(params, type.getId());
        String fromFile = TypeGenerator.className(type.getId()) + "FromFile";
        String fromTerm = TypeGenerator.className(type.getId()) + "FromTerm";

        print("  public " + returnType + ' ' + fromFile + "(java.io.InputStream stream)");
        println(" throws java.io.IOException {");
        println("    return " + fromTerm + "(factory.readFromFile(stream));");
        println("  }");
        println();
    }

    private void genListFromTerm(ListType type,
  			boolean forwarding, String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String returnTypeName = TypeGenerator.qualifiedClassName(params, type.getId());
        String className = TypeGenerator.className(type);
        String elementType = type.getElementType();
        String elementTypeName = TypeGenerator.qualifiedClassName(params, elementType);
        String nextElement;

        if (!getConverter().isReserved(elementType)) {
            nextElement =
                TypeGenerator.className(elementType) + "FromTerm(list.getFirst())";
        } else {
            nextElement =
                getConverter().makeATermToBuiltinConversion(elementType, "list");
        }

        println(
            "  public "
                + returnTypeName
                + " "
                + className
                + "FromTerm(aterm.ATerm trm) {");
        if(!forwarding) {
        	println("     if (trm instanceof aterm.ATermList) {");
        	println("        aterm.ATermList list = ((aterm.ATermList) trm).reverse();");
        	println("        " + returnTypeName + " result = make" + className + "();");
        	println("        for (; !list.isEmpty(); list = list.getNext()) {");
        	println("           " + elementTypeName + " elem = " + nextElement + ";");
        	println("            result = make" + className + "(elem, result);");
        	println("        }");
        	println("        return result;");
        	println("     }");
        	println("     else {");
        	println(
        			"       throw new RuntimeException(\"This is not a "
        			+ className
							+ ": \" + trm);");
        	println("     }");
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+className
              + "FromTerm(trm);");
        }
        	println("  }");
        println();
    }

    private void genSeparatedListFromTermMethod(SeparatedListType type,
    			boolean forwarding, String moduleName) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        String returnTypeName = TypeGenerator.qualifiedClassName(params, type.getId());
        String className = TypeGenerator.className(type);
        String makeClassName = "make" + className;
        String manyPattern = patternListVariable(type);
        String elementType = type.getElementType();
        // String elementClass = TypeGenerator.className(elementType);
        // String elementClassFromTerm = elementClass + "FromTerm";
        Separators separators = type.getSeparators();
        int headLength = 1 + separators.getLength(); // on the ATerm level
        int tailIndex = 1 + type.countSeparatorFields(); // on the abstract
        // level

        println(
            "  public "
                + returnTypeName
                + " "
                + className
                + "FromTerm(aterm.ATerm trm) {");
        if(!forwarding) {
        	println("    if (!(trm instanceof aterm.ATermList)) {");
        	println(
            "      throw new IllegalArgumentException(\"This is not a "
                + className
                + ": \" + trm);");
        	println("    }");
        	println();
        	println("    aterm.ATermList list = (aterm.ATermList) trm;");
        	println("    if (list.isEmpty()) {");
        	println("      return " + makeClassName + "();");
        	println("    }");
        	println();
        	println("    if (list.getLength() == 1) {");

        	String singletonElement =
            buildFromTermListElement(elementType, "list.getFirst()");

        	println("      return " + makeClassName + "(" + singletonElement + ");");
        	println("    }");
        	println();
        	println("    int length = (list.getLength() / " + headLength + ") + 1;");
        	println("    java.util.List[] nodes = new java.util.List[length-1];");
        	println();
        	println("    for (int i = 0; i < length - 1; i++) {");
        	println("      java.util.List args = list.match(" + manyPattern + ");");
        	println("      if (args == null) {");
        	println(
        			"        throw new IllegalArgumentException(\"This is not a "
        			+ className
							+ ": \" + trm);");
        	println("      }");
        	println("      nodes[i] = args;");
        	println("      list = (aterm.ATermList) args.get(" + tailIndex + ");");
        	println("    }");
        	println();
        	println("    if (list.getLength() != 1) {");
        	println(
        			"      throw new IllegalArgumentException(\"This is not a "
        			+ className
							+ ": \" + trm);");
        	println("    }");
        	println();
        	println(
        			"    "
        			+ returnTypeName
							+ " result = "
							+ makeClassName
							+ "("
							+ singletonElement
							+ ");");
        	println();
        	println("    for (int i = length - 2; i >= 0; i--) {");
        	println("      java.util.List children = nodes[i];");
        	String elementTypeName = TypeGenerator.qualifiedClassName(params, elementType);

        	String head =
            buildFromTermListElement(elementType, "(aterm.ATerm) children.get(0)");
        	println("      " + elementTypeName + " head = " + head + ";");

        	genFromTermSeparatorFieldAssigments(type);

        	String separatorArgs = buildOptionalSeparatorArguments(type);

        	println(
        			"      result = " + makeClassName + "(head, " + separatorArgs + "result);");
        	println("    }");
        	println();
        	println("    return result;");
        } else {//forwarding 
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+"."+className
                + "FromTerm(trm);");
        }
        println("  }");
        println();
    }

    private String buildFromTermListElement(String elementType, String element) {
        if (getConverter().isReserved(elementType)) {
            element = getConverter().makeATermToBuiltinConversion(elementType, element);
        } else {
            String elementClass = TypeGenerator.className(elementType);
            element = elementClass + "FromTerm(" + element + ")";
        }
        return element;
    }

    private String buildOptionalSeparatorArguments(SeparatedListType type) {
        Iterator separatorFields = type.separatorFieldIterator();
        String separatorArgs = buildActualTypedAltArgumentList(separatorFields, false);
        if (separatorArgs.length() > 0) {
            separatorArgs += ", ";
        }
        return separatorArgs;
    }

    private void genSeparatedListToTerm(SeparatedListType type, boolean forwarding, String moduleName) {
        String paramTypeName =
            TypeGenerator.qualifiedClassName(getJavaGenerationParameters(), type.getId());
        String manyPattern = patternListVariable(type);
        String elementType = type.getElementType();

        println("  public aterm.ATerm toTerm(" + paramTypeName + " arg) {");
        if(!forwarding) {
        	println("    if (arg.isEmpty()) {");
        	println("      return factory.getEmpty();");
        	println("    }");
        	println();
        	println("    if (arg.isSingle()) {");

        	print("      return factory.makeList(");
        	if (!getConverter().isReserved(elementType)) {
            print("arg.getHead().toTerm()");
        	} else {
            print("arg.getFirst()");
        	}
        	println(");");
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

        	String continueConversion = "";
        	if (!getConverter().isReserved(elementType)) {
            continueConversion = ".toTerm()";
        	}
        	String convertedHead =
            getConverter().makeBuiltinToATermConversion(
                elementType,
                "nodes[0].getHead()");
        	convertedHead += continueConversion;

        	println("    aterm.ATerm result = factory.makeList(" + convertedHead + ");");
        	println("    for (int i = 1; i < length; i++) {");
        	println("      java.util.List args = new java.util.LinkedList();");

        	String boxedHead = BoxingBuilder.buildBoxer(elementType, "nodes[i].getHead()");
        	boxedHead += continueConversion;

        	println("      args.add(" + boxedHead + ");");

        	Iterator separators = type.separatorFieldIterator();
        	while (separators.hasNext()) {
            Field separator = (Field) separators.next();
            String fieldId = separator.getId();
            String fieldType = separator.getType();
            String capitalizedFieldId =
                StringConversions.makeCapitalizedIdentifier(fieldId);
            String fieldGetter = "get" + capitalizedFieldId + "()";
            String boxedField = BoxingBuilder.buildBoxer(fieldType, fieldGetter);

            continueConversion = ".toTerm()";
            if (getConverter().isReserved(fieldType)) {
                continueConversion = "";
            }
            boxedField += continueConversion;
            println("      args.add(nodes[i]." + boxedField + ");");
        	}

        	println("      args.add(result);");
        	println("      result = " + manyPattern + ".make(args);");
        	println("    }");
        	println();
        	println("    return result;");
        	
        } else {//forwarding
        	println("    return "+FactoryGenerator.className(moduleName).toLowerCase()+
        			"toTerm(arg);");
        }
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
            println(
                "      "
                    + fieldType
                    + " "
                    + fieldId
                    + " = "
                    + buildFieldMatchResultRetriever(i, field)
                    + ";");
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
        return TypeGenerator.className(type) + "_" + AlternativeGenerator.className(type,alt);
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
