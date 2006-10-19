package aterm;
import jjtraveler.VisitFailure;


public class ATermFwdVoid implements aterm.Visitor {

    public ATermFwdVoid() {}

    public jjtraveler.Visitable visit(jjtraveler.Visitable v) 
    throws jjtraveler.VisitFailure {
	if (v instanceof aterm.Visitable) {
	    return ((aterm.Visitable) v).accept(this);
	}
	    throw new jjtraveler.VisitFailure();
    }

    public aterm.Visitable visitATerm(ATerm arg) throws VisitFailure {
        voidVisitATerm(arg);
        return arg;
    }

    public aterm.Visitable visitInt(ATermInt arg) throws VisitFailure {
        voidVisitInt(arg);
        return arg;
    }

    public aterm.Visitable visitReal(ATermReal arg) throws VisitFailure {
        voidVisitReal(arg);
        return arg;
    }

    public aterm.Visitable visitAppl(ATermAppl arg) throws VisitFailure {
        voidVisitAppl(arg);
        return arg;
    }

    public aterm.Visitable visitList(ATermList arg) throws VisitFailure {
        voidVisitList(arg);
        return arg;
    }

    public aterm.Visitable visitPlaceholder(ATermPlaceholder arg) throws VisitFailure {
        voidVisitPlaceholder(arg);
        return arg;
    }

    public aterm.Visitable visitBlob(ATermBlob arg) throws VisitFailure {
        voidVisitBlob(arg);
        return arg;
    }

    public aterm.Visitable visitAFun(AFun fun) throws VisitFailure {
        return fun;
    }


    // methods to re-implement for void visitation

    public void voidVisitATerm(ATerm arg) throws VisitFailure {
    }

    public void voidVisitInt(ATermInt arg) throws VisitFailure {
        voidVisitATerm(arg);
    }

    public void voidVisitReal(ATermReal arg) throws VisitFailure {
        voidVisitATerm(arg);
    }

    public void voidVisitAppl(ATermAppl arg) throws VisitFailure {
        voidVisitATerm(arg);
    }

    public void voidVisitList(ATermList arg) throws VisitFailure {
        voidVisitATerm(arg);
    }

    public void voidVisitPlaceholder(ATermPlaceholder arg) throws VisitFailure {
        voidVisitATerm(arg);
    }

    public void voidVisitBlob(ATermBlob arg) throws VisitFailure {
        voidVisitATerm(arg);
    }
}

