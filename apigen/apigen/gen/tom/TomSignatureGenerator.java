package apigen.gen.tom;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.ListType;
import apigen.adt.NamedListType;
import apigen.adt.Type;
import apigen.adt.api.types.Module;
import apigen.gen.GenerationParameters;
import apigen.gen.Generator;
import apigen.gen.StringConversions;
import apigen.gen.java.JavaGenerationParameters;
import apigen.gen.java.TypeGenerator;

public class TomSignatureGenerator extends Generator {
	private TomSignatureImplementation impl;
	private ADT adt;
	private String prefix;
	private String packagePrefix;
	private Module module;
  private String apiName = "";

	public TomSignatureGenerator(ADT adt, TomSignatureImplementation impl, GenerationParameters params, Module module) {
		super(params);
		this.module = module;
		
		setExtension(".tom");
		String moduleName = module.getModulename().getName();
		this.apiName = (moduleName.equals("")?params.getApiName():moduleName);
		
		//System.out.println("outputDir = " + params.getOutputDirectory());
		//System.out.println("apiname   = " + this.apiName);
		
		//setDirectory(params.getOutputDirectory());
		
		//System.out.println("getDirectory   = " + getDirectory());
		
		
		
		setFileName(StringConversions.makeIdentifier((moduleName.equals(""))?params.getApiName():moduleName));
		this.adt = adt;
		this.impl = impl;
		this.prefix = params.getPrefix();
		this.packagePrefix = "";
		if(params instanceof JavaGenerationParameters) {
			JavaGenerationParameters javaParams = (JavaGenerationParameters) params;
			if(javaParams.getPackageName() != null) {
				this.packagePrefix += javaParams.getPackageName() + ".";
			}
			this.packagePrefix += javaParams.getApiExtName(module).toLowerCase() + ".";
			this.packagePrefix += TypeGenerator.packageName() + ".";
			setDirectory(buildDirectoryName(javaParams.getOutputDirectory(), javaParams.getPackageName()));
		} else {
			setDirectory(params.getOutputDirectory());
		}
	}
	
	  private String buildDirectoryName(String baseDir, String pkgName) {
        StringBuffer buf = new StringBuffer();
        buf.append(baseDir);
        buf.append(File.separatorChar);

        if (pkgName != null) {
            buf.append(pkgName.replace('.', File.separatorChar));
        }

        return buf.toString();
    }

	
	public void generate() {
		genTomBuiltinTypes();
		genTomTypes(adt);
	}

  private String CheckStampTemplate(
                                    String checkStamp,
                                    String setStamp,
                                    String getImplementation) {
		return 
      "  check_stamp(t) {" + checkStamp + "}\n"
      + "  set_stamp(t) {" + setStamp + "}\n"
      + "  get_implementation(t) {" + getImplementation + "}\n"
      ;
  }

	private String TypeTermTemplate(
		String type,
		String impl,
		String equals,
    String checkStamp,
    String setStamp,
    String getImplementation) {

		return "%typeterm "
			+ type
			+ "{\n"
			+ "  implement { "
			+ impl
			+ "}\n"
			+ "  equals(t1,t2) {"
			+ equals
			+ "}\n"
      + CheckStampTemplate(checkStamp, setStamp, getImplementation)
			+ "}";
	}

	private String TypeListTemplate(
		String type,
		String impl,
		String equals,
		String get_head,
		String get_tail,
		String is_empty,
    String checkStamp,
    String setStamp,
    String getImplementation) {

		return "%typelist "
			+ type
			+ "{\n"
			+ "  implement { "
			+ impl
			+ "}\n"
			+ "  equals(t1,t2) {"
			+ equals
			+ "}\n"
			+ "  get_head(l) {"
			+ get_head
			+ "}\n"
			+ "  get_tail(l) {"
			+ get_tail
			+ "}\n"
			+ "  is_empty(l) {"
			+ is_empty
			+ "}\n"
      + CheckStampTemplate(checkStamp, setStamp, getImplementation)
			+ "}";
	}

	private void genTomBuiltinTypes() {
		println("%include { string.tom }");
		println("%include { int.tom }");
		println("%include { double.tom }");
		println("%include { aterm.tom }");
		println("%include { atermlist.tom }");
	}

	private void genTomTypes(ADT api) {
		String moduleName = module.getModulename().getName();
		Set modules = api.getImportsClosureForModule(moduleName);
		modules.add(module.getModulename().getName()); //do not forget myself
		
		//System.out.println("closure for "+moduleName+ " "+modules);
		Iterator moduleIt = modules.iterator();
		while(moduleIt.hasNext()) {
			Iterator types = api.typeIterator((String)moduleIt.next());

			while (types.hasNext()) {
				Type type = (Type) types.next();
				genTomType(type);
			}
		}
		
	}

