
package apigen.gen;

import java.util.HashMap;
import java.util.Map;

public class TypeConverter {	
	private Map reservedTypes;
	
	public TypeConverter(String integer, String real, String string, String term) {
		reservedTypes = new HashMap();
		
		reservedTypes.put("int",integer);
		reservedTypes.put("real",real);
		reservedTypes.put("str",string);
		reservedTypes.put("term",term);
		
	}
	
	public boolean isReserved(String t) {
		return reservedTypes.containsKey(t);
	}
	
	public String getType(String t) {
		if (isReserved(t)) {
			return (String) reservedTypes.get(t);
		}
		else {
			return t;
		}
	}
}
