package apigen.gen;

import java.util.HashMap;
import java.util.Map;

public class StringConversions {
	static private Map specialChars;

    /**
     * A translation table from non-alphanumerics to acronyms.
     */
    
    /* (non-javadoc) 
     * Do *NOT* add an entry for '-' (Dash) here, the dash is used
	 * as a word-separator!
	 */
	protected static final String[] SPECIAL_CHAR_WORDS =
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

    /** 
     * Load the translation table into a hash table
     */
	static {
		specialChars = new HashMap();

		for (int i = 0; i < SPECIAL_CHAR_WORDS.length; i++) {
			String word = SPECIAL_CHAR_WORDS[i];
			specialChars.put(new Character(word.charAt(0)), word.substring(1));
		}
	}

    /**
     * Creates a C/Java identifier by replacing non-alphanumeric characters
     * by some acronym. Dashes are interpreted as word separators, which is
     * implemented using CamelCase style.
     *
     */
	static public String makeIdentifier(String id) {
		StringBuffer buf = new StringBuffer();
		boolean cap_next = false;

		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (isSpecialChar(c)) {
				buf.append(getSpecialCharWord(c));
				cap_next = true;
			} else {
				switch (c) {
					case '-' :
						cap_next = true;
						break;
					default :
						if (cap_next) {
							buf.append(Character.toUpperCase(c));
							cap_next = false;
						} else {
							buf.append(c);
						}
						break;
				}
			}
		}

		return buf.toString();
	}

  /**
   * Makes a capitalized C/Java identifier, see also makeIdenfifier
   * 
   */
  static public String makeCapitalizedIdentifier(String id) {
  	return capitalize(makeIdentifier(id));
  }
    
    /**
     * Capitalize the first letter of a string
     *
     */
	static public String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	static private boolean isSpecialChar(char c) {
		return getSpecialCharWord(c) != null;
	}

	static private String getSpecialCharWord(char c) {
		return (String) specialChars.get(new Character(c));
	}

    /**
     * Escape all double quotes and backslashes in a string using a leading backslash
     */
	static public String escapeQuotes(String s) {
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
}
