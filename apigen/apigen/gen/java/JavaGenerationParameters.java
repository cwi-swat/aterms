package apigen.gen.java;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apigen.gen.GenerationParameters;

public class JavaGenerationParameters extends GenerationParameters {
	private boolean visitable;
	private boolean generateJar;
	private String packageName;
	private List imports;
	private String version;

	public JavaGenerationParameters() {
		super();
		imports = new LinkedList();
		setGenerateJar(true);
	}

	public void parseArguments(List args) {
		Iterator iter = args.iterator();
		while (iter.hasNext()) {
			String arg = (String) iter.next();
			if ("--package".startsWith(arg) || "-p".startsWith(arg)) {
				shift(iter);
				setPackageName(shiftArgument(iter));
			}	else if ("--import".startsWith(arg) || "-m".startsWith(arg)) {
				shift(iter);
				addImport(shiftArgument(iter));
			} else if ("--visitable".startsWith(arg) || "-t".startsWith(arg)) {
				shift(iter);
				setVisitable(true);
			} else if ("--nojar".startsWith(arg)) {
				shift(iter);
				setGenerateJar(false);
			} else if ("--version".startsWith(arg) || "-V".startsWith(arg)) {
				shift(iter);
				setVersion(shiftArgument(iter));
			}
		}
		super.parseArguments(args);
	}

	public String usage() {
		StringBuffer buf = new StringBuffer(super.usage());
		buf.append("\t-p | --package <package>       package name (optional)\n");
		buf.append("\t-m | --import <package>        list of added import package(can be repeated)\n");
		buf.append("\t-t | --visitable               [off]\n");
		buf.append("\t-V | --version <version>       specify api-version used for generated jar file\n");
		buf.append("\t--nojar                        Do not generate Jar file [off]\n");
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

	public boolean isGenerateJar() {
		return generateJar;
	}

	public void setGenerateJar(boolean generateJar) {
		this.generateJar = generateJar;
	}
	
	public String getVersion() {
	  return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
		
	public void check() {
		super.check();
		if (getVersion() == null && isGenerateJar()) {
			throw new IllegalArgumentException("No API version specified");
		}
	}
}