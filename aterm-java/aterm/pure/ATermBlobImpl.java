package aterm.pure;

import java.util.List;

import shared.SharedObject;

import aterm.*;

class ATermBlobImpl extends ATermImpl implements ATermBlob {
  byte[] data;

  public int getType() {
    return ATerm.BLOB;
  }

  protected void init(int hashCode, ATermList annos, byte[] data) {
    super.init(hashCode, annos);
    this.data = data;
  }
  
  public Object clone() {
    ATermBlobImpl clone = new ATermBlobImpl();
    clone.init(hashCode(), getAnnotations(), data);
    return clone;
  }
  
  public boolean equivalent(SharedObject obj) {
    if (super.equivalent(obj)) {
      ATermBlob peer = (ATermBlob) obj;
      return peer.getBlobData() == data;
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
        if (afun.getName().equals("blob") && afun.getArity() == 0 && !afun.isQuoted()) {
          list.add(data);
          return true;
        }
      }
    }

    return super.match(pattern, list);
  }

  public byte[] getBlobData() {
    return data;
  }

  public int getBlobSize() {
    return data.length;
  }

  public ATerm setAnnotations(ATermList annos) {
    return getPureFactory().makeBlob(data, annos);
  }

  public void accept(ATermVisitor v) throws ATermVisitFailure {
    v.visitBlob(this);
  }

}
