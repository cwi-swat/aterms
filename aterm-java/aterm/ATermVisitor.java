/*
 * Java version of the ATerm library
 * Copyright (C) 2002, CWI, LORIA-INRIA
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

package aterm;

import aterm.visitor.*;

public class ATermVisitor implements Visitor {
  public void visit(Visitable visitable) throws ATermVisitFailure {
    ((ATermVisitable) visitable).accept(this);
  }

  public void visitATerm(ATerm arg) throws ATermVisitFailure {
  }

  public void visitInt(ATermInt arg) throws ATermVisitFailure {
    visitATerm(arg);
  }


  public void visitReal(ATermReal arg) throws ATermVisitFailure {
    visitATerm(arg);
  }


  public void visitAppl(ATermAppl arg) throws ATermVisitFailure {
    visitATerm(arg);
  }


  public void visitList(ATermList arg) throws ATermVisitFailure {
    visitATerm(arg);
  }


  public void visitPlaceholder(ATermPlaceholder arg) throws ATermVisitFailure {
    visitATerm(arg);
  }


  public void visitBlob(ATermBlob arg) throws ATermVisitFailure {
    visitATerm(arg);
  }

  public void visitAFun(AFun arg) throws ATermVisitFailure {
    visitATerm(arg);
  }
}
