package apigen.gen.tom;

public interface TomSignatureImplementation {
	
	String IncludePrefix();
	
	String TypeName(String type);
	String TypeImpl(String type);
	String TypeEquals(String type, String arg1, String arg2);
	String TypeGetStamp();
	String TypeSetStamp(String type);
	String TypeGetImplementation(String arg);
	
	String OperatorName(String type, String id);
	String OperatorType(String type, String id);
	String OperatorGetSlot(String arg, String fieldType, String field_id);
	String OperatorIsFSym(String string, String type, String alt);
	String OperatorMake(String type, String alt, String args);

	String FieldName(String id);
	String FieldType(String type);

	String ListHead(String type);
	String ListTail(String type);
	String ListEmpty(String type);
	String ListIsList(String term, String type);
	String ListmakeEmpty(String type);
	String ListmakeInsert(String type, String eltType);
}
