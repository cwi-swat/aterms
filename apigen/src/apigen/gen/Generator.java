package apigen.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public abstract class Generator {
	// current stream to write to
	protected PrintStream stream;
	
	protected StringConversions stringConverter;
	protected TypeConverter      typeConverter;

	// basic generator options
	protected static boolean verbose = false;
	protected static boolean folding = false;

    protected Generator() {
    	initStringConverter();
    	initTypeConverter();	
    }
	
	public void initStringConverter() {
		stringConverter = new StringConversions();
	}
	
	abstract protected void initTypeConverter();
	
	protected void println() {
		stream.println();
	}

	//}}}
	//{{{ protected void println(String msg)

	protected void println(String msg) {
		stream.println(msg);
	}

	//}}}
	//{{{ protected void print(String msg)

	protected void print(String msg) {
		stream.print(msg);
	}

	//}}}

	//{{{ protected void info(String msg)

	protected void info(String msg) {
		if (verbose) {
			System.err.println(msg);
		}
	}

	//}}}
  
  protected void createStream(String file) 
  {
    try {
      stream = new PrintStream(new FileOutputStream(file));
    }
    catch (FileNotFoundException exc) {
      System.err.println("fatal error: Failed to open " + file + " for writing.");
      System.exit(1);
    }
  }
  
  protected void createFileStream(String name, String ext, String directory) {
	  char sep = File.separatorChar;
	  File base = new File(directory);
    
	  if (!base.exists()) {
		if(!base.mkdirs()) {
		  throw new RuntimeException("could not create output directory " + directory);
		}
	  }
	  else if (!base.isDirectory()) {
		throw new RuntimeException(directory + " is not a directory");
	  }
    
	  createStream(directory + sep + name + ext);
	}

}
