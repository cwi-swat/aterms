package apigen.gen;

import java.util.HashMap;
import java.util.Map;

public class TypeConverter implements TypeConversions {
	private Map reservedTypes;

	/**
	 * Create a new TypeConverter
	 * 
	 * @param conv
	 *            The mapping of builtin (ATerm) types to target language types
	 */
	public TypeConverter(TypeConversions conv) {
		reservedTypes = new HashMap();

		reservedTypes.put("int", conv.getIntegerType());
		reservedTypes.put("real", conv.getRealType());
		reservedTypes.put("str", conv.getStringType());
		reservedTypes.put("term", conv.getTermType());
		reservedTypes.put("list", conv.getListType());

	}

	/**
	 * Tests whether a type-name is reserved
	 *  
	 */
	public boolean isReserved(String t) {
		return reservedTypes.containsKey(t);
	}

	/**
	 * Transforms reserved type to their target implementation and leaves other
	 * types alone.
	 *  
	 */
	public String getType(String t) {
		if (isReserved(t)) {
			return (String) reservedTypes.get(t);
		}
		else {
			return t;
		}
	}

	/**
	 * Returns the implementation type of: int
	 */
	public String getIntegerType() {
		return (String) reservedTypes.get("int");
	}

	/**
	 * Returns the implementation type of: real
	 */
	public String getRealType() {
		return (String) reservedTypes.get("real");
	}

	/**
	 * Returns the implementation type of: str
	 */
	public String getStringType() {
		return (String) reservedTypes.get("str");
	}

	/**
	 * Returns the implementation type of: term
	 */
	public String getTermType() {
		return (String) reservedTypes.get("term");
	}

	/**
	 * Returns the implementation type of: list
	 */
	public String getListType() {
		return (String) reservedTypes.get("list");
	}

}
