package apigen.gen;

import java.util.LinkedList;
import java.util.List;

public class GenerationParameters {
	private String outputDirectory;
	private String packageName;
	private String apiName;
	private String prefix;
	private boolean verbose;
	private boolean folding;
	private List inputFiles;

	public GenerationParameters() {
		imports = new LinkedList();
		inputFiles = new LinkedList();
	}

	public boolean isFolding() {
		return folding;
	}

	public void setFolding(boolean folding) {
		this.folding = folding;
	}

	public List getInputFiles() {
		return inputFiles;
	}

	public void addInputFile(String fileName) {
		inputFiles.add(fileName);
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
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

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}


	
	private boolean visitable;	// TODO refactor into JavaGenerationParameters
	
	public boolean isVisitable() {
		return visitable;
	}

	public void setVisitable(boolean visitable) {
		this.visitable = visitable;
	}

	private List imports;

	public void addImport(String importName) {
		imports.add(importName);
	}

	public List getImports() {
		return imports;
	}

}
