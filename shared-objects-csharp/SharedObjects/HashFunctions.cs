/* Copyright (c) 2003, CWI, LORIA-INRIA All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation; either version 2, or 
 * (at your option) any later version.
 */

using System;

namespace SharedObjects
{
	/// <summary>
	/// Summary description for HashFunctions.
	/// </summary>
	public class HashFunctions
	{
		static public int oneAtATime(Object[] o) 
		{
			// [arg1,...,argn,symbol]
			int hash = 0;
			for (int i = 0; i < o.Length; i++) 
			{
				hash += o[i].GetHashCode();
				hash += (hash << 10);
				hash ^= (hash >> 6);
			}
			hash += (hash << 3);
			hash ^= (hash >> 11);
			hash += (hash << 15);
			//return (hash & 0x0000FFFF);
			return hash;
		}

		static public int simple(Object[] o) 
		{
			// [arg1,...,argn,symbol]
			int hash = o[o.Length - 1].GetHashCode();
			//      res = 65599*res + o[i].GetHashCode();
			//      res = 16*res + (1+i)*o[i].GetHashCode();
			for (int i = 0; i < o.Length - 1; i++) 
			{
				hash = 16 * hash + o[i].GetHashCode();
			}
			return hash;
		}

		static public int cwi(Object[] o) 
		{
			// [arg1,...,argn,symbol]
			int hash = 0;
			for (int i = 0; i < o.Length; i++) 
			{
				hash = (hash << 1) ^ (hash >> 1) ^ o[i].GetHashCode();
			}
			return hash;
		}

		static public int doobs(Object[] o) 
		{
			//System.out.println("static doobs_hashFuntion");

			int initval = 0; /* the previous hash value */
			int a, b, c, len;

			/* Set up the internal state */
			len = o.Length;
			a = b = unchecked((int)0x9e3779b9); /* the golden ratio; an arbitrary value */
			c = initval; /* the previous hash value */

			/*---------------------------------------- handle most of the key */
			int k = 0;
			while (len >= 12) 
			{
				a
					+= (o[k + 0].GetHashCode() + (o[k + 1].GetHashCode() << 8) + (o[k + 2].GetHashCode() << 16) + (o[k + 3].GetHashCode() << 24));
				b
					+= (o[k + 4].GetHashCode() + (o[k + 5].GetHashCode() << 8) + (o[k + 6].GetHashCode() << 16) + (o[k + 7].GetHashCode() << 24));
				c
					+= (o[k
					+ 8].GetHashCode()
					+ (o[k + 9].GetHashCode() << 8)
					+ (o[k + 10].GetHashCode() << 16)
					+ (o[k + 11].GetHashCode() << 24));
				//mix(a,b,c);
				a -= b;
				a -= c;
				a ^= (c >> 13);
				b -= c;
				b -= a;
				b ^= (a << 8);
				c -= a;
				c -= b;
				c ^= (b >> 13);
				a -= b;
				a -= c;
				a ^= (c >> 12);
				b -= c;
				b -= a;
				b ^= (a << 16);
				c -= a;
				c -= b;
				c ^= (b >> 5);
				a -= b;
				a -= c;
				a ^= (c >> 3);
				b -= c;
				b -= a;
				b ^= (a << 10);
				c -= a;
				c -= b;
				c ^= (b >> 15);

				k += 12;
				len -= 12;
			}

			/*------------------------------------- handle the last 11 bytes */
			c += o.Length;
			switch (len) /* all the case statements fall through */ 
			{
				case 11 :
					c += (o[k + 10].GetHashCode() << 24);
					goto case 10;
				case 10 :
					c += (o[k + 9].GetHashCode() << 16);
					goto case 9;
				case 9 :
					c += (o[k + 8].GetHashCode() << 8);
					/* the first byte of c is reserved for the length */
					goto case 8;
				case 8 :
					b += (o[k + 7].GetHashCode() << 24);
					goto case 7;
				case 7 :
					b += (o[k + 6].GetHashCode() << 16);
					goto case 6;
				case 6 :
					b += (o[k + 5].GetHashCode() << 8);
					goto case 5;
				case 5 :
					b += o[k + 4].GetHashCode();
					goto case 4;
				case 4 :
					a += (o[k + 3].GetHashCode() << 24);
					goto case 3;
				case 3 :
					a += (o[k + 2].GetHashCode() << 16);
					goto case 2;
				case 2 :
					a += (o[k + 1].GetHashCode() << 8);
					goto case 1;
				case 1 :
					a += o[k + 0].GetHashCode();
					/* case 0: nothing left to add */
					break;
			}
			//mix(a,b,c);
			c = mix(a, b, c);

			/*-------------------------------------------- report the result */
			return c;
		}

