package apigen.adt.api;

import aterm.*;
import aterm.pure.PureFactory;
public class ADTFactory extends PureFactory
{
  private Entries protoEntries;
  private aterm.AFun funEntry_Constructor;
  private Entry protoEntry_Constructor;
  private aterm.ATerm patternEntry_Constructor;
  private aterm.AFun funEntry_List;
  private Entry protoEntry_List;
  private aterm.ATerm patternEntry_List;
  static protected Entries emptyEntries;
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
    protoEntries = new Entries(this);
    protoEntries.init(84, null, null, null);
    emptyEntries = (Entries) build(protoEntries);
    emptyEntries.init(84, emptyEntries, null, null);


    patternEntry_Constructor = parse("constructor(<term>,<term>,<term>)");
    funEntry_Constructor = makeAFun("_Entry_Constructor", 3, false);
    protoEntry_Constructor = new Entry_Constructor(this);

    patternEntry_List = parse("list(<term>,<term>)");
    funEntry_List = makeAFun("_Entry_List", 2, false);
    protoEntry_List = new Entry_List(this);

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

  public Entries makeEntries() {
    return emptyEntries;
  }
  public Entries makeEntries(Entry elem ) {
    return (Entries) makeEntries(elem, emptyEntries);
  }
  public Entries makeEntries(Entry head, Entries tail) {
    return (Entries) makeEntries((aterm.ATerm) head, (aterm.ATermList) tail, empty);
  }
  protected Entries makeEntries(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    synchronized (protoEntries) {
      protoEntries.initHashCode(annos,head,tail);
      return (Entries) build(protoEntries);
    }
  }
  public Entries EntriesFromTerm(aterm.ATerm trm)
  {
     if (trm instanceof aterm.ATermList) {
        aterm.ATermList list = ((aterm.ATermList) trm).reverse();
        Entries result = makeEntries();
        for (; !list.isEmpty(); list = list.getNext()) {
          Entry elem = EntryFromTerm(list.getFirst());
           if (elem != null) {
             result = makeEntries(elem, result);
           }
           else {
             throw new RuntimeException("Invalid element in Entries: " + elem);
           }
        }
        return result;
     }
     else {
       throw new RuntimeException("This is not a Entries: " + trm);
     }
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
