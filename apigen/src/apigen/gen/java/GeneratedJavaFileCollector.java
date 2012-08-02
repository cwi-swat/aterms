package apigen.gen.java;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import apigen.gen.GenerationObserver;

public class GeneratedJavaFileCollector implements GenerationObserver {
	private List<String> generatedFiles;

	public GeneratedJavaFileCollector() {
		generatedFiles = new LinkedList<String>();
	}

	public void fileCreated(String directory, String fileName, String extension) {
		if (extension.equals(".java")) {
			generatedFiles.add(directory + File.separatorChar + fileName
					+ extension);
		}
	}

	public List<String> getGeneratedFiles() {
		return generatedFiles;
	}
}
