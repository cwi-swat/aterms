package apigen.adt;

import aterm.ATermFactory;

public class ListType extends Type {
	protected static final String SINGLE_LIST_ALT_NAME = "single";
	protected static final String MANY_LIST_ALT_NAME = "many";
	protected static final String EMPTY_LIST_ALT_NAME = "empty";

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
	
	public Alternative getEmptyAlternative() {
		return getAlternative(EMPTY_LIST_ALT_NAME);
	}
	
	public Alternative getSingleAlternative() {
		return getAlternative(SINGLE_LIST_ALT_NAME);
	}

	public Alternative getManyAlternative() {
		return getAlternative(MANY_LIST_ALT_NAME);
	}
}
