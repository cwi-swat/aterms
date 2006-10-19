package aterm;

public interface Visitable extends jjtraveler.Visitable {
	public aterm.Visitable accept(aterm.Visitor visitor) throws jjtraveler.VisitFailure;
}
