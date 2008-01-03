using System;
using System.Collections;
using System.IO;
using System.Text;
using JJTraveler;

namespace aterm
{
	/// <summary>
	/// Summary description for ATermWriter.
	/// </summary>
	public class ATermWriter : ATermFwd
	{
		private static char[] TOBASE64 = {
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

		internal StreamWriter stream;
		internal int position;
		internal Hashtable table;
		internal int next_abbrev;

		public ATermWriter(Stream stream) : base(new Identity())
		{
			this.stream = new StreamWriter(stream);
		}

		public virtual Stream getStream() 
		{
			stream.Flush();
			return stream.BaseStream;
		}

		private void emitAbbrev(int abbrev) 
		{
			stream.Write('#');
			position++;

			StringBuilder buf = new StringBuilder();

			if (abbrev == 0) 
			{
				buf.Append(TOBASE64[0]);
			}

			while (abbrev > 0) 
			{
				buf.Append(TOBASE64[abbrev % 64]);
				abbrev /= 64;
			}

			// Reverse buffer
			string txt = buf.ToString();
			for(int i = 0; i < txt.Length; i++)
			{
				buf[txt.Length - 1 - i] = txt[i];
			}
			//In Java:	string txt = buf.reverse().ToString();
			//	stream.print(txt);
			stream.Write(buf.ToString());
			position += txt.Length;
		}

		public virtual void visitChild(ATerm child) // throws  VisitFailure 
		{
			if (table != null) 
			{
				int abbrev;
				if (table.Contains(child))
				{
					abbrev =  (int)table[child];
					emitAbbrev(abbrev);
					return;
				}
			}

			int start = position;
			if (child.getType() == ATermType.LIST) 
			{
				stream.Write('[');
				position++;
			}
			visit(child);
			if (child.getType() == ATermType.LIST) 
			{
				stream.Write(']');
				position++;
			}

			ATermList annos = child.getAnnotations();
			if (!annos.isEmpty()) 
			{
				stream.Write('{');
				position++;
				visit(annos);
				stream.Write('}');
				position++;
			}

			if (table != null) 
			{
				int length = position - start;
				if (length > PureFactory.abbrevSize(next_abbrev)) 
				{

					int key = next_abbrev++; // Or should this be ++next_abbrev?? (merijn)
					table.Add(key,child);
				}
			}
		}

		public override void visitAppl(ATermAppl appl) // throws VisitFailure 
		{
			AFun fun = appl.getAFun();
			string name = fun.ToString();
			stream.Write(name);
			position += name.Length;
			if (fun.getArity() > 0 || name.Equals("")) 
			{
				stream.Write('(');
				position++;
				for (int i = 0; i < fun.getArity(); i++) 
				{
					if (i != 0) 
					{
						stream.Write(',');
						position++;
					}
					visitChild(appl.getArgument(i));
				}
				stream.Write(')');
				position++;
			}
		}

		public override void visitList(ATermList list) // throws VisitFailure 
		{
			while (!list.isEmpty()) 
			{
				visitChild(list.getFirst());
				list = list.getNext();
				if (!list.isEmpty()) 
				{
					stream.Write(',');
					position++;
				}
			}
		}

		public override void visitPlaceholder(ATermPlaceholder ph) // throws VisitFailure 
		{
			stream.Write('<');
			position++;
			visitChild(ph.getPlaceholder());
			stream.Write('>');
			position++;
		}

		public override void visitInt(ATermInt i) // throws VisitFailure 
		{
			string txt = i.getInt().ToString();
			stream.Write(txt);
			position += txt.Length;
		}

		public override void visitReal(ATermReal r) // throws VisitFailure 
		{
			string txt = r.getReal().ToString();
			stream.Write(txt);
			position += txt.Length;
		}

		public override void visitBlob(ATermBlob blob) // throws VisitFailure 
		{
			string txt = blob.getBlobSize().ToString() + "#" + blob.GetHashCode().ToString();
			stream.Write(txt);
			position += txt.Length;
		}

		public virtual void initializeSharing() 
		{
			table = new Hashtable();
		}
	}
}
