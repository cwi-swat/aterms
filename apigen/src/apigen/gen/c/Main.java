package apigen.gen.c;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.api.ADTFactory;
import apigen.adt.api.Entries;
import apigen.gen.GenerationParameters;
import apigen.gen.tom.TomSignatureGenerator;
import aterm.ATermList;
import aterm.ParseError;

public class Main {
	private static GenerationParameters params = new GenerationParameters();

	private static boolean jtom;
	private static boolean jtype;
	private static boolean termCompatibility;
	private static String prologue;

	private static void usage() {
		System.err.println("usage: apigen.gen.c.Main [options]");
		System.err.println("options:");
		System.err.println("\t-prefix <prefix>          [\"\"]");
		System.err.println("\t-input <in>               <multiple allowed>");
		System.err.println("\t-output <outputdir>       [\".\"]");
		System.err.println("\t-name <api-name>          <obligatory>");
		System.err.println("\t-prologue <prologue>");
		System.err.println("\t-verbose");

		System.err.println("\t-jtom");
		System.err.println("\t-jtype");
		System.exit(1);
	}

	public final static void main(String[] args) {
		if (args.length == 0) {
			usage();
		}

		params.setOutputDirectory(".");
		params.setVerbose(false);
		params.setFolding(false);

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
			else if ("-name".startsWith(args[i])) {
				params.setApiName(args[++i]);
			}
			else if ("-output".startsWith(args[i])) {
				params.setOutputDirectory(args[++i]);
			}
			else if ("-prologue".startsWith(args[i])) {
				prologue = args[++i];
			}
			else if ("-input".startsWith(args[i])) {
				params.addInputFile(args[++i]);
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

		run();
	}

	private static void run() {
		Iterator iter = params.getInputFiles().iterator();
		String fileName = "";
		try {
			ADTFactory factory = new ADTFactory();
			ATermList all = factory.getEmpty();
			while (iter.hasNext()) {
				fileName = (String) iter.next();
				FileInputStream fis = new FileInputStream(fileName);
				all = all.concat((ATermList) factory.readFromFile(fis));
			}
			Entries entries = factory.EntriesFromTerm(all);
			generateAPI(new ADT(entries));
		}
		catch (FileNotFoundException e) {
			System.err.println("Error: File not found: " + fileName);
		}
		catch (IOException e) {
			System.err.println("Error: Could not read ADT from input: " + fileName);
		}
		catch (ParseError e) {
			System.err.println("Error: A parse error occurred in the ADT file:" + e);
		}
//		catch (RuntimeException e) {
//			System.err.println("Error: " + e.getMessage());
//		}
	}

	private static void generateAPI(ADT adt) {
		ADTFactory factory = new ADTFactory();
		APIGenerator apigen = new APIGenerator(adt, params, prologue, termCompatibility);
		apigen.run();
		if (jtom) {
			new TomSignatureGenerator(adt, new CTomSignatureImplementation(params.getPrefix(), jtype), params).run();
		}
		new CDictionaryGenerator(adt, params, factory, apigen.getAFunRegister()).run();
	}
}
