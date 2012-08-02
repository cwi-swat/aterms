using System;
using System.Collections;
using dotUnit.Framework;
using aterm;


namespace ATermTests
{
	/// <summary>
	/// Summary description for ParserTest.
	/// </summary>
	public class ParserTest : TestCase
	{
		private ATermFactory factory;

		public ParserTest(string name) : base(name)
		{
		}
		public virtual void TestParser() 
		{
			factory = Tester.theFactory;

			ATerm[] T = new ATerm[20];
			int index = 0;
    
			T[index++] = factory.parse("g");
			T[index++] = factory.parse("f()");
			T[index++] = factory.parse("f(1)");
			T[index++] = factory.parse("f(1,2)");
			T[index++] = factory.parse("f(1,2,<int>)");
			T[index++] = factory.parse("[]");
			T[index++] = factory.parse("[1]");
			T[index++] = factory.parse("[1,2]");
			T[index++] = factory.parse("<x>");
			T[index++] = factory.parse("3.14");
			T[index++] = factory.parse("[1,3.5,4e6,123.21E-3,-12]");
			T[index++] = factory.parse("f(\"x y z\"(),<abc(31)>,[])");
			T[index++] = factory.parse("home([<name(\"\",string)>,<phone(\"\",PhoneNumber)>])");
			T[index++] = factory.parse("[ a , b]");
			T[index++] = factory.parse("f(a){[x,y],[1,2]}");
			T[index++] = factory.parse("[(),(a)]");
			T[index++] = factory.parse("[1,a,f(1)]");
			T[index++] = factory.parse("[1,\"a\",f(1), \"g\"(a,\"b\")]");

			index = 0;
			AssertTrue(T[index++].ToString() == "g");
 			AssertTrue(T[index++].ToString() == "f");
			AssertTrue(T[index++].ToString() == "f(1)");
			AssertTrue(T[index++].ToString() == "f(1,2)");
			AssertTrue(T[index++].ToString() == "f(1,2,<int>)");
			AssertTrue(T[index++].ToString() == "[]");
			AssertTrue(T[index++].ToString() == "[1]");
			AssertTrue(T[index++].ToString() == "[1,2]");
			AssertTrue(T[index++].ToString() == "<x>");
			AssertTrue(T[index++].ToString() == "3.14");
			AssertTrue(T[index++].ToString() == "[1,3.5,4000000,0.12321,-12]");
			AssertTrue(T[index++].ToString() == "f(\"x y z\",<abc(31)>,[])");
			AssertTrue(T[index++].ToString() == "home([<name(\"\",string)>,<phone(\"\",PhoneNumber)>])");
			AssertTrue(T[index++].ToString() == "[a,b]");
			AssertTrue(T[index++].ToString() == "f(a){[x,y],[1,2]}");
			AssertTrue(T[index++].ToString() == "[(),(a)]");
			AssertTrue(T[index++].ToString() == "[1,a,f(1)]");
			AssertTrue(T[index++].ToString() == "[1,\"a\",f(1),\"g\"(a,\"b\")]");

			//			for(int i=0; i<index; i++) 
//			{
//				Console.Out.WriteLine("term " + i + ": " + T[i]);
//			}
    
		}
	}
}
