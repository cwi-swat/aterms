package aterm.pure;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import jjtraveler.VisitFailure;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermBlob;
import aterm.ATermInt;
import aterm.ATermList;
import aterm.ATermPlaceholder;
import aterm.ATermReal;
import aterm.Visitor;

class ATermWriter extends Visitor {

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

  public void visitChild(ATerm child) throws  VisitFailure {
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

  public void visitAppl(ATermAppl appl) throws VisitFailure {
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

  public void visitList(ATermList list) throws VisitFailure {
	while (!list.isEmpty()) {
	  visitChild(list.getFirst());
	  list = list.getNext();
	  if (!list.isEmpty()) {
		stream.print(',');
		position++;
	  }
	}
  }

  public void visitPlaceholder(ATermPlaceholder ph) throws VisitFailure {
	stream.print('<');
	position++;
	visitChild(ph.getPlaceholder());
	stream.print('>');
	position++;
  }

  public void visitInt(ATermInt i) throws VisitFailure {
	String txt = String.valueOf(i.getInt());
	stream.print(txt);
	position += txt.length();
  }

  public void visitReal(ATermReal r) throws VisitFailure {
	String txt = String.valueOf(r.getReal());
	stream.print(txt);
	position += txt.length();
  }

  public void visitBlob(ATermBlob blob) throws VisitFailure {
	String txt = String.valueOf(blob.getBlobSize()) + "#" + String.valueOf(blob.hashCode());
	stream.print(txt);
	position += txt.length();

  }

  public void initializeSharing() {
	table = new HashMap();
  }
  
} 

