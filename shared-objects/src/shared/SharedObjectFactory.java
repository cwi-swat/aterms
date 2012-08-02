/*
 * Copyright (c) 2003-2007, CWI and INRIA All rights reserved. Redistribution and use in source and
 * binary forms, with or without modification, are permitted provided that the following conditions
 * are met: * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. * Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. * Neither the name of the University of
 * California, Berkeley nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission. THIS SOFTWARE IS PROVIDED
 * BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package shared;

import java.lang.ref.WeakReference;

/**
 * The SharedObjectsFactory is a 'weak' constant pool for uniquely represented objects.
 * <br /><br />
 * This class is fully thread-safe, but tries to avoid (contended) locking as much as is
 * reasonably achievable. As a result this implementation should scale fairly well on
 * multi-core / processor systems; while also limiting synchronization overhead.
 * <br /><br />
 * WARNING: Do not edit this class unless you fully understand both Java's memory and threading model
 * and how the garbage collector(s) work. (You'll almost certainly 'break' something otherwise).<br />
 * The JMM spec (previously known as JSR-133) can be found here: <a href=http://java.sun.com/docs/books/jls/third_edition/html/memory.html>http://java.sun.com/docs/books/jls/third_edition/html/memory.html</a><br />
 * Even experts should be cautious. Every line of code is in a certain place for a reason. Modifying
 * anything may break thread-safety and / or can have a serious impact on performance.
 * 
 * @author Arnold Lankamp
 */
public class SharedObjectFactory{
	private final static int DEFAULT_NR_OF_SEGMENTS_BITSIZE = 5;
	
	private final Segment[] segments;
	
	/**
	 * Default constructor.
	 */
	public SharedObjectFactory(){
		super();
		
		segments = new Segment[1 << DEFAULT_NR_OF_SEGMENTS_BITSIZE];
		for(int i = segments.length - 1; i >= 0; i--){
			segments[i] = new Segment(i);
		}
	}
	
	/**
	 * Constructor. This is only here for backwards compatibility. The user shouldn't specify the
	 * logsize, we'll resize the table ourselfs when needed.
	 * 
	 * @param initialLogSize
	 *            This is the argument that will be ignored.
	 */
	public SharedObjectFactory(int initialLogSize){
		this();
	}
	
	/**
	 * Removes stale entries from the set.
	 */
	public void cleanup(){
		for(int i = segments.length - 1; i >= 0; i--){
			Segment segment = segments[i];
			synchronized(segment){
				segment.cleanup();
			}
		}
	}
	
	/**
	 * Returns statistics.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		int nrOfSegments = segments.length;
		for(int i = 0; i < nrOfSegments; i++){
			int startHash = i << Segment.MAX_SEGMENT_BITSIZE;
			int endHash = ((i + 1) << Segment.MAX_SEGMENT_BITSIZE) - 1;
			
			sb.append("Segment hash range: ");
			sb.append(startHash);
			sb.append(" till ");
			sb.append(endHash);
			sb.append(" | ");
			sb.append(segments[i].toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Finds or creates the unique version of the given shared object prototype.
	 * 
	 * @param prototype
	 *            The prototype of the shared object we want the unique reference too.
	 * @return The reference to the unique shared object associated with the argument.
	 */
	public SharedObject build(SharedObject prototype){
		int hash = prototype.hashCode();
		int segmentNr = hash >>> (32 - DEFAULT_NR_OF_SEGMENTS_BITSIZE);
		
		return segments[segmentNr].get(prototype, hash);
	}
	
	/**
	 * Checks if the given shared object is present in this factory.
	 * 
	 * @param prototype
	 *            The shared object.
	 * @return True if this factory contains the given shared object; false otherwise.
	 */
	public boolean contains(SharedObject object){
		int hash = object.hashCode();
		int segmentNr = hash >>> (32 - DEFAULT_NR_OF_SEGMENTS_BITSIZE);
		
		return segments[segmentNr].contains(object, hash);
	}
	
	/**
	 * A segment is a hashtable that represents a certain part of the 'hashset'.
	 */
	private final static class Segment{
		private final static int MAX_SEGMENT_BITSIZE = 32 - DEFAULT_NR_OF_SEGMENTS_BITSIZE;
		private final static int DEFAULT_SEGMENT_BITSIZE = 5;
		private final static float DEFAULT_LOAD_FACTOR = 2f;
		
