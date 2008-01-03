using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for DictTest.
	/// </summary>
	public class DictTest : TestCase
	{
		private ATermFactory factory;
		public DictTest(string name) : base(name)
		{
		}
		public virtual void TestDict() 
		{
			factory = Tester.theFactory;
			ATermList dict = factory.makeList();
			ATerm key, value;

			for (int i = 0; i < 5; i++) 
			{
				key = factory.parse("key" + i);
				value = factory.parse("value" + i);
				dict = dict.dictPut(key, value);
			}

			key = factory.parse("key3");
			value = factory.parse("value3");
			AssertTrue(dict.dictGet(key).Equals(value));
		}
	}
}
