package apigen.gen.java;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Type;
import apigen.gen.tom.TomSignatureGenerator;
import aterm.ParseError;
import aterm.pure.PureFactory;

public class Main {
	private static boolean visitable = false;
	private static boolean jtom = false;
	private static boolean folding = false;
	private static boolean verbose = false;

	private static String basedir = ".";
	private static String pkg = "";
	private static List imports = null;
	private static String apiName = "";

	private static void usage() {
		System.err.println("usage: JavaGen [options]");
		System.err.println("options:");
		System.err.println("\t-verbose                  [off]");
		System.err.println("\t-package <package>        [\"\"]");
		System.err.println("\t-name <api name>          [improvised]");
		System.err.println("\t-basedir <basedir>        [\".\"]");
		System.err.println("\t-import <package>         (can be repeated)");
		System.err.println("\t-input <in>               [-]");
		System.err.println("\t-folding                  [off]");
		System.err.println("\t-visitable                [off]");
		System.err.println("\t-jtom                     [off]");
		System.exit(1);
	}

	public static void main(String[] args) {
		String input = "-";
		imports = new LinkedList();
		InputStream inputStream;

		if (args.length == 0) {
			usage();
		}

		for (int i = 0; i < args.length; i++) {
			if ("-help".startsWith(args[i])) {
				usage();
			} else if ("-verbose".startsWith(args[i])) {
				verbose = true;
			} else if ("-package".startsWith(args[i])) {
				pkg = args[++i];
			} else if ("-basedir".startsWith(args[i])) {
				basedir = args[++i];
			} else if ("-import".startsWith(args[i])) {
				imports.add(args[++i]);
			} else if ("-input".startsWith(args[i])) {
				input = args[++i];
			} else if ("-folding".startsWith(args[i])) {
				folding = true;
			} else if ("-visitable".startsWith(args[i])) {
				visitable = true;
			} else if ("-jtom".startsWith(args[i])) {
				jtom = true;
			} else if ("-name".startsWith(args[i])) {
				apiName = args[++i];
			} else {
				usage();
			}
		}

		if (input.equals("-")) {
			inputStream = System.in;
		} else {
			try {
				inputStream = new FileInputStream(input);
				if (apiName.equals("")) {
					if (input.equals("-")) {
						System.err.println("Please give a name to the API");
						usage();
					} else {
						apiName = input.substring(0, input.lastIndexOf((int) '.'));
					}
				}

				run(inputStream);

			} catch (FileNotFoundException e) {
				System.out.println("Failed to open ADT file: " + input + " for reading");
				System.exit(1);
			}
		}

	}

	static public void run(InputStream input) {
		try {
			generateAPI(new ADT(new PureFactory().readFromFile(input)));
		} catch (ParseError e) {
			System.err.println("A parse error occurred in the ADT file:");
			System.err.println(e);
		} catch (IOException e) {
			System.err.println("Could not read ADT from input: " + input);
		}
	}

	static private void generateAPI(ADT adt) throws IOException {
		new FactoryGenerator(adt, basedir, apiName, pkg, imports, verbose, folding).run();
		new GenericConstructorGenerator(basedir, apiName, pkg, verbose, visitable).run();
		new MakeRulesGenerator(adt, basedir, apiName, verbose).run();

        if (visitable) {
        	new VisitorGenerator(adt,basedir,apiName,pkg,imports,verbose,folding).run();
        	new ForwardGenerator(adt,basedir,apiName,pkg,imports,verbose,folding).run();
        }
        
		if (jtom) {
			JavaTomSignatureImplementation sigImpl = new JavaTomSignatureImplementation(apiName);
			new TomSignatureGenerator(adt, sigImpl, basedir, apiName, verbose, folding).run();
        }

		generateTypeClasses(adt);
	}

	static private void generateTypeClasses(ADT api) throws IOException {
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			new TypeImplGenerator(type, basedir, pkg, apiName, imports, verbose).run();
			new TypeGenerator(type, basedir, pkg, imports, verbose, folding).run();
			generateAlternativeClasses(type);
		}
	}

	static private void generateAlternativeClasses(Type type) {
		Iterator alt_iter = type.alternativeIterator();
		while (alt_iter.hasNext()) {
			Alternative alt = (Alternative) alt_iter.next();
			new AlternativeGenerator(type, alt, basedir, pkg, imports, verbose, false).run();
			new AlternativeImplGenerator(type, alt, apiName, basedir, pkg, imports, verbose, false, visitable).run();
		}
	}
}
