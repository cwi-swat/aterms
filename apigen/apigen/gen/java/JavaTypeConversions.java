package apigen.gen.java;

import apigen.gen.TypeConverter;

public class JavaTypeConversions extends TypeConverter {
	static {
		registerConversions("Integer","Double","String","ATerm");
	}
}
