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

	public void run() {
		stream = createStream(filename, extension, directory);
		generate();
		closeStream(stream);
	}

	/** Using print, println etc. create the contents of the file */
	abstract protected void generate();

	public void println() {
		stream.println();
	}

	public void println(String msg) {
		stream.println(msg);
	}

	public void print(String msg) {
		stream.print(msg);
	}

	public void info(String msg) {
		if (verbose) {
			System.err.println(msg);
		}
	}

	protected  void closeStream(PrintStream stream) {
		stream.close();
	}

	private PrintStream createStream(String file) {
		try {
			PrintStream stream = new PrintStream(new FileOutputStream(file));
			return stream;
		} catch (FileNotFoundException exc) {
			System.err.println(
				"fatal error: Failed to open " + file + " for writing.");
			System.exit(1);
		}
		
		return null;
	}

    protected String getPath(String directory, String name, String ext) {
    	return directory + File.separatorChar + name + ext;
    }
    
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
