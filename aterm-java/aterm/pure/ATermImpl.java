/*
 * Java version of the ATerm library
 * Copyright (C) 2002, CWI, LORIA-INRIA
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

package aterm.pure;

import java.io.*;
import java.util.*;

import shared.SharedObject;

import aterm.*;

public abstract class ATermImpl extends ATermVisitableImpl implements ATerm, SharedObject {
  private ATermList annotations;
  static protected PureFactory factory;
  private int hashCode;

  static public void initialize(PureFactory f) {
    factory = f;
  }

  static protected PureFactory getStaticFactory() {
    return factory;
  }
  
  public int hashCode() {
    return this.hashCode;
  }
  
  abstract public Object clone();

  protected void setHashCode(int hashcode) {
    this.hashCode = hashcode;
  }

  protected void internSetAnnotations(ATermList annos) {
    this.annotations = annos;
  }

  protected void init(int hashCode, ATermList annos) {
    this.hashCode = hashCode;
    this.annotations = annos;
  }
  
  public boolean equivalent(SharedObject obj) {
    try {
      return ((ATerm)obj).getType() == getType()
        && ((ATerm)obj).getAnnotations().equals(getAnnotations());
    } catch (ClassCastException e) {
      return false;
    }
  }
  
  public ATermFactory getFactory() {
    return getPureFactory();
  }
  
  protected PureFactory getPureFactory() {
    return factory;
  }
  
  public ATerm setAnnotation(ATerm label, ATerm anno) {
    ATermList new_annos = annotations.dictPut(label, anno);
    ATerm result = setAnnotations(new_annos);

    return result;
  }

  public ATerm removeAnnotation(ATerm label) {
    return setAnnotations(annotations.dictRemove(label));
  }

  public ATerm getAnnotation(ATerm label) {
    return annotations.dictGet(label);
  }

  public ATerm removeAnnotations() {
    return setAnnotations(factory.empty);
  }

  public ATermList getAnnotations() {
    return annotations;
  }

  public List match(String pattern) throws ParseError {
    return match(factory.parsePattern(pattern));
  }

  public List match(ATerm pattern) {
    List list = new LinkedList();
    if (match(pattern, list)) {
      return list;
    } else {
      return null;
    }
  }

  public boolean isEqual(ATerm term) {
    if (term instanceof ATermImpl) {
      return this == term;
    }

    return factory.isDeepEqual(this, term);
  }

  public boolean equals(Object obj) {
    if (obj instanceof ATermImpl) {
      return this == obj;
    }

    if (obj instanceof ATerm) {
      return factory.isDeepEqual(this, (ATerm) obj);
    }

    return false;
  }

  boolean match(ATerm pattern, List list) {
    if (pattern.getType() == PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
      if (type.getType() == ATerm.APPL) {
        ATermAppl appl = (ATermAppl) type;
        AFun afun = appl.getAFun();
        if (afun.getName().equals("term") && afun.getArity() == 0 && !afun.isQuoted()) {
          list.add(this);
          return true;
        }
      }
    }

    return false;
  }

  public ATerm make(List list) {
    return this;
  }

  public void writeToTextFile(ATermWriter writer) throws IOException {
    try {
      writer.visitChild(this);
      writer.getStream().flush();
    } catch (ATermVisitFailure e) {
      throw new IOException(e.getMessage());
    }
  }

  public void writeToSharedTextFile(OutputStream stream) throws IOException {
    ATermWriter writer = new ATermWriter(new BufferedOutputStream(stream));
    writer.initializeSharing();
    stream.write('!');
    writeToTextFile(writer);
  }

  public void writeToTextFile(OutputStream stream) throws IOException {
    ATermWriter writer = new ATermWriter(new BufferedOutputStream(stream));
    writeToTextFile(writer);
    /*
    PrintStream ps = new PrintStream(stream);
    ps.print(this.toString());
    */
  }

  public String toString() {
    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      ATermWriter writer = new ATermWriter(stream);
      writeToTextFile(writer);
      return stream.toString();
    } catch (IOException e) {
      throw new RuntimeException("IOException: " + e.getMessage());
    }
  }

  public int getNrSubTerms() {
    return 0;
  }

  public ATerm getSubTerm(int index) {
    throw new RuntimeException("no children!");
  }

  public ATerm setSubTerm(int index, ATerm t) {
    throw new RuntimeException("no children!");
  }

}