		private volatile Entry[] entries;
		
		private int bitSize;
		
		private int threshold;
		private int load;
		
		private volatile boolean flaggedForCleanup;
		private volatile WeakReference<GarbageCollectionDetector> garbageCollectionDetector;
		private int cleanupScaler;
		private int cleanupThreshold;
		
		private final int segmentID;

		private int numberOfFreeIDs;
		private int[] freeIDs;
		private int freeIDsIndex;
		private int nextFreeID;
		private final int maxFreeIDPlusOne;
		
		/**
		 * Constructor.
		 * 
		 * @param segmentID
		 *            The number identifying this segment.
		 */
		public Segment(int segmentID){
			super();
			
			this.segmentID = segmentID;
			
			bitSize = DEFAULT_SEGMENT_BITSIZE;
			int nrOfEntries = 1 << bitSize;
			
			entries = new Entry[nrOfEntries];
			
			threshold = (int) (nrOfEntries * DEFAULT_LOAD_FACTOR);
			load = 0;
			
			flaggedForCleanup = false;
			garbageCollectionDetector = new WeakReference<GarbageCollectionDetector>(new GarbageCollectionDetector(this)); // Allocate a (unreachable) GC detector.
			cleanupScaler = 50; // Init as 50% average cleanup percentage, to make sure the cleanup can and will be executed the first time.
			cleanupThreshold = cleanupScaler;
			
			numberOfFreeIDs = 1 << bitSize;
			freeIDs = new int[numberOfFreeIDs];
			freeIDsIndex = 0;
			nextFreeID = segmentID << MAX_SEGMENT_BITSIZE;
			maxFreeIDPlusOne = (segmentID + 1) << MAX_SEGMENT_BITSIZE;
		}
		
		/**
		 * Removes entries, who's value have been garbage collected, from this segment.
		 */
		private void cleanup(){
			Entry[] table = entries;
			int newLoad = load;
			
			for(int i = entries.length - 1; i >= 0; i--){
				Entry e = table[i];
				if(e != null){
					Entry previous = null;
					do{
						Entry next = e.next;
						
						if(e.get() == null){
							if(previous == null){
								table[i] = next;
							}else{
								previous.next = next;
							}
							
							newLoad--;
							
							if(e instanceof EntryWithID){
								EntryWithID ewid = (EntryWithID) e;
								releaseID(ewid.id);
							}
						}else{
							previous = e;
						}
						
						e = next;
					}while(e != null);
				}
			}
			
			load = newLoad;
			
			entries = table; // Create happens-before edge, to ensure any changes become visible.
		}
		
		/**
		 * Rehashes the segment. The entries in the bucket will remain in the same order, so there are
		 * only 'young -> old' references and not the other way around. This will reduce 'minor'
		 * garbage collection times.
		 */
		private void rehash(){
			int nrOfEntries = 1 << (++bitSize);
			int newHashMask = nrOfEntries - 1;
			
			Entry[] oldEntries = entries;
			Entry[] newEntries = new Entry[nrOfEntries];
			
			// Construct temporary entries that function as roots for the entries that remain in the current bucket
			// and those that are being shifted.
			Entry currentEntryRoot = new Entry(null, null, 0);
			Entry shiftedEntryRoot = new Entry(null, null, 0);
			
			int newLoad = load;
			int oldSize = oldEntries.length;
			for(int i = oldSize - 1; i >= 0; i--){
				Entry e = oldEntries[i];
				if(e != null){
					Entry lastCurrentEntry = currentEntryRoot;
					Entry lastShiftedEntry = shiftedEntryRoot;
					do{
						if(e.get() != null){ // Cleared entries should not be copied.
							int position = e.hash & newHashMask;
							
							if(position == i){
								lastCurrentEntry.next = e;
								lastCurrentEntry = e;
							}else{
								lastShiftedEntry.next = e;
								lastShiftedEntry = e;
							}
						}else{
							newLoad --;
							
							if(e instanceof EntryWithID){
								EntryWithID ewid = (EntryWithID) e;
								releaseID(ewid.id);
							}
						}
						
						e = e.next;
					}while(e != null);
					
					// Set the next pointers of the last entries in the buckets to null.
					lastCurrentEntry.next = null;
					lastShiftedEntry.next = null;
					
					newEntries[i] = currentEntryRoot.next;
					newEntries[i | oldSize] = shiftedEntryRoot.next; // The entries got shifted by the size of the old table.
				}
			}
			
			load = newLoad;
			
			threshold <<= 1;
			entries = newEntries; // Volatile write. Creates happens-before edge with the above changes.
		}
		
