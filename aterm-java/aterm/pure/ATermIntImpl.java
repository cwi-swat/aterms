package aterm.pure;

import java.util.List;

import shared.SharedObject;

import aterm.*;

class ATermIntImpl extends ATermImpl implements ATermInt {
  int value;

  public int getType() {
    return ATerm.INT;
  }

  protected ATermIntImpl(PureFactory factory) {
    super(factory);
  }
  
  protected void init(int hashCode, ATermList annos, int value) {
    super.init(hashCode, annos);
    this.value = value;
  }
  
  public Object clone() {
    ATermIntImpl clone = new ATermIntImpl(getPureFactory());
    clone.init(hashCode(), getAnnotations(), value);
    return clone;
  }
  
  public boolean equivalent(SharedObject obj) {
    if (super.equivalent(obj)) {
      ATermInt peer = (ATermInt) obj;
      return peer.getInt() == value;
    }
    
    return false;
  }

  protected boolean match(ATerm pattern, List list) {
    if (this.equals(pattern)) {
      return true;
    }

    if (pattern.getType() == ATerm.PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
        ATermAppl appl = (ATermAppl) type;
        AFun afun = appl.getAFun();
        if (afun.getName().equals("int") && afun.getArity() == 0 && !afun.isQuoted()) {
          list.add(new Integer(value));
          return true;
        }
      }
    }

    return super.match(pattern, list);
  }

  public int getInt() {
    return value;
  }

  public ATerm setAnnotations(ATermList annos) {
    return getPureFactory().makeInt(value, annos);
  }

  public void accept(ATermVisitor v) throws ATermVisitFailure {
    v.visitInt(this);
  }

}
