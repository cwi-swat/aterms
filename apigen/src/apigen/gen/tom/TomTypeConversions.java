package apigen.gen.tom;

import apigen.gen.TypeConversions;

public class TomTypeConversions implements TypeConversions {

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
