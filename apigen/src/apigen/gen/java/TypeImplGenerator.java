
package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.*;
import apigen.gen.StringConversions;

public class TypeImplGenerator extends JavaGenerator {
    private Type type;
    private String apiName;
    
	protected TypeImplGenerator(
	   Type type,
		String directory,
		String pkg,
		String apiName,
		List standardImports,
		boolean verbose) {
		super(directory, getClassImplName(type.getId()), pkg, standardImports, verbose, false);
		this.type = type;
		this.apiName = apiName;
	}
	
	public static String getClassImplName(String type) {
		return TypeGenerator.getClassName(type) + "Impl";
	}

	protected void generate() {
		genTypeClassImpl(type);
	}

	private void genTypeClassImpl(Type type) {
		String class_impl_name = getClassImplName(type.getId());
		String class_name = TypeGenerator.getClassName(type.getId());
		String get_factory = "getStatic" + FactoryGenerator.getFactoryClassName(apiName) + "()";
       
			println("abstract public class " + class_impl_name + " extends " + 
			GenericConstructorGenerator.getConstructorClassName(apiName));
			println("{");

			println("  static " + class_name + " fromString(String str)");
			println("  {");
			println("    aterm.ATerm trm = " + get_factory + ".parse(str);");
			println("    return fromTerm(trm);");
			println("  }");

			println("  static " + class_name + " fromTextFile(InputStream stream) " +
				"throws aterm.ParseError, IOException");
			println("  {");
			println("    aterm.ATerm trm = " + get_factory + ".readFromTextFile(stream);");
			println("    return fromTerm(trm);");
			println("  }");

			println("  public boolean isEqual(" + class_name + " peer)");
			println("  {");
			println("    return term.isEqual(peer.toTerm());");
			println("  }");

		println("  public static " + class_name + " fromTerm(aterm.ATerm trm)");
		println("  {");
		println("    " + class_name + " tmp;");
		genFromTermCalls(type);
		println();
		println("    throw new RuntimeException(\"This is not a " + class_name + ": \" + trm);" );
		println("  }");

		println();
		genTypeDefaultProperties(type);
		genDefaultGetAndSetMethods(type);
		println();
		println("}");
		println();
		
		}

	private void genFromTermCalls(Type type)
		{
		  String class_name = getClassName(type.getId());
		  Iterator alts = type.alternativeIterator();
		  while (alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();    
			String alt_class_name = AlternativeGenerator.getAltClassName(type, alt);
			println("    if ((tmp = " + alt_class_name + ".fromTerm(trm)) != null) {");
			println("      return tmp;");
			println("    }");
			println();
		  }
		}
		
		private void genDefaultGetAndSetMethods(Type type) {
		Iterator fields = type.fieldIterator();
    
		while (fields.hasNext()) {
		  Field field = (Field) fields.next();
		  genDefaultGetAndSetMethod(type, field);
		}
		}


	  private void genTypeDefaultProperties(Type type)
	  {
		genDefaultIsMethods(type);
		genDefaultHasMethods(type);
	  }
   
	private void genDefaultGetAndSetMethod(Type type, Field field) {
	   String class_name = TypeGenerator.getClassName(type.getId());
	   String field_name = StringConversions.makeCapitalizedIdentifier(field.getId());
	   String field_id = getFieldId(field.getId());
	   String field_type_id = TypeGenerator.getClassName(field.getType());
    
	   // getter    
	   println("  public " + field_type_id + " get" + field_name + "()");
	   println("  {");
	   println("     throw new RuntimeException(\"This " + class_name + " has no " + field_name + "\");"); 
	   println("  }");
	   println();
    
	   // setter
	   println("  public " + class_name + " set" + field_name + "(" + field_type_id + " " + field_id + ")");
	   println("  {");
	   println("     throw new RuntimeException(\"This " + class_name + " has no " + field_name + "\");");  
	   println("  }");
	   println();
	 }
	 
	private void genDefaultIsMethod(Alternative alt) {   
		   println("  public boolean is" + StringConversions.makeCapitalizedIdentifier(alt.getId()) + "()");
		   println("  {");
		   println("    return false;");
		   println("  }");
		   println();
	   }
  
	 private void genDefaultHasMethods(Type type)
	 {
	   Iterator fields = type.fieldIterator() ;
    
	   while (fields.hasNext()) {
		 Field field = (Field) fields.next();    
		 genDefaultHasMethod(field);
	   }
	 }
  
	 private void genDefaultHasMethod(Field field)
	 {    
		 println("  public boolean has" + StringConversions.makeCapitalizedIdentifier(field.getId()) + "()");
		 println("  {");
		 println("    return false;");
		 println("  }");
		 println();
	 }
  
	private void genDefaultIsMethods(Type type)
	{
	  Iterator alts = type.alternativeIterator();
	  while (alts.hasNext()) {
		Alternative alt = (Alternative) alts.next();    
		genDefaultIsMethod(alt);
	  }
	}
    
	private void genTypeClassImplFile(Type type) {
	   printPackageDecl();
	   imports.add("java.io.InputStream");
	   imports.add("java.io.IOException");
	   
	   printImports();
	   println();
    
	   genTypeClassImpl(type); 
	   stream.close();
       
	   
	 }
}
