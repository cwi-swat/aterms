package apigen.gen.java;

import apigen.gen.TypeConversions;

public class JavaTypeConversions implements TypeConversions {

	public String IntegerType() {
		return "Integer";
	}

	public String RealType() {
		return "Double";
	}

	public String TermType() {
		return "ATerm";
	}

	public String ListType() {
		return "ATermList";
	}

  public String StringType() {
		return "String";
	}
}
