package apigen.gen.tom.java;

import java.util.Iterator;
import java.util.List;

import apigen.gen.java.JavaGenerationParameters;


class JavaTomGenerationParameters extends JavaGenerationParameters {
	private boolean jtype;
	private boolean javaGenStuff;
	
	public void parseArguments(List args) {
		Iterator iter = args.iterator();
		while (iter.hasNext()) {
			String arg = (String) iter.next();
			if (arg.startsWith("--jtype")) {
				shift(iter);
				setJtype(true);
			} else if (arg.startsWith("--javagen")){
				shift(iter);
				setJavaGen(true);
			}
		}
		super.parseArguments(args);
	}

	public String usage() {
		StringBuffer buf = new StringBuffer(super.usage());
		buf.append("\t--jtype\t\t\t\t<insert sensible explanation about --jtype here>");
		buf.append("\n\t--javagen\t\t\tGenerate Java API thanks to adt-to-java");
		return buf.toString();
	}
	
	public boolean isJavaGen() {
		return javaGenStuff;
	}
	
  private void setJavaGen(boolean javaGen) {
    this.javaGenStuff = javaGen;
  }

	public boolean isJtype() {
		return jtype;
	}

	public void setJtype(boolean jtype) {
	    System.out.println("Setting JType to "+jtype);
		this.jtype = jtype;
	}
	
} 