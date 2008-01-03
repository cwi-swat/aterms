using System;
using System.Collections;
using System.IO;
using aterm;
using SharedObjects;

namespace ATermTests
{
	/// <summary>
	/// Summary description for Test1.
	/// </summary>
	public class Test1
	{
		private ATermFactory factory;

		[STAThread]
		static void Main(string[] args)
		{
			Test1 pureSuite = new Test1(new PureFactory());
			pureSuite.testAll();
		}


		public Test1(ATermFactory factory) 
		{
			this.factory=factory;
		}

		void assertTrue(bool condition) 
		{
			if(!condition) 
			{
				throw new Exception("assertion failed.");
			}
		}

		protected  void test(bool cond, String id) 
		{
			if(cond) 
			{
				Console.Out.WriteLine("\ttest " + id + " ok!");
			} 
			else 
			{
				throw new Exception("test " + id + " failed.");
			}
		}
  
		public void testMakeInt() 
		{
			ATermInt[] term = new ATermInt[2];

			term[0] = factory.makeInt(3);
			term[1] = factory.makeInt(3);

			assertTrue(term[0].getType() == ATermType.INT);
			assertTrue(term[0].getInt() == 3);
			assertTrue(term[0] == term[1]);

			assertTrue(term[0].ToString().Equals("3"));

			ArrayList result;

			result = term[0].match("3");
			assertTrue(result != null);
			assertTrue(result.Count == 0);

			result = term[0].match("<int>");
			assertTrue(result != null);
			assertTrue(result.Count == 1);
			assertTrue(result[0].Equals(3));
    
			Console.Out.WriteLine("pass: testMakeInt");
		}

		public void testMakeReal() 
		{
			ATermReal[] term = new ATermReal[2];

			term[0] = factory.makeReal(Math.PI);
			term[1] = factory.makeReal(Math.PI);

			assertTrue(term[0].getType() == ATermType.REAL);
			assertTrue(term[0].getReal() == Math.PI);
			assertTrue(term[0] == term[1]);

			ArrayList result;

			result = term[0].match("<real>");
			assertTrue(result != null);
			assertTrue(result.Count == 1);
			assertTrue(result[0].Equals(Math.PI));

			Console.Out.WriteLine("pass: testMakeReal");
		}

		public void testMakeAppl() 
		{
			AFun[] symmies = new AFun[4];
				ATermAppl[] apples = new ATermAppl[16];

					symmies[0] = factory.makeAFun("f0", 0, false);
			symmies[1] = factory.makeAFun("f1", 1, false);
			symmies[2] = factory.makeAFun("f6", 6, false);
			symmies[3] = factory.makeAFun("f10", 10, false);

			apples[0] = factory.makeAppl(symmies[0]);
			apples[1] = factory.makeAppl(symmies[1], (ATerm)apples[0]);
			apples[2] = factory.makeAppl(symmies[1], (ATerm)apples[1]);
			apples[3] = factory.makeAppl(symmies[1], (ATerm)apples[0]);
			apples[4] = factory.makeAppl(symmies[2], new ATerm[] {(ATerm)apples[0],
																	 (ATerm)apples[0], 
																	 (ATerm)apples[1],
																	 (ATerm)apples[0], 
																	 (ATerm)apples[0],
																	 (ATerm)apples[1]});
			apples[5] = factory.makeAppl(symmies[3], new ATerm[] {(ATerm)apples[0],
																	 (ATerm)apples[1],
																	 (ATerm)apples[0],
																	 (ATerm)apples[1],
																	 (ATerm)apples[0],
																	 (ATerm)apples[1],
																	 (ATerm)apples[0],
																	 (ATerm)apples[1],
																	 (ATerm)apples[0],
																	 (ATerm)apples[1]});
			apples[6] = (ATermAppl)apples[2].setArgument((ATerm)apples[0], 0);

			assertTrue(apples[6].isEqual(apples[1]));
			assertTrue(apples[1].isEqual(apples[3]));
			assertTrue(!apples[2].isEqual(apples[1]));
			assertTrue(!apples[2].isEqual(apples[6]));
			assertTrue(!apples[1].isEqual(apples[2]));
			assertTrue(!apples[2].isEqual(apples[3]));
			assertTrue(!apples[0].isEqual(apples[1]));

			Console.Out.WriteLine("pass: testMakeAppl");
		}