		static public int doobs(string s, int c) 
		{
			// o[] = [name,Integer(arity), Boolean(isQuoted)]
			// o[] = [value,offset,count,Integer(arity), Boolean(isQuoted)]

			int offset = 0;
			int count = 0;
			char[] source = null;

			count = s.Length;
			source = new char[count];
			offset = 0;
			s.CopyTo(0, source, 0, count);

			int a, b, len;
			/* Set up the internal state */
			len = count;
			a = b = unchecked((int)0x9e3779b9); /* the golden ratio; an arbitrary value */
			/*------------------------------------- handle the last 11 bytes */
			int k = offset;

			while (len >= 12) 
			{
				a += (source[k + 0] + (source[k + 1] << 8) + (source[k + 2] << 16) + (source[k + 3] << 24));
				b += (source[k + 4] + (source[k + 5] << 8) + (source[k + 6] << 16) + (source[k + 7] << 24));
				c += (source[k + 8] + (source[k + 9] << 8) + (source[k + 10] << 16) + (source[k + 11] << 24));
				// mix(a,b,c);
				a -= b;
				a -= c;
				a ^= (c >> 13);
				b -= c;
				b -= a;
				b ^= (a << 8);
				c -= a;
				c -= b;
				c ^= (b >> 13);
				a -= b;
				a -= c;
				a ^= (c >> 12);
				b -= c;
				b -= a;
				b ^= (a << 16);
				c -= a;
				c -= b;
				c ^= (b >> 5);
				a -= b;
				a -= c;
				a ^= (c >> 3);
				b -= c;
				b -= a;
				b ^= (a << 10);
				c -= a;
				c -= b;
				c ^= (b >> 15);

				k += 12;
				len -= 12;
			}
			/*---------------------------------------- handle most of the key */
			c += count;
			switch (len) 
			{
				case 11 :
					c += (source[k + 10] << 24);
					goto case 10;
				case 10 :
					c += (source[k + 9] << 16);
					goto case 9;
				case 9 :
					c += (source[k + 8] << 8);
					goto case 8;
					/* the first byte of c is reserved for the length */
				case 8 :
					b += (source[k + 7] << 24);
					goto case 7;
				case 7 :
					b += (source[k + 6] << 16);
					goto case 6;
				case 6 :
					b += (source[k + 5] << 8);
					goto case 5;
				case 5 :
					b += source[k + 4];
					goto case 4;
				case 4 :
					a += (source[k + 3] << 24);
					goto case 3;
				case 3 :
					a += (source[k + 2] << 16);
					goto case 2;
				case 2 :
					a += (source[k + 1] << 8);
					goto case 1;
				case 1 :
					a += source[k + 0];
					break;
					/* case 0: nothing left to add */
			}

			c = mix(a,b,c);
	   
			return c;
		}

		static public int mix(int a, int b, int c) 
		{
			a -= b; a -= c; a ^= (c >> 13);
			b -= c; b -= a; b ^= (a << 8);
			c -= a; c -= b; c ^= (b >> 13);
			a -= b; a -= c; a ^= (c >> 12);
			b -= c; b -= a; b ^= (a << 16);
			c -= a; c -= b; c ^= (b >> 5);
			a -= b; a -= c; a ^= (c >> 3);
			b -= c; b -= a; b ^= (a << 10);
			c -= a; c -= b; c ^= (b >> 15);

			return c;
		}	
	}
}

