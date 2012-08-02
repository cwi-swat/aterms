package apigen.adt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;
import aterm.ATermPlaceholder;

public class Type {
	private String moduleName;
    private String id;
    private AlternativeList alts;
    private Map<String, Field> fields;
    private List<Field> field_list;

    public Type(String id, String moduleName) {
        this.id = id;
        this.moduleName = moduleName;
        alts = new AlternativeList();
        fields = new HashMap<String, Field>();
        field_list = new ArrayList<Field>();
    }

    public String getId() {
        return id;
    }
    
    public String getModuleName() {
      return moduleName;
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
        Iterator<Alternative> alts = alternativeIterator();

        while (alts.hasNext()) {
            Alternative alt = alts.next();

            if (alt.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    private void addField(String id, String type, Location location) {
        if (id.equals("int")) {
            throw new RuntimeException(
                "Illegal use of reserved name \""
                    + id
                    + "\" as field name in type "
                    + this.id);
        }

        Field field = fields.get(id);

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

    public Iterator<Alternative> alternativeIterator() {
        return alts.iterator();
    }

    public Alternative getAlternative(String altId) {
        Iterator<Alternative> iter = alts.iterator();

        while (iter.hasNext()) {
            Alternative element = iter.next();

            if (element.getId().equals(altId)) {
                return element;
            }
        }

        return null;
    }

    public Field getAltField(String altId, String fieldId) {
        Iterator<Field> iter = altFieldIterator(altId);

        while (iter.hasNext()) {
            Field field = iter.next();

            if (field.getId().equals(fieldId)) {
                return field;
            }
        }

        return null;
    }

    public Iterator<Field> fieldIterator() {
        return field_list.iterator();
    }

    public Iterator<Field> altFieldIterator(final String altId) {
        Comparator<Field> comp = new Comparator<Field>() {
            public int compare(Field o1, Field o2) {
                Field field1 = o1;
                Field field2 = o2;

                Iterator<Step> path1 = field1.getLocation(altId).stepIterator();
                Iterator<Step> path2 = field2.getLocation(altId).stepIterator();

                while (path1.hasNext()) {
                    if (!path2.hasNext()) {
                        throw new RuntimeException(
                            "incompatible paths: " + field1 + "," + field2);
                    }
                    Step step1 = path1.next();
                    Step step2 = path2.next();
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

        SortedSet<Field> sortedAltFields = new TreeSet<Field>(comp);

        Iterator<Field> iter = fields.values().iterator();
        while (iter.hasNext()) {
            Field field = iter.next();
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
        return "type[" + id + ",\n\t" + alts.toString() + ",\n\t" + fields.toString() + "]\n\tfrom module "+moduleName;
    }

    public int getAltArity(Alternative alt) {
        Iterator<Field> fields = altFieldIterator(alt.getId());
        int arity = 0;

        for (arity = 0; fields.hasNext(); fields.next()) {
            arity++;
        }

        return arity;
    }
    
}
