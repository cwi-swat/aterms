package apigen.gen.tom;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;
import apigen.gen.Generator;
import apigen.gen.StringConversions;
import apigen.gen.java.JavaGenerationParameters;
import apigen.gen.java.TypeGenerator;

public class TomSignatureGenerator extends Generator {
	protected String apiName = "";
	private TomSignatureImplementation impl;
	private ADT adt;
	private String prefix;
	private String packagePrefix;

	public TomSignatureGenerator(ADT adt, TomSignatureImplementation impl, GenerationParameters params) {
		super(params);
		setDirectory(params.getOutputDirectory());
		setExtension(".tom");
		setFileName(StringConversions.makeIdentifier(params.getApiName()));
		this.adt = adt;
		this.impl = impl;
		this.prefix = params.getPrefix();

    if(params instanceof JavaGenerationParameters) {
      JavaGenerationParameters javaParams = (JavaGenerationParameters) params;
      this.packagePrefix = javaParams.getPackageName() + "." + javaParams.getApiName() + "." + TypeGenerator.packageName() + ".";
    } else {
      this.packagePrefix = "";
    }

	}
	
	public void generate() {
		genTomBuiltinTypes();
		genTomTypes(adt);
	}

	private String TypeTermTemplate(
		String type,
		String impl,
		String get_fun_sym,
		String cmp_fun_sym,
		String get_subterm,
		String equals) {

		return "%typeterm "
			+ type
			+ "{\n"
			+ "  implement { "
			+ impl
			+ "}\n"
			+ "  get_fun_sym(t) {"
			+ get_fun_sym
			+ "}\n"
			+ "  cmp_fun_sym(s1,s2) { "
			+ cmp_fun_sym
			+ "}\n"
			+ "  get_subterm(t,n) {"
			+ get_subterm
			+ "}\n"
			+ "  equals(t1,t2) {"
			+ equals
			+ "}\n"
			+ "}";
	}

	private String TypeListTemplate(
		String type,
		String impl,
		String get_fun_sym,
		String cmp_fun_sym,
		String equals,
		String get_head,
		String get_tail,
		String is_empty) {

		return "%typelist "
			+ type
			+ "{\n"
			+ "  implement { "
			+ impl
			+ "}\n"
			+ "  get_fun_sym(t) {"
			+ get_fun_sym
			+ "}\n"
			+ "  cmp_fun_sym(s1,s2) { "
			+ cmp_fun_sym
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
			+ "}";
	}

	private void genTomBuiltinTypes() {
		println("%include { string.tom }");
		println("%include { int.tom }");
		println("%include { double.tom }");
		
		println(
			TypeTermTemplate(
				impl.ATermName(),
				impl.ATermImpl(),
				impl.ATermGetFunSym("t"),
				impl.ATermCmpFunSym("s1", "s2"),
				impl.ATermGetSubTerm("t", "n"),
				impl.ATermEquals("t1", "t2")));
		println();
		println(
			TypeListTemplate(
				impl.ATermListName(),
				impl.ATermListImpl(),
				impl.ATermListGetFunSym("t"),
				impl.ATermListCmpFunSym("s1", "s2"),
				impl.ATermListEquals("t1", "t2"),
				impl.ATermListGetFirst("l"),
				impl.ATermListGetNext("l"),
				impl.ATermListIsEmpty("l")));
		println();
	}

	private void genTomTypes(ADT api) {
		Iterator types = api.typeIterator();

		while (types.hasNext()) {
			Type type = (Type) types.next();
			genTomType(type);
		}
	}

	private void genTomType(Type type) {
		if (type instanceof ListType) {
			String eltType = ((ListType) type).getElementType();

			println(
				TypeListTemplate(
					impl.TypeName(type.getId()),
					impl.TypeImpl(packagePrefix + prefix + type.getId()),
					impl.TypeGetFunSym("t"),
					impl.TypeCmpFunSym("s1", "s2"),
					impl.TypeEquals(type.getId(), "t1", "t2"),
					impl.ListHead(type.getId()),
					impl.ListTail(type.getId()),
					impl.ListEmpty(type.getId())));
			println();
			genTomConcOperator(type, eltType);
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
					impl.TypeGetFunSym("t"),
					impl.TypeCmpFunSym("s1", "s2"),
					impl.TypeGetSubTerm("t", "n"),
					impl.TypeEquals(type.getId(), "t1", "t2")));

			println();
			genTomAltOperators(type);
		}

	}

	private void genTomManyOperator(Type type, String eltType, String class_name) {
		// many operator
		println("%op " + type.getId() + " many" + type.getId() + "(head:" + eltType + ", tail:" + type.getId() + ") {");
		println("  fsym { null }");

		println("  is_fsym(t) { " + prefix + impl.OperatorIsFSym("t", class_name, "many") + "}");
		println("  get_slot(head,t) { " + impl.OperatorGetSlot("t", class_name, "head") + "}");
		println("  get_slot(tail,t) { " + impl.OperatorGetSlot("t", class_name, "tail") + "}");
		println("  make(e,l) {" + impl.ListmakeInsert(type.getId(), eltType) + "}");
		println("}");
	}

	private void genTomEmptyOperator(Type type, String class_name) {
		println("%op " + type.getId() + " empty" + type.getId() + "{");
		println("  fsym { null }");
		println("  is_fsym(t) { " + prefix + impl.OperatorIsFSym("t", class_name, "empty") + "}");
		println("  make() {" + impl.ListmakeEmpty(type.getId()) + "}");
		println("}");
	}

	private void genTomConcOperator(Type type, String eltType) {
		// conc operator
		println("%oplist " + type.getId() + " conc" + eltType + "(" + eltType + "*) {");
		println("  fsym { null }");
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
		println("  fsym {" + impl.OperatorFSym(class_name, operator_name) + "}");
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
