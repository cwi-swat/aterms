package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.ListType;
import apigen.adt.NormalListType;
import apigen.adt.SeparatedListType;
import apigen.adt.Type;
import apigen.adt.api.Separators;
import apigen.gen.StringConversions;
import aterm.ATermFactory;
import aterm.ATermList;

public class FactoryGenerator extends JavaGenerator {
    private String className;
    private ADT adt;
    private String apiName;

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
        this.apiName = apiName;
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
        genAlternativeMethods(api);
        genFactoryMakeLists(api);
        genTypeFromTermMethods(api);
        genTypeFromMethods(api);
        println("}");
    }

    private void genTypeFromTermMethods(ADT api) {
        Iterator types = api.typeIterator();

        while (types.hasNext()) {
            Type type = (Type) types.next();

            if (type instanceof NormalListType) {
                genListTypeFromTermMethod((NormalListType) type);
            } else if (type instanceof SeparatedListType) {
                genSeparatedListTypeFromTermMethod((SeparatedListType) type);
            } else {
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
                String className = TypeGenerator.className(type);
                println("  static protected " + className + " empty" + className + ";");
            }
        }

    }

    private void genFactoryMakeLists(ADT api) {
        Iterator types = api.typeIterator();

        while (types.hasNext()) {
            Type type = (Type) types.next();
            if (type instanceof ListType) {
                String className = TypeGenerator.className(type);
                String elementClassName =
                    TypeGenerator.className(((ListType) type).getElementType());

                String empty = "empty" + className;

                if (type instanceof NormalListType) {
                    genFactoryMakeEmptyList(className, empty);
                    genFactoryMakeSingletonList(className, elementClassName, empty);
                    genFactoryMakeManyList(className, elementClassName);
                    genFactoryMakeManyTermList(className);
                } else if (type instanceof SeparatedListType) {
                    SeparatedListType lType = (SeparatedListType) type;
                    genFactoryMakeEmptyList(className, empty);
                    genFactoryMakeSingletonSeparatedList(
                        lType,
                        className,
                        elementClassName,
                        empty);
                    genFactoryMakeManySeparatedList(lType, className, elementClassName);
                    genFactoryMakeManySeparatedTermList(lType, className);
                }
            }
        }
    }

    private void genFactoryMakeManyTermList(String className) {
        println(
            "  protected "
                + className
                + " make"
                + className
                + "(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {");
        println("    synchronized (proto" + className + ") {");
        println("      proto" + className + ".initHashCode(annos,head,tail);");
        println("      return (" + className + ") build(proto" + className + ");");
        println("    }");
        println("  }");
    }

    private void genFactoryMakeManyList(String className, String elementClassName) {
        println(
            "  public "
                + className
                + " make"
                + className
                + "("
                + elementClassName
                + " head, "
                + className
                + " tail) {");
        println(
            "    return ("
                + className
                + ") make"
                + className
                + "((aterm.ATerm) head, (aterm.ATermList) tail, empty);");
        println("  }");
    }

    private void genFactoryMakeManySeparatedList(
        SeparatedListType type,
        String className,
        String elementClassName) {
        String formalSeps = buildFormalTypedArgumentList(type.separatorFieldIterator());
        String actualSeps =
            buildActualTypedAltArgumentList(type.separatorFieldIterator());
        if (formalSeps.length() > 0) {
            formalSeps += ", ";
            actualSeps += ", ";
        }

        println(
            "  public "
                + className
                + " make"
                + className
                + "("
                + elementClassName
                + " head, "
                + formalSeps
                + className
                + " tail) {");
        println(
            "    return ("
                + className
                + ") make"
                + className
                + "((aterm.ATerm) head, "
                + actualSeps
                + "(aterm.ATermList) tail, empty);");
        println("  }");
    }

    private void genFactoryMakeManySeparatedTermList(
        SeparatedListType type,
        String className) {
        String formalSeps = buildFormalTypedArgumentList(type.separatorFieldIterator());
        String actualSeps =
            buildActualTypedAltArgumentList(type.separatorFieldIterator());
        if (formalSeps.length() > 0) {
            formalSeps += ", ";
            actualSeps += ", ";
        }

        println(
            "  protected "
                + className
                + " make"
                + className
                + "(aterm.ATerm head, "
                + formalSeps
                + "aterm.ATermList tail, aterm.ATermList annos) {");
        println("    synchronized (proto" + className + ") {");
        println(
            "      proto"
                + className
                + ".initHashCode(annos, head, "
                + actualSeps
                + "tail);");
        println("      return (" + className + ") build(proto" + className + ");");
        println("    }");
        println("  }");
    }

    private void genFactoryMakeSingletonList(
        String className,
        String elementClassName,
        String empty) {

        println(
            "  public "
                + className
                + " make"
                + className
                + "("
                + elementClassName
                + " elem ) {");
        println(
            "    return (" + className + ") make" + className + "(elem, " + empty + ");");
        println("  }");
    }

    private void genFactoryMakeSingletonSeparatedList(
        SeparatedListType type,
        String className,
        String elementClassName,
        String empty) {
        String separators = buildActualNullArgumentList(type.separatorFieldIterator());
        if (separators.length() > 0) {
            separators += ", ";
        }

        println(
            "  public "
                + className
                + " make"
                + className
                + "("
                + elementClassName
                + " elem ) {");
        println(
            "    return ("
                + className
                + ") make"
                + className
                + "(elem, "
                + separators
                + empty
                + ");");
        println("  }");
    }
    private void genFactoryMakeEmptyList(String className, String empty) {
        println("  public " + className + " make" + className + "() {");
        println("    return " + empty + ";");
        println("  }");
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
                println("  private aterm.ATerm pattern" + typeClassName + "Many;");
            } else {
                while (alts.hasNext()) {
                    Alternative alt = (Alternative) alts.next();
                    String altClassName = AlternativeGenerator.className(type, alt);

                    String protoVar = "proto" + altClassName;
                    String funVar = "fun" + altClassName;

                    println("  private aterm.AFun " + funVar + ";");
                    println("  private " + typeClassName + " " + protoVar + ";");
                    println("  private aterm.ATerm pattern" + altClassName + ";");
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
                    genSeparatedListToTerm((SeparatedListType) type);
                }
            } else {
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
        String altClassName = AlternativeGenerator.className(type, alt);

        String funVar = "fun" + altClassName;
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

    private void genInternalMakeMethod(Type type, Alternative alt) {
        String altClassName = AlternativeGenerator.className(type, alt);
        String protoVar = "proto" + altClassName;

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
    }

    private String patternVariable(String className) {
        return "pattern" + className;
    }

    private void genAltToTerm(Type type, Alternative alt) {
        String className = AlternativeGenerator.className(type, alt);
        String classImplName = AlternativeImplGenerator.className(type, alt);

        println("  protected aterm.ATerm toTerm(" + classImplName + " arg) {");
        println("    java.util.List args = new java.util.LinkedList();");

        Iterator fields = type.altFieldIterator(alt.getId());
        print(buildAddFieldsToListCalls(fields));
        println("    return make(" + patternVariable(className) + ", args);");
        println("  }");
        println();
    }

    private String buildAddFieldsToListCalls(Iterator fields) {
        String result = "";

        for (int i = 0; fields.hasNext(); i++) {
            Field field = (Field) fields.next();
            String field_type = field.getType();
            String getArgumentCall = "arg.getArgument(" + i + ")";

            if (field_type.equals("str")) {
                result += "    args.add(((aterm.ATermAppl)"
                    + getArgumentCall
                    + ").getAFun().getName());";
            } else if (field_type.equals("int")) {
                result += "    args.add(new Integer(((aterm.ATermInt)"
                    + getArgumentCall
                    + ").getInt()));";
            } else if (field_type.equals("real")) {
                result += "    args.add(new Double (((aterm.ATermReal)"
                    + getArgumentCall
                    + ").getReal()));";
            } else if (field_type.equals("term")) {
                result += "    args.add((aterm.ATerm)" + getArgumentCall + ");";
            } else if (field_type.equals("list")) {
                result += "    args.add((aterm.ATermList)" + getArgumentCall + ");";
            } else {
                result += "    args.add((("
                    + TypeGenerator.className(field_type)
                    + ")"
                    + getArgumentCall
                    + ").toTerm());";
            }
        }

        return result;
    }

    private void genFactoryInitialization(ADT api) {
        Iterator types = api.typeIterator();
        int listTypesCount = 0;

        while (types.hasNext()) {
            Type type = (Type) types.next();

            if (type instanceof NormalListType) {
                genNormalListTypeInitialization(listTypesCount, type);
                listTypesCount++;
            } else if (type instanceof SeparatedListType) {
                genSeparatedListTypeInitialization(
                    listTypesCount,
                    (SeparatedListType) type);
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
                    println(
                        "    "
                            + patternVariable(altClassName)
                            + " = parse(\""
                            + StringConversions.escapeQuotes(
                                alt.buildMatchPattern().toString())
                            + "\");");
                    println(
                        "    "
                            + funVar
                            + " = makeAFun(\""
                            + "_"
                            + afunName
                            + "\", "
                            + type.getAltArity(alt)
                            + ", false);");
                    println("    " + protoVar + " = new " + altClassName + "(this);");
                }
            }
            println();
        }

        Iterator bottoms = api.bottomTypeIterator();

        while (bottoms.hasNext()) {
            String type = (String) bottoms.next();

            if (!converter.isReserved(type)) {
                println(
                    "    "
                        + StringConversions.makeCapitalizedIdentifier(type)
                        + ".initialize(this);");
            }
        }
    }

    private void genNormalListTypeInitialization(int listTypesCount, Type type) {
        String className = TypeGenerator.className(type);
        genInitializedPrototype(className);
        genInitializeEmptyList(listTypesCount, className);
    }

    private void genSeparatedListTypeInitialization(
        int listTypesCount,
        SeparatedListType type) {
        String className = TypeGenerator.className(type);
        genInitializedPrototype(className);
        genInitializeEmptySeparatedList(type, listTypesCount, className);
        genInitializeManyPattern(type, className);
    }

    private void genInitializeManyPattern(SeparatedListType type, String className2) {
        Alternative alt = type.getManyAlternative();

        println(
            "    pattern"
                + TypeGenerator.className(type)
                + "Many"
                + " = parse(\""
                + StringConversions.escapeQuotes(alt.buildMatchPattern().toString())
                + "\");");
    }

    private void genInitializeEmptySeparatedList(
        SeparatedListType type,
        int listTypesCount,
        String className) {
        String emptyHashCode = buildInitialEmptyListHashcode(listTypesCount).toString();
        println(
            "    proto"
                + className
                + ".init("
                + emptyHashCode
                + ", null, null, "
                + buildAmountOfSeparatorsNullExpressions(type)
                + "null);");
        println(
            "    empty"
                + className
                + " = ("
                + className
                + ") build(proto"
                + className
                + ");");
        println(
            "    empty"
                + className
                + ".init("
                + emptyHashCode
                + ", empty"
                + className
                + ", null, "
                + buildAmountOfSeparatorsNullExpressions(type)
                + "null);");
    }

    private String buildAmountOfSeparatorsNullExpressions(SeparatedListType type) {
        String result = "";
        Iterator fields = type.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            result += "null, ";
        }
        return result;
    }

    private void genInitializeEmptyList(int listTypesCount, String className) {
        String emptyHashCode = buildInitialEmptyListHashcode(listTypesCount).toString();
        println(
            "    proto" + className + ".init(" + emptyHashCode + ", null, null, null);");
        println(
            "    empty"
                + className
                + " = ("
                + className
                + ") build(proto"
                + className
                + ");");
        println(
            "    empty"
                + className
                + ".init("
                + emptyHashCode
                + ", empty"
                + className
                + ", null, null);");
    }

    private void genInitializedPrototype(String className) {
        println("    proto" + className + " = new " + className + "(this);");
    }

    private Integer buildInitialEmptyListHashcode(int listTypesCount) {
        return new Integer(42 * (2 + listTypesCount));
    }

    private void genAltFromTerm(Type type, Alternative alt) {
        String subClassName = AlternativeGenerator.className(type, alt);
        String superClassName = TypeGenerator.className(type);
        Iterator fields;
        int argnr;

        println(
            "  public "
                + superClassName
                + " "
                + subClassName
                + "FromTerm(aterm.ATerm trm)");
        println("  {");
        println(
            "    java.util.List children = trm.match("
                + patternVariable(AlternativeGenerator.className(type, alt))
                + ");");
        println();
        println("    if (children != null) {");
        print("      " + superClassName + " tmp = make" + subClassName + "(");

        fields = type.altFieldIterator(alt.getId());
        argnr = 0;
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String fieldType;
            String fieldClass;
            print(buildFieldMatchResultRetriever(argnr, field));

            if (fields.hasNext()) {
                print(", ");
            }
            argnr++;
        }
        println(");");
        println("      return tmp;");
        println("    }"); // endif      
        println("    else {");
        println("      return null;");
        println("    }");
        println("  }");
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
        } else {
            result = fieldClass + "FromTerm( (aterm.ATerm) children.get(" + argnr + "))";
        }

        return result;
    }

    private void genTypeFromTermMethod(Type type) {
        String class_name = TypeGenerator.className(type);

        println(
            "  public " + class_name + " " + class_name + "FromTerm(aterm.ATerm trm)");
        println("  {");
        println("    " + class_name + " tmp;");
        genAltFromTermCalls(type);
        println();
        println(
            "    throw new RuntimeException(\"This is not a "
                + class_name
                + ": \" + trm);");
        println("  }");
    }

    protected void genAltFromTermCalls(Type type) {
        Iterator alts = type.alternativeIterator();
        while (alts.hasNext()) {
            Alternative alt = (Alternative) alts.next();
            String alt_class_name = AlternativeGenerator.className(type, alt);
            println("    tmp = " + alt_class_name + "FromTerm(trm);");
            println("    if (tmp != null) {");
            println("      return tmp;");
            println("    }");
            println();
        }
    }

    protected void genTypeFromStringMethod(Type type) {
        String className = TypeGenerator.className(type);

        println("  public " + className + " " + className + "FromString(String str)");
        println("  {");
        println("    aterm.ATerm trm = parse(str);");
        println("    return " + className + "FromTerm(trm);");
        println("  }");
    }

    protected void genTypeFromFileMethod(Type type) {
        String className = TypeGenerator.className(type);

        println(
            "  public "
                + className
                + " "
                + className
                + "FromFile(java.io.InputStream stream) throws java.io.IOException {");
        println("    return " + className + "FromTerm(readFromFile(stream));");
        println("  }");
    }

    private void genListTypeFromTermMethod(NormalListType type) {
        String className = ListTypeGenerator.className(type);
        String elementType = type.getElementType();
        String elementTypeName = TypeGenerator.className(elementType);

        if (converter.isReserved(elementType)) {
            // TODO: implement lists of builtins
            throw new RuntimeException(
                "List of " + elementType + " not yet implemented, sorry!");
        }

        println("  public " + className + " " + className + "FromTerm(aterm.ATerm trm)");
        println("  {");
        println("     if (trm instanceof aterm.ATermList) {");
        println("        aterm.ATermList list = ((aterm.ATermList) trm).reverse();");
        println("        " + className + " result = make" + className + "();");
        println("        for (; !list.isEmpty(); list = list.getNext()) {");
        println(
            "          "
                + elementTypeName
                + " elem = "
                + elementTypeName
                + "FromTerm(list.getFirst());");
        println("           if (elem != null) {");
        println("             result = make" + className + "(elem, result);");
        println("           }");
        println("           else {");
        println(
            "             throw new RuntimeException(\"Invalid element in "
                + className
                + ": \" + elem);");
        println("           }");
        println("        }");
        println("        return result;");
        println("     }");
        println("     else {");
        println(
            "       throw new RuntimeException(\"This is not a "
                + className
                + ": \" + trm);");
        println("     }");
        println("  }");
    }

    private String buildGetNextElementCall(SeparatedListType type) {
        Separators separators = type.getSeparators();
        String result = "getNext()";

        for (; !separators.isEmpty(); separators = separators.getTail()) {
            result += ".getNext()";
        }

        return result;
    }

    private void genSeparatedListTypeFromTermMethod(SeparatedListType type) {
        String className = ListTypeGenerator.className(type);
        String elementType = type.getElementType();
        String elementTypeName = TypeGenerator.className(elementType);
        Separators separators = type.getSeparators();
        String nextElement = buildGetNextElementCall(type);
        String actualSeparators =
            buildActualTypedAltArgumentList(type.separatorFieldIterator());
        if (actualSeparators.length() > 0) {
            actualSeparators += ", ";
        }

        // TODO: make this work for separators that are not palindromes, the reverse in 
        // combination with the match with the many pattern currently prevents this.
        // Note that non-palindrome separators do not occur naturally anyway if the API is
        // generated from an SDF definition.

        // TODO: fix a bug, this algorithm now flips around all separators in between the
        // elements. so [e1,l1,l2,e2] becomes [e1,l2,l1,e2] after a fromTerm!!

        println("  public " + className + " " + className + "FromTerm(aterm.ATerm trm)");
        println("  {");
        println("     if (!(trm instanceof aterm.ATermList)) {");
        println(
            "       throw new RuntimeException(\"This is not a "
                + className
                + ": \" + trm);");
        println("     }");

        println("     aterm.ATermList list = ((aterm.ATermList) trm);");
        println("     if (list.isEmpty()) {");
        println("       return make" + className + "();");
        println("     }");
        println("     else if (list.getLength() == 1) {");
        println(
            "       return make"
                + className
                + "("
                + elementTypeName
                + "FromTerm(list.getFirst()));");
        println("     }");
        println("     else {");
        println("       list = list.reverse();");
        println("       " + className + " result;");
        println("       " + elementTypeName + " head;");
        Iterator seps = type.separatorFieldIterator();
        while (seps.hasNext()) {
            Field sep = (Field) seps.next();
            String fieldId = AlternativeImplGenerator.getFieldId(sep.getId());
            String fieldType = TypeGenerator.className(sep.getType());
            println("        " + fieldType + " " + fieldId + ";");
        }
        println(
            "        java.util.List children = list.match(pattern"
                + className
                + "Many);");
        println("        if (children != null) {");
        genFromTermSeparatorHeadAssignment(type);
        int tailIndex = genFromTermSeparatorFieldAssigments(type);
        genFromTermSeparatorTailAssignment(tailIndex);
        println("          result = make" + className + "(head);");

        println("          while (children != null) {");
        println("            children = list.match(pattern" + className + "Many);");
        println("            if (children != null) {");
        genFromTermSeparatorHeadAssignment(type);
        println(
            "              result = make"
                + className
                + "(head,"
                + actualSeparators
                + "result);");
        tailIndex = genFromTermSeparatorFieldAssigments(type);
        genFromTermSeparatorTailAssignment(tailIndex);
        println("            }");
        println("          }");
        println("          if (list.getLength() == 1) {");
        println(
            "            return make"
                + className
                + "("
                + elementTypeName
                + "FromTerm(list.getFirst()), "
                + actualSeparators
                + "result);");
        println("          }");
        println("        }");
        println("      }");
        println(
            "     throw new RuntimeException(\"This is not a "
                + className
                + ": \" + trm);");
        println("  }");
    }

    private void genSeparatedListToTerm(SeparatedListType type) {
        String className = TypeGenerator.className(type);
        // TODO: Fix this algorithm for bugs in the ordering of the separators
        println("  public aterm.ATerm toTerm(" + className + " arg) {");
        println("    if (arg.isEmpty()) {");
        println("      return getEmpty();");
        println("    }");
        println("    if (arg.isSingle()) {");
        println("      return makeList(arg.getHead().toTerm());");
        println("    }");
        println("    aterm.ATermList result = getEmpty();");
        println("    while (!arg.isEmpty()) {");
        println("      java.util.List args = new java.util.LinkedList();");
        println("      args.add(arg.getHead().toTerm());");
        println("      if (!arg.isSingle()) {");
        println("        aterm.ATermList tmp = (aterm.ATermList) pattern" + className + "Many.make(args);");
        println("        result = result.concat(tmp.reverse());");
        println("      }");
        println("      else {");
        println("        return result.insert((aterm.ATerm) args.get(0));");
        println("      }");
        println("    }");
        println("    return result.reverse();");
        println("  }");
    }

    private void genFromTermSeparatorHeadAssignment(SeparatedListType type) {
        println(
            "            head = "
                + buildFieldMatchResultRetriever(0, type.getManyField("head"))
                + ";");
    }

    private void genFromTermSeparatorTailAssignment(int tailIndex) {
        println(
            "              list = (aterm.ATermList) children.get(" + tailIndex + ");");
    }

    private int genFromTermSeparatorFieldAssigments(SeparatedListType type) {
        Iterator fields = type.separatorFieldIterator();
        int i;
        for (i = 1; fields.hasNext(); i++) {
            Field field = (Field) fields.next();
            String fieldId = AlternativeImplGenerator.getFieldId(field.getId());
            String fieldType = TypeGenerator.className(field.getType());
            println(
                "            "
                    + fieldId
                    + " = "
                    + buildFieldMatchResultRetriever(i, field)
                    + ";");
        }
        return i;
    }
}