	private void genTomType(Type type) {
    String stamp = "get" 
      + StringConversions.makeCapitalizedIdentifier(apiName)
			+ "Factory().getPureFactory().makeList()";
		if ((type instanceof ListType) || (type instanceof NamedListType)) {
			String eltType = ((ListType) type).getElementType();
			String opName = "conc" + type.getId();
			if (type instanceof NamedListType) {
				eltType = ((NamedListType) type).getElementType();
				opName  = ((NamedListType) type).getOpName();
			}
			println(
				TypeListTemplate(
					impl.TypeName(type.getId()),
					impl.TypeImpl(packagePrefix + prefix + type.getId()),
					impl.TypeEquals(type.getId(), "t1", "t2"),
					impl.ListHead(type.getId()),
					impl.ListTail(type.getId()),
					impl.ListEmpty(type.getId()),
          "if(t.getAnnotation(" + stamp + ") == " + stamp + ")  return; else throw new RuntimeException(\"bad stamp\")",
          "(" + packagePrefix + prefix + type.getId()  + ")t.setAnnotation(" + stamp +"," + stamp + ")",
          "t"
          ));
			println();
			genTomConcOperator(type, eltType, opName);
			String class_name = "class_name";
			genTomEmptyOperator(type, class_name);
			genTomManyOperator(type, eltType, class_name);
			genListTypeTomAltOperators(type);
		}
		else {
			println(
				TypeTermTemplate(
					impl.TypeName(type.getId()),
					impl.TypeImpl(packagePrefix + prefix + type.getId()),
					impl.TypeEquals(type.getId(), "t1", "t2"),
          "if(t.getAnnotation(" + stamp + ") == " + stamp + ")  return; else throw new RuntimeException(\"bad stamp\")",
          "(" + packagePrefix + prefix + type.getId()  + ")t.setAnnotation(" + stamp +"," + stamp + ")",
          //"(" + impl.TypeName(type.getId())  + ")t.setAnnotation(" + stamp +"," + stamp + ")",
          "t"
          ));

			println();
			genTomAltOperators(type);
		}

	}

	private void genTomManyOperator(Type type, String eltType, String class_name) {
		// many operator
		println("%op " + type.getId() + " many" + type.getId() + "(head:" + eltType + ", tail:" + type.getId() + ") {");
		println("  is_fsym(t) { " + prefix + impl.OperatorIsFSym("t", class_name, "many") + "}");
		println("  get_slot(head,t) { " + impl.OperatorGetSlot("t", class_name, "head") + "}");
		println("  get_slot(tail,t) { " + impl.OperatorGetSlot("t", class_name, "tail") + "}");
		println("  make(e,l) {" + impl.ListmakeInsert(type.getId(), eltType) + "}");
		println("}");
	}

	private void genTomEmptyOperator(Type type, String class_name) {
		println("%op " + type.getId() + " empty" + type.getId() + "{");
		println("  is_fsym(t) { " + prefix + impl.OperatorIsFSym("t", class_name, "empty") + "}");
		println("  make() {" + impl.ListmakeEmpty(type.getId()) + "}");
		println("}");
	}

	private void genTomConcOperator(Type type, String eltType, String opName) {
		// conc operator
		println("%oplist " + type.getId() + " " + opName + "(" + eltType + "*) {");
		println("  is_fsym(t) {" + impl.ListIsList("t", type.getId()) + "}");
		println("  make_empty() {" + impl.ListmakeEmpty(type.getId()) + "}");
		println("  make_insert(e,l) {" + impl.ListmakeInsert(type.getId(), eltType) + "}");
		println("}");
	}

	private void genListTypeTomAltOperators(Type type) {
		Iterator alts = type.alternativeIterator();

		while (alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();
  		    if (!alt.isEmpty() && !alt.isMany() && !alt.isSingle()) {
				genTomAltOperator(type, alt);
			}
		}
	}

	private void genTomAltOperators(Type type) {
		Iterator alts = type.alternativeIterator();

		while (alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();
			genTomAltOperator(type, alt);
		}
	}

	private void genTomAltOperator(Type type, Alternative alt) {
		String class_name = impl.TypeName(type.getId());
		String operator_name = impl.OperatorName(type.getId(), alt.getId());
		print("%op " + impl.TypeName(type.getId()) + " " + operator_name);

		Iterator fields = type.altFieldIterator(alt.getId());
		if (fields.hasNext()) {
			print("(");
			while (fields.hasNext()) {
				Field field = (Field) fields.next();
				String field_id = impl.FieldName(field.getId());
				String field_class = impl.FieldType(field.getType());
				String field_type = field_class;
				print(field_id + ":" + field_type);

				if (fields.hasNext()) {
					print(", ");
				}
			}
			print(")");
		}
		println(" {");
		println("  is_fsym(t) { " + prefix + impl.OperatorIsFSym("t", class_name, operator_name) + "}");

		fields = type.altFieldIterator(alt.getId());
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			String field_id = StringConversions.makeIdentifier(field.getId());
			println("  get_slot(" + field_id + ",t) { " + impl.OperatorGetSlot("t", class_name, field_id) + "}");
		}

		String arg = "(";
		int arity = type.getAltArity(alt);
		for (int i = 0; i < arity; i++) {
			arg += ("t" + i);
			if (i < arity - 1) {
				arg += ", ";
			}
		}
		arg += ")";

		println("  make" + arg + " { " + impl.OperatorMake(class_name, operator_name, arg) + "}");

		println("}");
		println();
	}
}
