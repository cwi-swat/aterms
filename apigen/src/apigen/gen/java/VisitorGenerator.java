package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.adt.api.types.Module;
import apigen.gen.TypeConverter;

public class VisitorGenerator extends JavaGenerator {
    public static final String CLASS_NAME = "Visitor";
    

    private ADT adt;
    private String apiName;
    private Module module;
    private String traveler;
    
    public VisitorGenerator(ADT adt, JavaGenerationParameters params, Module module) {
        super(params);
        this.adt = adt;
        this.module = module;
        this.apiName = params.getApiExtName(module);
        this.traveler = params.getTravelerName();
    }

      public String getClassName() {
        return VisitorGenerator.className(module.getModulename().getName());
    }
    
    public static String className(String moduleName) {
        return moduleName + CLASS_NAME;
    }

    protected void generate() {
        printPackageDecl();

        println("public interface " + getClassName() + " {");
        genVisits(adt);
        println("}");
    }

    protected void genVisits(ADT adt) {
        TypeConverter typeConverter = new TypeConverter(new JavaTypeConversions("factory"));
        Iterator types = adt.typeIterator();
        while (types.hasNext()) {
            Type type = (Type) types.next();

            if (type instanceof ListType) {
                genListVisit(type);
            } else if (!typeConverter.isReserved(type.getId())) {
              /* builtin childs are not visitable */
                Iterator alts = type.alternativeIterator();
                while (alts.hasNext()) {
                    Alternative alt = (Alternative) alts.next();
                    genVisit(type, alt);
                }
            }
        }
    }

    private void genListVisit(Type type) {
        String classTypeName = ListTypeGenerator.className(type);
        String paramName = TypeGenerator.qualifiedClassName(
                    		  getJavaGenerationParameters(),
						  classTypeName);
        String returnName = paramName;
        genVisitDecl(classTypeName, paramName, returnName);
    }

    private void genVisit(Type type, Alternative alt) {
        String methodName = FactoryGenerator.concatTypeAlt(type, alt);
        String paramName =
            AlternativeGenerator.qualifiedClassName(
                getJavaGenerationParameters(),
                type,
                alt);
        String returnName = TypeGenerator.qualifiedClassName(
                getJavaGenerationParameters(),
                TypeGenerator.className(type));
        genVisitDecl(methodName, paramName, returnName);
    }

    private void genVisitDecl(String methodName, String paramTypeName, String returnTypeName) {
        println(
            "  public abstract " 
        		   + returnTypeName 
			   + " visit_"
                + methodName
                + "("
                + paramTypeName
                + " arg) throws " + traveler + ".VisitFailure;");
    }

     public static String qualifiedClassName(
            JavaGenerationParameters params, 
            String moduleName) {
        StringBuffer buf = new StringBuffer();
        String pkg = params.getPackageName();

        if (pkg != null) {
            buf.append(pkg);
            buf.append('.');
        }
        buf.append(params.getApiExtName(moduleName).toLowerCase());
        buf.append('.');
        buf.append(VisitorGenerator.className(moduleName));
        return buf.toString();
    }

    public String getPackageName() {
    		return apiName.toLowerCase();
    }

    public String getQualifiedClassName() {
        return getClassName();
    }

}
