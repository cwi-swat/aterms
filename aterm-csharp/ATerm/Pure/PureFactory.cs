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

using System;
using System.Collections;
using System.Text;
using System.IO;
using SharedObjects;

namespace aterm
{
	/// <summary>
	/// Summary description for PureFactory.
	/// </summary>
	public class PureFactory : SharedObjectFactory, ATermFactory
	{
		private static int DEFAULT_TERM_TABLE_SIZE = 16; // means 2^16 entries

		private ATermListImpl protoList;
		private ATermApplImpl protoAppl;
		private ATermIntImpl protoInt;
		private ATermRealImpl protoReal;
		private ATermBlobImpl protoBlob;
		private ATermPlaceholderImpl protoPlaceholder;
		private AFunImpl protoAFun;

		private ATermList empty;

		static bool isBase64(int c) 
		{
			return Char.IsLetterOrDigit((char) c) || c == '+' || c == '/';
		}

		static public int abbrevSize(int abbrev) 
		{
			int size = 1;

			if (abbrev == 0) 
			{
				return 2;
			}

			while (abbrev > 0) 
			{
				size++;
				abbrev /= 64;
			}

			return size;
		}

		public PureFactory() : this(DEFAULT_TERM_TABLE_SIZE)
		{
		}

		public PureFactory(int termTableSize) : base(termTableSize)
		{
			protoList = new ATermListImpl(this);
			protoAppl = new ATermApplImpl(this);
			protoInt = new ATermIntImpl(this);
			protoReal = new ATermRealImpl(this);
			protoBlob = new ATermBlobImpl(this);
			protoPlaceholder = new ATermPlaceholderImpl(this);
			protoAFun = new AFunImpl(this);

			protoList.init(42, null, null, null);
			empty = (ATermList) build(protoList);
			((ATermListImpl) empty).init(42, empty, null, null);
		}

		public virtual ATermInt makeInt(int val) 
		{
			return makeInt(val, empty);
		}

		public virtual ATermReal makeReal(double val) 
		{
			return makeReal(val, empty);
		}

		public virtual ATermList makeList() 
		{
			return empty;
		}

		public virtual ATermList makeList(ATerm singleton) 
		{
			return makeList(singleton, empty, empty);
		}

		public virtual ATermList makeList(ATerm first, ATermList next) 
		{
			return makeList(first, next, empty);
		}

		public virtual ATermPlaceholder makePlaceholder(ATerm type) 
		{
			return makePlaceholder(type, empty);
		}

		public virtual ATermBlob makeBlob(byte[] data) 
		{
			return makeBlob(data, empty);
		}

		public virtual AFun makeAFun(string name, int arity, bool isQuoted) 
		{
			lock (protoAFun) 
			{
				protoAFun.initHashCode(name, arity, isQuoted);
				return (AFun) build(protoAFun);
			}
		}

		public virtual ATermInt makeInt(int value, ATermList annos) 
		{
			lock (protoInt) 
			{
				protoInt.initHashCode(annos, value);
				return (ATermInt) build(protoInt);
			}
		}

		public virtual ATermReal makeReal(double value, ATermList annos) 
		{
			lock (protoReal) 
			{
				protoReal.init(hashReal(annos, value), annos, value);
				return (ATermReal) build(protoReal);
			}
		}

		static private int hashReal(ATermList annos, double value) 
		{
			return SharedObjects.HashFunctions.doobs(new Object[] { annos, value});
		}

		public virtual ATermPlaceholder makePlaceholder(ATerm type, ATermList annos) 
		{
			lock (protoPlaceholder) 
			{
				protoPlaceholder.init(hashPlaceholder(annos, type), annos, type);
				return (ATermPlaceholder) build(protoPlaceholder);
			}
		}

		static private int hashPlaceholder(ATermList annos, ATerm type) 
		{
			return SharedObjects.HashFunctions.doobs(new Object[] { annos, type });
		}

		public virtual ATermBlob makeBlob(byte[] data, ATermList annos) 
		{
			lock (protoBlob) 
			{
				protoBlob.init(hashBlob(annos, data), annos, data);
				return (ATermBlob) build(protoBlob);
			}
		}

