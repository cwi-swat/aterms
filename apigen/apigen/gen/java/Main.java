package apigen.gen.java;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.NormalListType;
import apigen.adt.SeparatedListType;
import apigen.adt.Type;
import apigen.adt.api.ADTFactory;
import apigen.adt.api.Entries;
import apigen.gen.GenerationParameters;
import apigen.gen.TypeConverter;
import apigen.gen.tom.TomSignatureGenerator;
import aterm.ATermList;
import aterm.ParseError;

public class Main {
	private static TypeConverter converter = new TypeConverter(new JavaTypeConversions());
	private static GenerationParameters params = new GenerationParameters();

	private static boolean jtom = false;
	private static boolean jtype = false;

	private static void usage() {
		System.err.println("usage: JavaGen [options]");
		System.err.println("options:");
		System.err.println("\t-i | --input <in>              <multiple allowed>");
		System.err.println("\t-f | --folding                 [off]");
		System.err.println("\t-o | --output <outputdir>      [\".\"]");
		System.err.println("\t-p | --package <package>       <optional>");
		System.err.println("\t-n | --name <api name>         <obligatory>");
		System.err.println("\t-i | --import <package>        (can be repeated)");
		System.err.println("\t-t | --visitable               [off]");
		System.err.println("\t-j | --jtom                    [off]");
		System.err.println("\t-J | --jtype                   [off]");
		System.err.println("\t-v | --verbose                 [off]");
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			usage();
		}

		params.setOutputDirectory(".");
		params.setVerbose(false);
		params.setFolding(false);
		params.setVisitable(false);

		for (int i = 0; i < args.length; i++) {
			if ("--input".startsWith(args[i]) || "-i".startsWith(args[i])) {
				params.addInputFile(args[++i]);
			}
			else if ("--folding".startsWith(args[i]) || "-f".startsWith(args[i])) {
				params.setFolding(true);
			}
			else if ("--output".startsWith(args[i]) || "-o".startsWith(args[i])) {
				params.setOutputDirectory(args[++i]);
			}
			else if ("--package".startsWith(args[i]) || "-p".startsWith(args[i])) {
				params.setPackageName(args[++i]);
			}
			else if ("--import".startsWith(args[i]) || "-m".startsWith(args[i])) {
				params.addImport(args[++i]);
			}
			else if ("--name".startsWith(args[i]) || "-n".startsWith(args[i])) {
				params.setApiName(args[++i]);
			}
			else if ("--visitable".startsWith(args[i]) || "-t".startsWith(args[i])) {
				params.setVisitable(true);
			}
			else if ("--verbose".startsWith(args[i]) || "-v".startsWith(args[i])) {
				params.setVerbose(true);
			}
			else if ("--jtom".startsWith(args[i]) || "-j".startsWith(args[i])) {
				jtom = true;
			}
			else if ("--jtype".startsWith(args[i]) || "-J".startsWith(args[i])) {
				jtype = true;
			}
			else if ("--help".startsWith(args[i]) || "-h".startsWith(args[i])) {
				usage();
				return;
			}
			else {
				System.err.println("Error: Not a valid option " + args[i]);
				usage();
				return;
			}
		}

		if (params.getApiName() == null) {
			System.err.println("Error: Please give a name for the API");
			usage();
			return;
		}
		run();

	}

	public static void run() {
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
		catch (RuntimeException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	static private void generateAPI(ADT adt) {
		new FactoryGenerator(adt, params).run();
		new GenericConstructorGenerator(adt, params).run();
		new MakeRulesGenerator(adt, params).run();

		if (params.isVisitable()) {
			new VisitorGenerator(adt, params).run();
			new ForwardGenerator(adt, params).run();
		}

		if (jtom) {
			JavaTomSignatureImplementation sigImpl = new JavaTomSignatureImplementation(params.getApiName(), jtype);
			new TomSignatureGenerator(adt, sigImpl, params).run();
		}

		generateTypeClasses(adt);
	}

	static private void generateTypeClasses(ADT api) {
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();

			if (type instanceof NormalListType) {
				new ListTypeGenerator(params, (NormalListType) type).run();
			}
			else if (type instanceof SeparatedListType) {
				new SeparatedListTypeGenerator(params, (SeparatedListType) type).run();

			}
			else if (!converter.isReserved(type.getId())) {
				new TypeGenerator(params, type).run();
				generateAlternativeClasses(type);
			}
		}
	}

	static private void generateAlternativeClasses(Type type) {
		Iterator alt_iter = type.alternativeIterator();
		while (alt_iter.hasNext()) {
			Alternative alt = (Alternative) alt_iter.next();
			new AlternativeGenerator(params, type, alt).run();
		}
	}
}
