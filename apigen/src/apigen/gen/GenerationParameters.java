package apigen.gen;

import java.util.LinkedList;
import java.util.List;

public class GenerationParameters {
	private String baseDir = ".";
	private String packageName = "";
	private List imports = null;
	private String apiName;
	private String prefix = "";
	private boolean visitable = false;
	private boolean verbose = false;
	private boolean folding = false;

	public boolean isFolding() {
		return folding;
	}

	public void setFolding(boolean folding) {
		this.folding = folding;
	}

	public GenerationParameters() {
		imports = new LinkedList();
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void addImport(String importName) {
		imports.add(importName);
	}

	public List getImports() {
		return imports;
	}

	public boolean isVisitable() {
		return visitable;
	}

	public void setVisitable(boolean visitable) {
		this.visitable = visitable;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
