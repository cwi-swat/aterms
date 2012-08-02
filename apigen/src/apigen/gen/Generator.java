package apigen.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class Generator {
	private GenerationParameters params;
	private List<GenerationObserver> listeners;
	private String directory;
	private String fileName;
	private String extension;
	private PrintStream stream;

	public Generator(GenerationParameters params) {
		this.params = params;
		if (params.isVerbose()) {
			addGenerationObserver(new VerboseGenerationObserver(System.err));
		}
	}

	public GenerationParameters getGenerationParameters() {
		return params;
	}

	public void run() {
		try {
			stream = createStream(getDirectory(), getFileName(), getExtension());
			fireFileCreated(getDirectory(), getFileName(), getExtension());
			generate();
			closeStream(stream);
		} catch (GenerationException exc) {
			System.err.println("An error occurred at generation time:");
			System.err.println(exc);
			System.exit(1);
		}
	}

	abstract protected void generate() throws GenerationException;

	public void println() {
		stream.println();
	}

	public void println(String msg) {
		stream.println(msg);
	}

	public void print(String msg) {
		stream.print(msg);
	}

	protected void closeStream(PrintStream stream) {
		stream.close();
	}

	private PrintStream createStream(String fileName)
			throws GenerationException {
		try {
			return new PrintStream(new FileOutputStream(fileName));
		} catch (FileNotFoundException exc) {
			throw new GenerationException("fatal error: Failed to open "
					+ fileName + " for writing.");
		}
	}

	private static String getPath(String directory, String fileName, String ext) {
		return directory + File.separatorChar + fileName + ext;
	}

	protected PrintStream createStream(String directory, String fileName,
			String extension) throws GenerationException {
		File base = new File(directory);

		if (!base.exists()) {
			if (!base.mkdirs()) {
				throw new GenerationException(
						"could not create output directory " + directory);
			}
		} else if (!base.isDirectory()) {
			throw new GenerationException(directory + " is not a directory");
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

	public final void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void addGenerationObserver(GenerationObserver aListener) {
		if (listeners == null) {
			listeners = new LinkedList<GenerationObserver>();
		}
		listeners.add(aListener);
	}

	public void removeGenerationObserver(GenerationObserver aListener) {
		if (listeners != null) {
			listeners.remove(aListener);
		}
	}

	protected void fireFileCreated(String directory, String fileName,
			String extension) {
		if (listeners != null) {
			Iterator<GenerationObserver> iter = listeners.iterator();
			while (iter.hasNext()) {
				iter.next().fileCreated(directory, fileName, extension);
			}
		}
	}
}