		/**
		 * Ensures the load in this segment will not exceed a certain threshold.
		 */
		private void ensureCapacity(){
			// Rehash if the load exceeds the threshold,
			// unless the segment is already stretched to it's maximum (since that would be a useless thing to do).
			if(load > threshold && bitSize < MAX_SEGMENT_BITSIZE){
				rehash();
			}
		}
		
		/**
		 * Attempts to run a cleanup if the garbage collector ran before the invocation of this function.
		 * This ensures that, in most cases, the buckets will contain no cleared entries. By doing this we
		 * speed up lookups significantly. Note that we will automatically throttle the frequency of the cleanups;
		 * in case we hardly every collect anything (either because there is no garbage or collections occur
		 * very frequently) it will be slowed down to as little as once per four garbage collections. When a lot
		 * of entries are being cleared the cleanup will run after every collection. Using this strategy 
		 * ensures us that we clean the segment exactly when it is needed and possible.
		 */
		private void tryCleanup(){
			if(flaggedForCleanup){
				synchronized(this){
					if(garbageCollectionDetector == null){ // Yes, in Java DCL works on volatiles.
						flaggedForCleanup = false;
						if(cleanupThreshold > 8){ // The 'magic' number 8 is chosen, so the cleanup will be done at least once after every four garbage collections.
							int oldLoad = load;
							
							cleanup();
							
							int cleanupPercentate;
							if(oldLoad == 0) cleanupPercentate = 50; // This prevents division by zero errors in case the table is still empty (keep the cleanup percentage that 50% in this case).
							else cleanupPercentate = 100 - ((load * 100) / oldLoad); // Calculate the percentage of entries that has been cleaned.
							cleanupScaler = (((cleanupScaler * 25) + (cleanupPercentate * 7)) >> 5); // Modify the scaler, depending on the history (weight = 25) and how much we cleaned up this time (weight = 7).
							if(cleanupScaler > 0){
								cleanupThreshold = cleanupScaler;
							}else{
								cleanupThreshold = 1; // If the scaler value became 0 (when we hardly every collect something), set the threshold to 1, so we only skip the next three garbage collections.
							}
						}else{
							cleanupThreshold <<= 1;
						}
						
						garbageCollectionDetector = new WeakReference<GarbageCollectionDetector>(new GarbageCollectionDetector(this)); // Allocate a new (unreachable) GC detector.
					}
				}
			}
		}
		
		/**
		 * Inserts the given shared object into the set.
		 * 
		 * @param object
		 *            The shared object to insert.
		 * @param hash
		 *            The hash the corresponds to the given shared object.
		 */
		private void put(SharedObject object, int hash){
			Entry[] table = entries;
			int hashMask = table.length - 1;
			int position = hash & hashMask;
			
			Entry next = table[position];
			// Assign a unique id if needed.
			if(object instanceof SharedObjectWithID){
				SharedObjectWithID sharedObjectWithID = (SharedObjectWithID) object;
				int id = generateID();
				sharedObjectWithID.setUniqueIdentifier(id);
				
				table[position] = new EntryWithID(next, sharedObjectWithID, hash, id);
			}else{
				table[position] = new Entry(next, object, hash);
			}
			
			load++;
			
			entries = table; // Create a happens-before edge for the added entry, to ensure visibility.
		}
		
		/**
		 * Check if the given shared object is present in this segment.
		 * NOTE: This method contains some duplicate code for efficiency reasons.
		 * 
		 * @param prototype
		 *            The shared object.
		 * @param hash
		 *            The hash associated with the given shared object.
		 * @return True if this segment contains the given shared object; false otherwise.
		 */
		public boolean contains(SharedObject prototype, int hash){
			Entry[] currentEntries = entries;
			int hashMask = currentEntries.length - 1;
			
			// Find the object (lock free).
			int position = hash & hashMask;
			Entry e = currentEntries[position];
			if(e != null){
				do{
					if(e.get() == prototype) return true;
					e = e.next;
				}while(e != null);
			}
			
			synchronized(this){
				currentEntries = entries;
				hashMask = currentEntries.length - 1;
				
				// Try again while holding the global lock for this segment.
				position = hash & hashMask;
				e = currentEntries[position];
				if(e != null){
					do{
						if(e.get() == prototype) return true;
						e = e.next;
					}while(e != null);
				}
			}
			return false;
		}
		
