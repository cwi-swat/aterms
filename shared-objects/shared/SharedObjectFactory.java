/*
 * 
 * Copyright (c) 2002, CWI, LORIA-INRIA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met: 
 * 
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer. 
 *  2. Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution. 
 *  3. Neither the name of the CWI or LORIA-INRIA nor the names of
 *     its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written
 *     permission.  
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
 * 
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

  static public int one_at_time_hashFuntion(Object[] o) {
    // [arg1,...,argn,symbol]
    int hash = 0;
    for (int i = 0; i < o.length; i++) {
      hash += o[i].hashCode();
      hash += (hash << 10);
      hash ^= (hash >> 6);
    }
    hash += (hash << 3);
    hash ^= (hash >> 11);
    hash += (hash << 15);
    //return (hash & 0x0000FFFF);
    return hash;
  }

  static public int simple_hashFuntion(Object[] o) {
    // [arg1,...,argn,symbol]
    int hash = o[o.length - 1].hashCode();
    //      res = 65599*res + o[i].hashCode();
    //      res = 16*res + (1+i)*o[i].hashCode();
    for (int i = 0; i < o.length - 1; i++) {
      hash = 16 * hash + o[i].hashCode();
    }
    return hash;
  }

  static public int cwi_hashFunction(Object[] o) {
    // [arg1,...,argn,symbol]
    int hash = 0;
    for (int i = 0; i < o.length; i++) {
      hash = (hash << 1) ^ (hash >> 1) ^ o[i].hashCode();
    }
    return hash;
  }

  static public int doobs_hashFunction(Object[] o) {
    //System.out.println("static doobs_hashFuntion");

    int initval = 0; /* the previous hash value */
    int a, b, c, len;

    /* Set up the internal state */
    len = o.length;
    a = b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    c = initval; /* the previous hash value */

    /*---------------------------------------- handle most of the key */
    int k = 0;
    while (len >= 12) {
      a
        += (o[k + 0].hashCode() + (o[k + 1].hashCode() << 8) + (o[k + 2].hashCode() << 16) + (o[k + 3].hashCode() << 24));
      b
        += (o[k + 4].hashCode() + (o[k + 5].hashCode() << 8) + (o[k + 6].hashCode() << 16) + (o[k + 7].hashCode() << 24));
      c
        += (o[k
          + 8].hashCode()
          + (o[k + 9].hashCode() << 8)
          + (o[k + 10].hashCode() << 16)
          + (o[k + 11].hashCode() << 24));
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
    c += o.length;
    switch (len) /* all the case statements fall through */ {
      case 11 :
        c += (o[k + 10].hashCode() << 24);
      case 10 :
        c += (o[k + 9].hashCode() << 16);
      case 9 :
        c += (o[k + 8].hashCode() << 8);
        /* the first byte of c is reserved for the length */
      case 8 :
        b += (o[k + 7].hashCode() << 24);
      case 7 :
        b += (o[k + 6].hashCode() << 16);
      case 6 :
        b += (o[k + 5].hashCode() << 8);
      case 5 :
        b += o[k + 4].hashCode();
      case 4 :
        a += (o[k + 3].hashCode() << 24);
      case 3 :
        a += (o[k + 2].hashCode() << 16);
      case 2 :
        a += (o[k + 1].hashCode() << 8);
      case 1 :
        a += o[k + 0].hashCode();
        /* case 0: nothing left to add */
    }
    //mix(a,b,c);
    c = mix(a, b, c);

    /*-------------------------------------------- report the result */
    return c;
  }

  static public int doobs_hashFunction(String s, int c) {
    // o[] = [name,Integer(arity), Boolean(isQuoted)]
    // o[] = [value,offset,count,Integer(arity), Boolean(isQuoted)]

    int offset = 0;
    int count = 0;
    char[] source = null;

    count = s.length();
    source = new char[count];
    offset = 0;
    s.getChars(0, count, source, 0);

    int a, b, len;
    /* Set up the internal state */
    len = count;
    a = b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    /*------------------------------------- handle the last 11 bytes */
    int k = offset;

    while (len >= 12) {
      a += (source[k + 0] + (source[k + 1] << 8) + (source[k + 2] << 16) + (source[k + 3] << 24));
      b += (source[k + 4] + (source[k + 5] << 8) + (source[k + 6] << 16) + (source[k + 7] << 24));
      c += (source[k + 8] + (source[k + 9] << 8) + (source[k + 10] << 16) + (source[k + 11] << 24));
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
    /*---------------------------------------- handle most of the key */
    c += count;
    switch (len) {
      case 11 :
        c += (source[k + 10] << 24);
      case 10 :
        c += (source[k + 9] << 16);
      case 9 :
        c += (source[k + 8] << 8);
        /* the first byte of c is reserved for the length */
      case 8 :
        b += (source[k + 7] << 24);
      case 7 :
        b += (source[k + 6] << 16);
      case 6 :
        b += (source[k + 5] << 8);
      case 5 :
        b += source[k + 4];
      case 4 :
        a += (source[k + 3] << 24);
      case 3 :
        a += (source[k + 2] << 16);
      case 2 :
        a += (source[k + 1] << 8);
      case 1 :
        a += source[k + 0];
        /* case 0: nothing left to add */
    }
    
    c = mix(a,b,c);

      //System.out.println("static doobs_hashFuntionAFun = " + c + ": " + s);
    return c;
  }

  private static int mix(int a, int b, int c) {
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
    
    return c;
  }



  /*
   * To build an object
   */

  public Object build(SharedObject prototype) {
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

    foundObj = (SharedObject) prototype.clone();
    tab[index] = new Entry(foundObj, tab[index]);
    nbAdd++;
    tableSize[index]++;
    return foundObj;
  }

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

  private static class Entry extends WeakReference {
    protected Entry next;
    protected int value = 0;
    public Entry(Object object, Entry next) {
      super(object);
      this.next = next;
    }
  }

}
