package apigen.adt.api;

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
  private aterm.AFun funEntry_SeparatedList;
  private Entry protoEntry_SeparatedList;
  private aterm.ATerm patternEntry_SeparatedList;
  private Separators protoSeparators;
  private aterm.AFun funSeparator_Default;
  private Separator protoSeparator_Default;
  private aterm.ATerm patternSeparator_Default;
  private Entries emptyEntries;
  private Separators emptySeparators;
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

    patternEntry_SeparatedList = parse("separated-list(<term>,<term>,<term>)");
    funEntry_SeparatedList = makeAFun("_Entry_Separated-List", 3, false);
    protoEntry_SeparatedList = new Entry_SeparatedList(this);

    protoSeparators = new Separators(this);
    protoSeparators.init(126, null, null, null);
    emptySeparators = (Separators) build(protoSeparators);
    emptySeparators.init(126, emptySeparators, null, null);


    patternSeparator_Default = parse("<term>");
    funSeparator_Default = makeAFun("_Separator_Default", 1, false);
    protoSeparator_Default = new Separator_Default(this);

  }
  protected Entry_Constructor makeEntry_Constructor(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (protoEntry_Constructor) {
      protoEntry_Constructor.initHashCode(annos,fun,args);
      return (Entry_Constructor) build(protoEntry_Constructor);
    }
  }

  public Entry_Constructor makeEntry_Constructor(aterm.ATerm _sort, aterm.ATerm _alternative, aterm.ATerm _termPattern) {
    aterm.ATerm[] args = new aterm.ATerm[] {_sort, _alternative, _termPattern};
    return makeEntry_Constructor(funEntry_Constructor, args, getEmpty());
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
    args.add((aterm.ATerm)arg.getSort());    args.add((aterm.ATerm)arg.getAlternative());    args.add((aterm.ATerm)arg.getTermPattern());    return make(patternEntry_Constructor, args);
  }

  protected Entry_List makeEntry_List(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (protoEntry_List) {
      protoEntry_List.initHashCode(annos,fun,args);
      return (Entry_List) build(protoEntry_List);
    }
  }

  public Entry_List makeEntry_List(aterm.ATerm _sort, aterm.ATerm _elemSort) {
    aterm.ATerm[] args = new aterm.ATerm[] {_sort, _elemSort};
    return makeEntry_List(funEntry_List, args, getEmpty());
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
    args.add((aterm.ATerm)arg.getSort());    args.add((aterm.ATerm)arg.getElemSort());    return make(patternEntry_List, args);
  }

  protected Entry_SeparatedList makeEntry_SeparatedList(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (protoEntry_SeparatedList) {
      protoEntry_SeparatedList.initHashCode(annos,fun,args);
      return (Entry_SeparatedList) build(protoEntry_SeparatedList);
    }
  }

  public Entry_SeparatedList makeEntry_SeparatedList(aterm.ATerm _sort, aterm.ATerm _elemSort, Separators _separators) {
    aterm.ATerm[] args = new aterm.ATerm[] {_sort, _elemSort, _separators};
    return makeEntry_SeparatedList(funEntry_SeparatedList, args, getEmpty());
  }

  public Entry Entry_SeparatedListFromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(patternEntry_SeparatedList);

    if (children != null) {
      Entry tmp = makeEntry_SeparatedList((aterm.ATerm) children.get(0), (aterm.ATerm) children.get(1), SeparatorsFromTerm( (aterm.ATerm) children.get(2)));
      return tmp;
    }
    else {
      return null;
    }
  }
  protected aterm.ATerm toTerm(Entry_SeparatedListImpl arg) {
    java.util.List args = new java.util.LinkedList();
    args.add((aterm.ATerm)arg.getSort());    args.add((aterm.ATerm)arg.getElemSort());    args.add((arg.getSeparators()).toTerm());    return make(patternEntry_SeparatedList, args);
  }

  protected Separator_Default makeSeparator_Default(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (protoSeparator_Default) {
      protoSeparator_Default.initHashCode(annos,fun,args);
      return (Separator_Default) build(protoSeparator_Default);
    }
  }

  public Separator_Default makeSeparator_Default(aterm.ATerm _termPattern) {
    aterm.ATerm[] args = new aterm.ATerm[] {_termPattern};
    return makeSeparator_Default(funSeparator_Default, args, getEmpty());
  }

  public Separator Separator_DefaultFromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(patternSeparator_Default);

    if (children != null) {
      Separator tmp = makeSeparator_Default((aterm.ATerm) children.get(0));
      return tmp;
    }
    else {
      return null;
    }
  }
  protected aterm.ATerm toTerm(Separator_DefaultImpl arg) {
    java.util.List args = new java.util.LinkedList();
    args.add((aterm.ATerm)arg.getTermPattern());    return make(patternSeparator_Default, args);
  }

  public Entries makeEntries() {
    return emptyEntries;
  }
  public Entries makeEntries(Entry elem ) {
    return (Entries) makeEntries(elem, emptyEntries);
  }
  public Entries makeEntries(Entry head, Entries tail) {
    return (Entries) makeEntries((aterm.ATerm) head, (aterm.ATermList) tail, getEmpty());
  }
  protected Entries makeEntries(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    synchronized (protoEntries) {
      protoEntries.initHashCode(annos,head,tail);
      return (Entries) build(protoEntries);
    }
  }
  public Separators makeSeparators() {
    return emptySeparators;
  }
  public Separators makeSeparators(Separator elem ) {
    return (Separators) makeSeparators(elem, emptySeparators);
  }
  public Separators makeSeparators(Separator head, Separators tail) {
    return (Separators) makeSeparators((aterm.ATerm) head, (aterm.ATermList) tail, getEmpty());
  }
  protected Separators makeSeparators(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    synchronized (protoSeparators) {
      protoSeparators.initHashCode(annos,head,tail);
      return (Separators) build(protoSeparators);
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

    tmp = Entry_SeparatedListFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }


    throw new RuntimeException("This is not a Entry: " + trm);
  }
  public Separators SeparatorsFromTerm(aterm.ATerm trm)
  {
     if (trm instanceof aterm.ATermList) {
        aterm.ATermList list = ((aterm.ATermList) trm).reverse();
        Separators result = makeSeparators();
        for (; !list.isEmpty(); list = list.getNext()) {
          Separator elem = SeparatorFromTerm(list.getFirst());
           if (elem != null) {
             result = makeSeparators(elem, result);
           }
           else {
             throw new RuntimeException("Invalid element in Separators: " + elem);
           }
        }
        return result;
     }
     else {
       throw new RuntimeException("This is not a Separators: " + trm);
     }
  }
  public Separator SeparatorFromTerm(aterm.ATerm trm)
  {
    Separator tmp;
    tmp = Separator_DefaultFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }


    throw new RuntimeException("This is not a Separator: " + trm);
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
  public Separators SeparatorsFromString(String str)
  {
    aterm.ATerm trm = parse(str);
    return SeparatorsFromTerm(trm);
  }
  public Separators SeparatorsFromFile(java.io.InputStream stream) throws java.io.IOException {
    return SeparatorsFromTerm(readFromFile(stream));
  }
  public Separator SeparatorFromString(String str)
  {
    aterm.ATerm trm = parse(str);
    return SeparatorFromTerm(trm);
  }
  public Separator SeparatorFromFile(java.io.InputStream stream) throws java.io.IOException {
    return SeparatorFromTerm(readFromFile(stream));
  }
}
