package apigen.adt;

import apigen.adt.api.Factory;


public class ListType extends Type {
    //TODO : make private?
    protected static final String SINGLE_LIST_ALT_NAME = "single";
    protected static final String MANY_LIST_ALT_NAME = "many";
    protected static final String EMPTY_LIST_ALT_NAME = "empty";
    protected static final String HEAD_FIELD_NAME = "head";
    protected static final String TAIL_FIELD_NAME = "tail";

    private Factory factory;
    private String elementType;

    public ListType(String id, String moduleName, String elementType, Factory factory) {
        super(id, moduleName);
        this.factory = factory;
        this.elementType = elementType;
    }

    public void addAlternative(Alternative alt) {
        throw new UnsupportedOperationException("cannot add an alternative to a list type, use addAlternatives.");
    }

    public void addAlternatives() {
        super.addAlternative(makeEmptyListConstructor());
        super.addAlternative(makeSingletonListConstructor());
        super.addAlternative(makeManyListConstructor());
    }

    public String getHeadFieldId() {
    	return HEAD_FIELD_NAME;
    }
    
    public String getTailFieldId() {
    	return TAIL_FIELD_NAME;
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

    protected Alternative makeEmptyListConstructor() {
        return new Alternative(EMPTY_LIST_ALT_NAME, getFactory().getPureFactory().parse("[]"));
    }

    protected Alternative makeSingletonListConstructor() {
        String pattern = "[" + buildHeadPattern() + "]";
        return new Alternative(SINGLE_LIST_ALT_NAME, getFactory().getPureFactory().parse(pattern));
    }

    protected Alternative makeManyListConstructor() {
        String head = buildHeadPattern();
        String tail = buildTailPattern();
        String pattern = "[" + head + "," + tail + "]";

        return new Alternative(MANY_LIST_ALT_NAME, getFactory().getPureFactory().parse(pattern));
    }

    protected String buildHeadPattern() {
        String head = "<" + HEAD_FIELD_NAME + "(" + elementType + ")>";
        return head;
    }

    protected String buildTailPattern() {
        String tail = "<[" + TAIL_FIELD_NAME + "(" + getId() + ")]>";
        return tail;
    }

    public Factory getFactory() {
        return factory;
    }
}
