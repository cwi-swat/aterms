package apigen.gen.c;

import apigen.gen.TypeConversions;

public class CTypeConversions implements TypeConversions {

	public String getIntegerType() {
		return "int";
	}

	public String getRealType() {
		return "float";
	}

	public String getTermType() {
		return "ATerm";
	}

	public String getListType() {
		return "ATermList";
	}

	public String getStringType() {
		return "char*";
	}
}
