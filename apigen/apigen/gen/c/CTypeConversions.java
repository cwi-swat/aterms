package apigen.gen.c;

import apigen.gen.TypeConversions;

public class CTypeConversions implements TypeConversions {
	
	public String IntegerType() {
		return "int";
	}

	public String RealType() {
		return "float";
	}

	
	public String TermType() {
		return "ATerm";
	}

	public String ListType() {
		return "ATermList";
	}
	
	public String StringType() {
		return "char*";
	}
}
