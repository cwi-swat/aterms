package aterm.pure;

import java.util.List;

import shared.SharedObject;

import aterm.*;

class ATermIntImpl extends ATermImpl implements ATermInt {
  int value;

  public int getType() {
    return ATerm.INT;
  }

  protected void init(int hashCode, ATermList annos, int value) {
    super.init(hashCode, annos);
    this.value = value;
  }

  protected void initHashCode(ATermList annos, int value) {
    this.value = value;
    this.internSetAnnotations(annos);
    this.setHashCode(this.hashFunction());
      //super.init(hashCode, annos);
  }

  
  public Object clone() {
    ATermIntImpl clone = new ATermIntImpl();
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

  private int hashFunction() {
    /* Set up the internal state */
    int a = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    int b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    int c = 2;          /* the previous hash value */

    /*------------------------------------- handle the last 11 bytes */
    a += (getAnnotations().hashCode()<<8);
    a += (value);
    
    a -= b; a -= c; a ^= (c >> 13);
    b -= c; b -= a; b ^= (a << 8);
    c -= a; c -= b; c ^= (b >> 13);
    a -= b; a -= c; a ^= (c >> 12);
    b -= c; b -= a; b ^= (a << 16);
    c -= a; c -= b; c ^= (b >> 5);
    a -= b; a -= c; a ^= (c >> 3);
    b -= c; b -= a; b ^= (a << 10);
    c -= a; c -= b; c ^= (b >> 15);

    /*-------------------------------------------- report the result */
    return c;
  }

}
