package apigen.adt;

import aterm.ATermFactory;

public class ListType extends Type {
	private String elementType;
	private ATermFactory factory;

	public ListType(ATermFactory factory, String id, String elementType) {
		super(id);
		this.factory = factory;
		this.elementType = elementType;
        
    	addAlternative(Alternative.makeManyListConstructor(this.factory,getId(),getElementType()));
    	addAlternative(Alternative.makeEmptyListConstructor(this.factory));
	}



	public String getElementType() {
		return elementType;
	}
}
