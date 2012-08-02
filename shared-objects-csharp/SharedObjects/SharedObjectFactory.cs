/* Copyright (c) 2003, CWI, LORIA-INRIA All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation; either version 2, or 
 * (at your option) any later version.
 */

using System;
using System.Collections;

namespace SharedObjects
{
	/// <summary>
	/// Summary description for SharedObjectFactory.
	/// </summary>
	public class SharedObjectFactory
	{
		internal int logSize; // tableSize = 2^logSize
		internal int _hashMask; // hashMask  = 2^logSize-1
		internal Entry[] table;

		private int[] tableSize;
		private int minThreshold;
		private int maxThreshold;
		private float loadFactor;
		private int[] usedId;
		private Int32 maxId;
		private int minId;
		private int indexId;
		private int currentId;

		private int nbCall = 0;
		private int nbFoundReference = 0;
		private int nbFoundExactReference = 0;
		private int nbAdd = 0;
		private int nbRemoved = 0;
		private int nbProjectionCollision = 0;
		private int nbHashingCollision = 0;
		private int nbSwapEntry = 0;
		private int nbIdRegeneration = 0;

		public SharedObjectFactory() : this(1)
		{
		}
		public SharedObjectFactory(int initialLogSize) 
		{

			this.logSize = initialLogSize;
			this._hashMask = hashMask();
			this.table = new Entry[hashSize()];

			this.tableSize = new int[hashSize()];
			this.loadFactor = 3.0f; 

			this.maxThreshold = (int) (hashSize() * loadFactor);
			this.minThreshold = 0;

			this.maxId = unchecked((1 << 31) - 1);
			this.minId = unchecked(- (1 << 31));
			this.currentId = this.minId;
			this.indexId = 0;
			this.usedId = new int[1];
			this.usedId[0] = this.maxId;
		}

		private int hashSize(int n) 
		{
			return (1 << n);
		}

		private int hashSize() 
		{
			return hashSize(logSize);
		}

		private int hashMask() 
		{
			return (hashSize() - 1);
		}

		private int hashKey(int key) 
		{
			return (key & _hashMask);
		}

		private bool cleanupDone = false;

		internal virtual void decreaseCapacity() 
		{
			int oldLogSize = logSize;
			logSize -= 2;
			_hashMask = hashMask();

			maxThreshold = (int) (hashSize() * loadFactor);
			minThreshold = maxThreshold / 10;

			rehash(hashSize(oldLogSize));
		}

		internal virtual void increaseCapacity() 
		{
			int oldLogSize = logSize;
			logSize++;
			_hashMask = hashMask();

			maxThreshold = (int) (hashSize() * loadFactor);
			minThreshold = maxThreshold / 10;

			rehash(hashSize(oldLogSize));
		}

		internal virtual void rehash(int oldCapacity) 
		{
			Entry[] oldMap = table;
			table = new Entry[hashSize()];
			tableSize = new int[hashSize()];

			for (int i = oldCapacity; i-- > 0;) 
			{
				for (Entry old = oldMap[i]; old != null;) 
				{
					// Remove references to garbage collected term
					while (old != null && old.Target == null) 
					{
						old = old.next;
						nbRemoved++;
					}

					if (old != null) 
					{
						Entry e = old;
						old = old.next;
						int index = hashKey(e.Target.GetHashCode());
						e.next = table[index];
						table[index] = e;

						tableSize[index]++;
					}
				}
			}
			oldMap = null;
		}

		/*
		 * Remove unused references
		 */
		public virtual void cleanup() 
		{
			Entry[] tab = table;

			for (int i = tab.Length; i-- > 0;) 
			{
				for (Entry e = tab[i], prev = null; e != null; e = e.next) 
				{
					if (e.Target == null) 
					{
						// Remove a reference to a garbage collected term
						if (prev != null) 
						{
							prev.next = e.next;
						}
						else 
						{
							tab[i] = e.next;
						}
						nbRemoved++;
						tableSize[i]--;
					}
					else 
					{
						prev = e;
					}
				}
			}
		}

		public override string ToString() 
		{
			Entry[] tab = table;

			string s = "";
			s += "table_size            = " + table.Length + "\n";
			s += "#call                 = " + nbCall + "\n";
			s += "nbFoundReference      = " + nbFoundReference + "\n";
			s += "nbFoundExactReference = " + nbFoundExactReference + "\n";
			s += "#BuiltObject          = " + nbAdd + "\n";
			s += "#RemovedReference     = " + nbRemoved + "\n";
			s += "#ID Regeneration      = " + nbIdRegeneration + "\n";

			double repartition = 0.0;
			double n = (nbAdd - nbRemoved);
			double m = tab.Length;

			for (int idx = 0; idx < tab.Length; idx++) 
			{
				double bj = tableSize[idx];
				repartition += bj * (1 + bj) / 2;
			}

			s += "#element              = "
				+ (int) n
				+ " ["
				+ (nbAdd - nbRemoved)
				+ "]\n";

			double bestRepartition = (n / (2.0 * m)) * (n + 2.0 * m - 1.0);

			s += "repartition           = " + (repartition / bestRepartition) + "\n";
			s += "#lookup/build         = "
				+ ((double) (nbFoundReference + nbAdd) / (double) nbCall)
				+ "\n";
			//((double)(nbFoundReference) / (double)nbCall) + "\n";
			s += "projectionCollision   = " + nbProjectionCollision + "\n";
			s += "hashingCollision      = " + nbHashingCollision + "\n";
			s += "swapEntry             = " + nbSwapEntry + "\n";

			// STAT
			int emptyEntry = 0;
			int usedSlot = 0;
			for (int idx = 0; idx < tab.Length; idx++) 
			{
				if (tab[idx] != null) 
				{
					usedSlot = 0;
					for (Entry e = tab[idx]; e != null; e = e.next) 
					{
						if (e.Target != null) 
						{
							usedSlot++;
						}
					}
				}
				else 
				{
					emptyEntry++;
				}

			}
			s += "used = " + (tab.Length - emptyEntry) + "\tempty = " + emptyEntry;

			return s;
		}

