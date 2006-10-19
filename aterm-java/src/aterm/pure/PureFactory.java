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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import shared.SharedObjectFactory;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermBlob;
import aterm.ATermFactory;
import aterm.ATermInt;
import aterm.ATermList;
import aterm.ATermPlaceholder;
import aterm.ATermReal;
import aterm.ParseError;

public class PureFactory extends SharedObjectFactory implements ATermFactory {

  private static int DEFAULT_TERM_TABLE_SIZE = 16; // means 2^16 entries

  private ATermListImpl protoList;

  private ATermApplImpl protoAppl;

  private ATermIntImpl protoInt;

  private ATermRealImpl protoReal;

  private ATermBlobImpl protoBlob;

  private ATermPlaceholderImpl protoPlaceholder;

  private AFunImpl protoAFun;

  private ATermList empty;

  static boolean isBase64(int c) {
    return Character.isLetterOrDigit(c) || c == '+' || c == '/';
  }

  static public int abbrevSize(int abbrev) {
    int size = 1;

    if (abbrev == 0) {
      return 2;
    }

    while (abbrev > 0) {
      size++;
      abbrev /= 64;
    }

    return size;
  }

  public PureFactory() {
    this(DEFAULT_TERM_TABLE_SIZE);
  }

  public PureFactory(int termTableSize) {
    super(termTableSize);

    protoList = new ATermListImpl(this);
    protoAppl = new ATermApplImpl(this);
    protoInt = new ATermIntImpl(this);
    protoReal = new ATermRealImpl(this);
    protoBlob = new ATermBlobImpl(this);
    protoPlaceholder = new ATermPlaceholderImpl(this);
    protoAFun = new AFunImpl(this);

    /*
     * 240146486 is a fix-point hashcode such that
     * empty.hashcode = empty.getAnnotations().hashCode
     * this magic value can be found using: findEmptyHashCode()
     */
    protoList.init(240146486, null, null, null);
    empty = (ATermList) build(protoList);
    //int magicHash = ((ATermListImpl) empty).findEmptyHashCode();
    ((ATermListImpl) empty).init(240146486, empty, null, null);

  }

  public ATermInt makeInt(int val) {
    return makeInt(val, empty);
  }

  public ATermReal makeReal(double val) {
    return makeReal(val, empty);
  }

  public ATermList makeList() {
    return empty;
  }

  public ATermList makeList(ATerm singleton) {
    return makeList(singleton, empty, empty);
  }

  public ATermList makeList(ATerm first, ATermList next) {
    return makeList(first, next, empty);
  }

  public ATermPlaceholder makePlaceholder(ATerm type) {
    return makePlaceholder(type, empty);
  }

  public ATermBlob makeBlob(byte[] data) {
    return makeBlob(data, empty);
  }

  public AFun makeAFun(String name, int arity, boolean isQuoted) {
    synchronized (protoAFun) {
      protoAFun.initHashCode(name, arity, isQuoted);
      return (AFun) build(protoAFun);
    }
  }

  public ATermInt makeInt(int value, ATermList annos) {
    synchronized (protoInt) {
      protoInt.initHashCode(annos, value);
      return (ATermInt) build(protoInt);
    }
  }

  public ATermReal makeReal(double value, ATermList annos) {
    synchronized (protoReal) {
      protoReal.init(hashReal(annos, value), annos, value);
      return (ATermReal) build(protoReal);
    }
  }

  static private int hashReal(ATermList annos, double value) {
    return shared.HashFunctions.doobs(new Object[] { annos,
      new Double(value) });
  }

  public ATermPlaceholder makePlaceholder(ATerm type, ATermList annos) {
    synchronized (protoPlaceholder) {
      protoPlaceholder.init(hashPlaceholder(annos, type), annos, type);
      return (ATermPlaceholder) build(protoPlaceholder);
    }
  }

