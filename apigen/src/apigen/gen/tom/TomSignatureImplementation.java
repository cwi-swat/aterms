package apigen.gen.tom;

public interface TomSignatureImplementation {
	String ATermName();
	String ATermImpl();
	String ATermGetFunSym(String arg);
	String ATermCmpFunSym(String arg1, String arg2);
	String ATermGetSubTerm(String arg1, String arg2);
	String ATermEquals(String arg1, String arg2);

	String ATermListName();
	String ATermListImpl();
	String ATermListGetFunSym(String arg);
	String ATermListCmpFunSym(String arg1, String arg2);
	String ATermListEquals(String arg1, String arg2);
	String ATermListGetFirst(String arg);
	String ATermListGetNext(String arg);
	String ATermListIsEmpty(String arg);
	
	String TypeName(String type);
	String TypeImpl(String type);
	String TypeGetFunSym(String arg1);
	String TypeCmpFunSym(String arg1, String arg2);
	String TypeGetSubTerm(String term, String n);
	String TypeEquals(String type, String arg1, String arg2);

	String OperatorName(String type, String id);
	String OperatorType(String type, String id);
	String OperatorGetSlot(String arg, String fieldType, String field_id);
	String OperatorIsFSym(String string, String type, String alt);
	String OperatorFSym(String type, String alt);
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
