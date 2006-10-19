package aterm;
import jjtraveler.VisitFailure;


public class ATermFwd implements aterm.Visitor {

    jjtraveler.Visitor any;

    public ATermFwd(jjtraveler.Visitor any) {
        this.any = any;
    }

    public jjtraveler.Visitable visit(jjtraveler.Visitable v) 
    throws jjtraveler.VisitFailure {
	if (v instanceof aterm.Visitable) {
	    return ((aterm.Visitable) v).accept(this);
	}
	    throw new jjtraveler.VisitFailure();
    }

    public aterm.Visitable visitATerm(ATerm arg) throws VisitFailure {
        return (aterm.Visitable) any.visit(arg);
    }

    public aterm.Visitable visitInt(ATermInt arg) throws VisitFailure {
        return visitATerm(arg);
    }

    public aterm.Visitable visitReal(ATermReal arg) throws VisitFailure {
        return visitATerm(arg);
    }

    public aterm.Visitable visitAppl(ATermAppl arg) throws VisitFailure {
        return visitATerm(arg);
    }

    public aterm.Visitable visitList(ATermList arg) throws VisitFailure {
        return visitATerm(arg);
    }

    public aterm.Visitable visitPlaceholder(ATermPlaceholder arg) throws VisitFailure {
        return visitATerm(arg);
    }

    public aterm.Visitable visitBlob(ATermBlob arg) throws VisitFailure {
        return visitATerm(arg);
    }

    public aterm.Visitable visitAFun(AFun fun) throws VisitFailure {
        return fun;
    }
}

