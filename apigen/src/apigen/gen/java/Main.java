package apigen.gen.java;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.ADTReader;
import apigen.adt.Alternative;
import apigen.adt.NormalListType;
import apigen.adt.SeparatedListType;
import apigen.adt.Type;
import apigen.gen.TypeConverter;

public class Main {
	public static void main(String[] arguments) {
		JavaGenerationParameters params = buildDefaultParameters();

		List args = new LinkedList(Arrays.asList(arguments));
		if (args.size() == 0) {
			usage(params);
			System.exit(1);
		}
		else if (args.contains("-h") || args.contains("--help")) {
			usage(params);
			return;
		}

		try {
			params.parseArguments(args);
			checkParameters(params);
		}
		catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage(params);
			System.exit(1);
		}

		generateAPI(params, ADTReader.readADT(params));
	}

	private static void checkParameters(JavaGenerationParameters params) {
		if (params.getApiName() == null) {
			throw new IllegalArgumentException("No API name specified");
		}
	}

	private static JavaGenerationParameters buildDefaultParameters() {
		JavaGenerationParameters params = new JavaGenerationParameters();
		params.setOutputDirectory(".");
		params.setVerbose(false);
		params.setVisitable(false);
		return params;
	}

	private static void usage(JavaGenerationParameters params) {
		System.err.println("Usage: apigen.gen.java.Main [parameters]");
		System.err.println("Parameters:");
		System.err.print(params.usage());
	}

	private static void generateAPI(JavaGenerationParameters params, ADT adt) {
		new FactoryGenerator(adt, params).run();
		new GenericConstructorGenerator(adt, params).run();
		//		new MakeRulesGenerator(adt, params).run();

		if (params.isVisitable()) {
			new VisitorGenerator(adt, params).run();
			new ForwardGenerator(adt, params).run();
		}

		generateTypeClasses(params, adt);
	}

	private static void generateTypeClasses(JavaGenerationParameters params, ADT api) {
		TypeConverter typeConverter = new TypeConverter(new JavaTypeConversions());
		Iterator typeIterator = api.typeIterator();
		while (typeIterator.hasNext()) {
			Type type = (Type) typeIterator.next();

			if (type instanceof NormalListType) {
				new ListTypeGenerator(params, (NormalListType) type).run();
			}
			else if (type instanceof SeparatedListType) {
				new SeparatedListTypeGenerator(params, (SeparatedListType) type).run();

			}
			else if (!typeConverter.isReserved(type.getId())) {
				new TypeGenerator(params, type).run();
				generateAlternativeClasses(params, type);
			}
		}
	}

	private static void generateAlternativeClasses(JavaGenerationParameters params, Type type) {
		Iterator altIterator = type.alternativeIterator();
		while (altIterator.hasNext()) {
			Alternative alt = (Alternative) altIterator.next();
			new AlternativeGenerator(params, type, alt).run();
		}
	}
}
