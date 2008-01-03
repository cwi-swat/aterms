using System;
using aterm;
using dotUnit.Framework;

namespace ATermTests
{
	/// <summary>
	/// Summary description for TestFib.
	/// </summary>
	public class FibTest : TestCase
	{
		private ATermFactory factory;

		private AFun zero, suc, plus, fib;
		private ATermAppl tzero;


		public FibTest(string name) : base(name) {}

		protected override void SetUp()
		{
			base.SetUp ();
		
            factory = Tester.theFactory;

			zero = factory.makeAFun("zero", 0, false);
			suc = factory.makeAFun("suc", 1, false);
			plus = factory.makeAFun("plus", 2, false);
			fib = factory.makeAFun("fib", 1, false);
			tzero = factory.makeAppl(zero);
		}

		public virtual void TestFib1() 
		{
			normalizePlus(
				factory.makeAppl(
				plus,
				factory.makeAppl(suc, factory.makeAppl(suc, tzero)),
				factory.makeAppl(suc, factory.makeAppl(suc, tzero))));
		}

		public virtual void TestFib2() 
		{
			// System.out.println("test 2");
			normalizeFib(
				factory.makeAppl(
				fib,
				factory.makeAppl(
				suc,
				factory.makeAppl(suc, factory.makeAppl(suc, factory.makeAppl(suc, tzero))))));

			// System.out.println("res = fib(4) = " + res);
		}

		public virtual void TestFib3() 
		{
			TestFib3(15);
		}

		public virtual void TestFib3(int n) 
		{
			ATermAppl N = tzero;
			for (int i = 0; i < n; i++) 
			{
				N = factory.makeAppl(suc, N);
			}
			normalizeFib(factory.makeAppl(fib, N));
		}
		public virtual void TestFib4()
		{
			Console.Out.WriteLine(factory.ToString());
		}

		public virtual ATermAppl normalizePlus(ATermAppl t) 
		{
			ATermAppl res = t;
			while (true) 
			{
				ATermAppl v0 = (ATermAppl) res.getArgument(0);

				// plus(s(s(s(s(s(x))))),y) => plus(x,s(s(s(s(s(y))))))
				if (v0.getAFun() == suc) 
				{
					ATermAppl v1 = (ATermAppl) v0.getArgument(0);
					if (v1.getAFun() == suc) 
					{
						ATermAppl v2 = (ATermAppl) v1.getArgument(0);
						if (v2.getAFun() == suc) 
						{
							ATermAppl v3 = (ATermAppl) v2.getArgument(0);
							if (v3.getAFun() == suc) 
							{
								ATermAppl v4 = (ATermAppl) v3.getArgument(0);
								if (v4.getAFun() == suc) 
								{
									res =
										factory.makeAppl(
										plus,
										v4.getArgument(0),
										factory.makeAppl(
										suc,
										factory.makeAppl(
										suc,
										factory.makeAppl(
										suc,
										factory.makeAppl(
										suc,
										factory.makeAppl(
										suc,
										res.getArgument(1)))))));
									continue;
								}
							}
						}
					}
				}

				// plus(0,x) = x
				if (v0.getAFun() == zero) 
				{
					res = (ATermAppl) res.getArgument(1);
					break;
				}

				// plus(s(x),y) => plus(x,s(y))
				if (v0.getAFun() == suc) 
				{
					res =
						factory.makeAppl(
						plus,
						v0.getArgument(0),
						factory.makeAppl(suc, res.getArgument(1)));
					continue;
				}
				break;
			}
			return res;
		}

		public virtual ATermAppl normalizeFib(ATermAppl t) 
		{

			ATermAppl res = t;
			while (true) 
			{
				// fib(0) = suc(0)
				ATermAppl v0 = (ATermAppl) res.getArgument(0);
				if (v0.getAFun() == zero) 
				{
					res = factory.makeAppl(suc, v0);
					break;
				}
				// fib(suc(0)) => suc(0)
				if (v0.getAFun() == suc) 
				{
					ATermAppl v1 = (ATermAppl) v0.getArgument(0);
					if (v1.getAFun() == zero) 
					{
						res = v0;
						break;
					}
				}
				//  fib(s(s(x))) => plus(fib(x),fib(s(x)))
				//     v0 v1
				if (v0.getAFun() == suc) 
				{
					ATermAppl v1 = (ATermAppl) v0.getArgument(0);
					if (v1.getAFun() == suc) 
					{
						ATermAppl v2 = (ATermAppl) v1.getArgument(0);
						ATermAppl fib1 = normalizeFib(factory.makeAppl(fib, v2));
						ATermAppl fib2 = normalizeFib(factory.makeAppl(fib, v1));
						//System.out.println("adding");
						res = normalizePlus(factory.makeAppl(plus, fib1, fib2));
						break;
					}
				}
				break;
			}
			return res;
		}
	}
}

