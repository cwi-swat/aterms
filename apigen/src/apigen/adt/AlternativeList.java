package apigen.adt;

import java.util.*;

import aterm.*;

public class AlternativeList extends LinkedList<Alternative> {
	public void keepByType(int type) {
		Iterator<Alternative> iter = iterator();
		while (iter.hasNext()) {
			Alternative alt = iter.next();
			if (alt.getPattern().getType() != type) {
				iter.remove();
			}
		}
	}

	public void keepByAFun(AFun afun) {
		Iterator<Alternative> iter = iterator();
		while (iter.hasNext()) {
			Alternative alt = iter.next();
			ATerm pattern = alt.getPattern();
			if (pattern.getType() == ATerm.APPL) {
				AFun pat_fun = ((ATermAppl) pattern).getAFun();
				if (!pat_fun.equals(afun)) {
					iter.remove();
				}
			}
			else {
				iter.remove();
			}
		}
	}

	public void removeEmptyList() {
		Iterator<Alternative> iter = iterator();
		while (iter.hasNext()) {
			Alternative alt = iter.next();
			ATerm pattern = alt.getPattern();
			if (pattern.getType() == ATerm.LIST && ((ATermList) pattern).isEmpty()) {
				iter.remove();
			}
		}
	}
    
	public Object clone() {
		AlternativeList copy = new AlternativeList();
		copy.addAll(this);
		return copy;
	}
}
