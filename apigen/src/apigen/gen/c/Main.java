package apigen.gen.c;

import java.io.*;
import java.util.*;

import aterm.*;
import aterm.pure.PureFactory;

import apigen.adt.*;
import apigen.gen.Generator;

public class Main {
	
	
	
	private ATermFactory factory;

	private InputStream input;

	//{{{ private static void usage()

	private static void usage() {
		System.err.println("usage: CGen [options]");
		System.err.println("options:");
		System.err.println("\t-prefix <prefix>          [\"\"]");
		System.err.println("\t-input <in>               [-]");
		System.err.println("\t-output <out>");
		System.err.println("\t-prologue <prologue>");
		System.exit(1);
	}

	//}}}

	//{{{ public final static void main(String[] args)

	public final static void main(String[] args) throws IOException {
		String prefix = "";
		String input = "-";
		String output = null;
		String prologue = null;
		InputStream inputStream;
		boolean make_term_compatibility = false;

		if (args.length == 0) {
			usage();
		}

		for (int i = 0; i < args.length; i++) {
			if ("-help".startsWith(args[i])) {
				usage();
			}
			else if ("-verbose".startsWith(args[i])) {
				verbose = true;
			}
			else if ("-prefix".startsWith(args[i])) {
				prefix = args[++i];
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
				make_term_compatibility = true;
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
				int extIndex = input.lastIndexOf((int) '.');
				output = input.substring(0, extIndex);
			}
		}

		Main gen =
			new Main(inputStream, output, prefix, prologue, make_term_compatibility);
	}

	//}}}

	//{{{ public CGen(InputStream input, String output, String prefix, String prologue)

	public Main(InputStream input,String output,String prefix,String prologue,
              boolean make_term_compatibility)
		throws IOException {
		this.input = input;
		this.output = output;
		this.capOutput = stringConverter.capitalize(output);
		this.prefix = prefix;
		this.prologue = prologue;
		this.make_term_compatibility = make_term_compatibility;
		afuns_by_name = new HashMap();
		afuns_by_afun = new HashMap();

		factory = new PureFactory();

		ATerm adt = factory.readFromFile(input);

		ADT api = new ADT(adt);

		String header_name = output + ".h";
		header = new PrintStream(new FileOutputStream(header_name));

		String source_name = output + ".c";
		source = new PrintStream(new FileOutputStream(source_name));

        new APIGenerator(apiName,prefix,verbose,folding).run();
        
       
   

    info("generating " + output + ".dict");
		ATerm dict = buildDictionary(api);
		OutputStream dict_out = new FileOutputStream(output + ".dict");
		dict.writeToTextFile(dict_out);
	}

	//}}}

	//{{{ private ATerm buildDictionary(API api)

	private ATerm buildDictionary(ADT api) {
		ATermList afun_list = factory.makeList();
		for (int i = nextAFun - 1; i >= 0; i--) {
			String name = prefix + "afun" + i;
			AFun afun = (AFun) afuns_by_name.get(name);
			ATerm[] args = new ATerm[afun.getArity()];
			for (int j = 0; j < afun.getArity(); j++) {
				args[j] = factory.parse("x");
			}
			ATerm term = factory.makeAppl(afun, args);
			afun_list = afun_list.insert(factory.make("[" + name + ",<term>]", term));
		}

		ATermList term_list = factory.makeList();

		Iterator types = api.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			String id = stringConverter.makeIdentifier(type.getId());
			Iterator alts = type.alternativeIterator();
			while (alts.hasNext()) {
				Alternative alt = (Alternative) alts.next();
				ATerm entry =
					factory.make(
						"[<appl>,<term>]",
						prefix + "pattern" + id + stringConverter.makeCapitalizedIdentifier(alt.getId()),
						alt.buildMatchPattern());
				term_list = factory.makeList(entry, term_list);
			}
		}

		return factory.make("[afuns(<term>),terms(<term>)]", afun_list, term_list);
	}

	//}}}
	
}
