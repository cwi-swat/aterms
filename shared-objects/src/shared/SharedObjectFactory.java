/*
 * Copyright (c) 2003-2007, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package shared;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public class SharedObjectFactory {
    protected int logSize; // tableSize = 2^logSize

    protected int hashMask; // hashMask = 2^logSize-1

    protected Entry[] table;

    private int[] tableSize;

    private int minThreshold;

    private int maxThreshold;

    private float loadFactor;

    private int[] usedId;

    private int maxId;

    private int minId;

    private int indexId;

    private int currentId;

    private int nbCall = 0;

    private int nbFoundReference = 0;

    private int nbFoundExactReference = 0;

    private int nbAdd = 0;

    private int nbRemoved = 0;

    private int nbProjectionCollision = 0;

    // private int nbHashingCollision = 0;

    private int nbSwapEntry = 0;

    private int nbIdRegeneration = 0;

    public SharedObjectFactory() {
        this(10);
    }

    public SharedObjectFactory(int initialLogSize) {

        this.logSize = initialLogSize;
        this.hashMask = hashMask();
        this.table = new Entry[hashSize()];

        this.tableSize = new int[hashSize()];
        this.loadFactor = 3.0f;

        this.maxThreshold = (int) (hashSize() * loadFactor);
        this.minThreshold = 0;

        this.maxId = (1 << 31) - 1;
        this.minId = -(1 << 31);
        this.currentId = this.minId;
        this.indexId = 0;
        this.usedId = new int[1];
        this.usedId[0] = this.maxId;
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
    }

    /*
     * Remove unused references
     */
    public void cleanup() {
        Entry tab[] = table;

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

    }

    public String toString() {
        Entry tab[] = table;

        String s = "";
        s += "table_size            = " + table.length + "\n";
        s += "#call                 = " + nbCall + "\n";
        s += "nbFoundReference      = " + nbFoundReference + "\n";
        s += "nbFoundExactReference = " + nbFoundExactReference + "\n";
        s += "#BuiltObject          = " + nbAdd + "\n";
        s += "#RemovedReference     = " + nbRemoved + "\n";
        s += "#ID Regeneration      = " + nbIdRegeneration + "\n";

        double repartition = 0.0;
        double n = (nbAdd - nbRemoved);
        double m = tab.length;

        for (int idx = 0; idx < tab.length; idx++) {
            double bj = tableSize[idx];
            repartition += bj * (1 + bj) / 2;
        }

        s += "#element              = " + (int) n + " [" + (nbAdd - nbRemoved)
                + "]\n";

        double bestRepartition = (n / (2.0 * m)) * (n + 2.0 * m - 1.0);

        s += "repartition           = " + (repartition / bestRepartition)
                + "\n";
        s += "#lookup/build         = "
                + ((double) (nbFoundReference + nbAdd) / (double) nbCall)
                + "\n";
        s += "projectionCollision   = " + nbProjectionCollision + "\n";
        //s += "hashingCollision      = " + nbHashingCollision + "\n";
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

        }
        s += "used = " + (tab.length - emptyEntry) + "\tempty = " + emptyEntry;

        return s;
    }

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
                }
                // create or lookup collision
                nbProjectionCollision++;
                //if (foundObj.hashCode() == hash) {
                //    nbHashingCollision++;
                //}

                prev = e;
            }
        }

        // No similar SharedObject found, so build a new one
        int count = nbAdd - nbRemoved;
        if (false && count < minThreshold) {
            // System.out.println("count = " + count + " < tminThreshold = " +
            // minThreshold);
            decreaseCapacity();
            tab = table;
            index = hashKey(hash);
        } else if (count >= maxThreshold) {
            /*
             * Very simple strategy: - try a cleanup - rehash next time
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
        if (prototype instanceof SharedObjectWithID) {
            ((SharedObjectWithID) foundObj).setUniqueIdentifier(getFreshId());
        }
        tab[index] = new Entry(foundObj, tab[index]);
        nbAdd++;
        tableSize[index]++;

        return foundObj;
    }

    private int getFreshId() {
        // System.out.println("CurrentId: "+currentId+" vs "+usedId[indexId]);
        if(currentId < usedId[indexId]) {
          //if(currentId == -(1 << 31)) {
          // System.out.println("First ID"+currentId);
          //}
          return currentId++;
        }
        // We try the next index in the usedId array
        do {
            indexId++;
            if (indexId < usedId.length) {
                // System.out.println("loop CurrentId: "+(currentId+1)+" vs
                // "+usedId[indexId]);
                if (++currentId < usedId[indexId]) {
                    // System.out.println("ID"+currentId);
                    return currentId++;
                }
            } else {
                regenerate();
                return getFreshId();
            }
        } while (true);
    }

    private void regenerate() {
        nbIdRegeneration++;
        // System.out.println("Regeneration of fresh unique IDs");
        ArrayList<Integer> list = new ArrayList<Integer>();
        // Collect all used Ids
        for (int i = 0; i < table.length; i++) {
            for (Entry e = table[i]; e != null; e = e.next) {

                if (e.get() instanceof SharedObjectWithID)
                    list.add(new Integer(((SharedObjectWithID) e.get())
                            .getUniqueIdentifier()));
            }
        }
        list.add(new Integer(maxId));
        int newSize = list.size();
        if(newSize >= (maxId - minId)) {
          throw new RuntimeException("No more unique identifier");
        }
        usedId = new int[newSize];
        for(int i = 0; i < newSize; i++) {
            usedId[i] = (list.get(i)).intValue();
        }
        // Sort array and reinitialize every thing
        Arrays.sort(usedId);
        // System.out.println("New Array with size "+newSize+" from
        // "+usedId[0]+" to "+usedId[newSize-2]);
        indexId = 0;
        currentId = minId;
    }

    private static class Entry extends WeakReference<Object> {
        protected Entry next;

        protected int value = 0;

        public Entry(Object object, Entry next) {
            super(object);
            this.next = next;
        }
    }

}
