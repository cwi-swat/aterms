package apigen.gen.tom.java;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.ADTReader;
import apigen.adt.api.types.Module;


public class Main {
	
	public static final void main(String[] arguments) {
		JavaTomGenerationParameters params = new JavaTomGenerationParameters();
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
		generateSignature(adt, params);

		if (params.isJavaGen()) { // generate Java Stuff
			apigen.gen.java.Main.generateAPI(adt, params);
		}
	}

	private static void generateSignature(ADT adt,  JavaTomGenerationParameters params) {
		Iterator it = adt.moduleIterator();
		while(it.hasNext()) {
			Module module = (Module) it.next();
			JavaTomSignatureImplementation signature = new JavaTomSignatureImplementation(params, module);
			new JavaTomSignatureGenerator(adt, signature, params, module).run();
		}
	}
	
	private static void usage(JavaTomGenerationParameters params) {
		System.err.println("Usage: java apigen.gen.tom.java.Main [options]");
		System.err.println("options:");
		System.err.println();
		System.err.println(params.usage());
	}
}
