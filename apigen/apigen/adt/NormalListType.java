package apigen.adt;

import aterm.ATermFactory;

public class NormalListType extends ListType {
	public NormalListType(ATermFactory factory, String id, String elementType) {
		super(id, factory, elementType);

		addAlternative(Alternative.makeManyListConstructor(this.factory, getId(), getElementType()));
		addAlternative(Alternative.makeEmptyListConstructor(this.factory));
	}
}
