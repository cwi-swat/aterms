package apigen.gen.c;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import aterm.AFun;

public class AFunRegister {
	private int nextAFun;
	private Map afuns_by_name;
	private Map afuns_by_afun;

	public AFunRegister() {
		nextAFun = 0;
		afuns_by_name = new HashMap();
		afuns_by_afun = new HashMap();
	}

    /**
     * Compute the string representation of an AFun, and as a *side-effect*
     * store this AFun with its strings representation in a local hash table
     * @param afun
     * @return String
     */
	public String lookup(AFun afun) {
		String name = (String) afuns_by_afun.get(afun);

		if (name == null) {
			name = "afun" + nextAFun++;
			afuns_by_name.put(name, afun);
			afuns_by_afun.put(afun, name);
		}

		return name;
	}
    
    /**
     * Get an iterator over all AFuns that have been processed by lookup
     * @return Iterator
     */
    public Iterator aFunIterator() {
        return afuns_by_name.values().iterator();
    }
}
