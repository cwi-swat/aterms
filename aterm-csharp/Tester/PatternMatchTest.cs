using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for PatternMatchTest.
	/// </summary>
	public class PatternMatchTest : TestCase
	{
		private ATermFactory factory;
		public PatternMatchTest(string name) : base(name)
		{
		}
		public virtual void TestPatternMatch() 
		{
			factory = Tester.theFactory;
			ATerm[] T = new ATerm[10];
			ArrayList result;
			ATerm empty = factory.makeList();
    
			T[0] = factory.parse("f(1,2,3)"); 
			T[1] = factory.parse("[1,2,3]"); 
			T[2] = factory.parse("f(a,\"abc\",2.3,<abc>)"); 
			T[3] = factory.parse("f(a,[])"); 

			AssertTrue("match-1a",T[0].match("f(1,2,3)") != null);

			result = T[1].match("<term>");
			//Console.Out.WriteLine("result = " + result);
			AssertTrue("match-1b",result != null && result[0].Equals(T[1]) );

    
			result = T[1].match("[<list>]");
			//Console.Out.WriteLine("result = " + result);
			AssertTrue("match-1c",result != null && result[0].Equals(T[1]) );

    
			result = T[1].match("[<int>,<list>]");
			//Console.Out.WriteLine("result = " + result);
			AssertTrue("match-1d",result != null &&
				result[0].Equals(1) &&
				result[1].Equals(factory.parse("[2,3]")) );
    
			//result = T[1].match("[<list>,2,<int>]");
			//Console.Out.WriteLine("result = " + result);

    
			result = factory.parse("f(a)").match("f(<term>)");
			//Console.Out.WriteLine("result = " + result);
			AssertTrue("match-2a",result != null &&
				result[0].Equals(factory.parse("a")) );
    
			result = factory.parse("f(a)").match("<term>");
			//Console.Out.WriteLine("result = " + result);
			AssertTrue("match-2b",result != null &&
				result[0].Equals(factory.parse("f(a)")) );
    
			result = factory.parse("f(a)").match("<fun(<term>)>");
			//Console.Out.WriteLine("result = " + result);
			AssertTrue("match-2c",result != null &&
				result[0].Equals("f") &&
				result[1].Equals(factory.parse("a")) );

			result = factory.parse("a").match("<fun>");
			//Console.Out.WriteLine("result = " + result);
			AssertTrue("match-2d",result != null &&
				result[0].Equals("a") );

			//result = factory.parse("f(<abc>)").match("f(<placeholder>)");
			//Console.Out.WriteLine("result = " + result);
			//test(result != null &&
			// result.get(0).Equals(factory.parse("<abc>")), "match-2e");
    
			result = T[0].match("f(1,<int>,3)"); 
			AssertTrue("match-3",result != null && result.Count == 1 &&  
				result[0].Equals(2) );
    
			result = T[2].match("f(<term>,<term>,<real>,<placeholder>)"); 
			//Console.Out.WriteLine("result = " + result); 
			AssertTrue("match-4a",result != null && result.Count == 4 );

			AssertTrue("match-4b",result[0].Equals(factory.parse("a")) );
			AssertTrue("match-4c",result[1].Equals(factory.parse("\"abc\"")) ); 
			AssertTrue("match-4d",result[2].Equals(2.3) ); 
			//test(result.get(3).Equals(factory.parse("<abc>")), "match-4e"); 

			result = T[1].match("[<list>]") ;
			AssertTrue("match-6a",result != null && result.Count == 1 &&  
				result[0].Equals(T[1]) ); 
    
			result = T[1].match("[<int>,<list>]"); 
			AssertTrue("match-6b",result != null && result.Count == 2 &&  
				result[0].Equals(1) ); 
			AssertTrue("match-6c",result[1].Equals((ATermList)factory.parse("[2,3]")));

			result = empty.match("[]");
			//Console.Out.WriteLine("result = " + result);
			AssertTrue("match-6d",result!=null && result.Count==0 );

			result = empty.match("[<list>]");
			//Console.Out.WriteLine("result = " + result);
			AssertTrue("match-6e",result[0].Equals((ATermList)factory.parse("[]")));
    
			result = T[0].match("<fun(<int>,<list>)>");
			AssertTrue("match-7a",result != null && result.Count == 3 ); 
			AssertTrue("match-7b",result[0].Equals("f") ); 
			AssertTrue("match-7c",result[1].Equals(1) ); 
			AssertTrue("match-7d",result[2].Equals((ATermList)factory.parse("[2,3]"))); 

			result = T[3].match("f(<term>,[<list>])");
			AssertTrue("match-8a",result != null && result.Count == 2 ); 
			AssertTrue("match-8b",result[0].Equals((ATerm)factory.parse("a")) ); 
			AssertTrue("match-8c",result[1] != null ); 
			AssertTrue("match-8d",((ATermList)((ArrayList)result)[1]).getLength()==0 ); 

			/*
		  result = T[0].match("<f>"); 
		  Console.Out.WriteLine("result = " + result);  
		  test(result != null && result.size()==1 &&  
			   result.get(0).Equals(T[0]), "match-8"); 
    
		  result = T[0].match("<f(1,2,<int>)>");
		  Console.Out.WriteLine("result = " + result);  
		  test(result != null && result.size() == 2, "match-9a"); 
		  test(result.get(0).Equals(T[0]), "match9b");  
		  test(result.get(1).Equals(new Integer(3)), "match-9b");
			*/

			result = factory.parse("fib(suc(suc(suc(suc(suc(suc(suc(suc(suc(suc(zero())))))))))))").match("fib(suc(<term()>))"); 
			//Console.Out.WriteLine("result = " + result); 

//			Console.Out.WriteLine("pass: testPatternMatch");
		}
		
	}
}
