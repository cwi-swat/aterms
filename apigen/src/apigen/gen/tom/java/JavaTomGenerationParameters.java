package apigen.gen.tom.java;

import java.util.Iterator;
import java.util.List;

import apigen.gen.java.JavaGenerationParameters;

class JavaTomGenerationParameters extends JavaGenerationParameters {
	private boolean jtype;
	private boolean javaGenStuff;

	public JavaTomGenerationParameters() {
		super();
		setOutputDirectory(".");
		setPrefix("");
	}

	public boolean isJavaGen() {
		return javaGenStuff;
	}

	public boolean isJtype() {
		return jtype;
	}

	@Override
	public void parseArguments(List<String> args) {
		Iterator<String> iter = args.iterator();
		while (iter.hasNext()) {
			String arg = iter.next();
			if (arg.startsWith("--jtype")) {
				shift(iter);
				setJtype(true);
			} else if (arg.startsWith("--javagen")) {
				shift(iter);
				setJavaGen(true);
			}
		}
		super.parseArguments(args);
	}

	private void setJavaGen(boolean javaGen) {
		javaGenStuff = javaGen;
	}

	public void setJtype(boolean jtype) {
		System.out.println("Setting JType to " + jtype);
		this.jtype = jtype;
	}

	@Override
	public String usage() {
		StringBuffer buf = new StringBuffer(super.usage());
		buf
				.append("\t--jtype\t\t\t\t<insert sensible explanation about --jtype here>");
		buf
				.append("\n\t--javagen\t\t\tGenerate Java API thanks to adt-to-java");
		return buf.toString();
	}

}