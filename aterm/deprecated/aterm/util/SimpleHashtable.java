package aterm.util;

import java.util.*;
import java.io.*;

public class SimpleHashtable
{
  private SimpleHashtableEntry table[];
  private int count;
  private int threshold;
  private float loadFactor;

  public SimpleHashtable(int initialCapacity, float loadFactor)
  {
    if((initialCapacity <= 0) || (loadFactor <= 0.0)) {
       throw new IllegalArgumentException();
    }
    this.loadFactor = loadFactor;
    table = new SimpleHashtableEntry[initialCapacity];
    threshold = (int)(initialCapacity * loadFactor);
  }
  public SimpleHashtable(int initialCapacity)
  {
    this(initialCapacity, 0.75f);
  }
  public SimpleHashtable()
  {
    this(101, 0.75f);
  }
  public int size()
  {
    return count;
  }
  public boolean isEmpty()
  {
    return count == 0;
  }
  public Enumeration elements()
  {
    return new SimpleHashtableEnumerator(table);
  }
  public synchronized boolean contains(SimpleHashtableEntry el)
  {
    int index = (el.hashCode() & 0x7FFFFFFF) % table.length;
    for(SimpleHashtableEntry e=table[index]; e!=null; e=e.getNextHashEntry()) {
      if(e.equals(el))
        return true;
    }
    return false;
  }
  public synchronized SimpleHashtableEntry get(SimpleHashtableEntry el)
  {
    int index = (el.hashCode() & 0x7FFFFFFF) % table.length;
    for(SimpleHashtableEntry e=table[index]; e!=null; e=e.getNextHashEntry()) {
      if(e.equals(el))
        return e;
    }
    return null;
  }
  public synchronized void put(SimpleHashtableEntry el)
  {
    int index = (el.hashCode() & 0x7FFFFFFF) % table.length;
    el.setNextHashEntry(table[index]);
    table[index] = el;
    count++;
  }
  public synchronized void remove(SimpleHashtableEntry el)
  {
    int index = (el.hashCode() & 0x7FFFFFFF) % table.length;
    SimpleHashtableEntry cur, prev = null;
    for(cur = table[index]; cur != null; cur = cur.getNextHashEntry()) {
      if(cur.equals(el)) {
        if(prev == null)
          table[index] = cur.getNextHashEntry();
        else
          prev.setNextHashEntry(cur.getNextHashEntry());
        count--;
        return;
      }
      prev = cur;
    }
  }
  protected synchronized void rehash()
  {
    int oldCapacity = table.length;
    SimpleHashtableEntry oldTable[] = table;

    int newCapacity = oldCapacity * 2 + 1;
    table = new SimpleHashtableEntry[newCapacity];

    threshold = (int)(newCapacity * loadFactor);

    for(int i = oldCapacity; i >= 0; i--) {
      SimpleHashtableEntry next;
      for(SimpleHashtableEntry e = oldTable[i]; e != null; e = next) {
        next = e.getNextHashEntry();
        int index = (e.hashCode() & 0x7FFFFFFF) % newCapacity;
        e.setNextHashEntry(table[index]);
        table[index] = e;
      }
    }
  }
}
class SimpleHashtableEnumerator implements Enumeration
{
  int index;
  SimpleHashtableEntry table[];
  SimpleHashtableEntry entry;

  SimpleHashtableEnumerator(SimpleHashtableEntry tb[]) {
    table = tb;
    index = table.length;
  }

  public synchronized boolean hasMoreElements() {
    if(entry != null)
      return true;
    while(index-- > 0) {
      if((entry = table[index]) != null)
        return true;
    }
    return false;
  }

  public synchronized Object nextElement() {
    if(entry == null)
      while((index-- > 0) && ((entry = table[index]) == null))
        ;
    if(entry != null) {
      SimpleHashtableEntry e = entry;
      entry = e.getNextHashEntry();
      return e;
    }
    throw new NoSuchElementException("SimpleHashtableEnumerator");
  }
}