  static private int hashPlaceholder(ATermList annos, ATerm type) {
    return shared.HashFunctions.doobs(new Object[] { annos, type });
  }

  public ATermBlob makeBlob(byte[] data, ATermList annos) {
    synchronized (protoBlob) {
      protoBlob.init(hashBlob(annos, data), annos, data);
      return (ATermBlob) build(protoBlob);
    }
  }

  static private int hashBlob(ATermList annos, byte[] data) {
    return shared.HashFunctions.doobs(new Object[] { annos, data });
  }

  public ATermList makeList(ATerm first, ATermList next, ATermList annos) {
    synchronized (protoList) {
      protoList.initHashCode(annos, first, next);
      return (ATermList) build(protoList);
    }
  }

  private static ATerm[] array0 = new ATerm[0];

  public ATermAppl makeAppl(AFun fun, ATerm[] args) {
    return makeAppl(fun, args, empty);
  }

  public ATermAppl makeAppl(AFun fun, ATerm[] args, ATermList annos) {
    synchronized (protoAppl) {
      protoAppl.initHashCode(annos, fun, args);
      return (ATermAppl) build(protoAppl);
    }
  }

  public ATermAppl makeApplList(AFun fun, ATermList list) {
    return makeApplList(fun, list, empty);
  }

  public ATermAppl makeApplList(AFun fun, ATermList list, ATermList annos) {
    ATerm[] arg_array;

    arg_array = new ATerm[list.getLength()];

    int i = 0;
    while (!list.isEmpty()) {
      arg_array[i++] = list.getFirst();
      list = list.getNext();
    }
    return makeAppl(fun, arg_array, annos);
  }

  public ATermAppl makeAppl(AFun fun) {
    return makeAppl(fun, array0);
  }

  public ATermAppl makeAppl(AFun fun, ATerm arg) {
    ATerm[] argarray1 = new ATerm[] { arg };
    return makeAppl(fun, argarray1);
  }