		/**
		 * Returns a reference to the unique version of the given shared object prototype.
		 * NOTE: This method contains some duplicate code for efficiency reasons.
		 * 
		 * @param prototype
		 *            A prototype matching the shared object we want a reference to.
		 * @param hash
		 *            The hash associated with the given shared object prototype.
		 * @return The reference to the unique version of the shared object.
		 */
		public final SharedObject get(SharedObject prototype, int hash){
			// Cleanup if necessary.
			tryCleanup();
			
			Entry[] currentEntries = entries;
			int hashMask = currentEntries.length - 1;
			
			// Find the object (lock free).
			int position = hash & hashMask;
			Entry e = currentEntries[position];
			if(e != null){
				do{
					if(hash == e.hash){
						SharedObject object = e.get();
						if(object != null){
							if(prototype.equivalent(object)){
								return object;
							}
						}
					}
					e = e.next;
				}while(e != null);
			}
			
			synchronized(this){
				// Try again while holding the global lock for this segment.
				currentEntries = entries;
				hashMask = currentEntries.length - 1;
				position = hash & hashMask;
				e = currentEntries[position];
				if(e != null){
					do{
						if(hash == e.hash){
							SharedObject object = e.get();
							if(object != null){
								if(prototype.equivalent(object)){
									return object;
								}
							}
						}
						e = e.next;
					}while(e != null);
				}
				
				// If we still can't find it, add it.
				ensureCapacity();
				SharedObject result = prototype.duplicate();
				put(result, hash);
				return result;
			}
		}
		
		/**
		 * Generates a unique identifier.
		 * 
		 * @return A unique identifier.
		 */
		private int generateID(){
			if(freeIDsIndex > 0){// Half the size of the freeIDs array when it is empty for three quarters.
				if(freeIDsIndex < (numberOfFreeIDs >> 2) && numberOfFreeIDs > 32){
					int newNumberOfFreeIDs = numberOfFreeIDs >> 1;
					int[] newFreeIds = new int[newNumberOfFreeIDs];
					System.arraycopy(freeIDs, 0, newFreeIds, 0, newNumberOfFreeIDs);
					freeIDs = newFreeIds;
					numberOfFreeIDs = newNumberOfFreeIDs;
				}
				return freeIDs[--freeIDsIndex];
			}
			
			if(nextFreeID != maxFreeIDPlusOne){
				return nextFreeID++;
			}
			
			// We ran out of id's.
			cleanup(); // In a last desperate attempt, try to do a cleanup to free up ids.
			if(freeIDsIndex > 0){
				return freeIDs[--freeIDsIndex];
			}
			
			// If we still can't get a free id throw an exception.
			throw new RuntimeException("No more unique identifiers available for segment("+segmentID+").");
		}
		
		/**
		 * Releases the given unique identifier, so it can be reused.
		 * 
		 * @param id
		 *            The identifier to release.
		 */
		private void releaseID(int id){
			if(freeIDsIndex == numberOfFreeIDs){// Double the size of the freeIDs array when it is full.
				int newNumberOfFreeIDs = numberOfFreeIDs << 1;
				int[] newFreeIds = new int[newNumberOfFreeIDs];
				System.arraycopy(freeIDs, 0, newFreeIds, 0, numberOfFreeIDs);
				freeIDs = newFreeIds;
				numberOfFreeIDs = newNumberOfFreeIDs;
			}
			
			freeIDs[freeIDsIndex++] = id;
		}
		
