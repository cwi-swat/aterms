package apigen.adt.api;

public class Factory {
  private aterm.pure.PureFactory factory;


  private apigen.adt.api.types.Entries proto_Entries;

  private aterm.AFun fun_Entry_Constructor;
  private apigen.adt.api.types.Entry proto_Entry_Constructor;
  private aterm.ATerm pattern_Entry_Constructor;
  private aterm.AFun fun_Entry_List;
  private apigen.adt.api.types.Entry proto_Entry_List;
  private aterm.ATerm pattern_Entry_List;
  private aterm.AFun fun_Entry_NamedList;
  private apigen.adt.api.types.Entry proto_Entry_NamedList;
  private aterm.ATerm pattern_Entry_NamedList;
  private aterm.AFun fun_Entry_SeparatedList;
  private apigen.adt.api.types.Entry proto_Entry_SeparatedList;
  private aterm.ATerm pattern_Entry_SeparatedList;

  private apigen.adt.api.types.Separators proto_Separators;

  private aterm.AFun fun_Separator_Default;
  private apigen.adt.api.types.Separator proto_Separator_Default;
  private aterm.ATerm pattern_Separator_Default;

  private apigen.adt.api.types.Modules proto_Modules;

  private aterm.AFun fun_Module_Modulentry;
  private apigen.adt.api.types.Module proto_Module_Modulentry;
  private aterm.ATerm pattern_Module_Modulentry;

  private apigen.adt.api.types.Imports proto_Imports;

  private aterm.AFun fun_Type_Type;
  private apigen.adt.api.types.Type proto_Type_Type;
  private aterm.ATerm pattern_Type_Type;

  private apigen.adt.api.types.Sorts proto_Sorts;

  private aterm.AFun fun_ModuleName_Name;
  private apigen.adt.api.types.ModuleName proto_ModuleName_Name;
  private aterm.ATerm pattern_ModuleName_Name;

  private apigen.adt.api.types.Entries empty_Entries;
  private apigen.adt.api.types.Separators empty_Separators;
  private apigen.adt.api.types.Modules empty_Modules;
  private apigen.adt.api.types.Imports empty_Imports;
  private apigen.adt.api.types.Sorts empty_Sorts;

  private Factory(aterm.pure.PureFactory factory) {
    this.factory = factory;
  }

  private static Factory instance = null;

  public static Factory getInstance(aterm.pure.PureFactory factory) {
    if (instance == null) {
        instance = new Factory(factory);
        instance.initialize();
    }
    if (instance.factory != factory) {
        throw new RuntimeException("Dont create two Factory factories with differents PureFactory ");
    } else {
        return instance;
    }
  }

  public aterm.pure.PureFactory getPureFactory() {
    return factory;
  }

  private void initialize() {

    proto_Entries = new apigen.adt.api.types.Entries(this);
    proto_Entries.init(84, null, null, null);
    empty_Entries = (apigen.adt.api.types.Entries) factory.build(proto_Entries);
    empty_Entries.init(84, empty_Entries, null, null);
    pattern_Entry_Constructor = factory.parse("constructor(<term>,<term>,<term>)");
    fun_Entry_Constructor = factory.makeAFun("_Entry_Constructor", 3, false);
    proto_Entry_Constructor = new apigen.adt.api.types.entry.Constructor(this);

    pattern_Entry_List = factory.parse("list(<term>,<term>)");
    fun_Entry_List = factory.makeAFun("_Entry_List", 2, false);
    proto_Entry_List = new apigen.adt.api.types.entry.List(this);

    pattern_Entry_NamedList = factory.parse("named-list(<term>,<term>,<term>)");
    fun_Entry_NamedList = factory.makeAFun("_Entry_NamedList", 3, false);
    proto_Entry_NamedList = new apigen.adt.api.types.entry.NamedList(this);

    pattern_Entry_SeparatedList = factory.parse("separated-list(<term>,<term>,<term>)");
    fun_Entry_SeparatedList = factory.makeAFun("_Entry_Separated-List", 3, false);
    proto_Entry_SeparatedList = new apigen.adt.api.types.entry.SeparatedList(this);

    proto_Separators = new apigen.adt.api.types.Separators(this);
    proto_Separators.init(126, null, null, null);
    empty_Separators = (apigen.adt.api.types.Separators) factory.build(proto_Separators);
    empty_Separators.init(126, empty_Separators, null, null);
    pattern_Separator_Default = factory.parse("<term>");
    fun_Separator_Default = factory.makeAFun("_Separator_Default", 1, false);
    proto_Separator_Default = new apigen.adt.api.types.separator.Default(this);

    proto_Modules = new apigen.adt.api.types.Modules(this);
    proto_Modules.init(168, null, null, null);
    empty_Modules = (apigen.adt.api.types.Modules) factory.build(proto_Modules);
    empty_Modules.init(168, empty_Modules, null, null);
    pattern_Module_Modulentry = factory.parse("modulentry(<term>,<term>,<term>,<term>)");
    fun_Module_Modulentry = factory.makeAFun("_Module_Modulentry", 4, false);
    proto_Module_Modulentry = new apigen.adt.api.types.module.Modulentry(this);

    proto_Imports = new apigen.adt.api.types.Imports(this);
    proto_Imports.init(210, null, null, null);
    empty_Imports = (apigen.adt.api.types.Imports) factory.build(proto_Imports);
    empty_Imports.init(210, empty_Imports, null, null);
    pattern_Type_Type = factory.parse("Type(<str>)");
    fun_Type_Type = factory.makeAFun("_Type_Type", 1, false);
    proto_Type_Type = new apigen.adt.api.types.type.Type(this);

    proto_Sorts = new apigen.adt.api.types.Sorts(this);
    proto_Sorts.init(252, null, null, null);
    empty_Sorts = (apigen.adt.api.types.Sorts) factory.build(proto_Sorts);
    empty_Sorts.init(252, empty_Sorts, null, null);
    pattern_ModuleName_Name = factory.parse("Name(<str>)");
    fun_ModuleName_Name = factory.makeAFun("_ModuleName_Name", 1, false);
    proto_ModuleName_Name = new apigen.adt.api.types.modulename.Name(this);

  }

/*genAlternativeMethods*/
  public apigen.adt.api.types.entry.Constructor makeEntry_Constructor(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (proto_Entry_Constructor) {
      proto_Entry_Constructor.initHashCode(annos, fun, args);
      return (apigen.adt.api.types.entry.Constructor) factory.build(proto_Entry_Constructor);
    }
  }

