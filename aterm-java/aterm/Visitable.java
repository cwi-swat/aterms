package aterm;


public interface Visitable extends jjtraveler.Visitable {
	public void accept(Visitor visitor) throws jjtraveler.VisitFailure;
}
