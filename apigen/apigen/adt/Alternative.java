package apigen.adt;

import java.util.List;

import apigen.adt.api.Separator;
import apigen.adt.api.Separators;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermFactory;
import aterm.ATermList;
import aterm.ATermPlaceholder;

public class Alternative {

    protected static final String[] BASIC_ATERM_TYPES =
        { "int", "real", "str", "term" };

    protected String id;
    protected ATerm pattern;

    public Alternative(String id, ATerm pattern) {
        this.id = id;
        this.pattern = pattern;
    }

    protected static boolean containsPlaceholder(ATerm term) {
        switch (term.getType()) {
            case ATerm.PLACEHOLDER :
                return true;

            case ATerm.LIST :
                {
                    ATermList list = (ATermList) term;
                    if (list.isEmpty()) {
                        return false;
                    }
                    return containsPlaceholder(list.getFirst())
                        || containsPlaceholder(list.getNext());
                }

            case ATerm.APPL :
                {
                    ATermAppl appl = (ATermAppl) term;
                    int arity = appl.getArity();
                    for (int i = 0; i < arity; i++) {
                        if (containsPlaceholder(appl.getArgument(i))) {
                            return true;
                        }
                    }
                    return false;
                }

            default :
                return false;
        }
    }

    public String getId() {
        return id;
    }

    public ATerm getPattern() {
        return pattern;
    }

    public int getPatternType() {
        ATerm match_pattern = buildMatchPattern();

        if (match_pattern.getType() == ATerm.PLACEHOLDER) {
            ATerm ph = ((ATermPlaceholder) match_pattern).getPlaceholder();
            if (ph.match("int") != null) {
                return ATerm.INT;
            } else if (ph.match("real") != null) {
                return ATerm.REAL;
            } else if (ph.match("str") != null) {
                return ATerm.APPL;
            } else if (ph.match("list") != null) {
                return ATerm.LIST;
            } else if (ph.match("term") != null) {
                return ATerm.APPL;
            } else if (ph.match("chars") != null) {
                return ATerm.LIST;
            } else {
                throw new RuntimeException("strange root pattern: " + match_pattern);
            }
        } else {
            return match_pattern.getType();
        }
    }

    public boolean containsPlaceholder() {
        return containsPlaceholder(pattern);
    }

    public ATerm buildMatchPattern() {
        return buildMatchPattern(pattern);
    }

    protected ATerm buildMatchPattern(ATerm t) {
        switch (t.getType()) {
            case ATerm.APPL :
                ATermAppl appl = (ATermAppl) t;
                AFun fun = appl.getAFun();
                ATerm[] newargs = new ATerm[fun.getArity()];
                for (int i = 0; i < fun.getArity(); i++) {
                    newargs[i] = buildMatchPattern(appl.getArgument(i));
                }
                return pattern.getFactory().makeAppl(fun, newargs);
            case ATerm.LIST :
                ATermList list = (ATermList) t;
                ATerm[] elems = new ATerm[list.getLength()];
                int i = 0;
                while (!list.isEmpty()) {
                    elems[i++] = buildMatchPattern(list.getFirst());
                    list = list.getNext();
                }
                for (i = elems.length - 1; i >= 0; i--) {
                    list = list.insert(elems[i]);
                }
                return list;
            case ATerm.PLACEHOLDER :
                ATerm ph = ((ATermPlaceholder) t).getPlaceholder();
                if (ph.getType() == ATerm.LIST) {
                    return pattern.getFactory().parse("<list>");
                } else {
                    ATerm type = ((ATermAppl) ph).getArgument(0);
                    String typeName = type.toString();

                    if (typeName.equals("chars")) {
                        return pattern.getFactory().parse("<list>");
                    } else if (isBasicATermType(typeName)) {
                        return pattern.getFactory().makePlaceholder(type);
                    } else {
                        return pattern.getFactory().parse("<term>");
                    }
                }
            default :
                return t;
        }
    }

    public boolean isEmpty() {
        List subst = getPattern().match("[]");
        return getId().equals(ListType.EMPTY_LIST_ALT_NAME) && (subst != null);
    }

    public boolean isMany() {
        if (getPattern().getType() == ATerm.LIST) {
            ATermList l = (ATermList) getPattern();
            ATerm headPh = l.getFirst();
            ATerm tailList = l.getNext();

            if (headPh != null
                && headPh.getType() == ATerm.PLACEHOLDER
                && tailList.getType() == ATerm.LIST) {
                ATerm head = ((ATermPlaceholder) headPh).getPlaceholder();
                ATerm tailPh = ((ATermList) tailList).getFirst();
                if (tailPh != null && tailPh.getType() == ATerm.PLACEHOLDER) {
                    ATerm tail = ((ATermPlaceholder) tailPh).getPlaceholder();

                    ATerm headPattern = getPattern().getFactory().parse("head(<term>)");
                    ATerm tailPattern = getPattern().getFactory().parse("[tail(<term>)]");

                    List subst1 = head.match(headPattern);
                    List subst2 = tail.match(tailPattern);
                    //System.out.println("head = " + head);
                    //System.out.println("tail = " + tail);

                    //System.out.println("headMatch = "+ subst1);
                    //System.out.println("tailMatch = "+ subst2);

                    if (getId().equals(ListType.MANY_LIST_ALT_NAME)
                        && (subst1 != null)
                        && (subst2 != null)) {
                        return true;
                    }

                }
            }
        }
        return false;
    }

    public static boolean isBasicATermType(String type) {
        for (int i = 0; i < BASIC_ATERM_TYPES.length; i++) {
            if (BASIC_ATERM_TYPES[i].equals(type)) {
                return true;
            }
        }

        return false;
    }

    public String toString() {
        return "alt[" + id + ", " + pattern + "]";
    }

}
