package apigen.adt.api.types;

public class Separators extends aterm.pure.ATermListImpl {
	private apigen.adt.api.Factory localFactory = null;

	public Separators(apigen.adt.api.Factory localFactory) {
		super(localFactory.getPureFactory());

		this.localFactory = localFactory;
		internSetAnnotations(factory.getEmpty());
		setHashCode(126);
	}

	public Separators(apigen.adt.api.Factory localFactory,
			aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
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
			Separators reversed = (Separators) this.reverse();
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

	public apigen.adt.api.types.Separator getHead() {
		return (apigen.adt.api.types.Separator) getFirst();
	}

	public Separators getTail() {
		return (Separators) getNext();
	}

	public boolean isSortSeparators() {
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
		if (peer instanceof Separators) {
			return super.equivalent(peer);
		}
		return false;
	}

	public shared.SharedObject duplicate() {
		return this;
	}

	public aterm.ATermList getEmpty() {
		return getApiFactory().makeSeparators();
	}

	public Separators insert(apigen.adt.api.types.Separator head) {
		return getApiFactory().makeSeparators(head, (Separators) this);
	}

	public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail,
			aterm.ATermList annos) {
		return getApiFactory().makeSeparators(head, tail, annos);
	}

	public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail) {
		return make(head, tail, getApiFactory().getPureFactory().getEmpty());
	}

	public aterm.ATermList insert(aterm.ATerm head) {
		return make(head, this);
	}

	public Separators reverseSeparators() {
		return getApiFactory().reverse(this);
	}

	public aterm.ATermList reverse() {
		return reverseSeparators();
	}

	public Separators concat(Separators peer) {
		return getApiFactory().concat(this, peer);
	}

	public aterm.ATermList concat(aterm.ATermList peer) {
		return concat((Separators) peer);
	}

	public Separators append(apigen.adt.api.types.Separator elem) {
		return getApiFactory().append(this, elem);
	}

	public aterm.ATermList append(aterm.ATerm elem) {
		return append((apigen.adt.api.types.Separator) elem);
	}

	public apigen.adt.api.types.Separator getSeparatorAt(int index) {
		return (apigen.adt.api.types.Separator) elementAt(index);
	}

}
