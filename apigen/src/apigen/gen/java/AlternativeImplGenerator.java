
package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.*;
import apigen.gen.StringConversions;


public class AlternativeImplGenerator extends JavaGenerator {
	private boolean visitable;
    private Type type;
	private Alternative alt;
	private String apiName;
	
    private String typeId;
    private String altId;
    private String className;
    private String subClassName;
    private String superClassName;
	
	protected AlternativeImplGenerator(
	    Type type,
	    Alternative alt,
	    String apiName,
		String directory,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding,
		boolean visitable) {
		super(directory, className(type.getId(),alt.getId()), pkg, standardImports, verbose);
		this.type = type;
		this.alt = alt;
		this.apiName = apiName;
		
		/* some abbreviations */
		this.typeId = type.getId();
		this.altId = alt.getId();
		this.className = className(typeId,altId);
		this.subClassName = AlternativeGenerator.className(type,alt);
		this.superClassName = TypeGenerator.className(type);
		this.visitable = visitable;
	}

	public static String className(String type, String alt) {
		return AlternativeGenerator.className(type, alt) + "Impl";
	}
	
	public static String className(Type type, Alternative alt) {
		return className(type.getId(),alt.getId()); 
	}
		
	protected void generate() {
		
		printPackageDecl();
		printImports();
		genAlternativeClassImpl(type, alt);
	}

	private void genAltInitialize(Alternative alt) 
	  {
		println("  static public void initializePattern()");
		println("  {");
		println("    pattern = getStaticFactory().parse(\"" + 
				  StringConversions.escapeQuotes(alt.buildMatchPattern().toString()) + "\");");
		println("  }");
		println();
		}

	  private void genAlternativeClassImpl(Type type, Alternative alt)
	  {
	    println("public class " + className);
	    println("extends " + typeId);
	    if (visitable) {
	      println("implements jjtraveler.Visitable");
	    }
	    println("{");
	    println("  static private aterm.ATerm pattern = null;");
	    println();
	    println("  protected aterm.ATerm getPattern() {");
	    println("    return pattern;");
	    println("  }");
	    
	    genAltFieldIndexMembers(type,alt);
	    genAltClone(type, alt);
	    genAltMake(type, alt);
	    genAltInitialize(alt);
	    genAltFromTerm(type, alt);
	    genAltToTerm(type,alt);
	    genOverrideProperties(type,alt);
	    genAltGetAndSetMethods(type, alt);
	    genAltHashFunction(type, alt);
	    
	    if (visitable) {
	      genAltVisitableInterface(type, alt);
	    }
	    
	    println("}");
	  }


	private void genAltHashFunction(Type type, Alternative alt) 
	  {       
		if (!hasReservedTypeFields(type,alt)) {
		  int arity = type.getAltArity(alt);
		  String goldenratio = "0x9e3779b9";
		  int changingArg = guessChangingArgument(type,alt);
		  String initval;
    
		  if (changingArg > 0) {
			initval = "getArgument(" + changingArg + ").hashCode()";
		  }
		  else {
			initval = "0";
		  }
    
		  println("  protected int hashFunction() {");
		  println("    int c = " + initval + " + (getAnnotations().hashCode()<<8);");
		  println("    int a = " + goldenratio + ";");
		  println("    int b = " + goldenratio + ";");
      
		  for (int i = arity - 1; i >= 0; i--) {
			int shift = (i%4) * 8;
			println("    " + "aaaabbbbcccc".toCharArray()[i%12] + 
					" += (getArgument(" + i + ").hashCode() << " + 
					shift + ");");
		  }
  
		  println();
		  println("    a -= b; a -= c; a ^= (c >> 13);");
		  println("    b -= c; b -= a; b ^= (a << 8);");
		  println("    c -= a; c -= b; c ^= (b >> 13);");
		  println("    a -= b; a -= c; a ^= (c >> 12);");
		  println("    b -= c; b -= a; b ^= (a << 16);");
		  println("    c -= a; c -= b; c ^= (b >> 5);");
		  println("    a -= b; a -= c; a ^= (c >> 3);");
		  println("    b -= c; b -= a; b ^= (a << 10);");
		  println("    c -= a; c -= b; c ^= (b >> 15);");
		  println();
		  println("    return c;");
		  println("  }");
		}
		}

		private int guessChangingArgument(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
    
		/* if an argument has the same type as the result type, there
		 * exists a chance of building a tower of this constructor where
		 * only this argument changes. Therefore, this argument must be
		 * very important in the computation of the hash code in order to
		 * avoid collissions
		 */
		for(int i = 0; fields.hasNext(); i++) {
		  Field field = (Field) fields.next();
      
		  if (field.getType().equals(type.getId())) {
			return i;
		  }
		}
    
			return -1;
		}