		protected  void testParser() 
		{
			ATerm[] T = new ATerm[20];
			int index = 0;
    
			T[index++] = factory.parse("g");
			T[index++] = factory.parse("f()");
			T[index++] = factory.parse("f(1)");
			T[index++] = factory.parse("f(1,2,<int>)");
			T[index++] = factory.parse("[]");
			T[index++] = factory.parse("[1]");
			T[index++] = factory.parse("[1,2]");
			T[index++] = factory.parse("[1,3.5,4e6,123.21E-3,-12]");
			T[index++] = factory.parse("[1,a,f(1)]");
			T[index++] = factory.parse("[1,\"a\",f(1), \"g\"(a,\"b\")]");

			for(int i=0; i<index; i++) 
			{
				Console.Out.WriteLine("term " + i + ": " + T[i]);
			}
    
		}

		protected  void testFileParser() 
		{
			try 
			{
				/*
			  FileOutputStream output = new FileOutputStream("testFileParser.txt");
			  String s = factory.parse("f(a,g(b))").toString();
			  output.write(s);
			  output.close();
				*/
				FileStream input = new FileStream("testFileParser.txt", FileMode.Open);
				ATerm result = factory.readFromTextFile(input);
				input.Close();
      
				Console.Out.WriteLine("result = " + result);
			} 
			catch (FileNotFoundException e1) 
			{
				Console.Out.WriteLine(e1);
			} 
			catch (IOException e2) 
			{
				Console.Out.WriteLine(e2);
			}
		}

 
		public void testMakeList() 
		{
			ATerm[] T = new ATerm[10];
			ATermList[] Ts = new ATermList[10];

			//Console.Out.WriteLine("testing ATermList class");
			T[0] = factory.parse("[0,1,2,3,4,5,4,3,2,1]");
			Ts[0] = (ATermList)T[0];
			T[1] = factory.parse("[]");
			Ts[1] = factory.makeList();
			T[2] = factory.parse("[1,2,3]");
			Ts[2] = (ATermList)T[2];
			T[3] = factory.parse("[4,5,6]");
			Ts[3] = (ATermList)T[3];
			T[4] = factory.parse("[1,2,3,4,5,6]");
			Ts[4] = (ATermList)T[4];

			//    T[5] = factory.parse("[1 , 2 , 3 , 4,5,6,7]");
			T[5] = factory.parse("[1,2,3,4,5,6,7]");
			Ts[5] = (ATermList)T[5];

			//T[6] = factory.parse("f(abc{[label,val]})");

			// test length
			test(Ts[0].getLength() == 10, "length-1");

			// test search
			test(Ts[0].indexOf(factory.makeInt(2), 0) == 2, "indexOf-1");
			test(Ts[0].indexOf(factory.makeInt(10), 0) == -1, "indexOf-2");
			test(Ts[0].indexOf(factory.makeInt(0), 0) == 0, "indexOf-3");
			test(Ts[0].indexOf(factory.makeInt(5), 0) == 5, "indexOf-4");

			// test lastIndexOf
    
			test(Ts[0].lastIndexOf(factory.makeInt(1), -1) == 9, "lastIndexOf-1");
			test(Ts[0].lastIndexOf(factory.makeInt(0), -1) == 0, "lastIndexOf-2");
			test(Ts[0].lastIndexOf(factory.makeInt(10), -1) == -1, "lastIndexOf-3");

			// test concat
			test(Ts[2].concat(Ts[3]).Equals(Ts[4]), "concat-1");
			test(Ts[0].concat(Ts[1]).Equals(Ts[0]), "concat-2");

			// test append
			test(Ts[4].append(factory.makeInt(7)).Equals(Ts[5]), "append-1");

			// test insert
			Ts[7] = Ts[3].insert(factory.parse("3"));
			Ts[7] = Ts[7].insert(factory.parse("2"));
			Ts[7] = Ts[7].insert(factory.parse("1"));
			test(Ts[7].Equals(Ts[4]), "insert-1");
    
			test(Ts[1].insert(factory.parse("1")).Equals(factory.parse("[1]")), "insert-2");

			test(Ts[4].insertAt(factory.parse("7"), Ts[4].getLength()).Equals(Ts[5]), "insert-3");

			// Test prefix/last
			test(Ts[5].getPrefix().Equals(Ts[4]), "prefix-1");
			test(Ts[5].getLast().Equals(factory.parse("7")), "last-1");

			Console.Out.WriteLine("pass: testMakeList");
		}
  
