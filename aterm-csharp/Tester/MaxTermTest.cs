using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for MaxTermTest.
	/// </summary>
	public class MaxTermTest : TestCase
	{
		private ATermFactory factory;
		public MaxTermTest(string name) : base(name)
		{
		}
		public virtual void testMaxTerm() 
		{
			factory = Tester.theFactory;
			AFun f = factory.makeAFun("f", 1, false);
			AFun a = factory.makeAFun("a", 0, false);

			int size = 500;
			ATerm[] array1 = new ATerm[size];
			ATerm[] array2 = new ATerm[size];

			long start   = DateTime.Now.Ticks;
//			Console.Out.WriteLine("array1");
			for(int i=0 ; i<size ; i++) 
			{
//				if(i%100 == 0) 
//				{
//					Console.Out.Write(i + "  ");
//				}

				int idx = i%10;
				array1[idx] = factory.makeAppl(a);
				for(int j=0 ; j<2*i ; j++) 
				{
					array1[idx] = factory.makeAppl(f,array1[idx]);
				}
				//Console.Out.WriteLine("array[" + i + "] = " + array[i]);
			}

//			Console.Out.WriteLine("\narray2");
			for(int i=0 ; i<size ; i++) 
			{
//				if(i%100 == 0) 
//				{
//					Console.Out.Write(i + "  ");
//				}

				int idx = i%10;
				array2[idx] = factory.makeAppl(a);
				for(int j=0 ; j<2*i ; j++) 
				{
					array2[idx] = factory.makeAppl(f,array2[idx]);
				}
				//Console.Out.WriteLine("array[" + i + "] = " + array[i]);
			}

//			Console.Out.WriteLine("\ntest");
			for(int i=0 ; i<size ; i++) 
			{
				if(i%500 == 0) 
				{
//					Console.Out.Write(i + "  ");
				}

				int idx = i%10;
				if(array1[idx] != array2[idx]) 
				{
//					Console.Out.WriteLine("array1[" + idx + "] = " + array1[idx]);
//					Console.Out.WriteLine("array2[" + idx + "] = " + array2[idx]);
					throw new Exception("i = " + idx);
				}
			}
			long end     = DateTime.Now.Ticks;

//			Console.Out.WriteLine("\ntest " + size + " ok in " + (end-start) + " ms");
//			Console.Out.WriteLine(factory);
		}
	}
}
