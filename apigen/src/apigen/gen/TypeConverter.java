package apigen.gen;

import java.util.HashMap;
import java.util.Map;

public class TypeConverter {
	private Map reservedTypes;

	public TypeConverter(TypeConversions conv) {
		reservedTypes = new HashMap();

		reservedTypes.put("int", conv.IntegerType());
		reservedTypes.put("real", conv.RealType());
		reservedTypes.put("str", conv.StringType());
		reservedTypes.put("term", conv.TermType());

	}

	public boolean isReserved(String t) {
		return reservedTypes.containsKey(t);
	}

	public String getType(String t) {
		if (isReserved(t)) {
			return (String) reservedTypes.get(t);
		} else {
			return t;
		}
	}
}
