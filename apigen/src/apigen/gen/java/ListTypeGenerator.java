package apigen.gen.java;

import java.util.List;

import apigen.adt.Type;


public class ListTypeGenerator extends TypeGenerator {

	public ListTypeGenerator(
		Type type,
		String directory,
		String pkg,
		String apiName,
		List standardImports,
		boolean verbose) {
		super(type, directory, pkg, apiName, standardImports, verbose);
	}
	
	protected String classModifier() {
		return "public";
	}
}
