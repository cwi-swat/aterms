package aterm.pure;

import aterm.*;
import java.util.List;

class ATermBlobImpl
  extends ATermImpl
  implements ATermBlob
{
  byte[] data;

  //{{{ static int hashFunction(byte[] data, ATermList annos)

  static int hashFunction(byte[] data, ATermList annos)
  {
    return Math.abs(data.hashCode() + annos.hashCode());
  }

  //}}}

  //{{{ public int hashCode()

  public int hashCode()
  {
    return hashFunction(data, annotations);
  }

  //}}}
  //{{{ public int getType()

  public int getType()
  {
    return ATerm.BLOB;
  }

  //}}}

  //{{{ protected ATermBlobImpl(PureFactory factory, byte[] data, ATermList annos)

  protected ATermBlobImpl(PureFactory factory, byte[] data, ATermList annos)
  {
    super(factory, annos);
    this.data = data;
  }

  //}}}

  //{{{ public boolean match(ATerm pattern, List list)

  protected boolean match(ATerm pattern, List list)
  {
    if (this.equals(pattern)) {
      return true; 
    }

    if (pattern.getType() == ATerm.PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder)pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
	ATermAppl appl = (ATermAppl)type;
	AFun  afun = appl.getAFun();
	if(afun.getName().equals("blob") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(data);
	  return true;
	}
      }
    }

    return super.match(pattern, list);
  }

  //}}}

  //{{{ public byte[] getBlobData()

  public byte[] getBlobData()
  {
    return data;
  }

  //}}}
  //{{{ public int getBlobSize()

  public int getBlobSize()
  {
    return data.length;
  }

  //}}}
  //{{{ public ATerm setAnnotations(ATermList annos)

  public ATerm setAnnotations(ATermList annos)
  {
    return factory.makeBlob(data, annos);
  }

  //}}}

  //{{{ public void accept(ATermVisitor v)

  public void accept(ATermVisitor v)
    throws ATermVisitFailure
  {
    v.visitBlob(this);
  }

  //}}}
}
