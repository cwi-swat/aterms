
package apigen.gen;

import java.util.HashMap;
import java.util.Map;

public class TypeConverter {	
	static private Map reservedTypes;
	
	protected static void registerConversions(String integer, String real, String string, String term) {
		reservedTypes = new HashMap();
		
		reservedTypes.put("int",integer);
		reservedTypes.put("real",real);
		reservedTypes.put("str",string);
		reservedTypes.put("term",term);
		
	}
	
	static public boolean isReserved(String t) {
		return reservedTypes.containsKey(t);
	}
	
	static public String getType(String t) {
		if (isReserved(t)) {
			return (String) reservedTypes.get(t);
		}
		else {
			return t;
		}
	}
}
