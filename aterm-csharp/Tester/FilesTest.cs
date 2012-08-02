using System;
using System.Collections;
using System.IO;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for FilesTest.
	/// </summary>
	public class FilesTest : TestCase
	{
		private ATermFactory factory;
		private const string srcdir = "../..";

		public FilesTest(string name) : base(name)
		{
		}
		public virtual void testFiles()// throws IOException 
		{
			factory = Tester.theFactory;
			ATerm t1 = factory.readFromFile(srcdir + "/test.trm");
//			Console.Out.WriteLine("done reading test.trm");
			ATerm t2 = factory.readFromFile(srcdir + "/test.taf");
//			Console.Out.WriteLine("done reading test.taf");

			StreamWriter stream = new StreamWriter(new FileStream("test.trm2", FileMode.OpenOrCreate));
			t1.writeToTextFile(stream.BaseStream);
			stream.WriteLine();
			stream.Close();
//			Console.Out.WriteLine("done writing test.trm2");

			stream = new StreamWriter(new FileStream("test.taf2", FileMode.OpenOrCreate));
			t1.writeToSharedTextFile(stream.BaseStream);
			stream.Close();
//			Console.Out.WriteLine("done writing test.taf2");

			AssertTrue(t1.Equals(t2));
		}
	}
}
