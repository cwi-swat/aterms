package aterm.pure;

import aterm.*;
import java.util.List;

class ATermRealImpl
  extends ATermImpl
  implements ATermReal
{
  double value;

  //{{{ static int hashFunction(double value, ATermList annos)

  static int hashFunction(double value, ATermList annos)
  {
    return Math.abs((new Double(value)).hashCode() + annos.hashCode());
  }

  //}}}

  //{{{ public int hashCode()

  public int hashCode()
  {
    return hashFunction(value, annotations);
  }

  //}}}
  //{{{ public int getType()

  public int getType()
  {
    return ATerm.REAL;
  }

  //}}}

  //{{{ protected ATermRealImpl(PureFactory factory, double value, ATermList annos)

  protected ATermRealImpl(PureFactory factory, double value, ATermList annos)
  {
    super(factory, annos);
    this.value = value;
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
	if(afun.getName().equals("real") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(new Double(value));
	  return true;
	}
      }
    }

    return super.match(pattern, list);
  }

  //}}}
  //{{{ public double getReal()

  public double getReal()
  {
    return value;
  }

  //}}}
  //{{{ public ATerm setAnnotations(ATermList annos)

  public ATerm setAnnotations(ATermList annos)
  {
    return factory.makeReal(value, annos);
  }

  //}}}

  //{{{ public void accept(ATermVisitor v)

  public void accept(ATermVisitor v)
    throws ATermVisitFailure
  {
    v.visitReal(this);
  }

  //}}}
}
