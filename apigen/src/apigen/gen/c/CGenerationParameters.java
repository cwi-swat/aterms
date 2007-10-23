package apigen.gen.c;

import java.util.Iterator;
import java.util.List;

import apigen.gen.GenerationParameters;

public class CGenerationParameters extends GenerationParameters {
	private String prologue;
	private boolean termCompatibility;
	private boolean folding;

	public void parseArguments(List<String> args) {
		Iterator<String> iter = args.iterator();
		while (iter.hasNext()) {
			String arg = iter.next();
			if (arg.startsWith("--prefix")) {
				shift(iter);
				setPrefix(shiftArgument(iter));
			} else if (arg.startsWith("--folding") || arg.startsWith("-f")) {
				shift(iter);
				setFolding(true);
			} else if (arg.startsWith("--prologue")) {
				shift(iter);
				setPrologue(shiftArgument(iter));
			} else if (arg.equals("--term-compatibility")) {
				shift(iter);
				setTermCompatibility(true);
			}
		}
		super.parseArguments(args);
	}

	public String usage() {
		StringBuffer buf = new StringBuffer(super.usage());
		buf.append("\t--prefix <prefix>         [\"\"]\n");
		buf.append("\t--folding                 [off]\n");
		buf.append("\t--prologue <file>         include prologue <file>\n");
		buf
				.append("\t--term-compatibility      use backwards compatible toTerm names\n");
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