		static private int hashBlob(ATermList annos, byte[] data) 
		{
			return SharedObjects.HashFunctions.doobs(new Object[] { annos, data });
		}

		public virtual ATermList makeList(ATerm first, ATermList next, ATermList annos) 
		{
			lock (protoList) 
			{
				protoList.initHashCode(annos, first, next);
				return (ATermList) build(protoList);
			}
		}

		private static ATerm[] array0 = new ATerm[0];

		public virtual ATermAppl makeAppl(AFun fun, ATerm[] args) 
		{
			return makeAppl(fun, args, empty);
		}

		public virtual ATermAppl makeAppl(AFun fun, ATerm[] args, ATermList annos) 
		{
			lock (protoAppl) 
			{
				protoAppl.initHashCode(annos, fun, args);
				return (ATermAppl) build(protoAppl);
			}
		}

		public virtual ATermAppl makeApplList(AFun fun, ATermList list) 
		{
			return makeApplList(fun, list, empty);
		}

		public virtual ATermAppl makeApplList(AFun fun, ATermList list, ATermList annos) 
		{
			ATerm[] arg_array;

			arg_array = new ATerm[list.getLength()];

			int i = 0;
			while (!list.isEmpty()) 
			{
				arg_array[i++] = list.getFirst();
				list = list.getNext();
			}
			return makeAppl(fun, arg_array, annos);
		}

		public virtual ATermAppl makeAppl(AFun fun) 
		{
			return makeAppl(fun, array0);
		}

		public virtual ATermAppl makeAppl(AFun fun, ATerm arg) 
		{
			ATerm[] argarray1 = new ATerm[] { arg };
			return makeAppl(fun, argarray1);
		}

		public virtual ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2) 
		{
			ATerm[] argarray2 = new ATerm[] { arg1, arg2 };
			return makeAppl(fun, argarray2);
		}