  public apigen.adt.api.types.entry.Constructor makeEntry_Constructor(aterm.ATerm _sort, aterm.ATerm _alternative, aterm.ATerm _termPattern) {
    aterm.ATerm[] args = new aterm.ATerm[] {_sort, _alternative, _termPattern};
    return makeEntry_Constructor(fun_Entry_Constructor, args, factory.getEmpty());
  }

  protected apigen.adt.api.types.Entry Entry_ConstructorFromTerm(aterm.ATerm trm) {
    java.util.List children = trm.match(pattern_Entry_Constructor);

    if (children != null) {
      return makeEntry_Constructor(
        (aterm.ATerm) children.get(0),
        (aterm.ATerm) children.get(1),
        (aterm.ATerm) children.get(2)
      );
    }
    else {
      return null;
    }
  }

  public aterm.ATerm toTerm(apigen.adt.api.types.entry.Constructor arg) {
    java.util.List args = new java.util.LinkedList();
    args.add((aterm.ATerm)arg.getSort());
    args.add((aterm.ATerm)arg.getAlternative());
    args.add((aterm.ATerm)arg.getTermPattern());
    return factory.make(pattern_Entry_Constructor, args);
  }

  public apigen.adt.api.types.entry.List makeEntry_List(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (proto_Entry_List) {
      proto_Entry_List.initHashCode(annos, fun, args);
      return (apigen.adt.api.types.entry.List) factory.build(proto_Entry_List);
    }
  }

  public apigen.adt.api.types.entry.List makeEntry_List(aterm.ATerm _sort, aterm.ATerm _elemSort) {
    aterm.ATerm[] args = new aterm.ATerm[] {_sort, _elemSort};
    return makeEntry_List(fun_Entry_List, args, factory.getEmpty());
  }

  protected apigen.adt.api.types.Entry Entry_ListFromTerm(aterm.ATerm trm) {
    java.util.List children = trm.match(pattern_Entry_List);

    if (children != null) {
      return makeEntry_List(
        (aterm.ATerm) children.get(0),
        (aterm.ATerm) children.get(1)
      );
    }
    else {
      return null;
    }
  }

  public aterm.ATerm toTerm(apigen.adt.api.types.entry.List arg) {
    java.util.List args = new java.util.LinkedList();
    args.add((aterm.ATerm)arg.getSort());
    args.add((aterm.ATerm)arg.getElemSort());
    return factory.make(pattern_Entry_List, args);
  }

  public apigen.adt.api.types.entry.NamedList makeEntry_NamedList(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (proto_Entry_NamedList) {
      proto_Entry_NamedList.initHashCode(annos, fun, args);
      return (apigen.adt.api.types.entry.NamedList) factory.build(proto_Entry_NamedList);
    }
  }

  public apigen.adt.api.types.entry.NamedList makeEntry_NamedList(aterm.ATerm _opname, aterm.ATerm _sort, aterm.ATerm _elemSort) {
    aterm.ATerm[] args = new aterm.ATerm[] {_opname, _sort, _elemSort};
    return makeEntry_NamedList(fun_Entry_NamedList, args, factory.getEmpty());
  }

  protected apigen.adt.api.types.Entry Entry_NamedListFromTerm(aterm.ATerm trm) {
    java.util.List children = trm.match(pattern_Entry_NamedList);

    if (children != null) {
      return makeEntry_NamedList(
        (aterm.ATerm) children.get(0),
        (aterm.ATerm) children.get(1),
        (aterm.ATerm) children.get(2)
      );
    }
    else {
      return null;
    }
  }

  public aterm.ATerm toTerm(apigen.adt.api.types.entry.NamedList arg) {
    java.util.List args = new java.util.LinkedList();
    args.add((aterm.ATerm)arg.getOpname());
    args.add((aterm.ATerm)arg.getSort());
    args.add((aterm.ATerm)arg.getElemSort());
    return factory.make(pattern_Entry_NamedList, args);
  }

