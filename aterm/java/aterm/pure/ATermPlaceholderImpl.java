package aterm.pure;

import aterm.*;
import java.util.List;
import visitor.*;

class ATermPlaceholderImpl
  extends ATermImpl
  implements ATermPlaceholder
{ 
  ATerm type;

  //{{{ static int hashFunction(ATerm type)

  static int hashFunction(ATerm type, ATermList annos)
  {
    return Math.abs(type.hashCode() + 1 + annos.hashCode());
  }

  //}}}

  //{{{ public int hashCode()

  public int hashCode()
  {
    return hashFunction(type, annotations);
  }

  //}}}
  //{{{ public int getType()

  public int getType()
  {
    return ATerm.PLACEHOLDER;
  }

  //}}}

  //{{{ protected ATermPlaceholderImpl(PureFactory factory, ATerm type, ATermList annos)

  protected ATermPlaceholderImpl(PureFactory factory, ATerm type, ATermList annos)
  {
    super(factory, annos);
    this.type = type;
  }

  //}}}

  //{{{ public boolean match(ATerm pattern, List list)

  public boolean match(ATerm pattern, List list)
  {
    if (pattern.getType() == ATerm.PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder)pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
	ATermAppl appl = (ATermAppl)type;
	AFun  afun = appl.getAFun();
	if(afun.getName().equals("placeholder") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(type);
	  return true;
	}
      }
    }


    return super.match(pattern, list);
  }

  //}}}
  //{{{ public ATerm make(List args)

  public ATerm make(List args)
  { 
    ATermAppl appl;
    AFun fun;
    String name;

    appl = (ATermAppl)type;
    fun  = appl.getAFun();
    name = fun.getName();
    if (!fun.isQuoted()) {
      if (fun.getArity() == 0) {
	if (name.equals("term")) {
	  ATerm t = (ATerm)args.get(0);
	  args.remove(0);

	  return t;
	} else if (name.equals("list")) {
	  ATermList l = (ATermList)args.get(0);
	  args.remove(0);

	  return l;
	} else if (name.equals("int")) {
	  Integer i = (Integer)args.get(0);
	  args.remove(0);

	  return factory.makeInt(i.intValue());
	} else if (name.equals("real")) {
	  Double d = (Double)args.get(0);
	  args.remove(0);

	  return factory.makeReal(d.doubleValue());
	} else if (name.equals("placeholder")) {
	  ATerm type = (ATerm)args.get(0);
	  args.remove(0);
	  return factory.makePlaceholder(type);
	}
      }
      if (name.equals("appl")) {
	ATermList oldargs = appl.getArguments();
	String newname = (String)args.get(0);
	args.remove(0);
	ATermList newargs = (ATermList)oldargs.make(args);
	AFun newfun = factory.makeAFun(newname, newargs.getLength(), false);
	return factory.makeAppl(newfun, newargs);
      }
    }
    throw new RuntimeException("illegal pattern: " + this);
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    String result;

    result = type.toString();
    if (type.getType() == LIST) {
      result = "[" + result + "]";
    }

    return "<" + result + ">" + super.toString();
  }

  //}}}
  //{{{ public ATerm getPlaceholder()

  public ATerm getPlaceholder()
  {
    return type;
  }

  //}}}
  //{{{ public ATerm setAnnotations(ATermList annos)

  public ATerm setAnnotations(ATermList annos)
  {
    return factory.makePlaceholder(type, annos);
  }

  //}}}

  //{{{ public boolean accept(ATermVisitor v)

  public boolean accept(ATermVisitor v)
  {
    return v.visitPlaceholder(this);
  }

  //}}}
  //{{{ public boolean acceptChildren(Visitor v)

  public boolean acceptChildren(Visitor v)
  {
    return v.visit(type);
  }

  //}}}
}
