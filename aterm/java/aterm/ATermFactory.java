/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/

package aterm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

public interface ATermFactory
{
  ATerm parse(String trm);
  ATerm make(String pattern, List args);
  ATerm make(ATerm pattern, List args);
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4, Object arg5, Object arg6);
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4, Object arg5);
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4);
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3);
  ATerm make(String pattern, Object arg1, Object arg2);
  ATerm make(String pattern, Object arg1);

  ATermInt makeInt(int val);
  ATermReal makeReal(double val);
  ATermList makeList();
  ATermList makeList(ATerm single);
  ATermList makeList(ATerm first, ATermList next);
  ATermPlaceholder makePlaceholder(ATerm type);
  ATermBlob makeBlob(byte[] data);

  AFun  makeAFun(String name, int arity, boolean isQuoted);
  ATermAppl makeAppl(AFun fun);
  ATermAppl makeAppl(AFun fun, ATerm arg);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4, ATerm arg5);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, 
		     ATerm arg4, ATerm arg5, ATerm arg6);
  ATermAppl makeAppl(AFun fun, ATerm[] args);
  ATermAppl makeAppl(AFun fun, ATermList args);

  ATerm setAnnotation(ATerm term, ATerm label, ATerm anno);
  ATerm removeAnnotation(ATerm term, ATerm label);

  ATerm readFromTextFile(InputStream stream) throws IOException;
  ATerm readFromBinaryFile(InputStream stream) throws IOException;
  ATerm readFromFile(InputStream stream) throws IOException;

  ATerm importTerm(ATerm term);
}
