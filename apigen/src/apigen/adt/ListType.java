package apigen.adt;

import aterm.ATermFactory;

public class ListType extends Type {
    //TODO : make private?
    protected static final String SINGLE_LIST_ALT_NAME = "single";
    protected static final String MANY_LIST_ALT_NAME = "many";
    protected static final String EMPTY_LIST_ALT_NAME = "empty";
    protected static final String HEAD_FIELD_NAME = "head";
    protected static final String TAIL_FIELD_NAME = "tail";

    private ATermFactory factory;
    private String elementType;

    public ListType(String id, String elementType, ATermFactory factory) {
        super(id);
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
        return new Alternative(EMPTY_LIST_ALT_NAME, getFactory().parse("[]"));
    }

    protected Alternative makeSingletonListConstructor() {
        String pattern = "[" + buildHeadPattern() + "]";
        return new Alternative(SINGLE_LIST_ALT_NAME, getFactory().parse(pattern));
    }

    protected Alternative makeManyListConstructor() {
        String head = buildHeadPattern();
        String tail = buildTailPattern();
        String pattern = "[" + head + "," + tail + "]";

        return new Alternative(MANY_LIST_ALT_NAME, getFactory().parse(pattern));
    }

    protected String buildHeadPattern() {
        String head = "<" + HEAD_FIELD_NAME + "(" + elementType + ")>";
        return head;
    }

    protected String buildTailPattern() {
        String tail = "<[" + TAIL_FIELD_NAME + "(" + getId() + ")]>";
        return tail;
    }

    public ATermFactory getFactory() {
        return factory;
    }
}
