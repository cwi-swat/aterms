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

                genFactoryMakeEmptyList(className, empty);
                genFactoryMakeSingletonList(className, elementClassName, empty);
                genFactoryMakeManyList(className, elementClassName);
                genFactoryMakeManyTermList(className);

                // TODO: generated correct code for separated lists here
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
                continue;
            }

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
        for (int i = 0; fields.hasNext(); i++) {
            Field field = (Field) fields.next();
            String field_type = field.getType();
            String getArgumentCall = "arg.getArgument(" + i + ")";

            if (field_type.equals("str")) {
                println(
                    "    args.add(((aterm.ATermAppl)"
                        + getArgumentCall
                        + ").getAFun().getName());");
            } else if (field_type.equals("int")) {
                println(
                    "    args.add(new Integer(((aterm.ATermInt)"
                        + getArgumentCall
                        + ").getInt()));");
            } else if (field_type.equals("real")) {
                println(
                    "    args.add(new Double (((aterm.ATermReal)"
                        + getArgumentCall
                        + ").getReal()));");
            } else if (field_type.equals("term")) {
                println("    args.add((aterm.ATerm)" + getArgumentCall + ");");
            } else if (field_type.equals("list")) {
                println("    args.add((aterm.ATermList)" + getArgumentCall + ");");
            } else {
                println(
                    "    args.add((("
                        + TypeGenerator.className(field_type)
                        + ")"
                        + getArgumentCall
                        + ").toTerm());");
            }
        }
        println("    return make(" + patternVariable(className) + ", args);");
        println("  }");
        println();
    }

    private void genFactoryInitialization(ADT api) {
        Iterator types = api.typeIterator();
        int listTypesCount = 0;

        while (types.hasNext()) {
            Type type = (Type) types.next();

            if (type instanceof ListType) {
                String emptyHashCode = new Integer(42 * (2 + listTypesCount)).toString();
                String className = TypeGenerator.className(type);
                println("    proto" + className + " = new " + className + "(this);");
                println(
                    "    proto"
                        + className
                        + ".init("
                        + emptyHashCode
                        + ", null, null, null);");
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
            String fieldType = field.getType();
            String fieldClass = TypeGenerator.className(fieldType);

            if (fieldType.equals("str")) {
                print("(String) children.get(" + argnr + ")");
            } else if (fieldType.equals("int")) {
                print("((Integer) children.get(" + argnr + ")).intValue()");
            } else if (fieldType.equals("real")) {
                print("((Double) children.get(" + argnr + ")).doubleValue()");
            } else if (fieldType.equals("term")) {
                print("(aterm.ATerm) children.get(" + argnr + ")");
            } else if (fieldType.equals("list")) {
                print("(aterm.ATermList) children.get(" + argnr + ")");
            } else {
                print(
                    fieldClass + "FromTerm( (aterm.ATerm) children.get(" + argnr + "))");
            }

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
        println("  public " + className + " " + className + "FromTerm(aterm.ATerm trm)");
        println("  {");
        println("     if (trm instanceof aterm.ATermList) {");
        println("        aterm.ATermList list = ((aterm.ATermList) trm).reverse();");
        println("        " + className + " result = make" + className + "();");
        println("        for (; !list.isEmpty(); list = list." + nextElement + ") {");
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
        println("          }");
        println("          if (list.getNext().isEmpty()) {");
        println("             break;");
        println("          }");
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
}
