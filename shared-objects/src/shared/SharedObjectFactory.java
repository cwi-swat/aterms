/*
 * Copyright (c) 2003-2007, CWI and INRIA
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met: 
 * 	- Redistributions of source code must retain the above copyright
 * 	notice, this list of conditions and the following disclaimer.  
 * 	- Redistributions in binary form must reproduce the above copyright
 * 	notice, this list of conditions and the following disclaimer in the
 * 	documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the CWI, INRIA nor the names of its
 * 	contributors may be used to endorse or promote products derived from
 * 	this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package shared;

import java.lang.ref.WeakReference;

/**
 * The SharedObjectsFactory is a 'weak' constant pool for uniquely represented objects.
 * <br /><br />
 * This class is fully thread-safe, but tries to avoid (contended) locking as much as is
 * reasonably achievable. As a result this implementation should scale fairly well on
 * multi-core / processor systems; while also limiting synchronization overhead.
 * 
 * WARNING: Do not edit this class unless you fully understand both Java's memory and threading model
 * and how the garbage collector(s) work. (You'll likely 'break' something otherwise).<br />
 * The JMM spec (previously known as JSR-133) can be found here: <a href=http://java.sun.com/docs/books/jls/third_edition/html/memory.html>http://java.sun.com/docs/books/jls/third_edition/html/memory.html</a><br />
 * 
 * @author Arnold Lankamp
 */
public class SharedObjectFactory {
  private final static int DEFAULT_NR_OF_SEGMENTS_BITSIZE = 5;

  private final Segment[] segments;

