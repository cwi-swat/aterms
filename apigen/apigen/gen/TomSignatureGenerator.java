package apigen.gen;

import java.util.Iterator;

import apigen.adt.*;

public abstract class TomSignatureGenerator extends Generator {
    protected String api_name = "";
    
	protected void createTomSignatureStream(String name, String directory) {
		createFileStream(name, ".t", directory);
	}

	public void genTomSignatureFile(
		String directory,
		String api_name,
		ADT api) {
		String filename = buildId(api_name);
		this.api_name = api_name;
		createTomSignatureStream(filename, directory);

		info("generating " + filename + ".t");

		genTomBuiltinTypes();
		genTomTypes(api);
	}

	protected abstract String StringCmpFunSym(String arg1, String arg2);
	protected abstract String IntegerCmpFunSym(String arg1, String arg2);
	protected abstract String DoubleCmpFunSym(String arg1, String arg2);
	protected abstract String ATermCmpFunSym(String arg1, String arg2);

	protected abstract String StringEquals(String arg1, String arg2);
	protected abstract String IntegerEquals(String arg1, String arg2);
	protected abstract String DoubleEquals(String arg1, String arg2);
	protected abstract String ATermEquals(String arg1, String arg2);

	protected abstract String StringGetFunSym(String arg);
	protected abstract String IntegerGetFunSym(String arg);
	protected abstract String DoubleGetFunSym(String arg);
	protected abstract String ATermGetFunSym(String arg);

	protected abstract String StringGetSubTerm(String arg1, String arg2);
	protected abstract String IntegerGetSubTerm(String arg1, String arg2);
	protected abstract String DoubleGetSubTerm(String arg1, String arg2);
	protected abstract String ATermGetSubTerm(String arg1, String arg2);

	protected abstract String StringImpl();
	protected abstract String IntegerImpl();
	protected abstract String DoubleImpl();
	protected abstract String ATermImpl();

    protected abstract String TypeName(String type); 
	protected abstract String TypeImpl(String type);
	protected abstract String TypeGetFunSym(String arg1);
	protected abstract String TypeCmpFunSym(String arg1, String arg2);
	protected abstract String TypeGetSubTerm(String term, String n);
	protected abstract String TypeEquals(String arg1, String arg2);

    protected abstract String OperatorName(String id);
    protected abstract String OperatorType(String type, String id);
	protected abstract String OperatorGetSlot(String string, String field_id);
	protected abstract String OperatorIsFSym(String string, String type,  String alt);
	protected abstract String OperatorFSym(String type, String alt);
	protected abstract String OperatorMake(String type, String alt, String args);
		
    protected abstract String FieldName(String id);
    protected abstract String FieldType(String type);
    
	private String TypeTermTemplate(
		String type,
		String impl,
		String get_fun_sym,
		String cmp_fun_sym,
		String get_subterm,
		String equals) {

		return 
               "%typeterm " + type+ "{\n"
			+ "  implement { "+ impl+ "}\n"
			+ "  get_fun_sym(t) {"+ get_fun_sym + "}\n"
			+ "  cmp_fun_sym(s1,s2) { "+ cmp_fun_sym + "}\n"
			+ "  get_subterm(t,n) {" + get_subterm + "}\n"
			+ "  equals(t1,t2) {" + equals+ "}\n"
			+ "}";
	}

	private void genTomBuiltinTypes() {
		println(
			TypeTermTemplate(
				"String",
				StringImpl(),
				StringGetFunSym("t"),
				StringCmpFunSym("s1", "s2"),
				StringGetSubTerm("t", "n"),
				StringEquals("t1", "t2")));
		println();
		println(
			TypeTermTemplate(
				"Integer",
				IntegerImpl(),
				IntegerGetFunSym("t"),
				IntegerCmpFunSym("s1", "s2"),
				IntegerGetSubTerm("t", "n"),
				IntegerEquals("t1", "t2")));
		println();
		println(
			TypeTermTemplate(
				"Double",
				DoubleImpl(),
				DoubleGetFunSym("t"),
				DoubleCmpFunSym("s1", "s2"),
				DoubleGetSubTerm("t", "n"),
				DoubleEquals("t1", "t2")));
		println();
		println(
			TypeTermTemplate(
				"ATerm",
				ATermImpl(),
				ATermGetFunSym("t"),
				ATermCmpFunSym("s1", "s2"),
				ATermGetSubTerm("t", "n"),
				ATermEquals("t1", "t2")));
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
		println(
			TypeTermTemplate(
				TypeName(type.getId()),
				TypeImpl(type.getId()),
				TypeGetFunSym("t"),
				TypeCmpFunSym("s1", "s2"),
				TypeGetSubTerm("t", "n"),
				TypeEquals("t1", "t2"))
			);
		println();

		genTomAltOperators(type);
	}

	private void genTomAltOperators(Type type) {
		Iterator alts = type.alternativeIterator();

		while (alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();
			genTomAltOperator(type, alt);
		}
	}

	private void genTomAltOperator(Type type, Alternative alt) {
		String class_name = TypeName(type.getId());
		String operator_name = OperatorName(alt.getId());
		String alt_class_name = OperatorType(type.getId(), alt.getId());

		print("%op " + TypeName(type.getId()) + " " + operator_name);

		Iterator fields = type.altFieldIterator(alt.getId());
		if (fields.hasNext()) {
			print("(");
			while (fields.hasNext()) {
				Field field = (Field) fields.next();
				String field_id = FieldName(field.getId());
				String field_class = FieldType(field.getType());
				String field_type = field_class;
				print(field_id + ":" + field_type);

				if (fields.hasNext()) {
					print(", ");
				}
			}
			print(")");
		}
		println(" {");
		println("  fsym {" + OperatorFSym(class_name, operator_name) + "}");
		println("  is_fsym(t) { (" + OperatorIsFSym("t", class_name, operator_name) + "}");
			
		fields = type.altFieldIterator(alt.getId());
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			String field_id = buildId(field.getId());
			println("  get_slot("+ field_id + ",t) { " + OperatorGetSlot("t", field_id));
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
		
		println("  make"+ arg+ " { " + OperatorMake(class_name, operator_name, arg)	+ "}");

		println("}");
		println();
	}

	
	
}
