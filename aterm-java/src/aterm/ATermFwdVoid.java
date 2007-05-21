/*
 * Copyright (c) 2002-2007, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package aterm;
import jjtraveler.VisitFailure;

public class ATermFwdVoid implements aterm.Visitor {

    public ATermFwdVoid() {
    	super();
    }

    public jjtraveler.Visitable visit(jjtraveler.Visitable v) throws jjtraveler.VisitFailure {
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

    public aterm.Visitable visitLong(ATermLong arg) throws VisitFailure {
        voidVisitLong(arg);
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
    	// Left empty intentionally.
    }

    public void voidVisitInt(ATermInt arg) throws VisitFailure {
        voidVisitATerm(arg);
    }

    public void voidVisitLong(ATermLong arg) throws VisitFailure {
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