		private void genAltToTerm(Type type, Alternative alt) {
		if (hasReservedTypeFields(type, alt)) {
		  println("  public aterm.ATerm toTerm() {");
		  println("    if(term == null) {");
		  println("      java.util.List args = new java.util.LinkedList();");
		  Iterator fields = type.altFieldIterator(alt.getId());
		  for (int i = 0; fields.hasNext(); i++) {
			Field field = (Field) fields.next();
			String field_type = field.getType();
        
			if (field_type.equals("str")) {
			  println("      args.add(((aterm.ATermAppl) getArgument(" + i + ")).getAFun().getName());");
			}
			else if (field_type.equals("int")) {
			  println("      args.add(new Integer(((aterm.ATermInt) getArgument(" + i + ")).getInt()));");
			}
			else if (field_type.equals("real")) {
			  println("      args.add(new Double (((aterm.ATermReal) getArgument(" + i + ")).getReal()));");
			}
			else if (field_type.equals("term")) {
			  println("      args.add((aterm.ATerm) getArgument(" + i + "));");
			}
			else {
			  println("      args.add(((" + GenericConstructorGenerator.getConstructorClassName(apiName) + ") getArgument(" + i + ")).toTerm());");
			}
		  }
		  println("      setTerm(getFactory().make(getPattern(), args));");
		  println("    }");
		  println("    return term;");
		  println("  }");
		  println();
		}
		}
		
	private void genAltFieldIndexMembers(Type type, Alternative alt)
	  {
		Iterator fields = type.altFieldIterator(alt.getId());
		int argnr = 0;
    
		while (fields.hasNext()) {
		  Field field = (Field) fields.next();
		  String fieldId = getFieldIndex(field.getId());
      
		  println("  private static int " + fieldId + " = " + argnr + ";");
		  argnr++;
		}
	  }
  	
		private void genAltFromTerm(Type type, Alternative alt)
	  {
		Iterator fields;
		int argnr;
    
		println("  static public " + superClassName + " fromTerm(aterm.ATerm trm)");
		println("  {");
		println("    java.util.List children = trm.match(pattern);");  
		println();
		println("    if (children != null) {");    
		print  ("      " + superClassName + " tmp = getStatic" + FactoryGenerator.className(apiName) + 
				"().make" + subClassName + "(");

		fields = type.altFieldIterator(alt.getId());
		argnr = 0;            
		while (fields.hasNext()) {
		  Field field = (Field) fields.next();
		  String fieldType = field.getType();
		  String fieldClass = TypeGenerator.className(fieldType);
      
		  if (fieldType.equals("str")) {
			print("(String) children.get(" + argnr + ")");
		  }
		  else if (fieldType.equals("int")) {
			print("(Integer) children.get(" + argnr + ")");
		  }
		  else if (fieldType.equals("real")) {
			print("(Double) children.get(" + argnr + ")");
		  }
		  else if (fieldType.equals("term")) {
			print("(aterm.ATerm) children.get(" + argnr + ")");
		  }
		  else {
			print(fieldClass + ".fromTerm( (aterm.ATerm) children.get(" + argnr + "))");
		  }
      
		  if (fields.hasNext()) {
			print(", ");
		  }
		  argnr++;
		}
		println(");");
		println("      tmp.setTerm(trm);");
		println("      return tmp;");
		println("    }"); // endif      
		println("    else {");
		println("      return null;");
		println("    }");
		println("  }");
	  }
	  
	private void genAltGetAndSetMethod(Field field) {
		String fieldName = StringConversions.makeCapitalizedIdentifier(field.getId());
		String fieldId = getFieldId(field.getId());
		String fieldType = field.getType();
		String fieldClass = TypeGenerator.className(fieldType);
		String fieldIndex = getFieldIndex(field.getId());

		// getter    
		println("  public " + fieldClass + " get" + fieldName + "()");
		println("  {");
    
		if (fieldType.equals("str")) {
		  println("   return ((aterm.ATermAppl) this.getArgument(" + fieldIndex + ")).getAFun().getName();");
		}
		else if (fieldType.equals("int")) {
		  println("   return new Integer(((aterm.ATermInt) this.getArgument(" + fieldIndex + ")).getInt());");
		}
		else if (fieldType.equals("double")) {
		  println("   return new Double(((aterm.ATermReal) this.getArgument(" + fieldIndex + ")).getReal());");
		}
		else if (fieldType.equals("term")) {
		  println("   return this.getArgument(" + fieldIndex + ");");    
		}
		else {
		  println("    return (" + fieldClass + ") this.getArgument(" + fieldIndex + ") ;");
		}
    
		println("  }");
		println();

		// setter    
		println("  public " + superClassName + " set" + fieldName + "(" +
				   fieldClass + " " + fieldId + ")");
		println("  {");
    
		print("    return (" + superClassName + ") super.setArgument(");
    
		if (fieldType.equals("str")) {
		  print("getFactory().makeAppl(getFactory().makeAFun(" + fieldId + ", 0, true))");
		}
		else if (fieldType.equals("int")) {
		  print("getFactory().makeInt(" + fieldId + ".intValue())");
		}
		else if (fieldType.equals("double")) {
		  print("getFactory().makeReal(" + fieldId + ".doubleValue())");
		}
		else {
		  print(fieldId);  
		}
    
		println(", " + fieldIndex + ");");
    
		println("  }");
		println();

	  }
	  
	

