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

	public String lookup(AFun afun) {
		String name = (String) afuns_by_afun.get(afun);

		if (name == null) {
			name = "afun" + nextAFun++;
			afuns_by_name.put(name, afun);
			afuns_by_afun.put(afun, name);
		}

		return name;
	}
    
    public Iterator aFunIterator() {
        return afuns_by_name.values().iterator();
    }
}
