package aterm.pure;

import shared.SharedObject;

import aterm.*;

class AFunImpl extends ATermImpl implements AFun {
  String name;
  int arity;
  boolean isQuoted;

  protected AFunImpl(PureFactory factory) {
    super(factory);
  }

  protected void init(int hashCode, String name, int arity, boolean isQuoted) {
    super.init(hashCode, null);
    this.name = name.intern();
    this.arity = arity;
    this.isQuoted = isQuoted;
  }

  public Object clone() {
    AFunImpl clone = new AFunImpl(getPureFactory());
    clone.init(hashCode(), name, arity, isQuoted);
    return clone;
  }

  public boolean equivalent(SharedObject obj) {
    try {
      AFun peer = (AFun) obj;
      return peer.getName() == name && peer.getArity() == arity && peer.isQuoted() == isQuoted;
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
    StringBuffer result = new StringBuffer(name.length());

    if (isQuoted) {
      result.append('"');
    }

    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      switch (c) {
        case '\n' :
          result.append('\\');
          result.append('n');
          break;
        case '\t' :
          result.append('\\');
          result.append('t');
          break;
        case '\b' :
          result.append('\\');
          result.append('b');
          break;
        case '\r' :
          result.append('\\');
          result.append('r');
          break;
        case '\f' :
          result.append('\\');
          result.append('f');
          break;
        case '\\' :
          result.append('\\');
          result.append('\\');
          break;
        case '\'' :
          result.append('\\');
          result.append('\'');
          break;
        case '\"' :
          result.append('\\');
          result.append('\"');
          break;

        case '!' :
        case '@' :
        case '#' :
        case '$' :
        case '%' :
        case '^' :
        case '&' :
        case '*' :
        case '(' :
        case ')' :
        case '-' :
        case '_' :
        case '+' :
        case '=' :
        case '|' :
        case '~' :
        case '{' :
        case '}' :
        case '[' :
        case ']' :
        case ';' :
        case ':' :
        case '<' :
        case '>' :
        case ',' :
        case '.' :
        case '?' :
        case ' ' :
        case '/' :
          result.append(c);
          break;

        default :
          if (Character.isLetterOrDigit(c)) {
            result.append(c);
          } else {
            result.append('\\');
            result.append((char) ((int) '0' + (int) c / 64));
            c = (char) (c % 64);
            result.append((char) ((int) '0' + (int) c / 8));
            c = (char) (c % 8);
            result.append((char) ((int) '0' + (int) c));
          }
      }
    }

    if (isQuoted) {
      result.append('"');
    }

    return result.toString();
  }

  public void accept(ATermVisitor v) throws ATermVisitFailure {
    v.visitAFun(this);
  }

}
