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

public interface ATermFactory
{
  ATerm parse(String trm);
  ATerm make(String pattern, List args);
  ATerm make(ATerm pattern, List args);

  AFun  makeAFun(String name, int arity, boolean isQuoted);

  ATermInt makeInt(int val);

  ATermReal makeReal(double val);

  ATermAppl makeAppl(AFun fun);
  ATermAppl makeAppl(AFun fun, List args);
  
  ATermList makeList(ATerm first, ATerm next);

  ATermPlaceholder makePlaceholder(ATerm type);

  ATermBlob makeBlob(byte[] data);

  ATerm readFromTextFile(InputStream stream);
  ATerm readFromBinaryFile(InputStream stream);
  ATerm readFromFile(InputStream stream);

  void writeToTextFile(OutputStream stream);
  void writeToBinaryFile(OutputStream stream);

  ATerm import(ATerm term);
}
