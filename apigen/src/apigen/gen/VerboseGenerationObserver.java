package apigen.gen;

import java.io.File;
import java.io.PrintStream;

public class VerboseGenerationObserver implements GenerationObserver {
	private PrintStream outputStream;
	
	public VerboseGenerationObserver(PrintStream stream) {
		this.outputStream = stream;
	}

	public void fileCreated(String directory, String fileName, String extension) {
		outputStream.println("file created: " + directory + File.separatorChar + fileName + extension);
	}
}
