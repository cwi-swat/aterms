package apigen.gen.java;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
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
import apigen.gen.GenerationObserver;
import apigen.gen.Generator;
import apigen.gen.TypeConverter;

public class Main {
	public static void main(String[] arguments) {
		JavaGenerationParameters params = buildDefaultParameters();

		List args = new LinkedList(Arrays.asList(arguments));
		if (args.size() == 0) {
			usage(params);
			return;
		}
		else if (args.contains("-h") || args.contains("--help")) {
			usage(params);
			return;
		}

		try {
			params.parseArguments(args);
			params.check();
		}
		catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage(params);
			return;
		}

		generateAPI(ADTReader.readADT(params), params);
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

	public static void generateAPI(ADT adt, JavaGenerationParameters params) {
		GeneratedJavaFileCollector l = new GeneratedJavaFileCollector();

		run(new FactoryGenerator(adt, params), l);
		run(new AbstractTypeGenerator(adt, params), l);

		if (params.isVisitable()) {
			run(new VisitorGenerator(adt, params), l);
			run(new ForwardGenerator(adt, params), l);
		}

		generateTypeClasses(adt, params, l);
		showGeneratedFiles(params, l.getGeneratedFiles());
	}

	private static void showGeneratedFiles(
		JavaGenerationParameters params,
		List generatedFiles) {
		StringBuffer buf = new StringBuffer();
		Iterator iter = generatedFiles.iterator();
		while (iter.hasNext()) {
			String fileName = (String) iter.next();
			buf.append(fileName);
			if (iter.hasNext()) {
				buf.append(' ');
			}
		}
		
		try {
			PrintStream out = new PrintStream(new FileOutputStream("apigen.env"));
			out.println("APINAME=" + params.getApiName());
			out.println("VERSION=" + params.getVersion());
			out.println("DIRECTORY=" + params.getOutputDirectory());
			out.println("FILES=\"" + buf.toString() + '"');
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void run(Generator generator, GenerationObserver l) {
		generator.addGenerationObserver(l);
		generator.run();
	}

	private static void generateTypeClasses(ADT adt, JavaGenerationParameters params, GenerationObserver l) {
		TypeConverter typeConverter = new TypeConverter(new JavaTypeConversions());
		Iterator typeIterator = adt.typeIterator();
		while (typeIterator.hasNext()) {
			Type type = (Type) typeIterator.next();

			if (type instanceof NormalListType) {
				run(new ListTypeGenerator(params, (NormalListType) type), l);
			}
			else if (type instanceof SeparatedListType) {
				run(new SeparatedListTypeGenerator(params, (SeparatedListType) type), l);

			}
			else if (!typeConverter.isReserved(type.getId())) {
				run(new TypeGenerator(params, type), l);
				generateAlternativeClasses(params, type, l);
			}
		}
	}

	private static void generateAlternativeClasses(JavaGenerationParameters params, Type type, GenerationObserver l) {
		Iterator altIterator = type.alternativeIterator();
		while (altIterator.hasNext()) {
			Alternative alt = (Alternative) altIterator.next();
			run(new AlternativeGenerator(params, type, alt), l);
		}
	}
}
