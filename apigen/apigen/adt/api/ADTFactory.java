package apigen.adt.api;

import aterm.*;
import aterm.pure.PureFactory;
public class ADTFactory extends PureFactory
{
  private aterm.AFun funEntries_Empty;
  private Entries protoEntries_Empty;
  private aterm.ATerm patternEntries_Empty;
  private aterm.AFun funEntries_List;
  private Entries protoEntries_List;
  private aterm.ATerm patternEntries_List;
  private aterm.AFun funEntry_Constructor;
  private Entry protoEntry_Constructor;
  private aterm.ATerm patternEntry_Constructor;
  private aterm.AFun funEntry_List;
  private Entry protoEntry_List;
  private aterm.ATerm patternEntry_List;
  public ADTFactory()
  {
     super();
     initialize();
  }
  public ADTFactory(int logSize)
  {
     super(logSize);
     initialize();
  }
  private void initialize()
  {

    patternEntries_Empty = parse("[]");
    funEntries_Empty = makeAFun("_Entries_Empty", 0, false);
    protoEntries_Empty = new Entries_Empty(this);

    patternEntries_List = parse("[<term>,<list>]");
    funEntries_List = makeAFun("_Entries_List", 2, false);
    protoEntries_List = new Entries_List(this);


    patternEntry_Constructor = parse("constructor(<term>,<term>,<term>)");
    funEntry_Constructor = makeAFun("_Entry_Constructor", 3, false);
    protoEntry_Constructor = new Entry_Constructor(this);

    patternEntry_List = parse("list(<term>,<term>)");
    funEntry_List = makeAFun("_Entry_List", 2, false);
    protoEntry_List = new Entry_List(this);

  }
  protected Entries_Empty makeEntries_Empty(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (protoEntries_Empty) {
      protoEntries_Empty.initHashCode(annos,fun,args);
      return (Entries_Empty) build(protoEntries_Empty);
    }
  }

  public Entries_Empty makeEntries_Empty() {
    aterm.ATerm[] args = new aterm.ATerm[] {};
    return makeEntries_Empty( funEntries_Empty, args, empty);
  }

  public Entries Entries_EmptyFromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(patternEntries_Empty);

    if (children != null) {
      Entries tmp = makeEntries_Empty();
      tmp.setTerm(trm);
      return tmp;
    }
    else {
      return null;
    }
  }
  protected aterm.ATerm toTerm(Entries_EmptyImpl arg) {
    java.util.List args = new java.util.LinkedList();
    return make(patternEntries_Empty, args);
  }

  protected Entries_List makeEntries_List(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (protoEntries_List) {
      protoEntries_List.initHashCode(annos,fun,args);
      return (Entries_List) build(protoEntries_List);
    }
  }

  public Entries_List makeEntries_List(Entry _head, Entries _tail) {
    aterm.ATerm[] args = new aterm.ATerm[] {_head, _tail};
    return makeEntries_List( funEntries_List, args, empty);
  }

  public Entries Entries_ListFromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(patternEntries_List);

    if (children != null) {
      Entries tmp = makeEntries_List(EntryFromTerm( (aterm.ATerm) children.get(0)), EntriesFromTerm( (aterm.ATerm) children.get(1)));
      tmp.setTerm(trm);
      return tmp;
    }
    else {
      return null;
    }
  }
  protected aterm.ATerm toTerm(Entries_ListImpl arg) {
    java.util.List args = new java.util.LinkedList();
    args.add(((Entry)arg.getArgument(0)).toTerm());
    args.add(((Entries)arg.getArgument(1)).toTerm());
    return make(patternEntries_List, args);
  }

  protected Entry_Constructor makeEntry_Constructor(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (protoEntry_Constructor) {
      protoEntry_Constructor.initHashCode(annos,fun,args);
      return (Entry_Constructor) build(protoEntry_Constructor);
    }
  }

  public Entry_Constructor makeEntry_Constructor(aterm.ATerm _sort, aterm.ATerm _alternative, aterm.ATerm _termPattern) {
    aterm.ATerm[] args = new aterm.ATerm[] {_sort, _alternative, _termPattern};
    return makeEntry_Constructor( funEntry_Constructor, args, empty);
  }

  public Entry Entry_ConstructorFromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(patternEntry_Constructor);

    if (children != null) {
      Entry tmp = makeEntry_Constructor((aterm.ATerm) children.get(0), (aterm.ATerm) children.get(1), (aterm.ATerm) children.get(2));
      tmp.setTerm(trm);
      return tmp;
    }
    else {
      return null;
    }
  }
  protected aterm.ATerm toTerm(Entry_ConstructorImpl arg) {
    java.util.List args = new java.util.LinkedList();
    args.add((aterm.ATerm)arg.getArgument(0));
    args.add((aterm.ATerm)arg.getArgument(1));
    args.add((aterm.ATerm)arg.getArgument(2));
    return make(patternEntry_Constructor, args);
  }

  protected Entry_List makeEntry_List(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (protoEntry_List) {
      protoEntry_List.initHashCode(annos,fun,args);
      return (Entry_List) build(protoEntry_List);
    }
  }

  public Entry_List makeEntry_List(aterm.ATerm _sort, aterm.ATerm _elemSort) {
    aterm.ATerm[] args = new aterm.ATerm[] {_sort, _elemSort};
    return makeEntry_List( funEntry_List, args, empty);
  }

  public Entry Entry_ListFromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(patternEntry_List);

    if (children != null) {
      Entry tmp = makeEntry_List((aterm.ATerm) children.get(0), (aterm.ATerm) children.get(1));
      tmp.setTerm(trm);
      return tmp;
    }
    else {
      return null;
    }
  }
  protected aterm.ATerm toTerm(Entry_ListImpl arg) {
    java.util.List args = new java.util.LinkedList();
    args.add((aterm.ATerm)arg.getArgument(0));
    args.add((aterm.ATerm)arg.getArgument(1));
    return make(patternEntry_List, args);
  }

  public Entries EntriesFromTerm(aterm.ATerm trm)
  {
    Entries tmp;
    tmp = Entries_EmptyFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    tmp = Entries_ListFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }


    throw new RuntimeException("This is not a Entries: " + trm);
  }
  public Entry EntryFromTerm(aterm.ATerm trm)
  {
    Entry tmp;
    tmp = Entry_ConstructorFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    tmp = Entry_ListFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }


    throw new RuntimeException("This is not a Entry: " + trm);
  }
  public Entries EntriesFromString(String str)
  {
    aterm.ATerm trm = parse(str);
    return EntriesFromTerm(trm);
  }
  public Entries EntriesFromFile(java.io.InputStream stream) throws java.io.IOException {
    return EntriesFromTerm(readFromFile(stream));
  }
  public Entry EntryFromString(String str)
  {
    aterm.ATerm trm = parse(str);
    return EntryFromTerm(trm);
  }
  public Entry EntryFromFile(java.io.InputStream stream) throws java.io.IOException {
    return EntryFromTerm(readFromFile(stream));
  }
}