  public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2) {
    ATerm[] argarray2 = new ATerm[] { arg1, arg2 };
    return makeAppl(fun, argarray2);
  }

  public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3) {
    ATerm[] argarray3 = new ATerm[] { arg1, arg2, arg3 };
    return makeAppl(fun, argarray3);
  }

  public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3,
      ATerm arg4) {
    ATerm[] argarray4 = new ATerm[] { arg1, arg2, arg3, arg4 };
    return makeAppl(fun, argarray4);
  }

  public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3,
      ATerm arg4, ATerm arg5) {
    ATerm[] argarray5 = new ATerm[] { arg1, arg2, arg3, arg4, arg5 };
    return makeAppl(fun, argarray5);
  }

  public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3,
      ATerm arg4, ATerm arg5, ATerm arg6) {
    ATerm[] args = { arg1, arg2, arg3, arg4, arg5, arg6 };
    return makeAppl(fun, args);
  }

  public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3,
      ATerm arg4, ATerm arg5, ATerm arg6, ATerm arg7) {
    ATerm[] args = { arg1, arg2, arg3, arg4, arg5, arg6, arg7 };
    return makeAppl(fun, args);
  }

  public ATermList getEmpty() {
    return empty;
  }

  private ATerm parseAbbrev(ATermReader reader) throws IOException {
    ATerm result;
    int abbrev;

    int c = reader.read();

    abbrev = 0;
    while (isBase64(c)) {
      abbrev *= 64;
      if (c >= 'A' && c <= 'Z') {
        abbrev += c - 'A';
      } else if (c >= 'a' && c <= 'z') {
        abbrev += c - 'a' + 26;
      } else if (c >= '0' && c <= '9') {
        abbrev += c - '0' + 52;
      } else if (c == '+') {
        abbrev += 62;
      } else if (c == '/') {
        abbrev += 63;
      } else {
        throw new RuntimeException("not a base-64 digit: " + c);
      }

      c = reader.read();
    }

    result = reader.getTerm(abbrev);

    return result;
  }

  private ATerm parseNumber(ATermReader reader) throws IOException {
	StringBuilder str = new StringBuilder();
    ATerm result;

    do {
      str.append((char) reader.getLastChar());
    } while (Character.isDigit(reader.read()));

    if (reader.getLastChar() != '.' && reader.getLastChar() != 'e'
        && reader.getLastChar() != 'E') {
      int val;
      try {
        val = Integer.parseInt(str.toString());
      } catch (NumberFormatException e) {
        throw new ParseError("malformed int");
      }
      result = makeInt(val);
    } else {
      if (reader.getLastChar() == '.') {
        str.append('.');
        reader.read();
        if (!Character.isDigit(reader.getLastChar()))
          throw new ParseError("digit expected");
        do {
          str.append((char) reader.getLastChar());
        } while (Character.isDigit(reader.read()));
      }
      if (reader.getLastChar() == 'e' || reader.getLastChar() == 'E') {
        str.append((char) reader.getLastChar());
        reader.read();
        if (reader.getLastChar() == '-' || reader.getLastChar() == '+') {
          str.append((char) reader.getLastChar());
          reader.read();
        }
        if (!Character.isDigit(reader.getLastChar()))
          throw new ParseError("digit expected!");
        do {
          str.append((char) reader.getLastChar());
        } while (Character.isDigit(reader.read()));
      }
      double val;
      try {
        val = Double.valueOf(str.toString()).doubleValue();
      } catch (NumberFormatException e) {
        throw new ParseError("malformed real");
      }
      result = makeReal(val);
    }
    return result;
  }

  private String parseId(ATermReader reader) throws IOException {
    int c = reader.getLastChar();
    StringBuilder buf = new StringBuilder(32);

    do {
      buf.append((char) c);
      c = reader.read();
    } while (Character.isLetterOrDigit(c) || c == '_' || c == '-'
        || c == '+' || c == '*' || c == '$');

    return buf.toString();
  }

  private String parseString(ATermReader reader) throws IOException {
    boolean escaped;
    StringBuilder str = new StringBuilder();

    do {
      escaped = false;
      if (reader.read() == '\\') {
        reader.read();
        escaped = true;
      }

      if (escaped) {
        switch (reader.getLastChar()) {
          case 'n':
            str.append('\n');
            break;
          case 't':
            str.append('\t');
            break;
          case 'b':
            str.append('\b');
            break;
          case 'r':
            str.append('\r');
            break;
          case 'f':
            str.append('\f');
            break;
          case '\\':
            str.append('\\');
            break;
          case '\'':
            str.append('\'');
            break;
            case '\"':
              str.append('\"');
            break;
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
            str.append(reader.readOct());
            break;
          default:
            str.append('\\').append((char) reader.getLastChar());
        }
      } else if (reader.getLastChar() != '\"')
        str.append((char) reader.getLastChar());
    } while (escaped || reader.getLastChar() != '"');

    return str.toString();
  }

  private ATermList parseATerms(ATermReader reader) throws IOException {
    ATerm[] terms = parseATermsArray(reader);
    ATermList result = empty;
    for (int i = terms.length - 1; i >= 0; i--) {
      result = makeList(terms[i], result);
    }

    return result;
  }

  private ATerm[] parseATermsArray(ATermReader reader) throws IOException {
    List list = new ArrayList();
    ATerm term;

    term = parseFromReader(reader);
    list.add(term);
    while (reader.getLastChar() == ',') {
      reader.readSkippingWS();
      term = parseFromReader(reader);
      list.add(term);
    }

    ATerm[] array = new ATerm[list.size()];
    ListIterator iter = list.listIterator();
    int index = 0;
    while (iter.hasNext()) {
      array[index++] = (ATerm) iter.next();
    }
    return array;
  }

  synchronized private ATerm parseFromReader(ATermReader reader)
    throws IOException {
    ATerm result;
    int c, start, end;
    String funname;

    start = reader.getPosition();
    switch (reader.getLastChar()) {
      case -1:
        throw new ParseError("premature EOF encountered.");

      case '#':
        return parseAbbrev(reader);

      case '[':

        c = reader.readSkippingWS();
        if (c == -1) {
          throw new ParseError("premature EOF encountered.");
        }

        if (c == ']') {
          c = reader.readSkippingWS();
          result = empty;
        } else {
          result = parseATerms(reader);
          if (reader.getLastChar() != ']') {
            throw new ParseError("expected ']' but got '"
                + (char) reader.getLastChar() + "'");
          }
          c = reader.readSkippingWS();
        }

        break;

      case '<':

        c = reader.readSkippingWS();
        ATerm ph = parseFromReader(reader);

        if (reader.getLastChar() != '>') {
          throw new ParseError("expected '>' but got '"
              + (char) reader.getLastChar() + "'");
        }

        c = reader.readSkippingWS();

        result = makePlaceholder(ph);

        break;

      case '"':

        funname = parseString(reader);

        c = reader.readSkippingWS();
        if (reader.getLastChar() == '(') {
          c = reader.readSkippingWS();
          if (c == -1) {
            throw new ParseError("premature EOF encountered.");
          }
          if (reader.getLastChar() == ')') {
            result = makeAppl(makeAFun(funname, 0, true));
          } else {
            ATerm[] list = parseATermsArray(reader);

            if (reader.getLastChar() != ')') {
              throw new ParseError("expected ')' but got '"
                  + reader.getLastChar() + "'");
            }
            result = makeAppl(makeAFun(funname, list.length, true),
                list);
          }
          c = reader.readSkippingWS();
          if (c == -1) {
            throw new ParseError("premature EOF encountered.");
          }
        } else {
          result = makeAppl(makeAFun(funname, 0, true));
        }

        break;

      case '(':

        c = reader.readSkippingWS();
        if (c == -1) {
          throw new ParseError("premature EOF encountered.");
        }
        if (reader.getLastChar() == ')') {
          result = makeAppl(makeAFun("", 0, false));
        } else {
          ATerm[] list = parseATermsArray(reader);

          if (reader.getLastChar() != ')') {
            throw new ParseError("expected ')' but got '"
                + (char)reader.getLastChar() + "'");
          }
          result = makeAppl(makeAFun("", list.length, false), list);
        }
        c = reader.readSkippingWS();
        if (c == -1) {
          throw new ParseError("premature EOF encountered.");
        }

        break;

      case '-':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        result = parseNumber(reader);
        c = reader.skipWS();
        break;

      default:
        c = reader.getLastChar();
        if (Character.isLetter(c)) {

          funname = parseId(reader);
          c = reader.skipWS();
          if (reader.getLastChar() == '(') {
            c = reader.readSkippingWS();
            if (c == -1) {
              throw new ParseError("premature EOF encountered.");
            }
            if (reader.getLastChar() == ')') {
              result = makeAppl(makeAFun(funname, 0, false));
            } else {
              ATerm[] list = parseATermsArray(reader);

              if (reader.getLastChar() != ')') {
                throw new ParseError("expected ')' but got '"
                    + (char)reader.getLastChar() + "'");
              }
              result = makeAppl(
                  makeAFun(funname, list.length, false), list);
            }
            c = reader.readSkippingWS();
          } else {
            result = makeAppl(makeAFun(funname, 0, false));
          }

        } else {
          throw new ParseError("illegal character: "
              + (char)reader.getLastChar());
        }
    }

    if (reader.getLastChar() == '{') {

      ATermList annos;
      if (reader.readSkippingWS() == '}') {
        reader.readSkippingWS();
        annos = empty;
      } else {
        annos = parseATerms(reader);
        if (reader.getLastChar() != '}') {
          throw new ParseError("'}' expected");
        }
        reader.readSkippingWS();
      }
      result = result.setAnnotations(annos);

    }

    /* Parse some ToolBus anomalies for backwards compatibility */
    if (reader.getLastChar() == ':') {
      reader.read();
      ATerm anno = parseFromReader(reader);
      result = result.setAnnotation(parse("type"), anno);
    }

    if (reader.getLastChar() == '?') {
      reader.readSkippingWS();
      result = result.setAnnotation(parse("result"), parse("true"));
    }

    end = reader.getPosition();
    reader.storeNextTerm(result, end - start);

    return result;
  }

  public ATerm parse(String trm) {
    try {
      ATermReader reader = new ATermReader(new StringReader(trm), trm.length());
      reader.readSkippingWS();
      ATerm result = parseFromReader(reader);
      return result;
    } catch (IOException e) {
      throw new ParseError("premature end of string");
    }
  }

  public ATerm make(String trm) {
    return parse(trm);
  }

  public ATerm make(String pattern, List args) {
    return make(parse(pattern), args);
  }

  public ATerm make(String pattern, Object arg1) {
    List args = new LinkedList();
    args.add(arg1);
    return make(pattern, args);
  }

  public ATerm make(String pattern, Object arg1, Object arg2) {
    List args = new LinkedList();
    args.add(arg1);
    args.add(arg2);
    return make(pattern, args);
  }

  public ATerm make(String pattern, Object arg1, Object arg2, Object arg3) {
    List args = new LinkedList();
    args.add(arg1);
    args.add(arg2);
    args.add(arg3);
    return make(pattern, args);
  }

  public ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
      Object arg4) {
    List args = new LinkedList();
    args.add(arg1);
    args.add(arg2);
    args.add(arg3);
    args.add(arg4);
    return make(pattern, args);
  }

  public ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
      Object arg4, Object arg5) {
    List args = new LinkedList();
    args.add(arg1);
    args.add(arg2);
    args.add(arg3);
    args.add(arg4);
    args.add(arg5);
    return make(pattern, args);
  }

  public ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
      Object arg4, Object arg5, Object arg6) {
    List args = new LinkedList();
    args.add(arg1);
    args.add(arg2);
    args.add(arg3);
    args.add(arg4);
    args.add(arg5);
    args.add(arg6);
    return make(pattern, args);
  }

  public ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
      Object arg4, Object arg5, Object arg6, Object arg7) {
    List args = new LinkedList();
    args.add(arg1);
    args.add(arg2);
    args.add(arg3);
    args.add(arg4);
    args.add(arg5);
    args.add(arg6);
    args.add(arg7);
    return make(pattern, args);
  }

  public ATerm make(ATerm pattern, List args) {
    return pattern.make(args);
  }

  ATerm parsePattern(String pattern) throws ParseError {
    return parse(pattern);
  }

  protected boolean isDeepEqual(ATermImpl t1, ATerm t2) {
    throw new UnsupportedOperationException("not yet implemented!");
  }

  private ATerm readFromSharedTextFile(ATermReader reader) throws IOException {
    reader.initializeSharing();
    return parseFromReader(reader);
  }

  private ATerm readFromTextFile(ATermReader reader) throws IOException {
    return parseFromReader(reader);
  }

  public ATerm readFromTextFile(InputStream stream) throws IOException {
    ATermReader reader = new ATermReader(new BufferedReader(new InputStreamReader(stream)));
    reader.readSkippingWS();

    return readFromTextFile(reader);
  }

  public ATerm readFromSharedTextFile(InputStream stream) throws IOException {
    ATermReader reader = new ATermReader(new BufferedReader(new InputStreamReader(stream)));
    reader.readSkippingWS();

    if (reader.getLastChar() != '!') {
      throw new IOException("not a shared text file!");
    }

    reader.readSkippingWS();

    return readFromSharedTextFile(reader);
  }

  public ATerm readFromBinaryFile(InputStream stream) {
    throw new RuntimeException("not yet implemented!");
  }

  public ATerm readFromFile(InputStream stream) throws IOException {
    ATermReader reader = new ATermReader(new BufferedReader(new InputStreamReader(stream)));
    reader.readSkippingWS();

    int last_char = reader.getLastChar();
    if (last_char == '!') {
      reader.readSkippingWS();
      return readFromSharedTextFile(reader);
    } else if (Character.isLetterOrDigit(last_char)
        || last_char == '_' || last_char == '[' || last_char == '-') {
      return readFromTextFile(reader);
    } else {
      throw new RuntimeException(
          "BAF files are not supported by this factory.");
    }
  }

  public ATerm readFromFile(String file) throws IOException {
    return readFromFile(new FileInputStream(file));
  }

  public ATerm importTerm(ATerm term) {
    throw new RuntimeException("not yet implemented!");
  }

}