  public apigen.adt.api.types.entry.SeparatedList makeEntry_SeparatedList(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (proto_Entry_SeparatedList) {
      proto_Entry_SeparatedList.initHashCode(annos, fun, args);
      return (apigen.adt.api.types.entry.SeparatedList) factory.build(proto_Entry_SeparatedList);
    }
  }

  public apigen.adt.api.types.entry.SeparatedList makeEntry_SeparatedList(aterm.ATerm _sort, aterm.ATerm _elemSort, apigen.adt.api.types.Separators _separators) {
    aterm.ATerm[] args = new aterm.ATerm[] {_sort, _elemSort, _separators};
    return makeEntry_SeparatedList(fun_Entry_SeparatedList, args, factory.getEmpty());
  }

  protected apigen.adt.api.types.Entry Entry_SeparatedListFromTerm(aterm.ATerm trm) {
    java.util.List children = trm.match(pattern_Entry_SeparatedList);

    if (children != null) {
      return makeEntry_SeparatedList(
        (aterm.ATerm) children.get(0),
        (aterm.ATerm) children.get(1),
        SeparatorsFromTerm((aterm.ATerm) children.get(2))
      );
    }
    else {
      return null;
    }
  }

  public aterm.ATerm toTerm(apigen.adt.api.types.entry.SeparatedList arg) {
    java.util.List args = new java.util.LinkedList();
    args.add((aterm.ATerm)arg.getSort());
    args.add((aterm.ATerm)arg.getElemSort());
    args.add(arg.getSeparators().toTerm());
    return factory.make(pattern_Entry_SeparatedList, args);
  }

  public apigen.adt.api.types.separator.Default makeSeparator_Default(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (proto_Separator_Default) {
      proto_Separator_Default.initHashCode(annos, fun, args);
      return (apigen.adt.api.types.separator.Default) factory.build(proto_Separator_Default);
    }
  }

  public apigen.adt.api.types.separator.Default makeSeparator_Default(aterm.ATerm _termPattern) {
    aterm.ATerm[] args = new aterm.ATerm[] {_termPattern};
    return makeSeparator_Default(fun_Separator_Default, args, factory.getEmpty());
  }

  protected apigen.adt.api.types.Separator Separator_DefaultFromTerm(aterm.ATerm trm) {
    java.util.List children = trm.match(pattern_Separator_Default);

    if (children != null) {
      return makeSeparator_Default(
        (aterm.ATerm) children.get(0)
      );
    }
    else {
      return null;
    }
  }

  public aterm.ATerm toTerm(apigen.adt.api.types.separator.Default arg) {
    java.util.List args = new java.util.LinkedList();
    args.add((aterm.ATerm)arg.getTermPattern());
    return factory.make(pattern_Separator_Default, args);
  }

  public apigen.adt.api.types.module.Modulentry makeModule_Modulentry(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (proto_Module_Modulentry) {
      proto_Module_Modulentry.initHashCode(annos, fun, args);
      return (apigen.adt.api.types.module.Modulentry) factory.build(proto_Module_Modulentry);
    }
  }

  public apigen.adt.api.types.module.Modulentry makeModule_Modulentry(apigen.adt.api.types.ModuleName _modulename, apigen.adt.api.types.Imports _imports, apigen.adt.api.types.Sorts _sorts, apigen.adt.api.types.Entries _entries) {
    aterm.ATerm[] args = new aterm.ATerm[] {_modulename, _imports, _sorts, _entries};
    return makeModule_Modulentry(fun_Module_Modulentry, args, factory.getEmpty());
  }

  protected apigen.adt.api.types.Module Module_ModulentryFromTerm(aterm.ATerm trm) {
    java.util.List children = trm.match(pattern_Module_Modulentry);

    if (children != null) {
      return makeModule_Modulentry(
        ModuleNameFromTerm((aterm.ATerm) children.get(0)),
        ImportsFromTerm((aterm.ATerm) children.get(1)),
        SortsFromTerm((aterm.ATerm) children.get(2)),
        EntriesFromTerm((aterm.ATerm) children.get(3))
      );
    }
    else {
      return null;
    }
  }

  public aterm.ATerm toTerm(apigen.adt.api.types.module.Modulentry arg) {
    java.util.List args = new java.util.LinkedList();
    args.add(arg.getModulename().toTerm());
    args.add(arg.getImports().toTerm());
    args.add(arg.getSorts().toTerm());
    args.add(arg.getEntries().toTerm());
    return factory.make(pattern_Module_Modulentry, args);
  }

  public apigen.adt.api.types.type.Type makeType_Type(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (proto_Type_Type) {
      proto_Type_Type.initHashCode(annos, fun, args);
      return (apigen.adt.api.types.type.Type) factory.build(proto_Type_Type);
    }
  }

  public apigen.adt.api.types.type.Type makeType_Type(String _name) {
    aterm.ATerm[] args = new aterm.ATerm[] {(aterm.ATerm) factory.makeAppl(factory.makeAFun(_name, 0, true))};
    return makeType_Type(fun_Type_Type, args, factory.getEmpty());
  }

  protected apigen.adt.api.types.Type Type_TypeFromTerm(aterm.ATerm trm) {
    java.util.List children = trm.match(pattern_Type_Type);

    if (children != null) {
      return makeType_Type(
        (String) children.get(0)
      );
    }
    else {
      return null;
    }
  }

