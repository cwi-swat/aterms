package aterm;

import jjtraveler.*;

public class Visitor extends VoidVisitor implements jjtraveler.Visitor
{
    public void voidVisit(jjtraveler.Visitable any)  throws VisitFailure {
        if (any instanceof Visitable) {
            ((Visitable) any).accept(this);
        }
        else {
            throw new VisitFailure();
        }
    }
    
  //{{{ public void visitATerm(ATerm arg) throws ATermVisitFailure

  public void visitATerm(ATerm arg) throws VisitFailure
  {
  }

  //}}}

  //{{{ public void visitInt(ATermInt arg) throws ATermVisitFailure

  public void visitInt(ATermInt arg) throws VisitFailure
  {
    visitATerm(arg);
  }

  //}}}
  //{{{ public void visitReal(ATermReal arg) throws ATermVisitFailure

  public void visitReal(ATermReal arg) throws VisitFailure
  {
    visitATerm(arg);
  }

  //}}}
  //{{{ public void visitAppl(ATermAppl arg) throws ATermVisitFailure

  public void visitAppl(ATermAppl arg) throws VisitFailure
  {
    visitATerm(arg);
  }

  //}}}
  //{{{ public void visitList(ATermList arg) throws ATermVisitFailure

  public void visitList(ATermList arg) throws VisitFailure
  {
    visitATerm(arg);
  }

  //}}}
  //{{{ public void visitPlaceholder(ATermPlaceholder arg) throws ATermVisitFailure

  public void visitPlaceholder(ATermPlaceholder arg) throws VisitFailure
  {
    visitATerm(arg);
  }

  //}}}
  //{{{ public void visitBlob(ATermBlob arg) throws ATermVisitFailure

  public void visitBlob(ATermBlob arg) throws VisitFailure
  {
    visitATerm(arg);
  }

  //}}}
  
  public void visitAFun(AFun fun) throws VisitFailure 
  {
      // do nothing
  }
}
