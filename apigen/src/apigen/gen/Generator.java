package apigen.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public abstract class Generator {
	protected String directory;
	protected String filename;
	protected String extension;
	protected boolean verbose;
	protected boolean folding;
	protected PrintStream stream;

    /**
     * Generate a file using print, println, etc.
     * @param directory The path to the new file
     * @param filename  The name of the new file
     * @param extension The extension of the new file
     * @param verbose   Print information on stderr?
     * @param folding   Print folding comments?
     */
	public Generator(
		String directory,
		String filename,
		String extension,
		boolean verbose,
		boolean folding) {
		this.directory = directory;
		this.filename = filename;
		this.extension = extension;
		this.verbose = verbose;
		this.folding = folding;
	}

    /**
     * Create a new file and use the abstract generate() function to print its contents.
     *
     */
	public void run() {
		stream = createStream(filename, extension, directory);
		info ("generating " + filename + extension);
		generate();
		closeStream(stream);
	}

	/**
	 * Generates the contents of the file using the printing facilities offered by this 
	 * class
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
		if (verbose) {
			System.err.println(msg);
		}
	}

    /**
     * Close a file stream
     */
	protected  void closeStream(PrintStream stream) {
		stream.close();
	}

    /**
     * Create a file if possible
     * @param file A complete path to the file
     * @return PrintStream a handle to the new file
     */
	private PrintStream createStream(String file) {
		try {
			PrintStream stream = new PrintStream(new FileOutputStream(file));
			return stream;
		} catch (FileNotFoundException exc) {
			throw new RuntimeException("fatal error: Failed to open " + file + " for writing.");
		}
	}

    protected String getPath(String directory, String name, String ext) {
    	return directory + File.separatorChar + name + ext;
    }
    
    /**
     * Create a file stream and create the path to it if necessary
     * @param name The name of the file
     * @param ext The extension of the file
     * @param directory The path to the file
     * @return PrintStream A PrintStream to a new file
     */
	protected PrintStream createStream(String name, String ext, String directory) {
		File base = new File(directory);

		if (!base.exists()) {
			if (!base.mkdirs()) {
				throw new RuntimeException(
					"could not create output directory " + directory);
			}
		} else if (!base.isDirectory()) {
			throw new RuntimeException(directory + " is not a directory");
		}

		return createStream(getPath(directory,name,ext));
	}
}
