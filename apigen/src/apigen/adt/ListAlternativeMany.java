package apigen.adt;

import aterm.ATermFactory;

public class ListAlternativeMany extends ListAlternative {
	public ListAlternativeMany(String type, String elementType, ATermFactory factory)  {
		super(type, elementType);
		this.id = "many";
		this.pattern = factory.parse("[<head(" + elementType + ")>," +                                  "<[tail(" + type + ")]>]");
	}
}
