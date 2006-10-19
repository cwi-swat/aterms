package aterm.pure;

import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import jjtraveler.VisitFailure;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermBlob;
import aterm.ATermFwdVoid;
import aterm.ATermInt;
import aterm.ATermList;
import aterm.ATermPlaceholder;
import aterm.ATermReal;
import aterm.stream.BufferedOutputStreamWriter;

class ATermWriter extends ATermFwdVoid {

  private static char[] TOBASE64 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
    'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
    'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
    'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
    'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', '+', '/' };

  private BufferedOutputStreamWriter stream;

  private int position;

  private Map table;

  private int next_abbrev;

  ATermWriter(OutputStream stream) {
    this.stream = new BufferedOutputStreamWriter(stream);
  }
  
  public Writer getStream() {
    return stream;
  }

  private void emitAbbrev(int abbrev) {
    stream.write('#');
    position++;

    StringBuilder buf = new StringBuilder();

      if (abbrev == 0) {
        buf.append(TOBASE64[0]);
      }

      while (abbrev > 0) {
        buf.append(TOBASE64[abbrev % 64]);
        abbrev /= 64;
      }
      String txt = buf.reverse().toString();
      stream.write(txt);
      position += txt.length();
    }

    public void voidVisitChild(ATerm child) throws VisitFailure {
      if (table != null) {
        Integer abbrev = (Integer) table.get(child);
        if (abbrev != null) {
          emitAbbrev(abbrev.intValue());
          return;
        }
      }

      int start = position;
      if (child.getType() == ATerm.LIST) {
        stream.write('[');
        position++;
      }
      visit(child);
      if (child.getType() == ATerm.LIST) {
        stream.write(']');
        position++;
      }

      ATermList annos = child.getAnnotations();
      if (!annos.isEmpty()) {
        stream.write('{');
        position++;
        visit(annos);
        stream.write('}');
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

    public void voidVisitAppl(ATermAppl appl) throws VisitFailure {
      AFun fun = appl.getAFun();
      String name = fun.toString();
      stream.write(name);
      position += name.length();
      if (fun.getArity() > 0 || name.length() == 0) {
        stream.write('(');
        position++;
        for (int i = 0; i < fun.getArity(); i++) {
          if (i != 0) {
            stream.write(',');
            position++;
          }
          voidVisitChild(appl.getArgument(i));
        }
        stream.write(')');
        position++;
      }
    }

    public void voidVisitList(ATermList list) throws VisitFailure {
      while (!list.isEmpty()) {
        voidVisitChild(list.getFirst());
        list = list.getNext();
        if (!list.isEmpty()) {
          stream.write(',');
          position++;
        }
      }
    }

    public void voidVisitPlaceholder(ATermPlaceholder ph) throws VisitFailure {
      stream.write('<');
      position++;
      voidVisitChild(ph.getPlaceholder());
      stream.write('>');
      position++;
    }

    public void voidVisitInt(ATermInt i) throws VisitFailure {
      String txt = String.valueOf(i.getInt());
      stream.write(txt);
      position += txt.length();
    }

    public void voidVisitReal(ATermReal r) throws VisitFailure {
      String txt = String.valueOf(r.getReal());
      stream.write(txt);
      position += txt.length();
    }

    public void voidVisitBlob(ATermBlob blob) throws VisitFailure {
      String txt = String.valueOf(blob.getBlobSize()) + '#' + String.valueOf(blob.hashCode());
      stream.write(txt);
      position += txt.length();
    }

  public void initializeSharing() {
    table = new HashMap();
  }

}