  public aterm.ATerm toTerm(apigen.adt.api.types.type.Type arg) {
    java.util.List args = new java.util.LinkedList();
    args.add(arg.getName());
    return factory.make(pattern_Type_Type, args);
  }

  public apigen.adt.api.types.modulename.Name makeModuleName_Name(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    synchronized (proto_ModuleName_Name) {
      proto_ModuleName_Name.initHashCode(annos, fun, args);
      return (apigen.adt.api.types.modulename.Name) factory.build(proto_ModuleName_Name);
    }
  }

  public apigen.adt.api.types.modulename.Name makeModuleName_Name(String _name) {
    aterm.ATerm[] args = new aterm.ATerm[] {(aterm.ATerm) factory.makeAppl(factory.makeAFun(_name, 0, true))};
    return makeModuleName_Name(fun_ModuleName_Name, args, factory.getEmpty());
  }

  protected apigen.adt.api.types.ModuleName ModuleName_NameFromTerm(aterm.ATerm trm) {
    java.util.List children = trm.match(pattern_ModuleName_Name);

    if (children != null) {
      return makeModuleName_Name(
        (String) children.get(0)
      );
    }
    else {
      return null;
    }
  }

  public aterm.ATerm toTerm(apigen.adt.api.types.modulename.Name arg) {
    java.util.List args = new java.util.LinkedList();
    args.add(arg.getName());
    return factory.make(pattern_ModuleName_Name, args);
  }

/*genMakeLists*/
  public apigen.adt.api.types.Entries makeEntries() {
    return empty_Entries;
  }

  public apigen.adt.api.types.Entries makeEntries(apigen.adt.api.types.Entry elem) {
    return (apigen.adt.api.types.Entries) makeEntries(elem, empty_Entries);
  }

  public apigen.adt.api.types.Entries makeEntries(apigen.adt.api.types.Entry head, apigen.adt.api.types.Entries tail) {
    return (apigen.adt.api.types.Entries) makeEntries((aterm.ATerm) head, (aterm.ATermList) tail, factory.getEmpty());
  }

  protected apigen.adt.api.types.Entries makeEntries(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    synchronized (proto_Entries) {
      proto_Entries.initHashCode(annos, head, tail);
      return (apigen.adt.api.types.Entries) factory.build(proto_Entries);
    }
  }

  public apigen.adt.api.types.Entries makeEntries(apigen.adt.api.types.Entry elem0, apigen.adt.api.types.Entry elem1) {
    return makeEntries(elem0, makeEntries(elem1));
  }

  public apigen.adt.api.types.Entries makeEntries(apigen.adt.api.types.Entry elem0, apigen.adt.api.types.Entry elem1, apigen.adt.api.types.Entry elem2) {
    return makeEntries(elem0, makeEntries(elem1, elem2));
  }

  public apigen.adt.api.types.Entries makeEntries(apigen.adt.api.types.Entry elem0, apigen.adt.api.types.Entry elem1, apigen.adt.api.types.Entry elem2, apigen.adt.api.types.Entry elem3) {
    return makeEntries(elem0, makeEntries(elem1, elem2, elem3));
  }

  public apigen.adt.api.types.Entries makeEntries(apigen.adt.api.types.Entry elem0, apigen.adt.api.types.Entry elem1, apigen.adt.api.types.Entry elem2, apigen.adt.api.types.Entry elem3, apigen.adt.api.types.Entry elem4) {
    return makeEntries(elem0, makeEntries(elem1, elem2, elem3, elem4));
  }

  public apigen.adt.api.types.Entries makeEntries(apigen.adt.api.types.Entry elem0, apigen.adt.api.types.Entry elem1, apigen.adt.api.types.Entry elem2, apigen.adt.api.types.Entry elem3, apigen.adt.api.types.Entry elem4, apigen.adt.api.types.Entry elem5) {
    return makeEntries(elem0, makeEntries(elem1, elem2, elem3, elem4, elem5));
  }

  public apigen.adt.api.types.Entries reverse(apigen.adt.api.types.Entries arg) {
    apigen.adt.api.types.Entries reversed = makeEntries();
    while (!arg.isEmpty()) {
      reversed = makeEntries(arg.getHead(), reversed);
      arg = arg.getTail();
    }
    return reversed;
  }

  public apigen.adt.api.types.Entries concat(apigen.adt.api.types.Entries arg0, apigen.adt.api.types.Entries arg1) {
    apigen.adt.api.types.Entries result = arg1;

    for (apigen.adt.api.types.Entries list = reverse(arg0); !list.isEmpty(); list = list.getTail()) {
      result = makeEntries(list.getHead(), result);
    }

    return result;
  }

  public apigen.adt.api.types.Entries append(apigen.adt.api.types.Entries list, apigen.adt.api.types.Entry elem) {
    return concat(list, makeEntries(elem));
  }

  public apigen.adt.api.types.Separators makeSeparators() {
    return empty_Separators;
  }

  public apigen.adt.api.types.Separators makeSeparators(apigen.adt.api.types.Separator elem) {
    return (apigen.adt.api.types.Separators) makeSeparators(elem, empty_Separators);
  }

