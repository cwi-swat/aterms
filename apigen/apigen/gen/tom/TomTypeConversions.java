package apigen.gen.tom;

import apigen.gen.TypeConversions;

public class TomTypeConversions implements TypeConversions {

	public String IntegerType() {
		return "int";
	}

	public String RealType() {
		return "double";
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
