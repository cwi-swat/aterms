package apigen.gen.c;

import java.io.PrintStream;

import apigen.gen.Generator;

abstract public class CGenerator extends Generator {
	protected PrintStream headerStream;

	public CGenerator(String directory, String filename, boolean verbose, boolean folding) {
		super(directory, filename, ".c", verbose, folding);
	}

	public void run() {
		stream = createStream(filename, ".c", directory);
		headerStream = createStream(filename, ".h", directory);

		generate();

		closeStream(stream);
		closeStream(headerStream);
	}

	public void hprintln() {
		headerStream.println();
	}

	public void hprintln(String msg) {
		headerStream.println(msg);
	}

	public void hprint(String msg) {
		headerStream.print(msg);
	}

    public void hwrite(int b) {
			headerStream.write(b);
    }
    
	protected void printFoldOpen(PrintStream out, String comment) {
		out.println("/*{{" + "{  " + comment + " */");
		out.println();
	}

	protected void printFoldClose(PrintStream out) {
		out.println();
		out.println("/*}}" + "}  */");
	}

	protected void hprintFoldOpen(String comment) {
			printFoldOpen(headerStream,comment);
	}

	protected void hprintFoldClose() {
		printFoldClose(headerStream);
	}
	
	protected void printFoldOpen(String comment) {
		printFoldOpen(stream,comment);
	}

	protected void printFoldClose() {
		printFoldClose(stream);
	}
			
}
