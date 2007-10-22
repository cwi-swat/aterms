package apigen.adt.api.types;

public class Entries extends aterm.pure.ATermListImpl {
	private apigen.adt.api.Factory localFactory = null;

	public Entries(apigen.adt.api.Factory localFactory) {
	    super(localFactory.getPureFactory());
	    
		this.localFactory = localFactory;
	    internSetAnnotations(factory.getEmpty());
	    setHashCode(84);
	}
	
	public Entries(apigen.adt.api.Factory localFactory, aterm.ATermList annos,aterm.ATerm first, aterm.ATermList next) {
		super(localFactory.getPureFactory(), annos, first, next);
		this.localFactory = localFactory;
	}

	public apigen.adt.api.Factory getApiFactory() {
		return localFactory;
	}

	protected aterm.ATerm term = null;

	public aterm.ATerm toTerm() {
		aterm.ATermFactory atermFactory = getApiFactory().getPureFactory();
		if (this.term == null) {
			Entries reversed = (Entries) this.reverse();
			aterm.ATermList tmp = atermFactory.makeList();
			for (; !reversed.isEmpty(); reversed = reversed.getTail()) {
				aterm.ATerm elem = reversed.getHead().toTerm();
				tmp = atermFactory.makeList(elem, tmp);
			}
			this.term = tmp;
		}
		return this.term;
	}

	public String toString() {
		return toTerm().toString();
	}

	public apigen.adt.api.types.Entry getHead() {
		return (apigen.adt.api.types.Entry) getFirst();
	}

	public Entries getTail() {
		return (Entries) getNext();
	}

	public boolean isSortEntries() {
		return true;
	}

	public boolean isEmpty() {
		return getFirst() == getEmpty().getFirst()
				&& getNext() == getEmpty().getNext();
	}

	public boolean isMany() {
		return !isEmpty();
	}

	public boolean isSingle() {
		return !isEmpty() && getNext().isEmpty();
	}

	public boolean hasHead() {
		return !isEmpty();
	}

	public boolean hasTail() {
		return !isEmpty();
	}

	public boolean equivalent(shared.SharedObject peer) {
		if (peer instanceof Entries) {
			return super.equivalent(peer);
		}
		return false;
	}

	public shared.SharedObject duplicate() {
		return this;
	}

	public aterm.ATermList getEmpty() {
		return getApiFactory().makeEntries();
	}

	public Entries insert(apigen.adt.api.types.Entry head) {
		return getApiFactory().makeEntries(head, this);
	}

	public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail,
			aterm.ATermList annos) {
		return getApiFactory().makeEntries(head, tail, annos);
	}

	public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail) {
		return make(head, tail, getApiFactory().getPureFactory().getEmpty());
	}

	public aterm.ATermList insert(aterm.ATerm head) {
		return make(head, this);
	}

	public Entries reverseEntries() {
		return getApiFactory().reverse(this);
	}

	public aterm.ATermList reverse() {
		return reverseEntries();
	}

	public Entries concat(Entries peer) {
		return getApiFactory().concat(this, peer);
	}

	public aterm.ATermList concat(aterm.ATermList peer) {
		return concat((Entries) peer);
	}

	public Entries append(apigen.adt.api.types.Entry elem) {
		return getApiFactory().append(this, elem);
	}

	public aterm.ATermList append(aterm.ATerm elem) {
		return append((apigen.adt.api.types.Entry) elem);
	}

	public apigen.adt.api.types.Entry getEntryAt(int index) {
		return (apigen.adt.api.types.Entry) elementAt(index);
	}

}
