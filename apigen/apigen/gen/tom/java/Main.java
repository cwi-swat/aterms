package apigen.gen.tom.java;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.ADTReader;
import apigen.gen.tom.TomGenerationParameters;
import apigen.gen.tom.TomSignatureGenerator;

public class Main {
	public static final void main(String[] arguments) {
		TomGenerationParameters params = buildDefaultParameters();
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
		}
		catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage(params);
			System.exit(1);
		}

		ADT adt = ADTReader.readADT(params);
		JavaTomSignatureImplementation signature = new JavaTomSignatureImplementation(params);
		new TomSignatureGenerator(adt, signature, params).run();
	}

	private static TomGenerationParameters buildDefaultParameters() {
		TomGenerationParameters params = new TomGenerationParameters();
		params.setOutputDirectory(".");
		params.setPrefix("");
		return params;
	}

	private static void usage(TomGenerationParameters params) {
		System.err.println("Usage: apigen.gen.tom.java.Main [options]");
		System.err.println("options:");
		System.err.println();
		System.err.println(params.usage());
	}
}