  public apigen.adt.api.types.Separators makeSeparators(apigen.adt.api.types.Separator head, apigen.adt.api.types.Separators tail) {
    return (apigen.adt.api.types.Separators) makeSeparators((aterm.ATerm) head, (aterm.ATermList) tail, factory.getEmpty());
  }

  protected apigen.adt.api.types.Separators makeSeparators(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    synchronized (proto_Separators) {
      proto_Separators.initHashCode(annos, head, tail);
      return (apigen.adt.api.types.Separators) factory.build(proto_Separators);
    }
  }

  public apigen.adt.api.types.Separators makeSeparators(apigen.adt.api.types.Separator elem0, apigen.adt.api.types.Separator elem1) {
    return makeSeparators(elem0, makeSeparators(elem1));
  }

  public apigen.adt.api.types.Separators makeSeparators(apigen.adt.api.types.Separator elem0, apigen.adt.api.types.Separator elem1, apigen.adt.api.types.Separator elem2) {
    return makeSeparators(elem0, makeSeparators(elem1, elem2));
  }

  public apigen.adt.api.types.Separators makeSeparators(apigen.adt.api.types.Separator elem0, apigen.adt.api.types.Separator elem1, apigen.adt.api.types.Separator elem2, apigen.adt.api.types.Separator elem3) {
    return makeSeparators(elem0, makeSeparators(elem1, elem2, elem3));
  }

  public apigen.adt.api.types.Separators makeSeparators(apigen.adt.api.types.Separator elem0, apigen.adt.api.types.Separator elem1, apigen.adt.api.types.Separator elem2, apigen.adt.api.types.Separator elem3, apigen.adt.api.types.Separator elem4) {
    return makeSeparators(elem0, makeSeparators(elem1, elem2, elem3, elem4));
  }

  public apigen.adt.api.types.Separators makeSeparators(apigen.adt.api.types.Separator elem0, apigen.adt.api.types.Separator elem1, apigen.adt.api.types.Separator elem2, apigen.adt.api.types.Separator elem3, apigen.adt.api.types.Separator elem4, apigen.adt.api.types.Separator elem5) {
    return makeSeparators(elem0, makeSeparators(elem1, elem2, elem3, elem4, elem5));
  }

  public apigen.adt.api.types.Separators reverse(apigen.adt.api.types.Separators arg) {
    apigen.adt.api.types.Separators reversed = makeSeparators();
    while (!arg.isEmpty()) {
      reversed = makeSeparators(arg.getHead(), reversed);
      arg = arg.getTail();
    }
    return reversed;
  }

  public apigen.adt.api.types.Separators concat(apigen.adt.api.types.Separators arg0, apigen.adt.api.types.Separators arg1) {
    apigen.adt.api.types.Separators result = arg1;

    for (apigen.adt.api.types.Separators list = reverse(arg0); !list.isEmpty(); list = list.getTail()) {
      result = makeSeparators(list.getHead(), result);
    }

    return result;
  }

  public apigen.adt.api.types.Separators append(apigen.adt.api.types.Separators list, apigen.adt.api.types.Separator elem) {
    return concat(list, makeSeparators(elem));
  }

  public apigen.adt.api.types.Modules makeModules() {
    return empty_Modules;
  }

  public apigen.adt.api.types.Modules makeModules(apigen.adt.api.types.Module elem) {
    return (apigen.adt.api.types.Modules) makeModules(elem, empty_Modules);
  }

  public apigen.adt.api.types.Modules makeModules(apigen.adt.api.types.Module head, apigen.adt.api.types.Modules tail) {
    return (apigen.adt.api.types.Modules) makeModules((aterm.ATerm) head, (aterm.ATermList) tail, factory.getEmpty());
  }

  protected apigen.adt.api.types.Modules makeModules(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    synchronized (proto_Modules) {
      proto_Modules.initHashCode(annos, head, tail);
      return (apigen.adt.api.types.Modules) factory.build(proto_Modules);
    }
  }

  public apigen.adt.api.types.Modules makeModules(apigen.adt.api.types.Module elem0, apigen.adt.api.types.Module elem1) {
    return makeModules(elem0, makeModules(elem1));
  }

  public apigen.adt.api.types.Modules makeModules(apigen.adt.api.types.Module elem0, apigen.adt.api.types.Module elem1, apigen.adt.api.types.Module elem2) {
    return makeModules(elem0, makeModules(elem1, elem2));
  }

  public apigen.adt.api.types.Modules makeModules(apigen.adt.api.types.Module elem0, apigen.adt.api.types.Module elem1, apigen.adt.api.types.Module elem2, apigen.adt.api.types.Module elem3) {
    return makeModules(elem0, makeModules(elem1, elem2, elem3));
  }

  public apigen.adt.api.types.Modules makeModules(apigen.adt.api.types.Module elem0, apigen.adt.api.types.Module elem1, apigen.adt.api.types.Module elem2, apigen.adt.api.types.Module elem3, apigen.adt.api.types.Module elem4) {
    return makeModules(elem0, makeModules(elem1, elem2, elem3, elem4));
  }

  public apigen.adt.api.types.Modules makeModules(apigen.adt.api.types.Module elem0, apigen.adt.api.types.Module elem1, apigen.adt.api.types.Module elem2, apigen.adt.api.types.Module elem3, apigen.adt.api.types.Module elem4, apigen.adt.api.types.Module elem5) {
    return makeModules(elem0, makeModules(elem1, elem2, elem3, elem4, elem5));
  }

