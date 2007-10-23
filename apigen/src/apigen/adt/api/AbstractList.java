package apigen.adt.api;

abstract public class AbstractList extends aterm.pure.ATermListImpl {
  private apigen.adt.api.Factory abstractTypeFactory;

  public AbstractList(apigen.adt.api.Factory abstractTypeFactory, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super(abstractTypeFactory.getPureFactory(), annos, first, next);
    this.abstractTypeFactory = abstractTypeFactory;
  }

  abstract public aterm.ATerm toTerm();

  public String toString() {
    return toTerm().toString();
  }

  public apigen.adt.api.Factory getApiFactory() {
    return abstractTypeFactory;
  }

  public boolean isSortEntries() {
    return false;
  }

  public boolean isSortEntry() {
    return false;
  }

  public boolean isSortSeparators() {
    return false;
  }

  public boolean isSortSeparator() {
    return false;
  }

  public boolean isSortModules() {
    return false;
  }

  public boolean isSortModule() {
    return false;
  }

  public boolean isSortImports() {
    return false;
  }

  public boolean isSortType() {
    return false;
  }

  public boolean isSortSorts() {
    return false;
  }

  public boolean isSortModuleName() {
    return false;
  }

  public boolean isEmpty() {
    return getFirst()==getEmpty().getFirst() && getNext()==getEmpty().getNext();
  }

  public boolean isMany() {
    return !isEmpty();
  }

  public boolean isSingle() {
    return !isEmpty() && getNext().isEmpty();
  }

  public boolean hasHead() {
    return !isEmpty();
  }

  public boolean hasTail() {
    return !isEmpty();
  }

}
