package apigen.gen.tom.java;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.ADTReader;
import apigen.gen.tom.TomSignatureGenerator;

public class Main {
	
	public static final void main(String[] arguments) {
		JavaTomGenerationParameters params = buildDefaultParameters();
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

		ADT adt = ADTReader.readADT(params);
		JavaTomSignatureImplementation signature = new JavaTomSignatureImplementation(params);
		new JavaTomSignatureGenerator(adt, signature, params).run();

		if (params.isJavaGen()) { // generate Java Stuff
			apigen.gen.java.Main.generateAPI(adt, params);
		}
	}

	private static JavaTomGenerationParameters buildDefaultParameters() {
		JavaTomGenerationParameters params = new JavaTomGenerationParameters();
		params.setOutputDirectory(".");
		params.setPrefix("");
		return params;
	}

	private static void usage(JavaTomGenerationParameters params) {
		System.err.println("Usage: java apigen.gen.tom.java.Main [options]");
		System.err.println("options:");
		System.err.println();
		System.err.println(params.usage());
	}
}
