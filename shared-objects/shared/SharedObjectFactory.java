/* Copyright (c) 2003, CWI, LORIA-INRIA All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 */

package shared;

import java.lang.ref.WeakReference;

public class SharedObjectFactory {
  protected int logSize; // tableSize = 2^logSize
  protected int hashMask; // hashMask  = 2^logSize-1
  protected Entry[] table;

  private int[] tableSize;
  private int initialCapacity;
  private int minThreshold;
  private int maxThreshold;
  private float loadFactor;

  private int nbCall = 0;
  private int nbFoundReference = 0;
  private int nbFoundExactReference = 0;
  private int nbAdd = 0;
  private int nbRemoved = 0;
  private int nbProjectionCollision = 0;
  private int nbHashingCollision = 0;
  private int nbSwapEntry = 0;

  public SharedObjectFactory() {
    this(10);
  }

  public SharedObjectFactory(int initialLogSize) {

    
    this.logSize = initialLogSize;
    this.hashMask = hashMask();
    this.table = new Entry[hashSize()];

    // for statistics
    this.tableSize = new int[hashSize()];
    this.initialCapacity = hashSize();
    this.loadFactor = 3.0f; //0.75f;
      //this.loadFactor = 0.75f;

    this.maxThreshold = (int) (hashSize() * loadFactor);
    this.minThreshold = 0;
  }

  private int hashSize(int n) {
    return (1 << n);
  }

  private int hashSize() {
    return hashSize(logSize);
  }

  private int hashMask() {
    return (hashSize() - 1);
  }

  private int hashKey(int key) {
    return (key & hashMask);
  }

  private boolean cleanupDone = false;

  protected void decreaseCapacity() {
    int oldLogSize = logSize;
    logSize -= 2;
    hashMask = hashMask();

    maxThreshold = (int) (hashSize() * loadFactor);
    minThreshold = maxThreshold / 10;

    rehash(hashSize(oldLogSize));
  }

  protected void increaseCapacity() {
    int oldLogSize = logSize;
    logSize++;
    hashMask = hashMask();

    maxThreshold = (int) (hashSize() * loadFactor);
    minThreshold = maxThreshold / 10;

    rehash(hashSize(oldLogSize));
  }

  protected void rehash(int oldCapacity) {
    Entry oldMap[] = table;
    table = new Entry[hashSize()];
    tableSize = new int[hashSize()];

    int oldRemoved = nbRemoved;
    for (int i = oldCapacity; i-- > 0;) {
      for (Entry old = oldMap[i]; old != null;) {
        // Remove references to garbage collected term
        while (old != null && old.get() == null) {
          old = old.next;
          nbRemoved++;
        }

        if (old != null) {
          Entry e = old;
          old = old.next;
          int index = hashKey(e.get().hashCode());
          e.next = table[index];
          table[index] = e;

          tableSize[index]++;
        }

      }
    }
    oldMap = null;
    System.out.println("rehash: newCapacity = " + hashSize() + " (" + (nbRemoved - oldRemoved) + " removal)");
    //System.out.println(this);
  }

  /*
   * Remove unused references
   */
  public void cleanup() {
    Entry tab[] = table;
    int oldRemoved = nbRemoved;
    for (int i = tab.length; i-- > 0;) {
      for (Entry e = tab[i], prev = null; e != null; e = e.next) {
        if (e.get() == null) {
          // Remove a reference to a garbage collected term
          if (prev != null) {
            prev.next = e.next;
          } else {
            tab[i] = e.next;
          }
          nbRemoved++;
          tableSize[i]--;
        } else {
          prev = e;
        }
      }
    }
    System.out.println("cleanup: " + (nbRemoved - oldRemoved) + " removed references");

  }
  /*
    static ReferenceQueue rq = new ReferenceQueue();  
    public void cleanup() {
    Entry e;
      //System.gc();
      System.out.println("cleanup2: ");
      while ((e = (Entry)rq.poll()) != null) {
      System.out.println("e = " + e);
      }
      }
  */