		protected  void testPatternMatch() 
		{
			ATerm[] T = new ATerm[10];
			ArrayList result;
			ATerm empty = factory.makeList();
    
			T[0] = factory.parse("f(1,2,3)"); 
			T[1] = factory.parse("[1,2,3]"); 
			T[2] = factory.parse("f(a,\"abc\",2.3,<abc>)"); 
			T[3] = factory.parse("f(a,[])"); 

			test(T[0].match("f(1,2,3)") != null, "match-1a");

			result = T[1].match("<term>");
			//Console.Out.WriteLine("result = " + result);
			test(result != null && result[0].Equals(T[1]), "match-1b");

    
			result = T[1].match("[<list>]");
			//Console.Out.WriteLine("result = " + result);
			test(result != null && result[0].Equals(T[1]), "match-1c");

    
			result = T[1].match("[<int>,<list>]");
			//Console.Out.WriteLine("result = " + result);
			test(result != null &&
				result[0].Equals(1) &&
				result[1].Equals(factory.parse("[2,3]")), "match-1d");
    
			//result = T[1].match("[<list>,2,<int>]");
			//Console.Out.WriteLine("result = " + result);

    
			result = factory.parse("f(a)").match("f(<term>)");
			//Console.Out.WriteLine("result = " + result);
			test(result != null &&
				result[0].Equals(factory.parse("a")), "match-2a");
    
			result = factory.parse("f(a)").match("<term>");
			//Console.Out.WriteLine("result = " + result);
			test(result != null &&
				result[0].Equals(factory.parse("f(a)")), "match-2b");
    
			result = factory.parse("f(a)").match("<fun(<term>)>");
			//Console.Out.WriteLine("result = " + result);
			test(result != null &&
				result[0].Equals("f") &&
				result[1].Equals(factory.parse("a")), "match-2c");

			result = factory.parse("a").match("<fun>");
			//Console.Out.WriteLine("result = " + result);
			test(result != null &&
				result[0].Equals("a"), "match-2d");

			//result = factory.parse("f(<abc>)").match("f(<placeholder>)");
			//Console.Out.WriteLine("result = " + result);
			//test(result != null &&
			// result.get(0).Equals(factory.parse("<abc>")), "match-2e");
    
			result = T[0].match("f(1,<int>,3)"); 
			test(result != null && result.Count == 1 &&  
				result[0].Equals(2), "match-3");
    
			result = T[2].match("f(<term>,<term>,<real>,<placeholder>)"); 
			//Console.Out.WriteLine("result = " + result); 
			test(result != null && result.Count == 4, "match-4a");

			test(result[0].Equals(factory.parse("a")), "match-4b");
			test(result[1].Equals(factory.parse("\"abc\"")), "match-4c"); 
			test(result[2].Equals(2.3), "match-4d"); 
			//test(result.get(3).Equals(factory.parse("<abc>")), "match-4e"); 

			result = T[1].match("[<list>]") ;
			test(result != null && result.Count == 1 &&  
				result[0].Equals(T[1]), "match-6a"); 
    
			result = T[1].match("[<int>,<list>]"); 
			test(result != null && result.Count == 2 &&  
				result[0].Equals(1), "match-6b"); 
			test(result[1].Equals((ATermList)factory.parse("[2,3]")), 
				"match-6c");

			result = empty.match("[]");
			//Console.Out.WriteLine("result = " + result);
			test(result!=null && result.Count==0, "match-6d");

			result = empty.match("[<list>]");
			//Console.Out.WriteLine("result = " + result);
			test(result[0].Equals((ATermList)factory.parse("[]")), 
				"match-6e");
    
			result = T[0].match("<fun(<int>,<list>)>");
			test(result != null && result.Count == 3, "match-7a"); 
			test(result[0].Equals("f"), "match-7b"); 
			test(result[1].Equals(1), "match-7c"); 
			test(result[2].Equals((ATermList)factory.parse("[2,3]")), 
				"match-7d"); 

			result = T[3].match("f(<term>,[<list>])");
			test(result != null && result.Count == 2, "match-8a"); 
			test(result[0].Equals((ATerm)factory.parse("a")), "match-8b"); 
			test(result[1] != null, "match-8c"); 
			test(((ATermList)((ArrayList)result)[1]).getLength()==0, "match-8d"); 

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

			Console.Out.WriteLine("pass: testPatternMatch");
		}
  
