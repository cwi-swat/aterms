package apigen.gen;

import java.util.HashMap;
import java.util.Map;

public class StringConversions {
	private Map specialChars;

	// Do *NOT* add an entry for '-' (Dash) here, the dash is used
	// as a word-separator!

	protected final String[] SPECIAL_CHAR_WORDS =
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

	public StringConversions() {
		specialChars = new HashMap();

		for (int i = 0; i < SPECIAL_CHAR_WORDS.length; i++) {
			String word = SPECIAL_CHAR_WORDS[i];
			specialChars.put(new Character(word.charAt(0)), word.substring(1));
		}
	}

	public String makeIdentifier(String id) {
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

    public String makeCapitalizedIdentifier(String id) {
    	return capitalize(makeIdentifier(id));
    }
    
	public String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private boolean isSpecialChar(char c) {
		return getSpecialCharWord(c) != null;
	}

	private String getSpecialCharWord(char c) {
		return (String) specialChars.get(new Character(c));
	}

	public String escapeQuotes(String s) {
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
