package apigen.gen.c;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.ADTReader;
import apigen.adt.api.Factory;
import aterm.pure.PureFactory;

public class Main {
	public final static void main(String[] arguments) {
		CGenerationParameters params = buildDefaultParameters();

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
			params.check();
		}
		catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage(params);
			System.exit(1);
		}

		generateAPI(params, ADTReader.readADT(params));
	}

	private static void usage(CGenerationParameters params) {
		System.err.println("Usage: apigen.gen.c.Main [options]");
		System.err.println("options:");
		System.err.println();
		System.err.println(params.usage());
	}

	private static CGenerationParameters buildDefaultParameters() {
		CGenerationParameters params = new CGenerationParameters();
		params.setOutputDirectory(".");
		params.setPrefix("");
		params.setVerbose(false);
		params.setFolding(false);
		return params;
	}

	private static void generateAPI(CGenerationParameters params, ADT adt) {
		Factory factory = Factory.getInstance(new PureFactory());
		APIGenerator apigen = new APIGenerator(params, adt);
		apigen.run();
		new CDictionaryGenerator(adt, params, factory.getPureFactory(), apigen.getAFunRegister()).run();
		try {
			PrintStream out = new PrintStream(new FileOutputStream("apigen.env"));
			out.println("APINAME=" + params.getApiName());
			out.println("DIRECTORY=" + params.getOutputDirectory());
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
