package apigen.gen.tom.c;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.ADTReader;
import apigen.adt.api.types.Module;
import apigen.gen.tom.TomSignatureGenerator;

public class Main {
	public static final void main(String[] arguments) {
		CTomGenerationParameters params = buildDefaultParameters();
		List<String> args = new LinkedList<String>(Arrays.asList(arguments));
		if (args.size() == 0) {
			usage(params);
			System.exit(1);
		} else if (args.contains("-h") || args.contains("--help")) {
			usage(params);
			return;
		}

		try {
			params.parseArguments(args);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage(params);
			System.exit(1);
		}

		ADT adt = ADTReader.readADT(params);
		CTomSignatureImplementation signature = new CTomSignatureImplementation(
				params);
		generateSignature(adt, signature, params);

		if (params.isCGen()) { // generate C Stuff
			apigen.gen.c.Main.generateAPI(params, adt);
		}
	}

	private static void generateSignature(ADT adt,
			CTomSignatureImplementation signature,
			CTomGenerationParameters params) {
		Iterator<Module> it = adt.moduleIterator();
		while (it.hasNext()) {
			Module module = it.next();
			// TODO: Not a CTomSignatureGenerator???
			new TomSignatureGenerator(adt, signature, params, module).run();
		}
	}

	private static CTomGenerationParameters buildDefaultParameters() {
		CTomGenerationParameters params = new CTomGenerationParameters();
		params.setOutputDirectory(".");
		params.setPrefix("");
		return params;
	}

	private static void usage(CTomGenerationParameters params) {
		System.err.println("Usage: apigen.gen.tom.c.Main [options]");
		System.err.println("options:");
		System.err.println();
		System.err.println(params.usage());
	}
}
