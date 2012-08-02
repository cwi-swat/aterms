using System;
using System.Collections;
using aterm;
using dotUnit.Framework;

namespace ATermTests
{
	/// <summary>
	/// Summary description for TestFibInterpreted.
	/// </summary>
	public class FibInterpretedTest : TestCase
	{
		private ATermFactory factory;

		private AFun zero, suc, plus, fib;
		private ATermAppl tzero;
		private ATerm fail;

		private ATerm[] lhs;
		private ATerm[] rhs;

		public FibInterpretedTest(string name) : base(name) {}

		protected override void SetUp()
		{
			base.SetUp();
			factory = Tester.theFactory;

			zero = factory.makeAFun("zero", 0, false);
			suc = factory.makeAFun("suc", 1, false);
			plus = factory.makeAFun("plus", 2, false);
			fib = factory.makeAFun("fib", 1, false);
			tzero = factory.makeAppl(zero);
			fail = factory.parse("fail");
		}

		public virtual void initRules() 
		{
			lhs = new ATerm[10];
			rhs = new ATerm[10];
			int ruleNumber = 0;

			// fib(zero) -> suc(zero)
			lhs[ruleNumber] = factory.parse("fib(zero)");
			rhs[ruleNumber] = factory.parse("suc(zero)");
			ruleNumber++;

			// fib(suc(zero)) -> suc(zero)
			lhs[ruleNumber] = factory.parse("fib(suc(zero))");
			rhs[ruleNumber] = factory.parse("suc(zero)");
			ruleNumber++;

			// fib(suc(suc(X))) -> plus(fib(X),fib(suc(X)))
			lhs[ruleNumber] = factory.parse("fib(suc(suc(<term>)))");
			rhs[ruleNumber] = factory.parse("plus(fib(<term>),fib(suc(<term>)))");
			ruleNumber++;

			// plus(zero,X) -> X
			lhs[ruleNumber] = factory.parse("plus(zero,<term>)");
			rhs[ruleNumber] = factory.parse("<term>");
			ruleNumber++;

			// plus(suc(X),Y) -> plus(X,suc(Y))
			lhs[ruleNumber] = factory.parse("plus(suc(<term>),<term>)");
			rhs[ruleNumber] = factory.parse("plus(<term>,suc(<term>))");
			ruleNumber++;

			// congruence (suc)
			lhs[ruleNumber] = factory.parse("suc(<term>)");
			rhs[ruleNumber] = factory.parse("suc(<term>)");
			ruleNumber++;

			// congruence (plus)
			lhs[ruleNumber] = factory.parse("plus(<term>,<term>)");
			rhs[ruleNumber] = factory.parse("plus(<term>,<term>)");
			ruleNumber++;

		}

		public virtual ATerm oneStep(ATerm subject) 
		{
			int ruleNumber = 0;
			ArrayList list;

			// fib(zero) -> suc(zero)
			list = subject.match(lhs[ruleNumber]);
			if (list != null) 
			{
				return rhs[ruleNumber];
			}
			ruleNumber++;

			// fib(suc(zero)) -> suc(zero)
			list = subject.match(lhs[ruleNumber]);
			if (list != null) 
			{
				return rhs[ruleNumber];
			}
			ruleNumber++;

			// fib(suc(suc(X))) -> plus(fib(X),fib(suc(X)))
			list = subject.match(lhs[ruleNumber]);
			if (list != null) 
			{
				ATerm X = (ATerm) list[0];
				list.Add(X);
				return factory.make(rhs[ruleNumber], list);
			}
			ruleNumber++;

			// plus(zero,X) -> X
			list = subject.match(lhs[ruleNumber]);
			if (list != null) 
			{
				return factory.make(rhs[ruleNumber], list);
			}
			ruleNumber++;

			// plus(suc(X),Y) -> plus(X,suc(Y))
			list = subject.match(lhs[ruleNumber]);
			if (list != null) 
			{
				return factory.make(rhs[ruleNumber], list);
			}
			ruleNumber++;

			// congruence (suc)
			list = subject.match(lhs[ruleNumber]);
			if (list != null) 
			{
				//System.out.println("congsuc"); // applied 1184122 times fir fib(14)
				ATerm X = (ATerm) list[0];
				ATerm Xp = oneStep(X);
				if (Xp.equals(fail)) 
				{
					return fail;
				} 
				else 
				{
					list.Clear();
					list.Add(Xp);
					return factory.make(rhs[ruleNumber], list);
				}
			}
			ruleNumber++;

			// congruence (plus)
			list = subject.match(lhs[ruleNumber]);
			if (list != null) 
			{
				//System.out.println("congplus"); // applied 9159 times fir fib(14)
				ATerm X = (ATerm) list[0];
				ATerm Xp = oneStep(X);
				if (Xp.Equals(fail)) 
				{
					ATerm Y = (ATerm) list[1];
					ATerm Yp = oneStep(Y);
					if (Yp.Equals(fail)) 
					{
						return fail;
					} 
					else 
					{
						list.Clear();
						list.Add(X);
						list.Add(Yp);
						return factory.make(rhs[ruleNumber], list);
					}
				} 
				else 
				{
					ATerm Y = (ATerm) list[1];
					list.Clear();
					list.Add(Xp);
					list.Add(Y);
					return factory.make(rhs[ruleNumber], list);
				}
			}
			ruleNumber++;

			return fail;
		}

		public virtual ATerm oneStepInnermost(ATerm subject) 
		{
			ArrayList list;

			// fib(zero) -> suc(zero)
			list = subject.match(lhs[0]);
			if (list != null) 
			{
				return rhs[0];
			}

			// fib(suc(zero)) -> suc(zero)
			list = subject.match(lhs[1]);
			if (list != null) 
			{
				return rhs[1];
			}

			// fib(suc(suc(X))) -> plus(fib(X),fib(suc(X)))
			list = subject.match(lhs[2]);
			if (list != null) 
			{
				ATerm X = (ATerm) list[0];
				ATerm X1 = normalize(factory.makeAppl(fib, X));
				ATerm X2 = normalize(factory.makeAppl(fib, factory.makeAppl(suc, X)));
				return factory.makeAppl(plus, X1, X2);
			}

			// plus(zero,X) -> X
			list = subject.match(lhs[3]);
			if (list != null) 
			{
				return (ATerm) list[0];
			}

			// plus(suc(X),Y) -> plus(X,suc(Y)))
			list = subject.match(lhs[4]);
			if (list != null) 
			{
				return factory.make(rhs[4], list);
			}

			return fail;
		}

		public virtual ATerm normalize(ATerm t) 
		{
			ATerm s = t;
			do 
			{
				t = s;
				s = oneStep(t);
			} while (!s.Equals(fail));
			return t;
		}

		public virtual void TestFibInterpreted() 
		{
				TestFibInterpreted(5);
		}

		
		public virtual void TestFibInterpreted(int n) 
		{
			initRules();
			ATermAppl N = tzero;
			for (int i = 0; i < n; i++) 
			{
				N = factory.makeAppl(suc, N);
			}
			ATerm tfib = factory.makeAppl(fib, N);
			normalize(tfib);
		}
	}
}

