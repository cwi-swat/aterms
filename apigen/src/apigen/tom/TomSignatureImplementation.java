package apigen.tom;

public interface TomSignatureImplementation {
	String StringName();
	String StringImpl();
	String StringCmpFunSym(String arg1, String arg2);
	String StringGetFunSym(String arg);
	String StringGetSubTerm(String arg1, String arg2);
	String StringEquals(String arg1, String arg2);

	String IntegerName();
	String IntegerImpl();
	String IntegerGetFunSym(String arg);
	String IntegerCmpFunSym(String arg1, String arg2);
	String IntegerGetSubTerm(String arg1, String arg2);
	String IntegerEquals(String arg1, String arg2);

	String DoubleName();
	String DoubleImpl();
	String DoubleGetFunSym(String arg);
	String DoubleCmpFunSym(String arg1, String arg2);
	String DoubleGetSubTerm(String arg1, String arg2);
	String DoubleEquals(String arg1, String arg2);

	String ATermName();
	String ATermImpl();
	String ATermGetFunSym(String arg);
	String ATermCmpFunSym(String arg1, String arg2);
	String ATermGetSubTerm(String arg1, String arg2);
	String ATermEquals(String arg1, String arg2);

	String TypeName(String type);
	String TypeImpl(String type);
	String TypeGetFunSym(String arg1);
	String TypeCmpFunSym(String arg1, String arg2);
	String TypeGetSubTerm(String term, String n);
	String TypeEquals(String type, String arg1, String arg2);

	String OperatorName(String id);
	String OperatorType(String type, String id);
	String OperatorGetSlot(String arg, String fieldType, String field_id);
	String OperatorIsFSym(String string, String type, String alt);
	String OperatorFSym(String type, String alt);
	String OperatorMake(String type, String alt, String args);

	String FieldName(String id);
	String FieldType(String type);
}
