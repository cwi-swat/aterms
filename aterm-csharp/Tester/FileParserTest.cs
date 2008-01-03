using System;
using System.Collections;
using System.IO;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for FileParserTest.
	/// </summary>
	public class FileParserTest : TestCase
	{
		private ATermFactory factory;
		public FileParserTest(string name) : base(name)
		{
		}

		public virtual void TestFileParser() 
		{
			factory = Tester.theFactory;
//			try 
//			{
				
			  StreamWriter output = new StreamWriter("testFileParser.txt");
			  string s = factory.parse("f(a,g(b))").ToString();
			  output.Write(s);
			  output.Close();
				
				FileStream input = new FileStream("testFileParser.txt", FileMode.Open);
				ATerm result = factory.readFromTextFile(input);
				input.Close();
				AssertTrue(result.ToString() == "f(a,g(b))");
      
//				Console.Out.WriteLine("result = " + result);
//			} 
//			catch (FileNotFoundException e1) 
//			{
//				Console.Out.WriteLine(e1);
//			} 
//			catch (IOException e2) 
//			{
//				Console.Out.WriteLine(e2);
//			}
		}
	}
}