		public virtual SharedObject build(SharedObject prototype) 
		{
			nbCall++;
	
			SharedObject foundObj;
			Entry[] tab = table;
			int hash = prototype.GetHashCode();
			int index = hashKey(hash);

			for (Entry e = tab[index], prev = null; e != null; e = e.next) 
			{
				foundObj = (SharedObject) e.Target;
				if (foundObj == null) 
				{
					// Found a reference to a garbage collected term
					// remove it to speed up lookups.
					if (prev != null) 
					{
						prev.next = e.next;
						e.Target = null; //clear();
					}
					else 
					{
						tab[index] = e.next;
						e.Target = null; // .clear();
					}
					nbRemoved++;
					tableSize[index]--;
				}
				else 
				{
					// Found a reference
					nbFoundReference++;
		
					if (prototype.equivalent(foundObj)) 
					{    
						nbFoundExactReference++;

						// swap the found object
						if (prev != null && (e.value - tab[index].value > 5)) 
						{
							nbSwapEntry++;
							prev.next = e.next;
							e.next = tab[index];
							tab[index] = e;
						}
						e.value++;
						return foundObj;
					}
					else 
					{
						// create or lookup collision
						nbProjectionCollision++;
						if (foundObj.GetHashCode() == hash) 
						{
							nbHashingCollision++;
						}
					}

					prev = e;
				}
			}

			// No similar SharedObject found, so build a new one
			int count = nbAdd - nbRemoved;
			if (false && count < minThreshold) 
			{
				//System.out.println("count = " + count + " < tminThreshold = " + minThreshold);
				decreaseCapacity();
				tab = table;
				index = hashKey(hash);
			}
			else if (count >= maxThreshold) 
			{
				/*
				 * Very simple strategy:
				 *  - try a cleanup
				 *  - rehash next time
				 */
				if (!cleanupDone) 
				{
					cleanupDone = true;
					System.GC.Collect();
					cleanup();
				}
				else 
				{
					cleanupDone = false;
					increaseCapacity();
					tab = table;
					index = hashKey(hash);
				}
			}

			foundObj = prototype.duplicate();
			if (prototype is SharedObjectWithID) 
			{
				((SharedObjectWithID) foundObj).setUniqueIdentifier(getFreshId());
			}
			tab[index] = new Entry(foundObj, tab[index]);
			nbAdd++;
			tableSize[index]++;
    
			return foundObj;
		}

		private int getFreshId() 
		{
			//System.out.println("CurrentId: "+currentId+" vs "+usedId[indexId]);
			if (currentId < usedId[indexId]) 
			{
				if (currentId == unchecked(- (1 << 31)))
				{
					//  System.out.println("First ID"+currentId);
				}
				return currentId++;
			}
			else 
			{
				// We try the next index in the usedId array
				do 
				{
					indexId++;
					if (indexId < usedId.Length) 
					{
						//System.out.println("loop CurrentId: "+(currentId+1)+" vs "+usedId[indexId]);
						if (++currentId < usedId[indexId]) 
						{
							//System.out.println("ID"+currentId);
							return currentId++;
						}
					}
					else 
					{
						regenerate();
						return getFreshId();
					}
				}
				while (true);
			}
		}

		private void regenerate() 
		{
			nbIdRegeneration++;
			//System.out.println("Regeneration of fresh unique IDs");
			ArrayList list = new ArrayList();
			// Collect all used Ids
			for (int i = 0; i < table.Length; i++) 
			{
				for (Entry e = table[i]; e != null; e = e.next) 
				{

					if (e.Target is SharedObjectWithID)
						list.Add(
							((SharedObjectWithID) e.Target).getUniqueIdentifier());
				}
			}
			list.Add(maxId);
			int newSize = list.Count;
			if (newSize >= (maxId - minId)) 
			{
				//System.out.println("No more unique identifier");
				Environment.Exit(1);
			}
			usedId = new int[newSize];
			for (int i = 0; i < newSize; i++) 
			{
				usedId[i] = (int)list[i];
			}
			// Sort array and reinitialize every thing
			Array.Sort(usedId);
			//System.out.println("New Array with size "+newSize+" from "+usedId[0]+" to "+usedId[newSize-2]);
			indexId = 0;
			currentId = minId;
		}

		public class Entry : WeakReference 
		{
			public Entry next;
			public int value = 0;
			public Entry(Object obj, Entry next) : base(obj)
			{
				this.next = next;
			}
		}
	}
}