		protected  void testPatternMake() 
		{
			ArrayList list = new ArrayList();
			ATerm result;

			list.Clear(); 
			result = factory.make("23", list);
			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); 
			result = factory.make("3.14", list);
			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); 
			result = factory.make("[1,2,3]", list);
			Console.Out.WriteLine("\tresult = " + result);
    
			list.Clear(); 
			result = factory.make("GL(\"toto\")", list);
			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); list.Add(1);
			result = factory.make("<int>", list);
			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); list.Add(3.14);
			result = factory.make("<real>", list);
			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); list.Add(factory.parse("f(a,b,c)"));
			result = factory.make("<term>", list);
			Console.Out.WriteLine("\tresult = " + result);
    

			list.Clear();
			list.Add(factory.parse("f(a,b,c)"));
			list.Add(3);
			list.Add(factory.parse("<abc>"));
			result = factory.make("[<term>,2,<int>,3.14,<placeholder>]", list);
			Console.Out.WriteLine("\tresult = " + result);

			list.Clear();
			list.Add(factory.parse("b"));
			list.Add(4);
			result = factory.make("f(1,<term>,c,<int>)", list);
			Console.Out.WriteLine("\tresult = " + result);

			list.Clear();
			list.Add(factory.parse("b"));
			list.Add(4);
			result = factory.make("f(1,g(<term>),c,h(<int>))", list);
			Console.Out.WriteLine("\tresult = " + result);

			//Ts[8] = factory.parse();
			list.Clear();
			list.Add(factory.parse("1"));
			list.Add(factory.parse("[]"));
			result = factory.make("[<term>,<list>]", list);
			Console.Out.WriteLine("\tresult = " + result);
			test(((ATermList)result).getFirst() == factory.parse("1"), "make-1a");
			test(((ATermList)result).getLength() == 1,"make-1b");

    
			/*
		  list.add(new Integer(1));
		  test(factory.make("<int>", list).Equals(T[0]), "make-1");

		  list.clear(); list.add(T[3]);
		  test(factory.make("<term>", list).Equals(T[3]), "make-2");

		  list.clear(); list.add( "b");
		  test(factory.make("<appl>", list).Equals(T[4]), "make-3");

		  list.clear(); list.add(new Double(3.14));
		  test(factory.make("<real>", list).Equals(
			factory.makeReal(3.14)), "make-4");

		  list.clear(); list.add(factory.makeAppl(
			factory.makeAFun("real",0,false)));
		  test(factory.make("<placeholder>", list).Equals(
			factory.parse("<real>")), "make-5");

		  list.clear(); list.add(T[7]);
		  test(factory.make("[<list>]", list).Equals(T[7]), "make-6");

		  list.clear();
		  list.add(T[3]);
		  list.add("b");
		  list.add(factory.makeList(T[5], factory.makeList()));
		  test(factory.make("f(<term>,<appl>,<list>)", list).Equals(T[6]), "make-7");

		  list.clear();
		  list.add("f");
		  list.add(new Integer(2));
		  test(factory.make("<appl(1,<int>,3)>", list).Equals(T[8]), "make-8");
			*/

			Console.Out.WriteLine("pass: testPatternMake");
		}

		public void testMaxTerm() 
		{
			AFun f = factory.makeAFun("f", 1, false);
			AFun a = factory.makeAFun("a", 0, false);

			int size = 500;
			ATerm[] array1 = new ATerm[size];
			ATerm[] array2 = new ATerm[size];

			long start   = DateTime.Now.Ticks;
			Console.Out.WriteLine("array1");
			for(int i=0 ; i<size ; i++) 
			{
				if(i%100 == 0) 
				{
					Console.Out.Write(i + "  ");
				}

				int idx = i%10;
				array1[idx] = factory.makeAppl(a);
				for(int j=0 ; j<2*i ; j++) 
				{
					array1[idx] = factory.makeAppl(f,array1[idx]);
				}
				//Console.Out.WriteLine("array[" + i + "] = " + array[i]);
			}

			Console.Out.WriteLine("\narray2");
			for(int i=0 ; i<size ; i++) 
			{
				if(i%100 == 0) 
				{
					Console.Out.Write(i + "  ");
				}

				int idx = i%10;
				array2[idx] = factory.makeAppl(a);
				for(int j=0 ; j<2*i ; j++) 
				{
					array2[idx] = factory.makeAppl(f,array2[idx]);
				}
				//Console.Out.WriteLine("array[" + i + "] = " + array[i]);
			}

			Console.Out.WriteLine("\ntest");
			for(int i=0 ; i<size ; i++) 
			{
				if(i%500 == 0) 
				{
					Console.Out.Write(i + "  ");
				}

				int idx = i%10;
				if(array1[idx] != array2[idx]) 
				{
					Console.Out.WriteLine("array1[" + idx + "] = " + array1[idx]);
					Console.Out.WriteLine("array2[" + idx + "] = " + array2[idx]);
					throw new Exception("i = " + idx);
				}
			}
			long end     = DateTime.Now.Ticks;

			Console.Out.WriteLine("\ntest " + size + " ok in " + (end-start) + " ms");
			Console.Out.WriteLine(factory);
		}
  
		public void testFib() 
		{
			TestFib t = new TestFib(factory);
			t.test1();
			t.test2();
			t.test3(10);
			Console.Out.WriteLine(factory.ToString());
		}

		public void testPrimes(int n) 
		{
			TestPrimes t = new TestPrimes(factory);
			long start   = DateTime.Now.Ticks;
			ATermList l  = t.getPrimes(n);
			long end     = DateTime.Now.Ticks;
    
			Console.Out.WriteLine("primes(" + n + ") in " + (end-start) + " ms");
			//Console.Out.WriteLine(" primes(" + n + ") = " + l);
			Console.Out.WriteLine("#primes(" + n + ") = " + l.getLength());
			Console.Out.WriteLine(factory);
		}

		public void testFibInterpreted() 
		{
			TestFibInterpreted t = new TestFibInterpreted(factory);
			t.initRules();
			t.test1(12);
    
			Console.Out.WriteLine(factory);
		}

  

		public void testAll() 
		{
			
				testMakeInt();
				testMakeReal();
				testMakeAppl();
				  //testParser();
				testMakeList();
				testPatternMatch();
				testPatternMake();
			  
			testFib();
			testPrimes(2000);
			//testFibInterpreted();
		}
	}
}
