package apigen.adt;

import java.util.Iterator;

import apigen.adt.api.Separators;
import apigen.util.FirstAndLastSkippingIterator;
import aterm.ATermFactory;

public class SeparatedListType extends ListType {
    private Separators separators;

    public SeparatedListType(
        ATermFactory factory,
        String id,
        String elementType,
        Separators separators) {
        super(id, factory, elementType);
        this.separators = separators;

        addAlternative(
            Alternative.makeManySeparatedListConstructor(
                this.factory,
                getId(),
                elementType,
                separators));
        addAlternative(Alternative.makeEmptyListConstructor(this.factory));
        addAlternative(
            Alternative.makeSingletonListConstructor(this.factory, elementType));
    }

    protected void setSeparators(Separators separators) {
        this.separators = separators;
    }
    
    public Separators getSeparators() {
        return separators;
    }

    public Iterator separatorFieldIterator() {
        return new FirstAndLastSkippingIterator(altFieldIterator("many"));
    }
    
    public Alternative getManyAlternative() {
        return getAlternative("many");
    }
    
    public Field getManyField(String fieldId) {
        return getAltField("many", fieldId);
    }
    
    public Iterator manyFieldIterator() {
        return altFieldIterator("many");
    }
    
}