		/**
		 * Returns statistics.
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString(){
			StringBuilder sb = new StringBuilder();
			
			synchronized(this){
				Entry[] table = entries;
				
				int tableSize = table.length;
				
				sb.append("Table size: ");
				sb.append(tableSize);
				sb.append(", ");
				
				sb.append("Number of entries: ");
				sb.append(load);
				sb.append(", ");
				
				sb.append("Threshold: ");
				sb.append(threshold);
				sb.append(", ");
				
				int nrOfFilledBuckets = 0;
				int totalNrOfCollisions = 0;
				int maxBucketLength = 0;
				for(int i = 0; i < tableSize; i++){
					Entry e = table[i];
					if(e != null){
						nrOfFilledBuckets++;
						int bucketLength = 1;
						while((e = e.next) != null){
							bucketLength++;
						}
						if(bucketLength > maxBucketLength) maxBucketLength = bucketLength;
						totalNrOfCollisions += bucketLength - 1;
					}
				}
				// Do some voodoo to round the results on a certain amount of decimals (3 and 1 respectively); or at least try to do so ....
				double averageBucketLength = 0;
				double distribution = 100;
				if(nrOfFilledBuckets != 0){
					averageBucketLength = (((double) ((totalNrOfCollisions * 1000) / nrOfFilledBuckets)) / 1000) + 1;
					distribution = 100 - (((double) (((totalNrOfCollisions * 1000) / nrOfFilledBuckets) / DEFAULT_LOAD_FACTOR)) / 10);
				}
				
				sb.append("Number of filled buckets: ");
				sb.append(nrOfFilledBuckets);
				sb.append(", ");
				
				sb.append("Load factor: ");
				sb.append(DEFAULT_LOAD_FACTOR);
				sb.append(", ");
				
				sb.append("Distribution (collisions vs filled buckets): "); // Total number of collisions vs number of filled buckets.
				sb.append(distribution);
				sb.append("%, ");
				
				sb.append("Total number of collisions: ");
				sb.append(totalNrOfCollisions);
				sb.append(", ");
				
				sb.append("Average (filled) bucket length: ");
				sb.append(averageBucketLength);
				sb.append(", ");
				
				sb.append("Maximal bucket length: ");
				sb.append(maxBucketLength);
				sb.append(", ");
				
				sb.append("Cleanup scaler: ");
				sb.append(cleanupScaler);
				sb.append("%");
			}
			
			return sb.toString();
		}
		
		/**
		 * An object that can be used to detect when a garbage collection has been executed.
		 * Instances of this object must be made weakly reachable for this to work.
		 * 
		 * @author Arnold Lankamp
		 */
		private static class GarbageCollectionDetector{
			private Segment segment;
			
			/**
			 * Constructor.
			 * 
			 * @param segment
			 *            The segment that we need to flag for cleanup after a garbage collection occurred.
			 */
			public GarbageCollectionDetector(Segment segment){
				this.segment = segment;
			}
			
			/**
			 * Executed after the garbage collector detects that this object is eligible for reclamation.
			 * When this happens it will flag the associated segment for cleanup.
			 * 
			 * @see java.lang.Object#finalize
			 */
			public void finalize(){
				segment.garbageCollectionDetector = null;
				segment.flaggedForCleanup = true;
			}
		}
		
		/**
		 * A bucket entry for a shared object.
		 * 
		 * @author Arnold Lankamp
		 */
		private static class Entry extends WeakReference<SharedObject>{
			public final int hash;
			public volatile Entry next; // This field is not final because we need to change it during cleanup and while rehashing.
			
			/**
			 * Constructor.
			 * 
			 * @param next
			 *            The next entry in the bucket.
			 * @param sharedObject
			 *            The shared object.
			 * @param hash
			 *            The hash that is associated with the given shared object.
			 */
			public Entry(Entry next, SharedObject sharedObject, int hash){
				super(sharedObject);
				
				this.next = next;
				this.hash = hash;
			}
		}
		
		/**
		 * A bucket entry for a shared object with a unique identifier.
		 * 
		 * @author Arnold Lankamp
		 */
		private static class EntryWithID extends Entry{
			public final int id;
			
			/**
			 * Constructor.
			 * 
			 * @param next
			 *            The next entry in the bucket.
			 * @param sharedObjectWithID
			 *            The shared object.
			 * @param hash
			 *            The hash that is associated with the given shared object.
			 * @param id
			 *            The unique identifier.
			 */
			public EntryWithID(Entry next, SharedObjectWithID sharedObjectWithID, int hash, int id){
				super(next, sharedObjectWithID, hash);
				
				this.id = id;
			}
		}
	}
}
