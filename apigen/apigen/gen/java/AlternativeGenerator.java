package apigen.gen.java;

import java.io.File;
import java.util.List;

import apigen.adt.*;
import apigen.gen.StringConversions;

public class AlternativeGenerator extends JavaGenerator {
	private Type type;
	private Alternative alt;

	protected AlternativeGenerator(
		Type type,
		Alternative alt,
		String directory,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, getAltClassName(type.getId(), alt.getId()), pkg, standardImports, verbose, folding);
		this.type = type;
		this.alt = alt;
	}

	public static String getAltClassName(String type, String alt) {
		return getClassName(type + "_" + StringConversions.makeCapitalizedIdentifier(alt));
	}

	public static String getAltClassName(Type type, Alternative alt) {
		return getAltClassName(type.getId(), alt.getId());
	}

	public void run() {
		if (!new File(getPath(directory, getAltClassName(type,alt), ".java")).exists()) {
			super.run();
		} else {
			info("preserving " + getAltClassName(type,alt));
		}
	}

	protected void generate() {
		printPackageDecl();
		genAlternativeClass(type, alt);
	}

	private void genAlternativeClass(Type type, Alternative alt) {
		String alt_class = getAltClassName(type, alt);
		String alt_impl_class = AlternativeImplGenerator.getAltClassImplName(type.getId(), alt.getId());

		println("public class " + alt_class);
		println("extends " + alt_impl_class);
		println("{");
		println();
		println("}");
	}
}