  public apigen.adt.api.types.Modules reverse(apigen.adt.api.types.Modules arg) {
    apigen.adt.api.types.Modules reversed = makeModules();
    while (!arg.isEmpty()) {
      reversed = makeModules(arg.getHead(), reversed);
      arg = arg.getTail();
    }
    return reversed;
  }

  public apigen.adt.api.types.Modules concat(apigen.adt.api.types.Modules arg0, apigen.adt.api.types.Modules arg1) {
    apigen.adt.api.types.Modules result = arg1;

    for (apigen.adt.api.types.Modules list = reverse(arg0); !list.isEmpty(); list = list.getTail()) {
      result = makeModules(list.getHead(), result);
    }

    return result;
  }

  public apigen.adt.api.types.Modules append(apigen.adt.api.types.Modules list, apigen.adt.api.types.Module elem) {
    return concat(list, makeModules(elem));
  }

  public apigen.adt.api.types.Imports makeImports() {
    return empty_Imports;
  }

  public apigen.adt.api.types.Imports makeImports(apigen.adt.api.types.ModuleName elem) {
    return (apigen.adt.api.types.Imports) makeImports(elem, empty_Imports);
  }

  public apigen.adt.api.types.Imports makeImports(apigen.adt.api.types.ModuleName head, apigen.adt.api.types.Imports tail) {
    return (apigen.adt.api.types.Imports) makeImports((aterm.ATerm) head, (aterm.ATermList) tail, factory.getEmpty());
  }

  protected apigen.adt.api.types.Imports makeImports(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    synchronized (proto_Imports) {
      proto_Imports.initHashCode(annos, head, tail);
      return (apigen.adt.api.types.Imports) factory.build(proto_Imports);
    }
  }

  public apigen.adt.api.types.Imports makeImports(apigen.adt.api.types.ModuleName elem0, apigen.adt.api.types.ModuleName elem1) {
    return makeImports(elem0, makeImports(elem1));
  }

  public apigen.adt.api.types.Imports makeImports(apigen.adt.api.types.ModuleName elem0, apigen.adt.api.types.ModuleName elem1, apigen.adt.api.types.ModuleName elem2) {
    return makeImports(elem0, makeImports(elem1, elem2));
  }

  public apigen.adt.api.types.Imports makeImports(apigen.adt.api.types.ModuleName elem0, apigen.adt.api.types.ModuleName elem1, apigen.adt.api.types.ModuleName elem2, apigen.adt.api.types.ModuleName elem3) {
    return makeImports(elem0, makeImports(elem1, elem2, elem3));
  }

  public apigen.adt.api.types.Imports makeImports(apigen.adt.api.types.ModuleName elem0, apigen.adt.api.types.ModuleName elem1, apigen.adt.api.types.ModuleName elem2, apigen.adt.api.types.ModuleName elem3, apigen.adt.api.types.ModuleName elem4) {
    return makeImports(elem0, makeImports(elem1, elem2, elem3, elem4));
  }

  public apigen.adt.api.types.Imports makeImports(apigen.adt.api.types.ModuleName elem0, apigen.adt.api.types.ModuleName elem1, apigen.adt.api.types.ModuleName elem2, apigen.adt.api.types.ModuleName elem3, apigen.adt.api.types.ModuleName elem4, apigen.adt.api.types.ModuleName elem5) {
    return makeImports(elem0, makeImports(elem1, elem2, elem3, elem4, elem5));
  }

  public apigen.adt.api.types.Imports reverse(apigen.adt.api.types.Imports arg) {
    apigen.adt.api.types.Imports reversed = makeImports();
    while (!arg.isEmpty()) {
      reversed = makeImports(arg.getHead(), reversed);
      arg = arg.getTail();
    }
    return reversed;
  }

  public apigen.adt.api.types.Imports concat(apigen.adt.api.types.Imports arg0, apigen.adt.api.types.Imports arg1) {
    apigen.adt.api.types.Imports result = arg1;

    for (apigen.adt.api.types.Imports list = reverse(arg0); !list.isEmpty(); list = list.getTail()) {
      result = makeImports(list.getHead(), result);
    }

    return result;
  }

  public apigen.adt.api.types.Imports append(apigen.adt.api.types.Imports list, apigen.adt.api.types.ModuleName elem) {
    return concat(list, makeImports(elem));
  }

  public apigen.adt.api.types.Sorts makeSorts() {
    return empty_Sorts;
  }

  public apigen.adt.api.types.Sorts makeSorts(apigen.adt.api.types.Type elem) {
    return (apigen.adt.api.types.Sorts) makeSorts(elem, empty_Sorts);
  }

  public apigen.adt.api.types.Sorts makeSorts(apigen.adt.api.types.Type head, apigen.adt.api.types.Sorts tail) {
    return (apigen.adt.api.types.Sorts) makeSorts((aterm.ATerm) head, (aterm.ATermList) tail, factory.getEmpty());
  }

  protected apigen.adt.api.types.Sorts makeSorts(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    synchronized (proto_Sorts) {
      proto_Sorts.initHashCode(annos, head, tail);
      return (apigen.adt.api.types.Sorts) factory.build(proto_Sorts);
    }
  }

