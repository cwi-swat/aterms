package aterm.pure;

import aterm.*;
import aterm.visitor.*;

import java.util.List;
import java.util.LinkedList;
import java.io.*;

public abstract class ATermImpl
  implements ATerm
{
  ATermList annotations;
  PureFactory factory;

  //{{{ public ATermImpl(PureFactory factory)

  public ATermImpl(PureFactory factory, ATermList annos)
  {
    this.factory = factory;
    this.annotations = annos;
  }

  //}}}
  //{{{ public ATermFactory getFactory()

  public ATermFactory getFactory()
  {
    return factory;
  }

  //}}}

  //{{{ public ATerm setAnnotation(ATerm label, ATerm anno)

  public ATerm setAnnotation(ATerm label, ATerm anno)
  {
    ATermList new_annos = annotations.dictPut(label, anno);
    ATerm result = setAnnotations(new_annos);

    return result;
  }

  //}}}
  //{{{ public ATerm removeAnnotation(ATerm label)

  public ATerm removeAnnotation(ATerm label)
  {
    return setAnnotations(annotations.dictRemove(label));
  }

  //}}}
  //{{{ public ATerm getAnnotation(ATerm label)

  public ATerm getAnnotation(ATerm label)
  {
    return annotations.dictGet(label);
  }

  //}}}

  //{{{ public ATerm removeAnnotations()

  public ATerm removeAnnotations()
  {
    return setAnnotations(factory.empty);
  }

  //}}}
  //{{{ public ATermList getAnnotations()

  public ATermList getAnnotations()
  {
    return annotations;
  }

  //}}}

  //{{{ public List match(String pattern)

  public List match(String pattern) 
    throws ParseError
  {
    return match(factory.parsePattern(pattern));
  }


  //}}}
  //{{{ public List match(ATerm pattern)

  public List match(ATerm pattern) 
  {
    List list = new LinkedList();
    if (match(pattern, list)) {
      return list;
    } else {
      return null;
    }
  }


  //}}}

  //{{{ public boolean isEqual(ATerm term)

  public boolean isEqual(ATerm term)
  {
    if(term instanceof ATermImpl) {
      return this == term;
    }

    return factory.isDeepEqual(this, term);
  }

  //}}}
  //{{{ public boolean equals(Object obj)

  public boolean equals(Object obj)
  {
    if (obj instanceof ATermImpl) {
      return this == obj;
    }

    if (obj instanceof ATerm) {
      return factory.isDeepEqual(this, (ATerm)obj);
    }

    return false;
  }

  //}}}

  //{{{ boolean match(ATerm pattern, List list)

  boolean match(ATerm pattern, List list)
  {
    if (pattern.getType() == PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder)pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
	ATermAppl appl = (ATermAppl)type;
	AFun  afun = appl.getAFun();
	if(afun.getName().equals("term") && afun.getArity() == 0 && !afun.isQuoted()) {
	  list.add(this);
	  return true;
	}
      }
    }
    
    return false;
  }

  //}}}

  //{{{ public ATerm make(List list)

  public ATerm make(List list)
  {
    return this;
  }

  //}}}

  //{{{ public void writeToTextFile(OutputStream stream)

  public void writeToTextFile(OutputStream stream)
    throws IOException
  {
    PrintStream ps = new PrintStream(stream);
    ps.print(this.toString());
  }

  //}}}
  //{{{ public String toString()

  public String toString()
  {
    if (annotations.isEmpty()) {
      return "";
    } else {
      StringBuffer result = new StringBuffer();
      result.append("{");
      ATermList list = annotations;
      while (!list.isEmpty()) {
	result.append(list.getFirst().toString());
	list = list.getNext();
	if (!list.isEmpty()) {
	  result.append(",");
	}
      }
      result.append("}");
      return result.toString();
    }
  }

  //}}}

  //{{{ public int getNrChildren()

  public int getNrChildren()
  {
    return 0;
  }

  //}}}
  //{{{ public Visitable getChild(int index)

  public Visitable getChild(int index)
  {
    throw new RuntimeException("no children!");
  }

  //}}}
}
