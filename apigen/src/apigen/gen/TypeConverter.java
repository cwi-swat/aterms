package apigen.gen;

import java.util.HashMap;
import java.util.Map;

public class TypeConverter implements TypeConversions {
	private Map reservedTypes;

	public TypeConverter(TypeConversions conv) {
		reservedTypes = new HashMap();

		reservedTypes.put("int", conv.IntegerType());
		reservedTypes.put("real", conv.RealType());
		reservedTypes.put("str", conv.StringType());
		reservedTypes.put("term", conv.TermType());

	}

	/**
	 * Tests whether a type-name is reserved
	 * 
	 */
	public boolean isReserved(String t) {
		return reservedTypes.containsKey(t);
	}

	/**
	 * Transforms reserved type to their target implementation and
	 * leaves other types alone.
	 * 
	 */
	public String getType(String t) {
		if (isReserved(t)) {
			return (String) reservedTypes.get(t);
		} else {
			return t;
		}
	}

	/**
	 * Returns the implementation type of: int
	 */
	public String IntegerType() {
		return (String) reservedTypes.get("int");
	}
	/**
	 * Returns the implementation type of: real
	 */
	public String RealType() {
		return (String) reservedTypes.get("real");
	}
	/**
	 * Returns the implementation type of: str
	 */
	public String StringType() {
		return (String) reservedTypes.get("str");
	}
	/**
	 * Returns the implementation type of: term
	 */
	public String TermType() {
		return (String) reservedTypes.get("term");
	}
}
