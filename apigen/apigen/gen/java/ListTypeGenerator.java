package apigen.gen.java;

import java.util.List;

import apigen.adt.Type;


public class ListTypeGenerator extends TypeGenerator {

	public ListTypeGenerator(
		Type type,
		String directory,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(type, directory, pkg, standardImports, verbose, folding);
	}
	
	protected String classModifier() {
		return "public";
	}
}
