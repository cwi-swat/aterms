package apigen.gen.tom;

import apigen.gen.TypeConversions;

public class TomTypeConversions implements TypeConversions {

	public String getIntegerType() {
		return "int";
	}

	public String getRealType() {
		return "double";
	}

	public String getTermType() {
		return "ATerm";
	}

	public String getListType() {
		return "ATermList";
	}

	public String getStringType() {
		return "String";
	}
}