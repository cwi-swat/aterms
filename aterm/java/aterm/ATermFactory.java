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
import java.util.List;

public interface ATermFactory
{
  ATerm parse(String trm);
  ATerm make(String pattern, List args);
  ATerm make(ATerm pattern, List args);


  ATermInt makeInt(int val);
  ATermReal makeReal(double val);
  ATermList makeList(ATerm first, ATermList next);
  ATermPlaceholder makePlaceholder(ATerm type);
  ATermBlob makeBlob(byte[] data);

  AFun  makeAFun(String name, int arity, boolean isQuoted);
  ATermAppl makeAppl(AFun fun);
  ATermAppl makeAppl(AFun fun, ATerm[] args);

  ATerm setAnnotation(ATerm term, ATerm label, ATerm anno);
  ATerm removeAnnotation(ATerm term, ATerm label);

  ATerm readFromTextFile(InputStream stream);
  ATerm readFromBinaryFile(InputStream stream);
  ATerm readFromFile(InputStream stream);

  void writeToTextFile(OutputStream stream);
  void writeToBinaryFile(OutputStream stream);

  ATerm importTerm(ATerm term);
}
