package apigen.gen;

import java.util.Iterator;

import apigen.adt.*;

public abstract class TomSignatureGenerator extends Generator {
    protected String apiName = "";
    private TomSignatureImplementation impl;
    private ADT adt;
	
	public TomSignatureGenerator(ADT adt, TomSignatureImplementation impl, 
	        String directory, String api_name, boolean verbose, boolean folding) {
	   super(directory, StringConversions.makeIdentifier(api_name), ".t", verbose, folding);
	   this.adt = adt;
	   this.impl = impl;
	}
	
	public void generate() {
		info("generating " + filename + ".t");

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
				impl.StringName(),
				impl.StringImpl(),
				impl.StringGetFunSym("t"),
				impl.StringCmpFunSym("s1", "s2"),
		impl.StringGetSubTerm("t", "n"),
		impl.StringEquals("t1", "t2")));
		println();
		println(
			TypeTermTemplate(
		impl.IntegerName(),
		impl.IntegerImpl(),
		impl.IntegerGetFunSym("t"),
		impl.IntegerCmpFunSym("s1", "s2"),
		impl.IntegerGetSubTerm("t", "n"),
		impl.IntegerEquals("t1", "t2")));
		println();
		println(
			TypeTermTemplate(
		impl.DoubleName(),
		impl.DoubleImpl(),
		impl.DoubleGetFunSym("t"),
		impl.DoubleCmpFunSym("s1", "s2"),
		impl.DoubleGetSubTerm("t", "n"),
		impl.DoubleEquals("t1", "t2")));
		println();
		println(
			TypeTermTemplate(
		impl.ATermName(),
		impl.ATermImpl(),
		impl.ATermGetFunSym("t"),
		impl.ATermCmpFunSym("s1", "s2"),
		impl.ATermGetSubTerm("t", "n"),
		impl.ATermEquals("t1", "t2")));
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
		impl.TypeName(type.getId()),
		impl.TypeImpl(type.getId()),
		impl.TypeGetFunSym("t"),
		impl.TypeCmpFunSym("s1", "s2"),
		impl.TypeGetSubTerm("t", "n"),
		impl.TypeEquals(type.getId(),"t1", "t2"))
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
		String class_name = impl.TypeName(type.getId());
		String operator_name = impl.OperatorName(alt.getId());
		String alt_class_name = impl.OperatorType(type.getId(), alt.getId());

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
		println("  is_fsym(t) { " + impl.OperatorIsFSym("t", class_name, operator_name) + "}");
			
		fields = type.altFieldIterator(alt.getId());
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			String field_id = StringConversions.makeIdentifier(field.getId());
			println("  get_slot("+ field_id + ",t) { " + impl.OperatorGetSlot("t", class_name, field_id) + "}");
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
		
		println("  make"+ arg+ " { " + impl.OperatorMake(class_name, operator_name, arg)	+ "}");

		println("}");
		println();
	}
}
