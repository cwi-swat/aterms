package apigen.adt;

import aterm.ATermFactory;

public class ListAlternativeEmpty extends ListAlternative {
    public ListAlternativeEmpty(String type, String elementType, ATermFactory factory) {
        super(type, elementType);
        this.id = "empty";
        this.pattern = factory.parse("[]");
    }


}
