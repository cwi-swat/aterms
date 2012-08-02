package apigen.gen.java;

import apigen.gen.TypeConverter;

public class EquivalentBuilder {
    static public String buildEquivalent(String type, String subject, String peer) {
        if (type.equals(TypeConverter.INT_TYPE)) {
            return buildIntegerEquivalent(subject, peer);
        } else if (type.equals(TypeConverter.REAL_TYPE)) {
            return buildRealEquivalent(subject, peer);
        } else if (type.equals(TypeConverter.LIST_TYPE)) {
            return buildListEquivalent(subject, peer);
        } else if (type.equals(TypeConverter.STR_TYPE)) {
            return buildStringEquivalent(subject, peer);
        } else if (type.equals(TypeConverter.TERM_TYPE)) {
            return buildTermEquivalent(subject, peer);
        } else {
            return buildNonReservedEquivalent(subject, peer);
        }
    }

    private static String buildNonReservedEquivalent(String subject, String peer) {
        return subject + ".equivalent(" + peer + ")";
    }

    private static String buildTermEquivalent(String subject, String peer) {
        return subject + ".equivalent(" + peer + ")";
    }

    private static String buildStringEquivalent(String subject, String peer) {
        return subject + ".equals(" + peer + ")";
    }

    private static String buildListEquivalent(String subject, String peer) {
        return buildTermEquivalent(subject, peer);
    }

    private static String buildRealEquivalent(String subject, String peer) {
        return "(" + subject + " == " + peer + ")";
    }

    private static String buildIntegerEquivalent(String subject, String peer) {
        return "(" + subject + " == " + peer + ")";
    }
}
