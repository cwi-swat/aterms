package apigen.gen.java;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apigen.gen.GenerationParameters;

public class JavaGenerationParameters extends GenerationParameters {
	private boolean visitable;
	private String packageName;
	private List imports;

	public JavaGenerationParameters() {
		super();
		imports = new LinkedList();
	}
	
	public void parseArguments(List args) {
		Iterator iter = args.iterator();
		while (iter.hasNext()) {
			String arg = (String) iter.next();
			if ("--package".startsWith(arg) || "-p".startsWith(arg)) {
				shift(iter);
				setPackageName(shiftArgument(iter));
			}
			else if ("--import".startsWith(arg) || "-m".startsWith(arg)) {
				shift(iter);
				addImport(shiftArgument(iter));
			}
			else if ("--visitable".startsWith(arg) || "-t".startsWith(arg)) {
				shift(iter);
				setVisitable(true);
			}
		}
		super.parseArguments(args);
	}
	
	public String usage() {
		StringBuffer buf = new StringBuffer(super.usage());
		buf.append("\t-p | --package <package>       <optional>\n");
		buf.append("\t-i | --import <package>        (can be repeated)\n");
		buf.append("\t-t | --visitable               [off]\n");
		return buf.toString();
	}

	public boolean isVisitable() {
		return visitable;
	}

	public void setVisitable(boolean visitable) {
		this.visitable = visitable;
	}

	public void addImport(String importName) {
		imports.add(importName);
	}

	public List getImports() {
		return imports;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}