package apigen.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public abstract class Generator {
	// current stream to write to
	protected PrintStream stream;

	// basic generator options
	protected static boolean verbose = false;
	protected static boolean folding = false;

	protected static Map specialChars;
	protected static char last_special_char;
	protected static String last_special_char_word;
	protected static Map reservedTypes;

	// Do *NOT* add an entry for '-' (Dash) here, the dash is used
	// as a word-separator!

	protected final static String[] SPECIAL_CHAR_WORDS =
		{
			"[BracketOpen",
			"]BracketClose",
			"{BraceOpen",
			"}BraceClose",
			"(ParenOpen",
			")ParenClose",
			"<LessThan",
			">GreaterThan",
			"|Bar",
			"&Amp",
			"+Plus",
			",Comma",
			".Period",
			"~Tilde",
			":Colon",
			";SemiColon",
			"=Equals",
			"#Hash",
			"/Slash",
			"\\Backslash",
			"*Star",
			"_Underscore",
			"$Dollar",
			"'SingleQuote",
			};

 
	protected void initializeConstants(String[][] RESERVED_TYPES) {
		specialChars = new HashMap();
		reservedTypes = new HashMap();

		for (int i = 0; i < SPECIAL_CHAR_WORDS.length; i++) {
			String word = SPECIAL_CHAR_WORDS[i];
			specialChars.put(new Character(word.charAt(0)), word.substring(1));
		}

		for (int i = 0; i < RESERVED_TYPES.length; i++) {
			reservedTypes.put(RESERVED_TYPES[i][0], RESERVED_TYPES[i][1]);
		}
	}

	//{{{ protected String buildId(String id)

	protected String buildId(String id) {
		StringBuffer buf = new StringBuffer();
		boolean cap_next = false;

		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (isSpecialChar(c)) {
				buf.append(getSpecialCharWord(c));
				cap_next = true;
			}
			else {
				switch (c) {
					case '-' :
						cap_next = true;
						break;
					default :
						if (cap_next) {
							buf.append(Character.toUpperCase(c));
							cap_next = false;
						}
						else {
							buf.append(c);
						}
						break;
				}
			}
		}

		return buf.toString();
	}

	//}}}
	//{{{ public static String capitalize(String s)

	public static String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	//}}}

	//{{{ protected boolean isSpecialChar(char c)

	protected boolean isSpecialChar(char c) {
		return getSpecialCharWord(c) != null;
	}

	//}}}
	//{{{ protected String getSpecialCharWord(char c)

	protected String getSpecialCharWord(char c) {
		if (c == last_special_char) {
			return last_special_char_word;
		}

		last_special_char = c;
		last_special_char_word = (String) specialChars.get(new Character(c));

		return last_special_char_word;
	}

	//}}}

	//{{{ protected boolean isReservedType(ATerm t)

	protected boolean isReservedType(String s) {
		return reservedTypes.containsKey(s);
	}

	//}}}

	//{{{ protected String escapeQuotes(String s)

	protected String escapeQuotes(String s) {
		StringBuffer buf = new StringBuffer(s.length() * 2);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '"' || c == '\\') {
				buf.append('\\');
			}
			buf.append(c);
		}
		return buf.toString();
	}

	//}}}

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
