package apigen.gen.tom;

import java.util.Iterator;
import java.util.List;

import apigen.gen.GenerationParameters;

public class TomGenerationParameters extends GenerationParameters {
	private boolean jtype;
	
	public void parseArguments(List args) {
		Iterator iter = args.iterator();
		while (iter.hasNext()) {
			String arg = (String) iter.next();
			if ("--jtype".startsWith(arg)) {
				shift(iter);
				setJtype(true);
			}
		}
		super.parseArguments(args);
	}
	
	public String usage() {
		return "\t--jtype <insert sensible explanation about --jtype here>";
	}
	
	public boolean isJtype() {
		return jtype;
	}

	public void setJtype(boolean jtype) {
		this.jtype = jtype;
	}
}
