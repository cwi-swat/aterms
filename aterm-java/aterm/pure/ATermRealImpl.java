package aterm.pure;

import java.util.List;

import shared.SharedObject;

import aterm.*;

class ATermRealImpl extends ATermImpl implements ATermReal {
  double value;

  public int getType() {
    return ATerm.REAL;
  }

  protected ATermRealImpl(PureFactory factory) {
    super(factory);
  }
  
  protected void init(int hashCode, ATermList annos, double value) {
    super.init(hashCode, annos);
    this.value = value;
  }
  
  public Object clone() {
    ATermRealImpl clone = new ATermRealImpl(getPureFactory());
    clone.init(hashCode(), getAnnotations(), value);
    return clone;
  }
  
  public boolean equivalent(SharedObject obj) {
    if (super.equivalent(obj)) {
      ATermReal peer = (ATermReal) obj;
      return peer.getReal() == value;
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
        if (afun.getName().equals("real") && afun.getArity() == 0 && !afun.isQuoted()) {
          list.add(new Double(value));
          return true;
        }
      }
    }

    return super.match(pattern, list);
  }

  public double getReal() {
    return value;
  }

  public ATerm setAnnotations(ATermList annos) {
    return getPureFactory().makeReal(value, annos);
  }

  public void accept(ATermVisitor v) throws ATermVisitFailure {
    v.visitReal(this);
  }

}
