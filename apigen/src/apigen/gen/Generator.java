package apigen.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public abstract class Generator {
	private GenerationParameters params;

	private String directory;
	private String fileName;
	private String extension;
	private PrintStream stream;

	public Generator(GenerationParameters params) {
		this.params = params;
	}

	public GenerationParameters getGenerationParameters() {
		return params;
	}

	/**
	 * Create a new file and use the abstract generate() function to print its
	 * contents.
	 *  
	 */
	public void run() {
		info("generating " + getFileName() + getExtension());
		stream = createStream(getDirectory(), getFileName(), getExtension());
		generate();
		closeStream(stream);
	}

	/**
	 * Generates the contents of the file using the printing facilities offered
	 * by this class
	 *  
	 */
	abstract protected void generate();

	/**
	 * Print an empty line to the target file
	 *  
	 */
	public void println() {
		stream.println();
	}

	/**
	 * Print a line to the target file
	 */
	public void println(String msg) {
		stream.println(msg);
	}

	/**
	 * Print a message to the target file
	 */
	public void print(String msg) {
		stream.print(msg);
	}

	/**
	 * Print a message on stderr if the verbose option is set to true
	 */
	public void info(String msg) {
		if (params.isVerbose()) {
			System.err.println(msg);
		}
	}

	/**
	 * Close a file stream
	 */
	protected void closeStream(PrintStream stream) {
		stream.close();
	}

	/**
	 * Create a file if possible
	 * 
	 * @param file
	 *            A complete path to the file
	 * @return PrintStream a handle to the new file
	 */
	private PrintStream createStream(String file) {
		try {
			PrintStream stream = new PrintStream(new FileOutputStream(file));
			return stream;
		}
		catch (FileNotFoundException exc) {
			throw new RuntimeException("fatal error: Failed to open " + file + " for writing.");
		}
	}

	private String getPath(String directory, String fileName, String ext) {
		return directory + File.separatorChar + fileName + ext;
	}

	protected PrintStream createStream(String directory, String fileName, String extension) {
		File base = new File(directory);

		if (!base.exists()) {
			if (!base.mkdirs()) {
				throw new RuntimeException("could not create output directory " + directory);
			}
		}
		else if (!base.isDirectory()) {
			throw new RuntimeException(directory + " is not a directory");
		}

		return createStream(getPath(directory, fileName, extension));
	}

	public String getDirectory() {
		return directory;
	}

	public String getFileName() {
		return fileName;
	}

	public String getExtension() {
		return extension;
	}

	public PrintStream getStream() {
		return stream;
	}

	public void setStream(PrintStream stream) {
		this.stream = stream;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
