package apigen.adt;

import aterm.*;

public class AlternativeConstructor extends Alternative {
	public AlternativeConstructor(String id, ATerm pattern) {
		this.id = id;
		this.pattern = pattern;
	}

	public boolean isConstructor() {
		return true;
	}
}
