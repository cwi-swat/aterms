package apigen.gen.java;

import apigen.gen.TypeConverter;

public class BoxingBuilder {
    static public String buildBoxer(String type, String subject) {
        if (type.equals(TypeConverter.INT_TYPE)) {
            return buildIntegerBoxer(subject);
        } else if (type.equals(TypeConverter.REAL_TYPE)) {
            return buildRealBoxer(subject);
        } else {
            return subject;
        }
    }

    static public String buildUnboxer(String type, String subject) {
        if (type.equals(TypeConverter.INT_TYPE)) {
            return buildIntegerUnboxer(subject);
        } else if (type.equals(TypeConverter.REAL_TYPE)) {
            return buildRealUnboxer(subject);
        } else {
            return subject;
        }
    }
    
    private static String buildRealBoxer(String subject) {
        return "new Double(" + subject + ")";
    }

    private static String buildIntegerBoxer(String subject) {
        return "new Integer(" + subject + ")";
    }
    
    private static String buildRealUnboxer(String subject) {
        return subject + ".doubleValue()";
    }

    private static String buildIntegerUnboxer(String subject) {
        return subject + ".intValue()";
    }
}
