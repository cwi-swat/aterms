package apigen.gen.java;

import apigen.gen.TypeConversions;

public class JavaTypeConversions implements TypeConversions {

	public String getIntegerType() {
		return "int";
	}

	public String getRealType() {
		return "double";
	}

	public String getTermType() {
		return "aterm.ATerm";
	}

	public String getListType() {
		return "aterm.ATermList";
	}

	public String getStringType() {
		return "String";
	}
}