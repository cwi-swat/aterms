package aterm.pure;

import aterm.*;
import java.util.List;

class ATermIntImpl
  extends ATermImpl
  implements ATermInt
{
  int value;

  //{{{ static int hashFunction(int value, ATermList annos)

  static int hashFunction(int value, ATermList annos)
  {
    return Math.abs(value + annos.hashCode());
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
    return ATerm.INT;
  }

  //}}}

  //{{{ protected ATermIntImpl(PureFactory factory, int value, ATermList annos)

  protected ATermIntImpl(PureFactory factory, int value, ATermList annos)
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
	if(afun.getName().equals("int") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(new Integer(value));
	  return true;
	}
      }
    }

    return super.match(pattern, list);
  }

  //}}}
  //{{{ public int getInt()

  public int getInt()
  {
    return value;
  }

  //}}}
  //{{{ public ATerm setAnnotations(ATermList annos)

  public ATerm setAnnotations(ATermList annos)
  {
    return factory.makeInt(value, annos);
  }

  //}}}

  //{{{ public void accept(ATermVisitor v)

  public void accept(ATermVisitor v)
    throws ATermVisitFailure
  {
    v.visitInt(this);
  }

  //}}}
}
