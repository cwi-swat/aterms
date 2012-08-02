using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for AnnosTest.
	/// </summary>
	public class AnnosTest : TestCase
	{
		private ATermFactory factory;
		public AnnosTest(string name) : base(name)
		{
		}
		public virtual void TestAnnos() 
		{
			factory = Tester.theFactory;
			ATerm t, key, value;

			t = factory.parse("f");
			for (int i = 0; i < 5; i++) 
			{
				key = factory.parse("key" + i);
				value = factory.parse("value" + i);
				t = t.setAnnotation(key, value);
			}

			key = factory.parse("key3");
			value = factory.parse("value3");
			AssertTrue(t.getAnnotation(key).Equals(value));
			t = t.removeAnnotation(key);
			AssertTrue(t.getAnnotation(key) == null);
		}

	}
}
