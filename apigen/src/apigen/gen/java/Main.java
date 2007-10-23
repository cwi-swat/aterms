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
import apigen.adt.ListType;
import apigen.adt.SeparatedListType;
import apigen.adt.Type;
import apigen.adt.api.types.Module;
import apigen.gen.GenerationObserver;
import apigen.gen.Generator;
import apigen.gen.TypeConverter;

public class Main {
	public static void main(String[] arguments) {
		JavaGenerationParameters params = new JavaGenerationParameters();
		ADT adt;
		List<String> args = new LinkedList<String>(Arrays.asList(arguments));
		if (args.size() == 0) {
			usage(params);
			return;
		} else if (args.contains("-h") || args.contains("--help")) {
			usage(params);
			return;
		}

		try {
			params.parseArguments(args);
			params.check();
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage(params);
			return;
		}
		try {
			adt = ADTReader.readADT(params);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage(params);
			return;
		}

		generateAPI(adt, params);
	}

	private static void usage(JavaGenerationParameters params) {
		System.err.println("Usage: apigen.gen.java.Main [parameters]");
		System.err.println("Parameters:");
		System.err.print(params.usage());
	}

	public static void generateAPI(ADT adt, JavaGenerationParameters params) {
		GeneratedJavaFileCollector l = new GeneratedJavaFileCollector();

		generateFactories(adt, params, l);
		generateAbstractTypes(adt, params, l);

		if (params.isVisitable()) {
			generateVisitors(adt, params, l);
			generateForward(adt, params, l);
			generateForwardVisitable(adt, params, l);
			generateForwardVoid(adt, params, l);
		}

		generateTypeClasses(adt, params, l);
		showGeneratedFiles(params, l.getGeneratedFiles());
	}

	private static void showGeneratedFiles(JavaGenerationParameters params,
			List<String> generatedFiles) {
		StringBuffer buf = new StringBuffer();
		Iterator<String> iter = generatedFiles.iterator();
		while (iter.hasNext()) {
			String fileName = iter.next();
			buf.append(fileName);
			if (iter.hasNext()) {
				buf.append(' ');
			}
		}

		if (params.isGenerateJar()) {
			try {
				PrintStream out = new PrintStream(new FileOutputStream(
						"apigen.env"));
				out.println("APINAME=" + params.getApiName());
				out.println("VERSION=" + params.getVersion());
				out.println("DIRECTORY=" + params.getOutputDirectory());
				out.println("FILES=\"" + buf.toString() + '"');
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private static void run(Generator generator, GenerationObserver l) {
		generator.addGenerationObserver(l);
		generator.run();
	}

	private static void generateFactories(ADT adt,
			JavaGenerationParameters params, GenerationObserver observer) {
		Iterator<Module> moduleIterator = adt.moduleIterator();
		while (moduleIterator.hasNext()) {
			Module module = moduleIterator.next();
			run(new FactoryGenerator(adt, params, module), observer);
		}
	}

	private static void generateAbstractTypes(ADT adt,
			JavaGenerationParameters params, GenerationObserver observer) {
		Iterator<Module> moduleIterator = adt.moduleIterator();
		while (moduleIterator.hasNext()) {
			Module module = moduleIterator.next();
			run(new AbstractTypeGenerator(adt, params, module), observer);
			run(new AbstractListGenerator(adt, params, module), observer);
		}
	}

	private static void generateTypeClasses(ADT adt,
			JavaGenerationParameters params, GenerationObserver observer) {
		TypeConverter typeConverter = new TypeConverter(
				new JavaTypeConversions("factory"));
		Iterator<Type> typeIterator = adt.typeIterator();
		while (typeIterator.hasNext()) {
			Type type = typeIterator.next();

			if (type instanceof ListType) {
				if (type instanceof SeparatedListType) {
					run(new SeparatedListTypeGenerator(params,
							(SeparatedListType) type), observer);

				} else {
					run(new ListTypeGenerator(adt, params, (ListType) type),
							observer);
				}
			} else if (!typeConverter.isReserved(type.getId())) {
				run(new TypeGenerator(params, type), observer);
				generateAlternativeClasses(adt, params, type, observer);
			}
		}
	}

	private static void generateAlternativeClasses(ADT adt,
			JavaGenerationParameters params, Type type, GenerationObserver l) {
		Iterator<Alternative> altIterator = type.alternativeIterator();
		while (altIterator.hasNext()) {
			Alternative alt = altIterator.next();
			run(new AlternativeGenerator(adt, params, type, alt), l);
		}
	}

	private static void generateVisitors(ADT adt,
			JavaGenerationParameters params, GenerationObserver l) {
		Iterator<Module> moduleIterator = adt.moduleIterator();
		while (moduleIterator.hasNext()) {
			Module module = moduleIterator.next();
			run(new VisitorGenerator(adt, params, module), l);
		}
	}

	private static void generateForward(ADT adt,
			JavaGenerationParameters params, GenerationObserver l) {
		Iterator<Module> moduleIterator = adt.moduleIterator();
		while (moduleIterator.hasNext()) {
			Module module = moduleIterator.next();
			run(new ForwardGenerator(adt, params, module), l);
		}
	}

	private static void generateForwardVisitable(ADT adt,
			JavaGenerationParameters params, GenerationObserver l) {
		Iterator<Module> moduleIterator = adt.moduleIterator();
		while (moduleIterator.hasNext()) {
			Module module = moduleIterator.next();
			run(new ForwardVisitableGenerator(adt, params, module), l);
		}
	}

	private static void generateForwardVoid(ADT adt,
			JavaGenerationParameters params, GenerationObserver l) {
		Iterator<Module> moduleIterator = adt.moduleIterator();
		while (moduleIterator.hasNext()) {
			Module module = moduleIterator.next();
			run(new ForwardVoidGenerator(adt, params, module), l);
		}
	}
}