  public String toString() {
    Entry tab[] = table;

    //System.out.println("start cleanup");
    //System.gc();
    //cleanup();

    String s = "";
    s += "table_size            = " + table.length + "\n";
    s += "#call                 = " + nbCall + "\n";
    s += "nbFoundReference      = " + nbFoundReference + "\n";
    s += "nbFoundExactReference = " + nbFoundExactReference + "\n";
    s += "#BuiltObject          = " + nbAdd + "\n";
    s += "#RemovedReference     = " + nbRemoved + "\n";

    double repartition = 0.0;
    double n = (double) (nbAdd - nbRemoved);
    double m = (double) tab.length;

    for (int idx = 0; idx < tab.length; idx++) {
      double bj = (double) tableSize[idx];
      repartition += bj * (1 + bj) / 2;
    }

    s += "#element              = " + (int) n + " [" + (nbAdd - nbRemoved) + "]\n";

    double bestRepartition = (n / (2.0 * m)) * (n + 2.0 * m - 1.0);

    s += "repartition           = " + (repartition / bestRepartition) + "\n";
    s += "#lookup/build         = " + ((double) (nbFoundReference + nbAdd) / (double) nbCall) + "\n";
    //((double)(nbFoundReference) / (double)nbCall) + "\n";
    s += "projectionCollision   = " + nbProjectionCollision + "\n";
    s += "hashingCollision      = " + nbHashingCollision + "\n";
    s += "swapEntry             = " + nbSwapEntry + "\n";

    // STAT
    int emptyEntry = 0;
    int usedSlot = 0;
    for (int idx = 0; idx < tab.length; idx++) {
      if (tab[idx] != null) {
        usedSlot = 0;
        for (Entry e = tab[idx]; e != null; e = e.next) {
          if (e.get() != null) {
            usedSlot++;
          }
        }
      } else {
        emptyEntry++;
      }

      if(false) {
        if (tableSize[idx] > 2 * loadFactor) {
          System.out.println(idx + " --> " + tableSize[idx] + " elements (usedSlot = " + usedSlot + ")");
          
          for(Entry e = tab[idx] ; e != null ; e = e.next) {
            System.out.println("\t" + e.get());
          }
        }
      }
    }
    //s += "maxThreshold = " + maxThreshold + "\n";
    s += "used = " + (tab.length - emptyEntry) + "\tempty = " + emptyEntry;

    return s;
  }

 

  /*
   * To build an object
   */

  public SharedObject build(SharedObject prototype) {
    nbCall++;

    SharedObject foundObj;
    Entry tab[] = table;
    int hash = prototype.hashCode();
    int index = hashKey(hash);

    for (Entry e = tab[index], prev = null; e != null; e = e.next) {
      foundObj = (SharedObject) e.get();
      if (foundObj == null) {
        // Found a reference to a garbage collected term
        // remove it to speed up lookups.
        if (prev != null) {
          prev.next = e.next;
          e.clear();
        } else {
          tab[index] = e.next;
          e.clear();
        }
        nbRemoved++;
        tableSize[index]--;
      } else {
        // Found a reference
        nbFoundReference++;
        if (prototype.equivalent(foundObj)) {
          nbFoundExactReference++;

          // swap the found object
          if (prev != null && (e.value - tab[index].value > 5)) {
            nbSwapEntry++;
            prev.next = e.next;
            e.next = tab[index];
            tab[index] = e;
          }
          e.value++;
          return foundObj;
        } else {
          // create or lookup collision
          nbProjectionCollision++;
          if (foundObj.hashCode() == hash) {
            nbHashingCollision++;
              //System.out.println("*** hashing collision ***");
              //System.out.println("proto  = " + prototype);
              //System.out.println(" found = " + foundObj);
          }
        }

        prev = e;
      }
    }

    // No similar SharedObject found, so build a new one
    int count = nbAdd - nbRemoved;
    if (false && count < minThreshold) {
      System.out.println("count = " + count + " < tminThreshold = " + minThreshold);
      decreaseCapacity();
      tab = table;
      index = hashKey(hash);
    } else if (count >= maxThreshold) {
      /*
       * Very simple strategy:
       *  - try a cleanup
       *  - rehash next time
       */
      if (!cleanupDone) {
        cleanupDone = true;
        System.gc();
        cleanup();
      } else {
        cleanupDone = false;
        increaseCapacity();
        tab = table;
        index = hashKey(hash);
      }
    }

    foundObj = prototype.duplicate();
    tab[index] = new Entry(foundObj, tab[index]);
    nbAdd++;
    tableSize[index]++;
    return foundObj;
  }

  /* 
   * Jurgen: removed because possibly never used
   *
   static public String encodeString(String value) {
    int len = value.length();
    char[] source = new char[len];
    char[] dest = new char[3 * len];
    // store the string in an array of chars
    value.getChars(0, len, source, 0);
    // copy and insert some escape chars
    for (int i = 0, pos = 0; i < len; i++) {
      if (source[i] == '"' || source[i] == '\\') {
        dest[pos++] = '\\';
        dest[pos++] = source[i];
      } else if (source[i] == '\n') {
        dest[pos++] = '\\';
        dest[pos++] = 'n';
      } else if (source[i] == '\t') {
        dest[pos++] = '\\';
        dest[pos++] = 't';
      } else {
        dest[pos++] = source[i];
      }
    }
    return new String(dest);
  }
  */

  private static class Entry extends WeakReference {
    protected Entry next;
    protected int value = 0;
    public Entry(Object object, Entry next) {
      super(object);
      this.next = next;
    }
  }

}