class ATermWriter extends ATermVisitor {

  private static char[] TOBASE64 =
    {
      'A',
      'B',
      'C',
      'D',
      'E',
      'F',
      'G',
      'H',
      'I',
      'J',
      'K',
      'L',
      'M',
      'N',
      'O',
      'P',
      'Q',
      'R',
      'S',
      'T',
      'U',
      'V',
      'W',
      'X',
      'Y',
      'Z',
      'a',
      'b',
      'c',
      'd',
      'e',
      'f',
      'g',
      'h',
      'i',
      'j',
      'k',
      'l',
      'm',
      'n',
      'o',
      'p',
      'q',
      'r',
      's',
      't',
      'u',
      'v',
      'w',
      'x',
      'y',
      'z',
      '0',
      '1',
      '2',
      '3',
      '4',
      '5',
      '6',
      '7',
      '8',
      '9',
      '+',
      '/' };

  PrintStream stream;
  int position;
  Map table;
  int next_abbrev;

  ATermWriter(OutputStream stream) {
    this.stream = new PrintStream(stream);
  }

  public OutputStream getStream() {
    return stream;
  }

  private void emitAbbrev(int abbrev) {
    stream.print('#');
    position++;

    StringBuffer buf = new StringBuffer();

    if (abbrev == 0) {
      buf.append(TOBASE64[0]);
    }

    while (abbrev > 0) {
      buf.append(TOBASE64[abbrev % 64]);
      abbrev /= 64;
    }
    String txt = buf.reverse().toString();
    stream.print(txt);
    position += txt.length();
  }

  public void visitChild(ATerm child) throws ATermVisitFailure {
    if (table != null) {
      Integer abbrev = (Integer) table.get(child);
      if (abbrev != null) {
        emitAbbrev(abbrev.intValue());
        return;
      }
    }

    int start = position;
    if (child.getType() == ATerm.LIST) {
      stream.print('[');
      position++;
    }
    visit(child);
    if (child.getType() == ATerm.LIST) {
      stream.print(']');
      position++;
    }

    ATermList annos = child.getAnnotations();
    if (!annos.isEmpty()) {
      stream.print('{');
      position++;
      visit(annos);
      stream.print('}');
      position++;
    }

    if (table != null) {
      int length = position - start;
      if (length > PureFactory.abbrevSize(next_abbrev)) {
        Integer key = new Integer(next_abbrev++);
        table.put(child, key);
      }
    }
  }

  public void visitAppl(ATermAppl appl) throws ATermVisitFailure {
    AFun fun = appl.getAFun();
    String name = fun.toString();
    stream.print(name);
    position += name.length();
    if (fun.getArity() > 0 || name.equals("")) {
      stream.print('(');
      position++;
      for (int i = 0; i < fun.getArity(); i++) {
        if (i != 0) {
          stream.print(',');
          position++;
        }
        visitChild(appl.getArgument(i));
      }
      stream.print(')');
      position++;
    }
  }

  public void visitList(ATermList list) throws ATermVisitFailure {
    while (!list.isEmpty()) {
      visitChild(list.getFirst());
      list = list.getNext();
      if (!list.isEmpty()) {
        stream.print(',');
        position++;
      }
    }
  }

  public void visitPlaceholder(ATermPlaceholder ph) throws ATermVisitFailure {
    stream.print('<');
    position++;
    visitChild(ph.getPlaceholder());
    stream.print('>');
    position++;
  }

  public void visitInt(ATermInt i) throws ATermVisitFailure {
    String txt = String.valueOf(i.getInt());
    stream.print(txt);
    position += txt.length();
  }

  public void visitReal(ATermReal r) throws ATermVisitFailure {
    String txt = String.valueOf(r.getReal());
    stream.print(txt);
    position += txt.length();
  }

  public void visitBlob(ATermBlob blob) throws ATermVisitFailure {
    String txt = String.valueOf(blob.getBlobSize()) + "#" + String.valueOf(blob.hashCode());
    stream.print(txt);
    position += txt.length();

  }

  public void initializeSharing() {
    table = new HashMap();
  }

}
