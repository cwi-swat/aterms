package apigen.adt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import apigen.adt.api.Entries;
import apigen.adt.api.Entry;
import apigen.adt.api.Separators;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermFactory;

public class ADT {
    List types;
    List bottomTypes;
    ATermFactory factory;

    public ADT(Entries adt) throws ADTException {
        types = new LinkedList();

        factory = (ATermFactory) adt.getADTFactory();

        List entries = new LinkedList();

        while (!adt.isEmpty()) {
            entries.add(adt.getHead());
            adt = adt.getTail();
        }

        while (!entries.isEmpty()) {
            List alts = new LinkedList();
            ListIterator iter = entries.listIterator();
            Entry first = (Entry) iter.next();
            alts.add(first);
            String typeId = ((ATermAppl) first.getSort()).getAFun().getName();

            iter.remove();
            while (iter.hasNext()) {
                try {
                    Entry entry = (Entry) iter.next();
                    if (((ATermAppl) entry.getSort())
                        .getAFun()
                        .getName()
                        .equals(typeId)) {
                        alts.add(entry);
                        iter.remove();
                    }
                } catch (ClassCastException exc) {
                    throw new ADTException(
                        "unexpected entry found:" + iter.previous());
                }
            }

            processAlternatives(typeId.toString(), alts);
        }

        computeBottomTypes();
    }

    private void computeBottomTypes() {
        bottomTypes = new LinkedList();
        Iterator types = typeIterator();

        // TODO: do not include builtin types as bottomTypes
        while (types.hasNext()) {
            Type type = (Type) types.next();
            Iterator fields = type.fieldIterator();

            while (fields.hasNext()) {
                Field field = (Field) fields.next();
                Iterator definedTypes = typeIterator();
                boolean defined = false;

                while (definedTypes.hasNext()) {
                    Type definedType = (Type) definedTypes.next();

                    if (field.getType().equals(definedType.getId())) {
                        defined = true;
                        break;
                    }
                }

                if (!defined) {
                    if (!bottomTypes.contains(field.getType())) {
                        bottomTypes.add(field.getType());
                    }
                }
            }
        }
    }

    private void processAlternatives(String typeId, List alts) {
        Entry first = (Entry) alts.get(0);

        if (first.isList() || first.isSeparatedList()) {
            if (alts.size() > 1) {
                throw new RuntimeException("Multiple definitions of same list: " + alts);
            }

            if (first.isSeparatedList()) {
                processSeparatedList(typeId, first);
            } else {
                processList(typeId, first);
            }
        } else {
            processConstructors(typeId, alts);
        }
    }

    private void processSeparatedList(String typeId, Entry entry) {
        String elementType = ((ATermAppl) entry.getElemSort()).getAFun().getName();
        Separators separators = entry.getSeparators();
        SeparatedListType type = new SeparatedListType(typeId, elementType, separators, factory);
        type.addAlternatives();
        types.add(type);
    }

    private void processList(String typeId, Entry first) {
        String elementType = ((ATermAppl) first.getElemSort()).getAFun().getName();
        ListType type = new ListType(typeId, elementType, factory);
        type.addAlternatives();
        types.add(type);
    }

    private void processConstructors(String typeId, List alts) {
        Type type = new Type(typeId);
        ListIterator iter = alts.listIterator();

        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();

            if (entry.isConstructor()) {
                String altId = ((ATermAppl) entry.getAlternative()).getAFun().getName();
                ATerm pattern = entry.getTermPattern();

                Alternative alt = new Alternative(altId, pattern);

                if (altId.equals("int")
                    || altId.equals("str")
                    || altId.equals("term")
                    || altId.equals("real")
                    || altId.equals("list")
                    || altId.equals("chars")) {
                    throw new RuntimeException(
                        "Illegal use of reserved name ("
                            + altId
                            + ") as name of alternative in "
                            + entry);
                } else {
                    addAlternative(typeId, type, altId, alt);
                }
            } else {
                throw new RuntimeException("Unexpected alternative");
            }
        }

        types.add(type);
    }

    private void addAlternative(
        String typeId,
        Type type,
        String altId,
        Alternative alt) {
        if (!type.hasAlternative(altId)) {
            type.addAlternative(alt);
        } else {
            throw new RuntimeException(
                altId + " is defined more than once for " + typeId);
        }
    }

    public Iterator typeIterator() {
        return types.iterator();
    }

    public Iterator bottomTypeIterator() {
        return bottomTypes.iterator();
    }
}
