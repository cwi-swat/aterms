package apigen.gen.java;

import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.NormalListType;
import apigen.adt.SeparatedListType;
import apigen.adt.Type;
import apigen.adt.api.ADTFactory;
import apigen.adt.api.Entries;
import apigen.gen.TypeConverter;
import apigen.gen.tom.TomSignatureGenerator;
import aterm.ParseError;

public class Main {
	private static boolean visitable = false;
	private static boolean jtom = false;
	private static boolean jtype = false;
	private static boolean folding = false;
	private static boolean verbose = false;

	private static TypeConverter converter = null;

	private static String basedir = ".";
	private static String pkg = "";
	private static List imports = null;
	private static String apiName = "";
	private static String prefix = "";

	/**
	 * Load the Java type conversions
	 */
	static {
		converter = new TypeConverter(new JavaTypeConversions());
	}

	private static void usage() {
		System.err.println("usage: JavaGen [options]");
		System.err.println("options:");
		System.err.println("\t-i || --input <in>              [-]");
		System.err.println("\t-f || --folding                 [off]");
		System.err.println("\t-o || --output <outputdir>        [\".\"]");
		System.err.println("\t-p || --package <package>        [\"\"]");
		System.err.println("\t-i || --import <package>         (can be repeated)");
		System.err.println("\t-n || --name <api name>          [improvised]");
		System.err.println("\t-t || --visitable                [off]");
		System.err.println("\t-j || --jtom                     [off]");
		System.err.println("\t-v || --verbose                  [off]");
		System.err.println("\t--jtype                    [off]");
	}

	public static void main(String[] args) {
		String input = "-";
		imports = new LinkedList();
		InputStream inputStream;

		if (args.length == 0) {
			usage();
		}

		for (int i = 0; i < args.length; i++) {
		  if ("--input".startsWith(args[i]) || "-i".startsWith(args[i])) {
			  input = args[++i];
		  }
		  else if ("--folding".startsWith(args[i]) || "-f".startsWith(args[i])) {
			  folding = true;
		  }
		  else if ("--output".startsWith(args[i]) || "-o".startsWith(args[i])) {
			  basedir = args[++i];
		  }
		  else if ("--package".startsWith(args[i]) || "-p".startsWith(args[i])) {
			  pkg = args[++i];
		  }
		  else if ("--import".startsWith(args[i]) || "-m".startsWith(args[i])) {
				imports.add(args[++i]);
			}
			else if ("--name".startsWith(args[i]) || "-n".startsWith(args[i])) {
				apiName = args[++i];
			}
			else if ("--visitable".startsWith(args[i]) || "-t".startsWith(args[i])) {
				visitable = true;
			}
			else if ("--jtom".startsWith(args[i]) || "-j".startsWith(args[i])) {
				jtom = true;
			}
			else if ("--verbose".startsWith(args[i]) || "-v".startsWith(args[i])) {
				verbose = true;
			}
			else if ("--help".startsWith(args[i])) {
				usage();
				return;
			}
			else if ("--jtype".startsWith(args[i])) {
				jtype = true;
			}
			else {
				System.err.println("Error: Not a valid option "+args[i]);
				usage();
				return;
			}
		}

		if (input.equals("-")) {
			inputStream = System.in;
		}
		else {
			try {
				inputStream = new FileInputStream(input);
				if (apiName.equals("")) {
					if (input.equals("-")) {
						System.err.println("Error: Please give a name to the API");
						usage();
						return;
					}
					else {
						File apiFile = new File(input);
						String apiFileName = apiFile.getName();
						apiName = apiFileName.substring(0, apiFileName.lastIndexOf((int) '.'));
					}
				}

				run(inputStream);

			}
			catch (FileNotFoundException e) {
				System.out.println("Error: Failed to open ADT file: " + input + " for reading");
			}
		}

	}

	static public void run(InputStream input) {
		try {
			ADTFactory factory = new ADTFactory();
			Entries entries = factory.EntriesFromFile(input);
			generateAPI(new ADT(entries));
		}
		catch (ParseError e) {
			System.err.println("Error: A parse error occurred in the ADT file:"+e);
		}
		catch (IOException e) {
			System.err.println("Error: Could not read ADT from input: " + input);
		}
		catch (RuntimeException e) {
			System.err.println("Error: "+e.getMessage());
		}
	}

	static private void generateAPI(ADT adt) throws IOException {
		new FactoryGenerator(adt, basedir, apiName, pkg, imports, verbose).run();
		new GenericConstructorGenerator(adt, basedir, apiName, pkg, verbose, visitable).run();
		new MakeRulesGenerator(adt, basedir, apiName, verbose).run();

		if (visitable) {
			new VisitorGenerator(adt, basedir, pkg, imports, verbose).run();
			new ForwardGenerator(adt, basedir, apiName, pkg, imports, verbose).run();
		}

		if (jtom) {
			JavaTomSignatureImplementation sigImpl = new JavaTomSignatureImplementation(apiName, jtype);
			new TomSignatureGenerator(adt, sigImpl, basedir, apiName, prefix, verbose, folding).run();
		}

		generateTypeClasses(adt);
	}

	static private void generateTypeClasses(ADT api) throws IOException {
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();

			if (type instanceof NormalListType) {
				new ListTypeImplGenerator((NormalListType) type, basedir, pkg, apiName, imports, verbose).run();
				new ListTypeGenerator(type, basedir, pkg, apiName, imports, verbose).run();
			}
			else if (type instanceof SeparatedListType) {
				new SeparatedListTypeImplGenerator((SeparatedListType) type, basedir, pkg, apiName, imports, verbose)
					.run();
				new ListTypeGenerator(type, basedir, pkg, apiName, imports, verbose).run();

			}
			else if (!converter.isReserved(type.getId())) {
				new TypeImplGenerator(type, basedir, pkg, apiName, imports, verbose).run();
				new TypeGenerator(type, basedir, pkg, apiName, imports, verbose).run();
				generateAlternativeClasses(type);
			}
		}
	}

	static private void generateAlternativeClasses(Type type) {
		Iterator alt_iter = type.alternativeIterator();
		while (alt_iter.hasNext()) {
			Alternative alt = (Alternative) alt_iter.next();

			new AlternativeGenerator(type, alt, basedir, pkg, apiName, imports, verbose).run();
			new AlternativeImplGenerator(type, alt, apiName, basedir, pkg, imports, verbose, visitable).run();
		}
	}
}
