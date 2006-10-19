package aterm;

import jjtraveler.VisitFailure;

public interface Visitor extends jjtraveler.Visitor {

	public aterm.Visitable visitATerm(ATerm arg) throws VisitFailure;

	public aterm.Visitable visitInt(ATermInt arg) throws VisitFailure;

	public aterm.Visitable visitReal(ATermReal arg) throws VisitFailure;

	public aterm.Visitable visitAppl(ATermAppl arg) throws VisitFailure;

	public aterm.Visitable visitList(ATermList arg) throws VisitFailure;

	public aterm.Visitable visitPlaceholder(ATermPlaceholder arg) throws VisitFailure;

	public aterm.Visitable visitBlob(ATermBlob arg) throws VisitFailure;

	public aterm.Visitable visitAFun(AFun fun) throws VisitFailure;
}
