package apigen.gen.tom.c;

import java.util.Iterator;
import java.util.List;

import apigen.gen.c.CGenerationParameters;


class CTomGenerationParameters extends CGenerationParameters {
	private boolean jtype;
	private boolean CGenStuff;
	
	public void parseArguments(List args) {
		Iterator iter = args.iterator();
		while (iter.hasNext()) {
			String arg = (String) iter.next();
			if (arg.startsWith("--jtype")) {
				shift(iter);
				setJtype(true);
			} else if (arg.startsWith("--cgen")){
				shift(iter);
				setCGen(true);
			}
		}
		super.parseArguments(args);
	}

	public String usage() {
		StringBuffer buf = new StringBuffer(super.usage());
		buf.append("\t--jtype\t\t\t\t<insert sensible explanation about --jtype here>");
		buf.append("\n\t--cgen\t\t\tcall to adt-to-c");
		return buf.toString();
	}
	
	public boolean isCGen() {
		return CGenStuff;
	}
	
  private void setCGen(boolean cGen) {
    this.CGenStuff = cGen;
  }

  
	public boolean isJtype() {
		return jtype;
	}

	public void setJtype(boolean jtype) {
		this.jtype = jtype;
	}
	
} 
