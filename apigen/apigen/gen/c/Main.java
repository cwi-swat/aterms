package apigen.gen.c;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import apigen.adt.ADT;
import apigen.adt.api.ADTFactory;
import apigen.adt.api.Entries;
import apigen.gen.GenerationParameters;
import apigen.gen.tom.TomSignatureGenerator;

public class Main {
	private static GenerationParameters params = new GenerationParameters();

	private static boolean jtom;
	private static boolean jtype;
	private static boolean termCompatibility;
	private static String output;
	private static String prologue;
	private static String prefix = "";

	private static void usage() {
		System.err.println("usage: apigen.gen.c.Main [options]");
		System.err.println("options:");
		System.err.println("\t-prefix <prefix>          [\"\"]");
		System.err.println("\t-input <in>               [-]");
		System.err.println("\t-output <out>");
		System.err.println("\t-prologue <prologue>");
		System.err.println("\t-jtom");
		System.err.println("\t-jtype");
		System.err.println("\t-verbose");
		System.exit(1);
	}

	public final static void main(String[] args) throws IOException {
		String input = "-";

		InputStream inputStream;

		if (args.length == 0) {
			usage();
		}

		for (int i = 0; i < args.length; i++) {
			if ("-help".startsWith(args[i])) {
				usage();
			}
			else if ("-verbose".startsWith(args[i])) {
				params.setVerbose(true);
			}
			else if ("-prefix".startsWith(args[i])) {
				params.setPrefix(args[++i]);
			}
			else if ("-output".startsWith(args[i])) {
				output = args[++i];
			}
			else if ("-prologue".startsWith(args[i])) {
				prologue = args[++i];
			}
			else if ("-input".startsWith(args[i])) {
				input = args[++i];
			}
			else if ("-compatible:term".equals(args[i])) {
				termCompatibility = true;
			}
			else if ("-jtom".startsWith(args[i])) {
				jtom = true;
			}
			else if ("-jtype".startsWith(args[i])) {
				jtype = true;
			}
			else {
				usage();
			}
		}

		if (input.equals("-")) {
			inputStream = System.in;
			if (output == null) {
				usage();
			}
		}
		else {
			inputStream = new FileInputStream(input);
			if (output == null) {
				int extIndex = input.lastIndexOf('.');
				output = input.substring(0, extIndex);
			}

			run(inputStream);
		}
	}

	static private void run(InputStream input) {
		ADT adt;

		try {
			ADTFactory factory = new ADTFactory();
			Entries entries = factory.EntriesFromFile(input);

			adt = new ADT(entries);

			APIGenerator apigen =
				new APIGenerator(adt, params, output, prologue, termCompatibility);
			apigen.run();
			if (jtom) {
				new TomSignatureGenerator(adt, new CTomSignatureImplementation(prefix, jtype), params).run();
			}
			new CDictionaryGenerator(adt, params, factory, ".", output, apigen.getAFunRegister()).run();

		}
		catch (IOException e) {
			System.out.println("Failed to read ADT from file");
			System.exit(1);
		}

	}
}
