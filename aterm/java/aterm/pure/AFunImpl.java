package aterm.pure;

import aterm.*;

class AFunImpl
  implements AFun
{
  String  name;
  int     arity;
  boolean isQuoted;

  //{{{ static int hashFunction(String name, int arity, boolean isQuoted)

  static int hashFunction(String name, int arity, boolean isQuoted)
  {
    return Math.abs(name.hashCode() ^ arity ^ (isQuoted ? 8 : 7));
  }

  //}}}
  
  //{{{ protected AFunImpl(String name, int arity, boolean isQuoted)

  protected AFunImpl(String name, int arity, boolean isQuoted)
  {
    this.name     = name;
    this.arity    = arity;
    this.isQuoted = isQuoted;
  }

  //}}}
  //{{{ public String getName()

  public String getName()
  {
    return name;
  }

  //}}}
  //{{{ public int getArity()

  public int getArity()
  {
    return arity;
  }

  //}}}
  //{{{ public boolean isQuoted()

  public boolean isQuoted()
  {
    return isQuoted;
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    StringBuffer result = new StringBuffer(name.length());

    if (isQuoted) {
      result.append('"');
    }

    for(int i=0; i<name.length(); i++) {
      char c = name.charAt(i);
      switch(c) {
        case '\n':	result.append('\\');	result.append('n');	break;
        case '\t':	result.append('\\');	result.append('t');	break;
        case '\b':	result.append('\\');	result.append('b');	break;
        case '\r':	result.append('\\');	result.append('r');	break;
        case '\f':	result.append('\\');	result.append('f');	break;
        case '\\':	result.append('\\');	result.append('\\');	break;
        case '\'':	result.append('\\');	result.append('\'');	break;
        case '\"':	result.append('\\');	result.append('\"');	break;

        case '!':	case '@':	case '#':	case '$':
        case '%':	case '^':	case '&':	case '*':
        case '(':	case ')':	case '-':	case '_':
        case '+':	case '=':	case '|':	case '~':
        case '{':	case '}':	case '[':	case ']':
        case ';':	case ':':	case '<':	case '>':
        case ',':	case '.':	case '?':       case ' ':
        case '/':
	  result.append(c);
	  break;

        default:
	  if(Character.isLetterOrDigit(c)) {
	    result.append(c);
	  } else {
	    result.append('\\');
	    result.append((char)((int)'0' + (int)c/64));
	    c = (char)(c % 64);
	    result.append((char)((int)'0' + (int)c/8));
	    c = (char)(c % 8);
	    result.append((char)((int)'0' + (int)c));
	  }
      }
    }

    if (isQuoted) {
      result.append('"');
    }

    return result.toString();
  }

  //}}}
}
