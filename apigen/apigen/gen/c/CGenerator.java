package apigen.gen.c;

import java.io.PrintStream;

import apigen.gen.Generator;

abstract public class CGenerator extends Generator {
	private static final String SOURCE_FILE_EXTENSION = ".c";
	private static final String HEADER_FILE_EXTENSION = ".h";

	private PrintStream headerStream;

	public CGenerator(CGenerationParameters params) {
		super(params);
		setDirectory(params.getOutputDirectory());
		setExtension(SOURCE_FILE_EXTENSION);
		setFileName(params.getApiName());
	}

	public CGenerationParameters getCGenerationParameters() {
		return (CGenerationParameters) getGenerationParameters();
	}

	/**
	 * Create an empty header file and an empty source file, then run the
	 * abstract code generator and close the two files afterwards
	 */
	public void run() {
		headerStream = createStream(getDirectory(), getFileName(), HEADER_FILE_EXTENSION);
		super.run();
		closeStream(headerStream);
	}

	/**
	 * Print an empty line in the header file
	 *  
	 */
	public void hprintln() {
		headerStream.println();
	}

	/**
	 * Print a line to the header file
	 * 
	 * @param msg
	 */
	public void hprintln(String msg) {
		headerStream.println(msg);
	}

	/**
	 * Print a message to the header file
	 * 
	 * @param msg
	 */
	public void hprint(String msg) {
		headerStream.print(msg);
	}

	/**
	 * Write a byte to the header file
	 * 
	 * @param b
	 */
	public void hwrite(int b) {
		headerStream.write(b);
	}

	/**
	 * Print an open fold comment to a stream
	 * 
	 * @param out
	 * @param comment
	 */
	protected void printFoldOpen(PrintStream out, String comment) {
		out.println("/*{{" + "{  " + comment + " */");
		out.println();
	}

	/**
	 * Print a close fold comment to a stream
	 */
	protected void printFoldClose(PrintStream out) {
		out.println();
		out.println("/*}}" + "}  */");
	}

	/**
	 * Print an open fold comment to the header file
	 * 
	 * @param comment
	 */
	protected void hprintFoldOpen(String comment) {
		printFoldOpen(headerStream, comment);
	}

	/**
	 * Print a close fold comment to the header file
	 *  
	 */
	protected void hprintFoldClose() {
		printFoldClose(headerStream);
	}

	/**
	 * Print an open fold coment to the source code
	 * 
	 * @param comment
	 */
	protected void printFoldOpen(String comment) {
		printFoldOpen(getStream(), comment);
	}

	/**
	 * Print a close fold comment to the source code
	 *  
	 */
	protected void printFoldClose() {
		printFoldClose(getStream());
	}

	/**
	 * Print an open fold comment to both the header and the source code
	 * 
	 * @param comment
	 */
	protected void bothPrintFoldOpen(String comment) {
		printFoldOpen(comment);
		hprintFoldOpen(comment);
	}

	/**
	 * Print a closing fold comment to both the header and the source code
	 *  
	 */
	protected void bothPrintFoldClose() {
		printFoldClose();
		hprintFoldClose();
	}

	/**
	 * Print a line to both the source code and the header file
	 * 
	 * @param msg
	 */
	protected void bothPrintln(String msg) {
		println(msg);
		hprintln(msg);
	}

	/**
	 * Print an empty line to both the source code and the header file
	 *  
	 */
	protected void bothPrintln() {
		println();
		hprintln();
	}

	/**
	 * Print a message to both the source code and the header file
	 * 
	 * @param msg
	 */
	protected void bothPrint(String msg) {
		print(msg);
		hprint(msg);
	}

}
