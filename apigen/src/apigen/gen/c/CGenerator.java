package apigen.gen.c;

import java.io.PrintStream;

import apigen.gen.GenerationException;
import apigen.gen.Generator;

abstract public class CGenerator extends Generator {
	private static final String SOURCE_FILE_EXTENSION = ".c";
	private static final String HEADER_FILE_EXTENSION = ".h";

	private PrintStream headerStream;
	private boolean folding;
	
	public CGenerator(CGenerationParameters params) {
		super(params);
		setDirectory(params.getOutputDirectory());
		setExtension(SOURCE_FILE_EXTENSION);
		setFileName(params.getApiName());
		folding = params.isFolding();
	}

	public CGenerationParameters getCGenerationParameters() {
		return (CGenerationParameters) getGenerationParameters();
	}

	/**
	 * Create an empty header file and an empty source file, then run the
	 * abstract code generator and close the two files afterwards
	 */
	public void run() {
        try {
		  headerStream = createStream(getDirectory(), getFileName(), HEADER_FILE_EXTENSION);
		  super.run();
		  closeStream(headerStream);
        }
        catch (GenerationException exc) {
            System.err.println("An error occurred at generation time:");
            System.err.println(exc);
            System.exit(1);
        }
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
	public void hprintFunDecl(String returnType, String funName, String funArgs, String macroReplacementStr, String macroArgs) {
		headerStream.println("#ifdef FAST_API");
		headerStream.println("#define " + funName + macroArgs + " (" + macroReplacementStr + ")");
		headerStream.println("#else");
		headerStream.println(returnType + " _" + funName + funArgs + ";");
		headerStream.println("#define " + funName + macroArgs + " (_" + funName + macroArgs + ")");
		headerStream.println("#endif");
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
		if (folding) {
			out.println("/*{{" + "{  " + comment + " */");
			out.println();
		}
	}

	/**
	 * Print a close fold comment to a stream
	 */
	protected void printFoldClose(PrintStream out) {
		if (folding) {
			out.println();
			out.println("/*}}" + "}  */");
		}
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
	
	protected void printDocHead(String title, String message) {
		println("/**");
		println(" * " + title + ". " + message);
	}
	
	protected void printlnDoc(String msg) {
		println(" * " + msg);
	}
	
	protected void printDocArg(String name, String message) {
		printlnDoc("\\param[in] " + name + " " + message);
	}
	
	protected void printDocReturn(String message) {
		printlnDoc("\\return " + message);
	}
	
	protected void printDocTail() {
		println(" */");
	}

}
