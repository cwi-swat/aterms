package apigen.adt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;
import aterm.ATermPlaceholder;

public class Type {
    private String id;
    private AlternativeList alts;
    private Map fields;
    private List field_list;

    public Type(String id) {
        this.id = id;

        alts = new AlternativeList();
        fields = new HashMap();
        field_list = new Vector();
    }

    public String getId() {
        return id;
    }

    public void addAlternative(Alternative alt) {
        alts.add(alt);

        extractFields(alt.getPattern(), new Location(alt.getId()));
    }

    private void extractFields(ATerm t, Location loc) {
        AFun fun;
        ATermAppl appl;
        ATermList list;

        switch (t.getType()) {
            case ATerm.APPL :
                //{{{ Call 'extractFields' for every argument

                appl = (ATermAppl) t;
                fun = appl.getAFun();
                for (int i = 0; i < fun.getArity(); i++) {
                    Location newloc = (Location) loc.clone();
                    newloc.addStep(new Step(Step.ARG, i));
                    extractFields(appl.getArgument(i), newloc);
                }

                //}}}
                break;

            case ATerm.LIST :
                //{{{ Call 'extractFields' for every element

                list = (ATermList) t;
                for (int i = 0; !list.isEmpty(); i++) {
                    Location newloc = (Location) loc.clone();
                    newloc.addStep(new Step(Step.ELEM, i));
                    extractFields(list.getFirst(), newloc);
                    list = list.getNext();
                }

                //}}}
                break;

            case ATerm.PLACEHOLDER :
                //{{{ Add a new field based on this placeholder

                ATerm ph = ((ATermPlaceholder) t).getPlaceholder();

                if (ph.getType() == ATerm.LIST) {
                    list = (ATermList) ph;
                    appl = (ATermAppl) list.elementAt(0);
                    String fieldId = appl.getAFun().getName();
                    appl = (ATermAppl) appl.getArgument(0);
                    String fieldType = appl.getAFun().getName();
                    loc.makeTail();
                    addField(fieldId, fieldType, loc);
                } else if (ph.getType() == ATerm.APPL) {
                    appl = (ATermAppl) ph;
                    String fieldId = appl.getAFun().getName();
                    appl = (ATermAppl) appl.getArgument(0);
                    String fieldType = appl.getAFun().getName();
                    addField(fieldId, fieldType, loc);
                } else {
                    throw new RuntimeException("illegal field spec: " + t);
                }

                //}}}
                break;

            default :
                break;
        }
    }

    public boolean hasAlternative(String id) {
        Iterator alts = alternativeIterator();

        while (alts.hasNext()) {
            Alternative alt = (Alternative) alts.next();

            if (alt.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    private void addField(String id, String type, Location location) {
        Field field;

        if (id.equals("term")
            || id.equals("int")
            || id.equals("str")
            || id.equals("real")
            || id.equals("list")
            || id.equals("chars")) {
            throw new RuntimeException(
                "Illegal use of reserved name \""
                    + id
                    + "\" as field name in type "
                    + this.id);
        }

        field = (Field) fields.get(id);

        if (field == null) {
            field = new Field(id, type);
            fields.put(id, field);
            field_list.add(field);
        } else if (field.getLocation(location.getAltId()) != null) {
            throw new RuntimeException(
                "\""
                    + id
                    + "\" occurs more than once in alternative \""
                    + location.getAltId()
                    + "\"");
        } else if (field.getType() != type) {
            throw new RuntimeException(
                "Illegal field name \""
                    + id
                    + "\" is used for different types of fields in type:"
                    + this.id);
        }

        field.addLocation(location);
    }

    public AlternativeList getAlternatives() {
        return (AlternativeList) alts.clone();
    }

    public Iterator alternativeIterator() {
        return alts.iterator();
    }

    public Alternative getAlternative(String altId) {
        Iterator iter = alts.iterator();

        while (iter.hasNext()) {
            Alternative element = (Alternative) iter.next();

            if (element.getId().equals(altId)) {
                return element;
            }
        }

        return null;
    }

    public Field getAltField(String altId, String fieldId) {
        Iterator iter = altFieldIterator(altId);

        while (iter.hasNext()) {
            Field field = (Field) iter.next();

            if (field.getId().equals(fieldId)) {
                return field;
            }
        }

        return null;
    }

    public Iterator fieldIterator() {
        return field_list.iterator();
    }

    public Iterator altFieldIterator(final String altId) {
        Comparator comp;

        //{{{ comp = new Comparator() { ... }

        comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Field field1 = (Field) o1;
                Field field2 = (Field) o2;

                Iterator path1 = field1.getLocation(altId).stepIterator();
                Iterator path2 = field2.getLocation(altId).stepIterator();

                while (path1.hasNext()) {
                    if (!path2.hasNext()) {
                        throw new RuntimeException(
                            "incompatible paths: " + field1 + "," + field2);
                    }
                    Step step1 = (Step) path1.next();
                    Step step2 = (Step) path2.next();
                    int type1 = step1.getType();
                    int type2 = step2.getType();

                    if (type1 == Step.TAIL && type2 == Step.ELEM) {
                        return 1;
                    }

                    if (type1 == Step.ELEM && type2 == Step.TAIL) {
                        return -1;
                    }

                    if (type1 != type2) {
                        throw new RuntimeException(
                            "incompatible paths: " + field1 + "," + field2);
                    }

                    if (step1.getIndex() < step2.getIndex()) {
                        return -1;
                    }

                    if (step1.getIndex() > step2.getIndex()) {
                        return 1;
                    }
                }
                if (path2.hasNext()) {
                    throw new RuntimeException(
                        "incompatible paths: " + field1 + "," + field2);
                }
                if (o1 != o2) {
                    throw new RuntimeException("asjemenou?");
                }
                return 0;
            }
        };

        SortedSet sortedAltFields = new TreeSet(comp);

        Iterator iter = fields.values().iterator();
        while (iter.hasNext()) {
            Field field = (Field) iter.next();
            if (field.hasAltId(altId)) {
                sortedAltFields.add(field);
            }
        }

        return sortedAltFields.iterator();
    }

    public int getAlternativeCount() {
        return alts.size();
    }

    public String toString() {
        return "type[" + id + ", " + alts.toString() + ",\n" + fields.toString() + "]";
    }

    public int getAltArity(Alternative alt) {
        Iterator fields = altFieldIterator(alt.getId());
        int arity = 0;

        for (arity = 0; fields.hasNext(); fields.next()) {
            arity++;
        }

        return arity;
    }
}