  /**
   * Default constructor.
   */
  public SharedObjectFactory() {
    super();

    segments = new Segment[1 << DEFAULT_NR_OF_SEGMENTS_BITSIZE];
    for(int i = segments.length - 1; i >= 0; i--) {
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
  public SharedObjectFactory(int initialLogSize) {
    this();
  }

  /**
   * Removes stale entries from the set.
   */
  public void cleanup() {
    int nrOfSegments = segments.length;
    for(int i = 0; i < nrOfSegments; i++) {
      Segment segment = segments[i];
      synchronized(segment) {
        segment.cleanup();
      }
    }
  }

  /**
   * Returns statistics.
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int nrOfSegments = segments.length;
    for(int i = 0; i < nrOfSegments; i++) {
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
   *            The prototype of the shared object we want the unique reference to.
   * @return The reference to the unique shared object associated with the argument.
   */
  public SharedObject build(SharedObject prototype) {
    int hash = prototype.hashCode();
    int segmentNr = hash >>> (32 - DEFAULT_NR_OF_SEGMENTS_BITSIZE);

    return segments[segmentNr].get(prototype, hash);
  }

  /**
   * A segment is a hashtable that represents a certain part of the 'hashset'.
   */
  private static class Segment {
    private final static int MAX_SEGMENT_BITSIZE = 32 - DEFAULT_NR_OF_SEGMENTS_BITSIZE;
    private final static int DEFAULT_SEGMENT_BITSIZE = 5;
    private final static float DEFAULT_LOAD_FACTOR = 2f;

    private volatile Entry[] entries;

    private volatile int hashMask;
    private int bitSize;

    private int threshold;
    private int load;

    private boolean attemptCleanup;
    private int cleanupPercentate;

    private final int segmentID;

    private FreeID freeIDList;
    private int nextFreeID;
    private final int maxFreeIDPlusOne;

    /**
     * Constructor.
     * 
     * @param segmentID
     *            The number identifying this segment.
     */
    public Segment(int segmentID) {
      super();

      this.segmentID = segmentID;

      bitSize = DEFAULT_SEGMENT_BITSIZE;
      int nrOfEntries = 1 << bitSize;

      hashMask = nrOfEntries - 1;

      entries = new Entry[nrOfEntries];

      threshold = (int) (nrOfEntries * DEFAULT_LOAD_FACTOR);
      load = 0;

      attemptCleanup = true;
      cleanupPercentate = 0;

      freeIDList = null;
      nextFreeID = segmentID << MAX_SEGMENT_BITSIZE;
      maxFreeIDPlusOne = (segmentID + 1) << MAX_SEGMENT_BITSIZE;
    }

    /**
     * Removes entries, who's value have been garbage collected, from this segment.
     */
    private void cleanup() {
      int newLoad = load;
      Entry[] table = entries;

      for(int i = table.length - 1; i >= 0; i--) {
        Entry e = table[i];
        if(e != null) {
          Entry previous = null;
          do {
            Entry next = e.next;

            if(e.get() == null) {
              if(previous == null) {
                table[i] = next;
              } else {
                previous.next = next;
              }

              newLoad--;

              if(e instanceof EntryWithID) {
                EntryWithID ewid = (EntryWithID) e;
                releaseID(ewid.id);
              }
            } else {
              previous = e;
            }

            e = next;
          } while(e != null);
        }
      }

      load = newLoad;

      entries = table; // Create happens-before edge, to ensure potential changes become visible.
    }

    /**
     * Ensures the load in this segment will not exceed a certain threshold. First we will try
     * to do a cleanup; if this is successfull enough (if 20%+ of the table was cleaned) we'll
     * do a cleanup next time we run low on space as well. Otherwise we'll do a rehash. This
     * strategy prevents the segment from growing to large (with would result in space being
     * wasted and / or a memory leak).<br />
     * NOTE: When rehashing the entries will remain in the same order; so there are only
     * 'young -> old' references and not the other way around. This will reduce 'minor' garbage
     * collection times.
     */
    private void ensureCapacity() {
      if(load > threshold) {
        if(bitSize >= MAX_SEGMENT_BITSIZE) attemptCleanup = true; // We won't do any more rehashes if the segment is already streched to it's maximum (since this would be a useless thing to do).

        if(attemptCleanup) { // Cleanup
          int oldLoad = load;

          cleanup();

          cleanupPercentate = 100 - ((load * 100) / oldLoad);
          if(cleanupPercentate < 20) attemptCleanup = false; // Rehash the next time the table reaches it's threshold if we didn't clean enough (otherwise the cleanups start to cost too much performance % wise).
        } else { // Rehash
          attemptCleanup = true;

          int nrOfEntries = 1 << (++bitSize);
          int newHashMask = nrOfEntries - 1;

          Entry[] oldEntries = entries;
          Entry[] newEntries = new Entry[nrOfEntries];

          Entry currentEntryRoot = new Entry(null, 0);
          Entry shiftedEntryRoot = new Entry(null, 0);
          int oldSize = oldEntries.length;

          for(int i = oldSize - 1; i >= 0; i--) {
            Entry e = oldEntries[i];
            if(e != null) {
              Entry lastCurrentEntry = currentEntryRoot;
              Entry lastShiftedEntry = shiftedEntryRoot;
              do {
                if(e.get() != null) {
                  int position = e.hash & newHashMask;

                  if(position == i) {
                    lastCurrentEntry.next = e;
                    lastCurrentEntry = e;
                  } else {
                    lastShiftedEntry.next = e;
                    lastShiftedEntry = e;
                  }
                } else {
                  load--;
                }

                e = e.next;
              } while(e != null);

              lastShiftedEntry.next = null;
              lastCurrentEntry.next = null;

              newEntries[i] = currentEntryRoot.next;
              newEntries[i | oldSize] = shiftedEntryRoot.next; // The entries got shifted by the size of the old table.
            }
          }

          threshold <<= 1;
          entries = newEntries;
          hashMask = newHashMask;
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
    private void put(SharedObject object, int hash) {
      // Assign an id if needed.
      Entry e;
      if(object instanceof SharedObjectWithID) {
        SharedObjectWithID sharedObjectWithID = (SharedObjectWithID) object;
        int id = generateID();
        sharedObjectWithID.setUniqueIdentifier(id);

        e = new EntryWithID(object, hash, id);
      } else {
        e = new Entry(object, hash);
      }

      Entry[] table = entries;
      int position = hash & hashMask;
      e.next = table[position];
      table[position] = e;

      load++;

      table = entries; // Create a happens-before edge for the added entry, to ensure visibility.
    }

    /**
     * Locates a reference to the shared version of the given object prototype (if present).
     * Note that this method also removes stale entries from the bucket and should ONLY be
     * called while holding the global lock for this segment; failing to do so will result
     * in undefined behaviour.
     * 
     * @param prototype
     *            A prototype matching the shared object we want a reference to.
     * @param hash
     *            The hash associated with the given shared object prototype.
     * @return The found shared object; null if it has not been found.
     */
    private SharedObject findObjectWhileUnderLock(SharedObject prototype, int hash) {
      int position = hash & hashMask;

      Entry[] table = entries;
      Entry e = table[position];
      Entry previous = null;
      if(e != null) {
        do {
          Entry next = e.next;
          SharedObject object = e.get();
          if(object != null) {
            if(hash == e.hash && prototype.equivalent(object)) {
              return object;
            }
            previous = e;
          } else {
            if(previous == null) {
              table[position] = next;
            } else {
              previous.next = next;
            }

            load--;

          }
          e = next;
        } while(e != null);
        entries = table; // Create a happens-before edge, to ensure visibility of the change.
      }

      return null;
    }

    /**
     * Returns a reference to the unique version of the given shared object prototype.
     * 
     * @param prototype
     *            A prototype matching the shared object we want a reference to.
     * @param hash
     *            The hash associated with the given shared object prototype.
     * @return The reference to the unique version of the shared object.
     */
    public final SharedObject get(SharedObject prototype, int hash) {
      // Find the object (lock free).
      Entry stop = entries[hash&hashMask];
      Entry e = stop;
      if(e != null) {
        do {
          if(hash == e.hash) {
            SharedObject object = e.get();
            if(object != null) {
              if(prototype.equivalent(object)) {
                return object;
              }
            }
          }
          e = e.next;
        } while(e != null);
      }

      synchronized(this) {
        // Try again while holding the global lock for this segment.
        if(entries[hash&hashMask] != stop) {
          SharedObject result = findObjectWhileUnderLock(prototype, hash);
          if(result != null) return result;
        }

        // If we still can't find it, add it.
        ensureCapacity();
        SharedObject result = prototype.duplicate();
        put(result, hash);
        return result;
      }

    }

    /**
     * A linked list node holding a free identifier.
     */
    private final static class FreeID {
      public final int id;
      public final FreeID next;

      public FreeID(int id, FreeID next) {
        this.id = id;
        this.next = next;
      }
    }

    /**
     * Generates a unique identifier.
     * 
     * @return A unique identifier.
     */
    private int generateID() {
      int id;
      if(freeIDList != null) {
        id = freeIDList.id;
        freeIDList = freeIDList.next;
      } else {
        id = nextFreeID++;
        if(id == maxFreeIDPlusOne) { // We ran out of id's.
          nextFreeID--; // Roll back, otherwise errors might occur on the next call.
          cleanup(); // Do a cleanup and try again.
          if(freeIDList != null) {
            id = freeIDList.id;
            freeIDList = freeIDList.next;
          } else {
            // If we still can't get a free id throw an exception.
            throw new RuntimeException("No more unique identifiers available for segment("+segmentID+").");
          }
        }
      }
      return id;
    }

    /**
     * Releases the given unique identifier, so it can be reused.
     * 
     * @param id
     *            The identifier to release.
     */
    private void releaseID(int id) {
      freeIDList = new FreeID(id, freeIDList);
    }

    /**
     * Returns statistics.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
      StringBuilder sb = new StringBuilder();

      synchronized(this) {
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
        for(int i = 0; i < tableSize; i++) {
          Entry e = table[i];
          if(e != null) {
            nrOfFilledBuckets++;
            int bucketLength = 1;
            while((e = e.next) != null) {
              bucketLength++;
            }
            if(bucketLength > maxBucketLength) maxBucketLength = bucketLength;
            totalNrOfCollisions += bucketLength - 1;
          }
        }
        // Do some voodoo to round the results on a certain amount of decimals (3 and 1 respectively); or at least try to do so ....
        double averageBucketLength = 0;
        double distribution = 100;
        if(nrOfFilledBuckets != 0) {
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
      }

      return sb.toString();
    }
  }

  /**
   * A bucket entry for a shared object.
   * 
   * @author Arnold Lankamp
   */
  private static class Entry extends WeakReference<SharedObject> {
    public final int hash;
    public Entry next; // This field is not final because we need to change it during cleanup and while rehashing.

    /**
     * Constructor.
     * 
     * @param sharedObject
     *            The shared object.
     * @param hash
     *            The hash that is associated with the given shared object.
     */
    public Entry(SharedObject sharedObject, int hash) {
      super(sharedObject);

      this.hash = hash;
    }
  }

  /**
   * A bucket entry for a shared object with a unique identifier.
   * 
   * @author Arnold Lankamp
   */
  private static class EntryWithID extends Entry {
    public final int id;

    /**
     * Constructor.
     * 
     * @param sharedObject
     *            The shared object.
     * @param hash
     *            The hash that is associated with the given shared object.
     * @param id
     *            The unique identifier.
     */
    public EntryWithID(SharedObject sharedObject, int hash, int id) {
      super(sharedObject, hash);

      this.id = id;
    }
  }
}