  public apigen.adt.api.types.Sorts makeSorts(apigen.adt.api.types.Type elem0, apigen.adt.api.types.Type elem1) {
    return makeSorts(elem0, makeSorts(elem1));
  }

  public apigen.adt.api.types.Sorts makeSorts(apigen.adt.api.types.Type elem0, apigen.adt.api.types.Type elem1, apigen.adt.api.types.Type elem2) {
    return makeSorts(elem0, makeSorts(elem1, elem2));
  }

  public apigen.adt.api.types.Sorts makeSorts(apigen.adt.api.types.Type elem0, apigen.adt.api.types.Type elem1, apigen.adt.api.types.Type elem2, apigen.adt.api.types.Type elem3) {
    return makeSorts(elem0, makeSorts(elem1, elem2, elem3));
  }

  public apigen.adt.api.types.Sorts makeSorts(apigen.adt.api.types.Type elem0, apigen.adt.api.types.Type elem1, apigen.adt.api.types.Type elem2, apigen.adt.api.types.Type elem3, apigen.adt.api.types.Type elem4) {
    return makeSorts(elem0, makeSorts(elem1, elem2, elem3, elem4));
  }

  public apigen.adt.api.types.Sorts makeSorts(apigen.adt.api.types.Type elem0, apigen.adt.api.types.Type elem1, apigen.adt.api.types.Type elem2, apigen.adt.api.types.Type elem3, apigen.adt.api.types.Type elem4, apigen.adt.api.types.Type elem5) {
    return makeSorts(elem0, makeSorts(elem1, elem2, elem3, elem4, elem5));
  }

  public apigen.adt.api.types.Sorts reverse(apigen.adt.api.types.Sorts arg) {
    apigen.adt.api.types.Sorts reversed = makeSorts();
    while (!arg.isEmpty()) {
      reversed = makeSorts(arg.getHead(), reversed);
      arg = arg.getTail();
    }
    return reversed;
  }

  public apigen.adt.api.types.Sorts concat(apigen.adt.api.types.Sorts arg0, apigen.adt.api.types.Sorts arg1) {
    apigen.adt.api.types.Sorts result = arg1;

    for (apigen.adt.api.types.Sorts list = reverse(arg0); !list.isEmpty(); list = list.getTail()) {
      result = makeSorts(list.getHead(), result);
    }

    return result;
  }

  public apigen.adt.api.types.Sorts append(apigen.adt.api.types.Sorts list, apigen.adt.api.types.Type elem) {
    return concat(list, makeSorts(elem));
  }

/*genTypeFromTermMethods*/
  public apigen.adt.api.types.Entries EntriesFromTerm(aterm.ATerm trm) {
     if (trm instanceof aterm.ATermList) {
        aterm.ATermList list = ((aterm.ATermList) trm).reverse();
        apigen.adt.api.types.Entries result = makeEntries();
        for (; !list.isEmpty(); list = list.getNext()) {
           apigen.adt.api.types.Entry elem = EntryFromTerm(list.getFirst());
            result = makeEntries(elem, result);
        }
        return result;
     }
     else {
       throw new RuntimeException("This is not a Entries: " + trm);
     }
  }

  public apigen.adt.api.types.Entry EntryFromTerm(aterm.ATerm trm) {
    apigen.adt.api.types.Entry tmp;
    tmp = Entry_ConstructorFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    tmp = Entry_ListFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    tmp = Entry_NamedListFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    tmp = Entry_SeparatedListFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    throw new IllegalArgumentException("This is not a Entry: " + trm);
  }

  public apigen.adt.api.types.Separators SeparatorsFromTerm(aterm.ATerm trm) {
     if (trm instanceof aterm.ATermList) {
        aterm.ATermList list = ((aterm.ATermList) trm).reverse();
        apigen.adt.api.types.Separators result = makeSeparators();
        for (; !list.isEmpty(); list = list.getNext()) {
           apigen.adt.api.types.Separator elem = SeparatorFromTerm(list.getFirst());
            result = makeSeparators(elem, result);
        }
        return result;
     }
     else {
       throw new RuntimeException("This is not a Separators: " + trm);
     }
  }

  public apigen.adt.api.types.Separator SeparatorFromTerm(aterm.ATerm trm) {
    apigen.adt.api.types.Separator tmp;
    tmp = Separator_DefaultFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    throw new IllegalArgumentException("This is not a Separator: " + trm);
  }

  public apigen.adt.api.types.Modules ModulesFromTerm(aterm.ATerm trm) {
     if (trm instanceof aterm.ATermList) {
        aterm.ATermList list = ((aterm.ATermList) trm).reverse();
        apigen.adt.api.types.Modules result = makeModules();
        for (; !list.isEmpty(); list = list.getNext()) {
           apigen.adt.api.types.Module elem = ModuleFromTerm(list.getFirst());
            result = makeModules(elem, result);
        }
        return result;
     }
     else {
       throw new RuntimeException("This is not a Modules: " + trm);
     }
  }

  public apigen.adt.api.types.Module ModuleFromTerm(aterm.ATerm trm) {
    apigen.adt.api.types.Module tmp;
    tmp = Module_ModulentryFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    throw new IllegalArgumentException("This is not a Module: " + trm);
  }

