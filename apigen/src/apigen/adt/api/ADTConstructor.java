package apigen.adt.api;

abstract public class ADTConstructor
extends aterm.pure.ATermApplImpl
implements aterm.ATerm
{
  protected aterm.ATerm term = null;

  ADTFactory factory = null;

  public ADTConstructor(ADTFactory factory) {
    this.factory = factory;
  }

  abstract public aterm.ATerm toTerm();
  public String toString() {
    return toTerm().toString();
  }
  protected void setTerm(aterm.ATerm term) {
   this.term = term;
  }
  public ADTFactory getADTFactory() {
    return factory;
  }
  public boolean isSortEntries() {
    return false;
  }

  public boolean isSortEntry() {
    return false;
  }

}