class HashedWeakRef extends WeakReference {
  protected HashedWeakRef next;

  public HashedWeakRef(Object object, HashedWeakRef next) {
    super(object);
    this.next = next;
  }
}

class ATermReader {
  private static final int INITIAL_TABLE_SIZE = 2048;
  private static final int TABLE_INCREMENT = 4096;
  
  private static final int INITIAL_BUFFER_SIZE = 1024;

  private Reader reader;

  private int last_char;
  private int pos;

  private int nr_terms;
  private ATerm[] table;
  
  private char[] buffer;
  private int limit;
  private int bufferPos;
  
  public ATermReader(Reader reader) {
	this(reader, INITIAL_BUFFER_SIZE);
  }

  public ATermReader(Reader reader, int bufferSize) {
    this.reader = reader;
    last_char = -1;
    pos = 0;
    
    if(bufferSize < INITIAL_BUFFER_SIZE)
    	buffer = new char[bufferSize];
    else
    	buffer = new char[INITIAL_BUFFER_SIZE];
    limit = -1;
    bufferPos = -1;
  }

  public void initializeSharing() {
    table = new ATerm[INITIAL_TABLE_SIZE];
    nr_terms = 0;
  }

  public void storeNextTerm(ATerm t, int size) {
    if (table == null) {
      return;
    }

    if (size <= PureFactory.abbrevSize(nr_terms)) {
      return;
    }

    if (nr_terms == table.length) {
      ATerm[] new_table = new ATerm[table.length + TABLE_INCREMENT];
      System.arraycopy(table, 0, new_table, 0, table.length);
      table = new_table;
    }

    table[nr_terms++] = t;
  }

  public ATerm getTerm(int index) {
    if (index < 0 || index >= nr_terms) {
      throw new RuntimeException("illegal index");
    }
    return table[index];
  }

  public int read() throws IOException {
	  if(bufferPos == limit){
		  limit = reader.read(buffer);
		  bufferPos = 0;
	  }
	  
	  if(limit == -1){
		  last_char = -1;
	  }else{
		  last_char = buffer[bufferPos++];
		  pos++;
	  }
	  
	  return last_char;
  }

  public int readSkippingWS() throws IOException {
    do {
      last_char = read();
    } while (Character.isWhitespace(last_char));

    return last_char;

  }

  public int skipWS() throws IOException {
    while (Character.isWhitespace(last_char)) {
      last_char = read();
    }

    return last_char;
  }

  public int readOct() throws IOException {
    int val = Character.digit(last_char, 8);
    val += Character.digit(read(), 8);

    if (val < 0) {
      throw new ParseError("octal must have 3 octdigits.");
    }

    val += Character.digit(read(), 8);

    if (val < 0) {
      throw new ParseError("octal must have 3 octdigits");
    }

    return val;
  }

  public int getLastChar() {
    return last_char;
  }

  public int getPosition() {
    return pos;
  }
}
