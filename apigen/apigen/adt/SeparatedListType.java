package apigen.adt;

import apigen.adt.api.Separators;
import aterm.ATermFactory;


public class SeparatedListType extends Type {
    protected Separators separators;
    protected String elementType;
    protected ATermFactory factory;
    
	public SeparatedListType(ATermFactory factory, String id, String elementType, Separators separators) {
		super(id);
		this.separators = separators;
		this.elementType = elementType;
		this.factory = factory;
		
		addAlternative(Alternative.makeManySeparatedListConstructor(this.factory,getId(),elementType,separators));
		addAlternative(Alternative.makeEmptyListConstructor(this.factory));
		addAlternative(Alternative.makeSingletonListConstructor(this.factory,elementType));
	}

	public Separators getSeparators() {
		return separators;
	}

	public String getElementType() {
		return elementType;
	}

}
