package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;
import apigen.gen.Generator;
import apigen.gen.StringConversions;

public class MakeRulesGenerator extends Generator {
	private static final int MAX_FILES_IN_MAKEFILE_VARIABLE = 50;

	private ADT adt;
	private String name;
	private int bucket;
	private String prefix;

	public MakeRulesGenerator(ADT adt, GenerationParameters params) {
		super();
		this.adt = adt;
		this.name = StringConversions.makeCapitalizedIdentifier(params.getApiName());
		this.prefix = name + "API";
		setDirectory(params.getBaseDir());
		setFileName(name + "MakeRules");
		setExtension("");
	}
	
	private static String getClassFileName(String className) {
		return className + ".java";
	}

	protected void generate() {
		Iterator types = adt.typeIterator();
		bucket = 0;
		int i = 0;

		while (types.hasNext()) {
			Type type = (Type) types.next();

			makeNewBucket(i);

			printTypeClassFiles(type);
			i++;

			if (!(type instanceof ListType)) {
				i = printAlternativesClassFiles(i, type);
			}
		}

		println();
		println();
		printAccumulatedVariable();
	}

	private int printAlternativesClassFiles(int i, Type type) {
		Iterator alts = type.alternativeIterator();
		while (alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();
			makeNewBucket(i);
			printAlternativeClassFiles(type, alt);
			i++;
		}

		return i;
	}

	protected void printAccumulatedVariable() {
		print(
			prefix
				+ "=\\\n"
				+ getClassFileName(FactoryGenerator.className(name))
				+ " "
				+ getClassFileName(GenericConstructorGenerator.className(name))
				+ "\\\n");

		while (--bucket >= 0) {
			print("${" + prefix + bucket + "} ");
		}
	}

	protected void printAlternativeClassFiles(Type type, Alternative alt) {
		print("\\\n" + getClassFileName(AlternativeGenerator.className(type.getId(), alt.getId())));
	}

	protected void printTypeClassFiles(Type type) {
		print("\\\n" + getClassFileName(TypeGenerator.className(type.getId())));
	}

	protected void makeNewBucket(int i) {
		if (i % MAX_FILES_IN_MAKEFILE_VARIABLE == 0) {
			println();
			println();
			print(prefix + bucket + "=");
			bucket++;
		}
	}

}