  public apigen.adt.api.types.Imports ImportsFromTerm(aterm.ATerm trm) {
     if (trm instanceof aterm.ATermList) {
        aterm.ATermList list = ((aterm.ATermList) trm).reverse();
        apigen.adt.api.types.Imports result = makeImports();
        for (; !list.isEmpty(); list = list.getNext()) {
           apigen.adt.api.types.ModuleName elem = ModuleNameFromTerm(list.getFirst());
            result = makeImports(elem, result);
        }
        return result;
     }
     else {
       throw new RuntimeException("This is not a Imports: " + trm);
     }
  }

  public apigen.adt.api.types.Type TypeFromTerm(aterm.ATerm trm) {
    apigen.adt.api.types.Type tmp;
    tmp = Type_TypeFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    throw new IllegalArgumentException("This is not a Type: " + trm);
  }

  public apigen.adt.api.types.Sorts SortsFromTerm(aterm.ATerm trm) {
     if (trm instanceof aterm.ATermList) {
        aterm.ATermList list = ((aterm.ATermList) trm).reverse();
        apigen.adt.api.types.Sorts result = makeSorts();
        for (; !list.isEmpty(); list = list.getNext()) {
           apigen.adt.api.types.Type elem = TypeFromTerm(list.getFirst());
            result = makeSorts(elem, result);
        }
        return result;
     }
     else {
       throw new RuntimeException("This is not a Sorts: " + trm);
     }
  }

  public apigen.adt.api.types.ModuleName ModuleNameFromTerm(aterm.ATerm trm) {
    apigen.adt.api.types.ModuleName tmp;
    tmp = ModuleName_NameFromTerm(trm);
    if (tmp != null) {
      return tmp;
    }

    throw new IllegalArgumentException("This is not a ModuleName: " + trm);
  }

/*genTypeFromMethods*/
  public apigen.adt.api.types.Entries EntriesFromString(String str) {
    return EntriesFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.Entries EntriesFromFile(java.io.InputStream stream) throws java.io.IOException {
    return EntriesFromTerm(factory.readFromFile(stream));
  }

  public apigen.adt.api.types.Entry EntryFromString(String str) {
    return EntryFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.Entry EntryFromFile(java.io.InputStream stream) throws java.io.IOException {
    return EntryFromTerm(factory.readFromFile(stream));
  }

  public apigen.adt.api.types.Separators SeparatorsFromString(String str) {
    return SeparatorsFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.Separators SeparatorsFromFile(java.io.InputStream stream) throws java.io.IOException {
    return SeparatorsFromTerm(factory.readFromFile(stream));
  }

  public apigen.adt.api.types.Separator SeparatorFromString(String str) {
    return SeparatorFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.Separator SeparatorFromFile(java.io.InputStream stream) throws java.io.IOException {
    return SeparatorFromTerm(factory.readFromFile(stream));
  }

  public apigen.adt.api.types.Modules ModulesFromString(String str) {
    return ModulesFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.Modules ModulesFromFile(java.io.InputStream stream) throws java.io.IOException {
    return ModulesFromTerm(factory.readFromFile(stream));
  }

  public apigen.adt.api.types.Module ModuleFromString(String str) {
    return ModuleFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.Module ModuleFromFile(java.io.InputStream stream) throws java.io.IOException {
    return ModuleFromTerm(factory.readFromFile(stream));
  }

  public apigen.adt.api.types.Imports ImportsFromString(String str) {
    return ImportsFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.Imports ImportsFromFile(java.io.InputStream stream) throws java.io.IOException {
    return ImportsFromTerm(factory.readFromFile(stream));
  }

  public apigen.adt.api.types.Type TypeFromString(String str) {
    return TypeFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.Type TypeFromFile(java.io.InputStream stream) throws java.io.IOException {
    return TypeFromTerm(factory.readFromFile(stream));
  }

  public apigen.adt.api.types.Sorts SortsFromString(String str) {
    return SortsFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.Sorts SortsFromFile(java.io.InputStream stream) throws java.io.IOException {
    return SortsFromTerm(factory.readFromFile(stream));
  }

  public apigen.adt.api.types.ModuleName ModuleNameFromString(String str) {
    return ModuleNameFromTerm(factory.parse(str));
  }

  public apigen.adt.api.types.ModuleName ModuleNameFromFile(java.io.InputStream stream) throws java.io.IOException {
    return ModuleNameFromTerm(factory.readFromFile(stream));
  }

/*genForwardingAlternativeMethods*/
/*genForwardingMakeLists*/
/*genForwardingTypeFromTermMethods*/
/*TODOgenForwardingTypeFromMethods*/
  public static String charsToString(aterm.ATerm arg) {
    aterm.ATermList list = (aterm.ATermList) arg;
    StringBuffer str = new StringBuffer();

    for ( ; !list.isEmpty(); list = list.getNext()) {
      str.append((char) ((aterm.ATermInt) list.getFirst()).getInt());
    }

    return str.toString();
  }

  public aterm.ATerm stringToChars(String str) {
    int len = str.length();
    byte chars[] = str.getBytes();
    aterm.ATermList result = getPureFactory().makeList();

    for (int i = len - 1; i >= 0; i--) {
      result = result.insert(getPureFactory().makeInt(chars[i]));
    }

    return (aterm.ATerm) result;
  }

}
