package apigen.gen.c;

import java.util.Iterator;
import java.util.List;

import apigen.gen.GenerationParameters;

public class CGenerationParameters extends GenerationParameters {
	private String prologue;
	private boolean termCompatibility;
	private boolean folding;

	public void parseArguments(List args) {
		Iterator iter = args.iterator();
		while (iter.hasNext()) {
			String arg = (String) iter.next();
			if ("-prefix".startsWith(arg)) {
				shift(iter);
				setPrefix(shiftArgument(iter));
			}
			else if ("--folding".startsWith(arg) || "-f".startsWith(arg)) {
				shift(iter);
				setFolding(true);
			}
			else if ("-prologue".startsWith(arg)) {
				shift(iter);
				setPrologue(shiftArgument(iter));
			}
			else if ("-compatible:term".equals(arg)) {
				shift(iter);
				setTermCompatibility(true);
			}
		}
		super.parseArguments(args);
	}
	
	public String usage() {
		StringBuffer buf = new StringBuffer();
		buf.append("\t-prefix <prefix>          [\"\"]\n");
		buf.append("\t-f | --folding                 [off]\n");
		buf.append("\t-prologue <file>          include prologue <file>\n");
		buf.append("\t-compatible:term          use backwards compatible toTerm names\n");
		return buf.toString();
	}

	public String getPrologue() {
		return prologue;
	}

	public void setPrologue(String prologue) {
		this.prologue = prologue;
	}

	public boolean isTermCompatibility() {
		return termCompatibility;
	}

	public void setTermCompatibility(boolean termCompatibility) {
		this.termCompatibility = termCompatibility;
	}

	public boolean isFolding() {
		return folding;
	}

	public void setFolding(boolean folding) {
		this.folding = folding;
	}

}
