package apigen.adt;

import aterm.ATermFactory;

public class ListType extends Type {
	protected ATermFactory factory;
	protected String elementType;

	public ListType(String id, ATermFactory factory, String elementType) {
		super(id);
		this.factory = factory;
		this.elementType = elementType;
	}

	public String getElementType() {
		return elementType;
	}

}
