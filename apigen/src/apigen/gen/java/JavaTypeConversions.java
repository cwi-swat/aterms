package apigen.gen.java;

import apigen.gen.TypeConversions;

public class JavaTypeConversions implements TypeConversions {

	public String IntegerType() {
		return "int";
	}

	public String RealType() {
		return "double";
	}

	public String TermType() {
		return "aterm.ATerm";
	}

	public String ListType() {
		return "aterm.ATermList";
	}

  public String StringType() {
		return "String";
	}
}