		public virtual ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3) 
		{
			ATerm[] argarray3 = new ATerm[] { arg1, arg2, arg3 };
			return makeAppl(fun, argarray3);
		}

		public virtual ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4) 
		{
			ATerm[] argarray4 = new ATerm[] { arg1, arg2, arg3, arg4 };
			return makeAppl(fun, argarray4);
		}

		public virtual ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4, ATerm arg5) 
		{
			ATerm[] argarray5 = new ATerm[] { arg1, arg2, arg3, arg4, arg5 };
			return makeAppl(fun, argarray5);
		}

		public virtual ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4, ATerm arg5, ATerm arg6) 
		{
			ATerm[] args = { arg1, arg2, arg3, arg4, arg5, arg6 };
			return makeAppl(fun, args);
		}

		public virtual ATermAppl makeAppl(
			AFun fun,
			ATerm arg1,
			ATerm arg2,
			ATerm arg3,
			ATerm arg4,
			ATerm arg5,
			ATerm arg6,
			ATerm arg7) 
		{
			ATerm[] args = { arg1, arg2, arg3, arg4, arg5, arg6, arg7 };
			return makeAppl(fun, args);
		}

		public virtual ATermList getEmpty() 
		{
			return empty;
		}

		private ATerm parseAbbrev(ATermReader reader) // throws IOException 
		{
			ATerm result;
			int abbrev;

			int c = reader.read();

			abbrev = 0;
			while (isBase64(c)) 
			{
				abbrev *= 64;
				if (c >= 'A' && c <= 'Z') 
				{
					abbrev += c - 'A';
				}
				else if (c >= 'a' && c <= 'z') 
				{
					abbrev += c - 'a' + 26;
				}
				else if (c >= '0' && c <= '9') 
				{
					abbrev += c - '0' + 52;
				}
				else if (c == '+') 
				{
					abbrev += 62;
				}
				else if (c == '/') 
				{
					abbrev += 63;
				}
				else 
				{
					throw new Exception("not a base-64 digit: " + c);
				}

				c = reader.read();
			}

			result = reader.getTerm(abbrev);

			return result;
		}

		private ATerm parseNumber(ATermReader reader) // throws IOException 
		{
			StringBuilder str = new StringBuilder();
			ATerm result;

			do 
			{
				str.Append((char) reader.getLastChar());
			}
			while (Char.IsDigit((char) reader.read()));

			if (reader.getLastChar() != '.' && reader.getLastChar() != 'e' && reader.getLastChar() != 'E') 
			{
				int val;
				try 
				{
					val = int.Parse(str.ToString());
				}
				catch (FormatException) 
				{
					throw new ParseError("malformed int");
				}
				result = makeInt(val);
			}
			else 
			{
				if (reader.getLastChar() == '.') 
				{
					str.Append('.');
					reader.read();
					if (!Char.IsDigit((char) reader.getLastChar()))
						throw new ParseError("digit expected");
					do 
					{
						str.Append((char) reader.getLastChar());
					}
					while (Char.IsDigit((char) reader.read()));
				}
				if (reader.getLastChar() == 'e' || reader.getLastChar() == 'E') 
				{
					str.Append((char) reader.getLastChar());
					reader.read();
					if (reader.getLastChar() == '-' || reader.getLastChar() == '+') 
					{
						str.Append((char) reader.getLastChar());
						reader.read();
					}
					if (!Char.IsDigit((char) reader.getLastChar()))
						throw new ParseError("digit expected!");
					do 
					{
						str.Append((char) reader.getLastChar());
					}
					while (Char.IsDigit((char) reader.read()));
				}
				double val;
				try 
				{
					val = double.Parse(str.ToString());
					//					Double.valueOf(str.ToString()).doubleValue();
				}
				catch (FormatException) 
				{
					throw new ParseError("malformed real");
				}
				result = makeReal(val);
			}
			return result;
		}

		private string parseId(ATermReader reader) // throws IOException 
		{
			int c = reader.getLastChar();
			StringBuilder buf = new StringBuilder(32);

			do 
			{
				buf.Append((char) c);
				c = reader.read();
			}
			while (Char.IsLetterOrDigit((char) c) || c == '_' || c == '-');

			return buf.ToString();
		}

		private string parseString(ATermReader reader) // throws IOException 
		{
			bool escaped;
			StringBuilder str = new StringBuilder();

			do 
			{
				escaped = false;
				if (reader.read() == '\\') 
				{
					reader.read();
					escaped = true;
				}

				if (escaped) 
				{
					switch (reader.getLastChar()) 
					{
						case 'n' :
							str.Append('\n');
							break;
						case 't' :
							str.Append('\t');
							break;
						case 'b' :
							str.Append('\b');
							break;
						case 'r' :
							str.Append('\r');
							break;
						case 'f' :
							str.Append('\f');
							break;
						case '\\' :
							str.Append('\\');
							break;
						case '\'' :
							str.Append('\'');
							break;
						case '\"' :
							str.Append('\"');
							break;
						case '0' : goto case '7';
						case '1' : goto case '7';
						case '2' : goto case '7';
						case '3' : goto case '7';
						case '4' : goto case '7';
						case '5' : goto case '7';
						case '6' : goto case '7';
						case '7' :
							str.Append(reader.readOct());
							break;
						default :
							str.Append('\\').Append((char) reader.getLastChar());
							break;
					}
				}
				else if (reader.getLastChar() != '\"')
					str.Append((char) reader.getLastChar());
			}
			while (escaped || reader.getLastChar() != '"');

			return str.ToString();
		}

		private ATermList parseATerms(ATermReader reader) // throws IOException 
		{
			ATerm[] terms = parseATermsArray(reader);
			ATermList result = empty;
			for (int i = terms.Length - 1; i >= 0; i--) 
			{
				result = makeList(terms[i], result);
			}
			return result;
		}

		private ATerm[] parseATermsArray(ATermReader reader) // throws IOException 
		{
			ArrayList list = new ArrayList();
			ATerm term;

			term = parseFromReader(reader);
			list.Add(term);
			while (reader.getLastChar() == ',') 
			{
				reader.readSkippingWS();
				term = parseFromReader(reader);
				list.Add(term);
			}

			ATerm[] array = new ATerm[list.Count];
			int index = 0;
			foreach(Object o in list)
			{
				array[index++] = (ATerm)o;
			}
/*
 * 			ListIterator iter = list.listIterator();
			int index = 0;
			while (iter.hasNext()) 
			{
				array[index++] = (ATerm) iter.next();
			}
*/
			return array;
		}

		/* lock  */
		private ATerm parseFromReader(ATermReader reader) // throws IOException 
		{
			ATerm result;
			int c, start, end;
			string funname;

			start = reader.getPosition();
			switch (reader.getLastChar()) 
			{
				case -1 :
					throw new ParseError("premature EOF encountered.");
					
				case '#' :
					return parseAbbrev(reader);

				case '[' :

					c = reader.readSkippingWS();
					if (c == -1) 
					{
						throw new ParseError("premature EOF encountered.");
					}

					if (c == ']') 
					{
						c = reader.readSkippingWS();
						result = (ATerm) empty;
					}
					else 
					{
						result = parseATerms(reader);
						if (reader.getLastChar() != ']') 
						{
							throw new ParseError("expected ']' but got '" + (char) reader.getLastChar() + "'");
						}
						c = reader.readSkippingWS();
					}

					break;

				case '<' :

					c = reader.readSkippingWS();
					ATerm ph = parseFromReader(reader);

					if (reader.getLastChar() != '>') 
					{
						throw new ParseError("expected '>' but got '" + (char) reader.getLastChar() + "'");
					}

					c = reader.readSkippingWS();

					result = makePlaceholder(ph);

					break;

				case '"' :

					funname = parseString(reader);

					c = reader.readSkippingWS();
					if (reader.getLastChar() == '(') 
					{
						c = reader.readSkippingWS();
						if (c == -1) 
						{
							throw new ParseError("premature EOF encountered.");
						}
						if (reader.getLastChar() == ')') 
						{
							result = makeAppl(makeAFun(funname, 0, true));
						}
						else 
						{
							ATerm[] list = parseATermsArray(reader);

							if (reader.getLastChar() != ')') 
							{
								throw new ParseError("expected ')' but got '" + reader.getLastChar() + "'");
							}
							result = makeAppl(makeAFun(funname, list.Length, true), list);
						}
						c = reader.readSkippingWS();
						if (c == -1) 
						{
							throw new ParseError("premature EOF encountered.");
						}
					}
					else 
					{
						result = makeAppl(makeAFun(funname, 0, true));
					}

					break;

				case '(' :

					c = reader.readSkippingWS();
					if (c == -1) 
					{
						throw new ParseError("premature EOF encountered.");
					}
					if (reader.getLastChar() == ')') 
					{
						result = makeAppl(makeAFun("", 0, false));
					}
					else 
					{
						ATerm[] list = parseATermsArray(reader);

						if (reader.getLastChar() != ')') 
						{
							throw new ParseError("expected ')' but got '" + reader.getLastChar() + "'");
						}
						result = makeAppl(makeAFun("", list.Length, false), list);
					}
					c = reader.readSkippingWS();
					if (c == -1) 
					{
						throw new ParseError("premature EOF encountered.");
					}

					break;

				case '-' : goto case '9';
				case '0' : goto case '9';
				case '1' : goto case '9';
				case '2' : goto case '9';
				case '3' : goto case '9';
				case '4' : goto case '9';
				case '5' : goto case '9';
				case '6' : goto case '9';
				case '7' : goto case '9';
				case '8' : goto case '9';
				case '9' :
					result = parseNumber(reader);
					c = reader.skipWS();
					break;

				default :
					c = reader.getLastChar();
					if (Char.IsLetter((char) c)) 
					{

						funname = parseId(reader);
						c = reader.skipWS();
						if (reader.getLastChar() == '(') 
						{
							c = reader.readSkippingWS();
							if (c == -1) 
							{
								throw new ParseError("premature EOF encountered.");
							}
							if (reader.getLastChar() == ')') 
							{
								result = makeAppl(makeAFun(funname, 0, false));
							}
							else 
							{
								ATerm[] list = parseATermsArray(reader);

								if (reader.getLastChar() != ')') 
								{
									throw new ParseError("expected ')' but got '" + reader.getLastChar() + "'");
								}
								result = makeAppl(makeAFun(funname, list.Length, false), list);
							}
							c = reader.readSkippingWS();
						}
						else 
						{
							result = makeAppl(makeAFun(funname, 0, false));
						}

					}
					else 
					{
						throw new ParseError("illegal character: " + reader.getLastChar());
					}
					break;
			}

			if (reader.getLastChar() == '{') 
			{

				ATermList annos;
				if (reader.readSkippingWS() == '}') 
				{
					reader.readSkippingWS();
					annos = empty;
				}
				else 
				{
					annos = parseATerms(reader);
					if (reader.getLastChar() != '}') 
					{
						throw new ParseError("'}' expected");
					}
					reader.readSkippingWS();
				}
				result = result.setAnnotations(annos);

			}

			/* Parse some ToolBus anomalies for backwards compatibility */
			if (reader.getLastChar() == ':') 
			{
				reader.read();
				ATerm anno = parseFromReader(reader);
				result = result.setAnnotation(parse("type"), anno);
			}

			if (reader.getLastChar() == '?') 
			{
				reader.readSkippingWS();
				result = result.setAnnotation(parse("result"), parse("true"));
			}

			end = reader.getPosition();
			reader.storeNextTerm(result, end - start);

			return result;
		}

		public virtual ATerm parse(string trm) 
		{
			try 
			{
				ATermReader reader = new ATermReader(new StringReader(trm));
				reader.readSkippingWS();
				ATerm result = parseFromReader(reader);
				return result;
			}
			catch (IOException) 
			{
				throw new ParseError("premature end of string");
			}
		}

		public virtual ATerm make(string trm) 
		{
			return parse(trm);
		}

		public virtual ATerm make(string pattern, ArrayList args) 
		{
			return make(parse(pattern), args);
		}

		public virtual ATerm make(string pattern, Object arg1) 
		{
			ArrayList args = new ArrayList();
			args.Add(arg1);
			return make(pattern, args);
		}

		public virtual ATerm make(string pattern, Object arg1, Object arg2) 
		{
			ArrayList args = new ArrayList();
			args.Add(arg1);
			args.Add(arg2);
			return make(pattern, args);
		}

		public virtual ATerm make(string pattern, Object arg1, Object arg2, Object arg3) 
		{
			ArrayList args = new ArrayList();
			args.Add(arg1);
			args.Add(arg2);
			args.Add(arg3);
			return make(pattern, args);
		}

		public virtual ATerm make(string pattern, Object arg1, Object arg2, Object arg3, Object arg4) 
		{
			ArrayList args = new ArrayList();
			args.Add(arg1);
			args.Add(arg2);
			args.Add(arg3);
			args.Add(arg4);
			return make(pattern, args);
		}

		public virtual ATerm make(string pattern, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) 
		{
			ArrayList args = new ArrayList();
			args.Add(arg1);
			args.Add(arg2);
			args.Add(arg3);
			args.Add(arg4);
			args.Add(arg5);
			return make(pattern, args);
		}

		public virtual ATerm make(string pattern, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) 
		{
			ArrayList args = new ArrayList();
			args.Add(arg1);
			args.Add(arg2);
			args.Add(arg3);
			args.Add(arg4);
			args.Add(arg5);
			args.Add(arg6);
			return make(pattern, args);
		}

		public virtual ATerm make(
			string pattern,
			Object arg1,
			Object arg2,
			Object arg3,
			Object arg4,
			Object arg5,
			Object arg6,
			Object arg7) 
		{
			ArrayList args = new ArrayList();
			args.Add(arg1);
			args.Add(arg2);
			args.Add(arg3);
			args.Add(arg4);
			args.Add(arg5);
			args.Add(arg6);
			args.Add(arg7);
			return make(pattern, args);
		}

		public virtual ATerm make(ATerm pattern, ArrayList args) 
		{
			return pattern.make(args);
		}

		public virtual ATerm parsePattern(string pattern) // throws ParseError 
		{
			return parse(pattern);
		}

		public virtual bool isDeepEqual(ATermImpl t1, ATerm t2) 
		{
			throw new MissingMemberException("not yet implemented!");
		}

		private ATerm readFromSharedTextFile(ATermReader reader) // throws IOException 
		{
			reader.initializeSharing();
			return parseFromReader(reader);
		}

		private ATerm readFromTextFile(ATermReader reader) // throws IOException 
		{
			return parseFromReader(reader);
		}

		public virtual ATerm readFromTextFile(Stream stream)  //throws IOException 
		{
			ATermReader reader = new ATermReader(new 
				StreamReader(stream));
			reader.readSkippingWS();

			return readFromTextFile(reader);
		}

		public virtual ATerm readFromSharedTextFile(Stream stream) // throws IOException 
		{
			ATermReader reader = new ATermReader(new StreamReader(stream));
			reader.readSkippingWS();

			if (reader.getLastChar() != '!') 
			{
				throw new IOException("not a shared text file!");
			}

			reader.readSkippingWS();

			return readFromSharedTextFile(reader);
		}

		public virtual ATerm readFromBinaryFile(Stream stream) 
		{
			throw new Exception("not yet implemented!");
		}

		public virtual ATerm readFromFile(Stream stream) // throws IOException 
		{
			ATermReader reader = new ATermReader(new StreamReader(stream));
			reader.readSkippingWS();

			int last_char = reader.getLastChar();
			if (last_char == '!') 
			{
				reader.readSkippingWS();
				return readFromSharedTextFile(reader);
			}
			else if (
				Char.IsLetterOrDigit((char) last_char) || last_char == '_' || last_char == '[' || last_char == '-') 
			{
				return readFromTextFile(reader);
			}
			else 
			{
				throw new Exception("BAF files are not supported by this factory.");
			}
		}

		public virtual ATerm readFromFile(string file) // throws IOException 
		{
			return readFromFile(new FileStream(file,FileMode.Open));
		}

		public virtual ATerm importTerm(ATerm term) 
		{
			throw new Exception("not yet implemented!");
		}
	}

	class HashedWeakRef : WeakReference 
	{
		internal HashedWeakRef next;

		public HashedWeakRef(Object obj, HashedWeakRef next) : base(obj)
		{
			this.next = next;
		}
	}

	class ATermReader 
	{
		private const int INITIAL_TABLE_SIZE = 2048;
		private const int TABLE_INCREMENT = 4096;
		private TextReader reader;
		private int last_char;
		private int pos;

		private int nr_terms;
		private ATerm[] table;

		public ATermReader(TextReader reader) 
		{
			this.reader = reader;
			last_char = -1;
			pos = 0;
		}

		public virtual void initializeSharing() 
		{
			table = new ATerm[INITIAL_TABLE_SIZE];
			nr_terms = 0;
		}

		public virtual void storeNextTerm(ATerm t, int size) 
		{
			if (table == null) 
			{
				return;
			}

			if (size <= PureFactory.abbrevSize(nr_terms)) 
			{
				return;
			}

			if (nr_terms == table.Length) 
			{
				ATerm[] new_table = new ATerm[table.Length + TABLE_INCREMENT];
				Array.Copy(table, 0, new_table, 0, table.Length);
				table = new_table;
			}

			table[nr_terms++] = t;
		}

		public virtual ATerm getTerm(int index) 
		{
			if (index < 0 || index >= nr_terms) 
			{
				throw new Exception("illegal index");
			}
			return table[index];
		}

		public virtual int read() // throws IOException 
		{
			last_char = reader.Read();
			pos++;
			return last_char;
		}

		public virtual int readSkippingWS() // throws IOException 
		{
			do 
			{
				last_char = reader.Read();
				pos++;
			}
			while (Char.IsWhiteSpace((char) last_char));

			return last_char;

		}

		public virtual int skipWS() // throws IOException 
		{
			while (Char.IsWhiteSpace((char) last_char)) 
			{
				last_char = reader.Read();
				pos++;
			}

			return last_char;
		}

		public virtual int readOct() // throws IOException 
		{
			int val = Convert.ToInt32(Convert.ToString(last_char), 8);
			val += Convert.ToInt32(Convert.ToString((char)read()), 8 );
			//		int val = Char.digit((char) last_char, 8);
			//		val += Character.digit((char) read(), 8);

			if (val < 0) 
			{
				throw new ParseError("octal must have 3 octdigits.");
			}

			val += Convert.ToInt32(Convert.ToString((char)read()), 8 );
			//		val += Character.digit((char) read(), 8);

			if (val < 0) 
			{
				throw new ParseError("octal must have 3 octdigits");
			}

			return val;
		}

		public virtual int getLastChar() 
		{
			return last_char;
		}

		public virtual int getPosition() 
		{
			return pos;
		}
	}
}

