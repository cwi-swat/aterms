package apigen.gen.tom.java;

//import java.util.ArrayList;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.ADTReader;
import apigen.gen.tom.TomSignatureGenerator;

public class Main {
	public static final void main(String[] arguments) {
		JavaTomGenerationParameters params = buildDefaultParameters();
		JavaTomGenerationParameters duplicatedParams = null;
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
		}
		catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage(params);
			return;
		}
		
		ADT adt = ADTReader.readADT(params);
		JavaTomSignatureImplementation signature = new JavaTomSignatureImplementation(params);
		
		if(params.isJavaGen() && params.getPackageName() != null) {
			// in that case we chenge output to output/package
			duplicatedParams = (JavaTomGenerationParameters) params.clone();
			duplicatedParams.setOutputDirectory(concat(params.getOutputDirectory(), params.getPackageName()));
			new TomSignatureGenerator(adt, signature, duplicatedParams).run();
		} else {
			new TomSignatureGenerator(adt, signature, params).run();
		}
		
		if(params.isJavaGen()) { // generate Java Stuff
			params.check();
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
	
	private static String concat(String outputDir, String packageName) {
		return outputDir+File.separatorChar+ packageName.replace('.', File.separatorChar);
	}
	 
}