	  private void genOverrideProperties(Type type, Alternative alt)
	  {
		genOverrideIsMethod(alt);
		genOverrideHasMethods(type, alt);
	  }
	 
	private void genOverrideIsMethod(Alternative alt) {
	  println("  public boolean is" + StringConversions.makeCapitalizedIdentifier(alt.getId()) + "()");
	  println("  {");
	  println("    return true;");
	  println("  }");
	  println();
	}
    
 
	private void genOverrideHasMethods(Type type, Alternative alt)
	{
	  Iterator fields = type.altFieldIterator(alt.getId());
    
	  while (fields.hasNext()) {
		Field field = (Field) fields.next();   
		genOverrideHasMethod(field);
	  }
	}
  
	private void genOverrideHasMethod(Field field)
	{
	   println("  public boolean has" + StringConversions.makeCapitalizedIdentifier(field.getId()) + "()");
	   println("  {");
	   println("    return true;");
	   println("  }");
	   println();
	}
  	
	private void genAltGetAndSetMethods(Type type, Alternative alt) {
    
		Iterator fields = type.altFieldIterator( alt.getId());
		while (fields.hasNext()) {
		  Field field = (Field) fields.next();
		  genAltGetAndSetMethod(field);
		}
    
		genOverrrideSetArgument(type, alt);
		}
		
	private void genOverrrideSetArgument(Type type, Alternative alt) {
			String alt_classname = AlternativeGenerator.className(type,alt);
		
			println("  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {");
			if (type.getAltArity(alt) > 0) {
			  println("    switch(i) {");
		
			  Iterator fields = type.altFieldIterator(alt.getId());
			  for (int i = 0; fields.hasNext(); i++) {
				  Field field = (Field) fields.next();
				  String field_type = field.getType();
				  String field_class = TypeGenerator.className(field_type);
			  
				  String instance_of;
			
				  if (field_type.equals("str")) {
					  instance_of = "aterm.ATermAppl";
				  }
				  else if (field_type.equals("int")) {
					  instance_of = "aterm.ATermInt";
				  }
				  else if (field_type.equals("double")) {
					  instance_of = "aterm.ATermReal";
				  }
				  else if (field_type.equals("term")) {
					  instance_of = "aterm.ATerm";
				  }
				  else {
					  instance_of = field_class;
				  }
			
				  println("      case " + i + ":");
				  println("        if (! (arg instanceof " + instance_of + ")) { ");
				  println("          throw new RuntimeException(\"Argument " + i + " of a " + alt_classname +
							" should have type " + field_type + "\");");
				  println("        }");
				  println("        break;");
			  }
			  println("      default: throw new RuntimeException(\"" + alt_classname + " does not have an argument at \" + i );");
			  println("    }");
			  println("    return super.setArgument(arg, i);");
			}
			else {
				println("      throw new RuntimeException(\"" + alt_classname + " has no arguments\");");
			}
			println("  }");
		}
		
	private void genAltMake(Type type, Alternative alt) {
		String altClassName = AlternativeGenerator.className(type,alt);
    
		println("  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] i_args," +
				" aterm.ATermList annos) {");
		println("    return get" + FactoryGenerator.className(apiName) + "().make" + altClassName +
				"(fun, i_args, annos);");
		println("  }");
		}


		private void genAltClone(Type type, Alternative alt) {
		String altClassName = AlternativeGenerator.className(type,alt);
		
		println("  public shared.SharedObject duplicate() {");
		println("    " + altClassName + " clone = new " + altClassName + "();");
		println("     clone.init(hashCode(), getAnnotations(), getAFun(), " +
				"getArgumentArray());");
		println("    return clone;");
		println("  }");
		println();
		}

  
	  // This is not well thought out yet!
	  private void genAltVisitableInterface(Type type, Alternative alt)
	  {
		String altClassName = AlternativeGenerator.className( type,alt );

		println("  public void accept(jjtraveler.Visitor v) throws jjtraveler.VisitFailure");
		println("  {");
		println("    v.visit" + altClassName + "(this);");
		println("  }");
		println();
	 }

	private int computeAltArityNotReserved(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
		int count = 0;
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			if (!converter.isReserved(field.getType())) {
				count++;
			}
		}
		return count;
	}

	private boolean hasReservedTypeFields(Type type, Alternative alt) {
		return computeAltArityNotReserved(type, alt) < type.getAltArity(alt);
	}
}
