package aterm.test;

import aterm.AFun;
import aterm.ATerm;
import aterm.ATermFactory;

public class VisitorBenchmark {
    static int id = 0;

    static ATermFactory factory = new aterm.pure.PureFactory();

    static AFun fun;

    public final static void main(String[] args) {
        try {
            int depth = 5;
            int fanout = 5;

            long beforeBuild = System.currentTimeMillis();
            fun = factory.makeAFun("f", fanout, false);
            ATerm term = buildTerm(depth, fanout);
            long beforeVisit = System.currentTimeMillis();
            NodeCounter nodeCounter = new NodeCounter();
            jjtraveler.Visitor topDownNodeCounter = new jjtraveler.TopDown(
                    nodeCounter);
            try {
                topDownNodeCounter.visit(term);
                long end = System.currentTimeMillis();
                System.out.println("Term of depth " + depth + " with fanout "
                        + fanout + " (" + nodeCounter.getCount() + " nodes)"
                        + ": build=" + (beforeVisit - beforeBuild) + ", visit="
                        + (end - beforeVisit));
                // System.out.println("term = " + term);
            } catch (jjtraveler.VisitFailure e) {
                System.err.println("WARING: VisitFailure: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.err.println("usage: java VisitorBenchmark <depth> <fanout>");
        }
    }

    private static ATerm buildTerm(int depth, int fanout) {
        if (depth == 1) {
            return factory.makeInt(id++);
        }
        ATerm[] args = new ATerm[fanout];
        ATerm arg = buildTerm(depth - 1, fanout);
        for (int i = 0; i < fanout; i++) {
            args[i] = arg;
        }
        return factory.makeAppl(fun, args);
    }
}

class NodeCounter implements jjtraveler.Visitor {
    private int count;

    public jjtraveler.Visitable visit(jjtraveler.Visitable visitable) {
        count++;
        return visitable;
    }

    public int getCount() {
        return count;
    }
}
