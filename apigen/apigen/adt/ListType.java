package apigen.adt;

import aterm.ATermFactory;

public class ListType extends Type {
	private String elementType;
	private ATermFactory factory;

	public ListType(ATermFactory factory, String id, String elementType) {
		super(id);
		this.factory = factory;
		this.elementType = elementType;
        
    addAlternative(makeEmptyListConstructor());
    addAlternative(makeManyListConstructor());
	}

	private Alternative makeManyListConstructor() {
		Alternative many =
			new Alternative(
				"many",
				factory.parse("[<head(" + getElementType() + ")>,<[tail(" + getId() + ")]>]"));

		return many;
	}

	private Alternative makeEmptyListConstructor() {
		Alternative empty = new Alternative("empty", factory.parse("[]"));
		return empty;
	}

	public String getElementType() {
		return elementType;
	}
}
