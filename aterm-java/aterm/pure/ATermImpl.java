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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import jjtraveler.VisitFailure;
import shared.SharedObject;
import shared.SharedObjectWithID;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermFactory;
import aterm.ATermList;
import aterm.ATermPlaceholder;
import aterm.ParseError;

public abstract class ATermImpl extends ATermVisitableImpl implements ATerm, SharedObjectWithID {
  private ATermList annotations;
  protected PureFactory factory;
  private int hashCode;
  private int uniqueId;
 
  protected ATermImpl(PureFactory factory) {
  	super();
  	this.factory = factory;
  }

  public int hashCode() {
    return this.hashCode;
  }
  
  abstract public SharedObject duplicate();

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

      if( ((ATerm)obj).getType() == getType() ) {
         return ((ATerm)obj).getAnnotations().equals(getAnnotations());
      }
      return false;
    } catch (ClassCastException e) {
      return false;
    }
  }

  public ATermFactory getFactory() {
    return factory;
  }
  
  protected PureFactory getPureFactory() {
    return (PureFactory) getFactory();
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
    return setAnnotations(((PureFactory) getFactory()).getEmpty());
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
    } catch (VisitFailure e) {
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

  public int getUniqueIdentifier() {
    return uniqueId;
  }
  
  public void setUniqueIdentifier(int uniqueId) {
    this.uniqueId = uniqueId;
  }
  
} 


