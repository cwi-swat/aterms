package apigen.adt;

import apigen.adt.api.Separators;
import aterm.ATermFactory;


public class SeparatedListType extends ListType {
    protected Separators separators;
    
	public SeparatedListType(ATermFactory factory, String id, String elementType, Separators separators) {
		super(id, factory, elementType);
		this.separators = separators;
		
		addAlternative(Alternative.makeManySeparatedListConstructor(this.factory,getId(),elementType,separators));
		addAlternative(Alternative.makeEmptyListConstructor(this.factory));
		addAlternative(Alternative.makeSingletonListConstructor(this.factory,elementType));
	}

	public Separators getSeparators() {
		return separators;
	}
}
