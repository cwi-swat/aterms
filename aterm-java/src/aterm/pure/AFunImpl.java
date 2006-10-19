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

import jjtraveler.VisitFailure;
import shared.SharedObject;

import aterm.*;

class AFunImpl extends ATermImpl implements AFun {

  protected AFunImpl(PureFactory factory) {
    super(factory);
  }

  String name;

  int arity;

  boolean isQuoted;

  protected void init(int hashCode, String name, int arity, boolean isQuoted) {
    super.init(hashCode, null);
    this.name = name.intern();
    this.arity = arity;
    this.isQuoted = isQuoted;
  }

  protected void initHashCode(String name, int arity, boolean isQuoted) {
    this.name = name.intern();
    this.arity = arity;
    this.isQuoted = isQuoted;
    this.setHashCode(this.hashFunction());
  }

  public SharedObject duplicate() {
    AFunImpl clone = new AFunImpl(factory);
    clone.init(hashCode(), name, arity, isQuoted);
    return clone;
  }

  public boolean equivalent(SharedObject obj) {
    try {
      AFun peer = (AFun) obj;
      return peer.getName().equals(name) && peer.getArity() == arity
        && peer.isQuoted() == isQuoted;
    } catch (ClassCastException e) {
      return false;
    }
  }

  public int getType() {
    return ATerm.AFUN;
  }

  public String getName() {
    return name;
  }

  public int getArity() {
    return arity;
  }

  public boolean isQuoted() {
    return isQuoted;
  }

  public ATerm getAnnotation(ATerm key) {
    throw new UnsupportedOperationException();
  }

  public ATermList getAnnotations() {
    throw new UnsupportedOperationException();
  }

  public ATerm setAnnotations(ATermList annos) {
    throw new UnsupportedOperationException();
  }

  public String toString() {
    StringBuilder result = new StringBuilder(name.length());

    if (isQuoted) {
      result.append('"');
    }
    
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      switch (c) {
        case '\n':
          result.append('\\');
          result.append('n');
          break;
        case '\t':
          result.append('\\');
          result.append('t');
          break;
        case '\b':
          result.append('\\');
          result.append('b');
          break;
        case '\r':
          result.append('\\');
          result.append('r');
          break;
        case '\f':
          result.append('\\');
          result.append('f');
          break;
        case '\\':
          result.append('\\');
          result.append('\\');
          break;
        case '\'':
          result.append('\\');
          result.append('\'');
          break;
          case '\"':
            result.append('\\');
          result.append('\"');
          break;

        case '!':
        case '@':
        case '#':
        case '$':
        case '%':
        case '^':
        case '&':
        case '*':
        case '(':
        case ')':
        case '-':
        case '_':
        case '+':
        case '=':
        case '|':
        case '~':
        case '{':
        case '}':
        case '[':
        case ']':
        case ';':
        case ':':
        case '<':
        case '>':
        case ',':
        case '.':
        case '?':
        case ' ':
        case '/':
          result.append(c);
          break;

        default:
          if (Character.isLetterOrDigit(c)) {
            result.append(c);
          } else {
            result.append('\\');
            result.append((char) ('0' + c / 64));
            c = (char) (c % 64);
            result.append((char) ('0' + c / 8));
            c = (char) (c % 8);
            result.append((char) ('0' + c));
          }
      }
    }
    
    if (isQuoted) {
      result.append('"');
    }

