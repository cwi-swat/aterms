package apigen.util;

import java.util.Iterator;

public class FirstAndLastSkippingIterator implements Iterator {
    private Iterator delegate;
    private Object next;
    private boolean haveNext;

    public FirstAndLastSkippingIterator(Iterator iter) {
        delegate = iter;
        if (delegate.hasNext()) {
            delegate.next();
        }
        if (delegate.hasNext()) {
            advance();
        }
    }

    private void advance() {
        next = delegate.next();
        haveNext = delegate.hasNext();
    }

    public boolean hasNext() {
        return haveNext;
    }

    public Object next() {
        Object result = null;
        if (haveNext) {
            result = next;
            advance();
        }
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
