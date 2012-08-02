package apigen.util;

import java.util.Iterator;

public class FirstAndLastSkippingIterator<E> implements Iterator<E> {
    private Iterator<E> delegate;
    private E next;
    private boolean haveNext;

    public FirstAndLastSkippingIterator(Iterator<E> iter) {
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

    public E next() {
        E result = null;
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
