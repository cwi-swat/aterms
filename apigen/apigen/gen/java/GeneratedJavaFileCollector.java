package apigen.gen.java;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import apigen.gen.GenerationObserver;

public class GeneratedJavaFileCollector implements GenerationObserver {
	private List generatedFiles;

	public GeneratedJavaFileCollector() {
		generatedFiles = new LinkedList();
	}

	public void fileCreated(String directory, String fileName, String extension) {
		if (extension.equals(".java")) {
			generatedFiles.add(directory + File.separatorChar + extension);
		}
	}

	public List getGeneratedFiles() {
		return generatedFiles;
	}
}