    return result.toString();
  }

  public aterm.Visitable accept(Visitor v) throws VisitFailure {
    return v.visitAFun(this);
  }

  private int hashFunction() {
    int a, b, c;
    /* Set up the internal state */
    a = b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    /*------------------------------------- handle the last 11 bytes */
    int len = name.length();
    if (len >= 12) {
      return hashFunction2();
    }
    c = (isQuoted) ? 7 * arity + 1 : arity + 1;
    c += len;
    switch (len) {
      case 11:
        c += (name.charAt(10) << 24);
      case 10:
        c += (name.charAt(9) << 16);
      case 9:
        c += (name.charAt(8) << 8);
        /* the first byte of c is reserved for the length */
      case 8:
        b += (name.charAt(7) << 24);
      case 7:
        b += (name.charAt(6) << 16);
      case 6:
        b += (name.charAt(5) << 8);
      case 5:
        b += name.charAt(4);
      case 4:
        a += (name.charAt(3) << 24);
      case 3:
        a += (name.charAt(2) << 16);
      case 2:
        a += (name.charAt(1) << 8);
      case 1:
        a += name.charAt(0);
        /* case 0: nothing left to add */
    }
    // mix(a,b,c);

    a -= b;
    a -= c;
    a ^= (c >> 13);
    b -= c;
    b -= a;
    b ^= (a << 8);
    c -= a;
    c -= b;
    c ^= (b >> 13);
    a -= b;
    a -= c;
    a ^= (c >> 12);
    b -= c;
    b -= a;
    b ^= (a << 16);
    c -= a;
    c -= b;
    c ^= (b >> 5);
    a -= b;
    a -= c;
    a ^= (c >> 3);
    b -= c;
    b -= a;
    b ^= (a << 10);
    c -= a;
    c -= b;
    c ^= (b >> 15);

    return c;
  }

  private int hashFunction2() {
    int offset = 0;
    int count = name.length();
    char[] source = new char[count];

    offset = 0;
    name.getChars(0, count, source, 0);
    int a, b, c;
    /* Set up the internal state */
    int len = count;
    a = b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    c = (isQuoted) ? 7 * (arity + 1) : arity + 1; // to avoid collison
    /*------------------------------------- handle the last 11 bytes */
    int k = offset;

    while (len >= 12) {
      a += (source[k + 0] + (source[k + 1] << 8) + (source[k + 2] << 16) + (source[k + 3] << 24));
      b += (source[k + 4] + (source[k + 5] << 8) + (source[k + 6] << 16) + (source[k + 7] << 24));
      c += (source[k + 8] + (source[k + 9] << 8) + (source[k + 10] << 16) + (source[k + 11] << 24));
      // mix(a,b,c);
      a -= b;
      a -= c;
      a ^= (c >> 13);
      b -= c;
      b -= a;
      b ^= (a << 8);
      c -= a;
      c -= b;
      c ^= (b >> 13);
      a -= b;
      a -= c;
      a ^= (c >> 12);
      b -= c;
      b -= a;
      b ^= (a << 16);
      c -= a;
      c -= b;
      c ^= (b >> 5);
      a -= b;
      a -= c;
      a ^= (c >> 3);
      b -= c;
      b -= a;
      b ^= (a << 10);
      c -= a;
      c -= b;
      c ^= (b >> 15);

      k += 12;
      len -= 12;
    }
    /*---------------------------------------- handle most of the key */
    c += count;
    switch (len) {
      case 11:
        c += (source[k + 10] << 24);
      case 10:
        c += (source[k + 9] << 16);
      case 9:
        c += (source[k + 8] << 8);
        /* the first byte of c is reserved for the length */
      case 8:
        b += (source[k + 7] << 24);
      case 7:
        b += (source[k + 6] << 16);
      case 6:
        b += (source[k + 5] << 8);
      case 5:
        b += source[k + 4];
      case 4:
        a += (source[k + 3] << 24);
      case 3:
        a += (source[k + 2] << 16);
      case 2:
        a += (source[k + 1] << 8);
      case 1:
        a += source[k + 0];
        /* case 0: nothing left to add */
    }
    // mix(a,b,c);
    a -= b;
    a -= c;
    a ^= (c >> 13);
    b -= c;
    b -= a;
    b ^= (a << 8);
    c -= a;
    c -= b;
    c ^= (b >> 13);
    a -= b;
    a -= c;
    a ^= (c >> 12);
    b -= c;
    b -= a;
    b ^= (a << 16);
    c -= a;
    c -= b;
    c ^= (b >> 5);
    a -= b;
    a -= c;
    a ^= (c >> 3);
    b -= c;
    b -= a;
    b ^= (a << 10);
    c -= a;
    c -= b;
    c ^= (b >> 15);

    //System.out.println("static doobs_hashFunctionAFun = " + c + ": " + name);
    return c;
  }

}
