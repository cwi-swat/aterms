package apigen.gen.c;

import java.io.IOException;
import java.util.Iterator;
import apigen.adt.*;
import apigen.gen.GenerationParameters;
import apigen.gen.Generator;
import apigen.gen.StringConversions;
import aterm.*;

public class CDictionaryGenerator extends Generator {
	private ADT adt;
	private AFunRegister afunRegister;
	private ATermFactory factory;

	public CDictionaryGenerator(
		ADT adt,
		GenerationParameters params,
		ATermFactory factory,
		String directory,
		String fileName,
		AFunRegister afunRegister) {
		super(params);
		this.adt = adt;
		this.factory = factory;
		this.afunRegister = afunRegister;
		setDirectory(directory);
		setExtension(".dict");
		setFileName(fileName);
	}

	protected void generate() {
		String apiName = getGenerationParameters().getApiName();
		info("generating " + apiName + ".dict");

		try {
			buildDictionary(adt).writeToTextFile(getStream());
		}
		catch (IOException e) {
			System.out.println("Could not write to dictionary file.");
			System.exit(1);
		}
	}

	private ATerm buildDictionary(ADT adt) {
		ATermList afun_list = makeAFunList();
		ATermList term_list = makeTermList(adt);

		return factory.make("[afuns(<term>),terms(<term>)]", afun_list, term_list);
	}

	private ATermList makeTermList(ADT adt) {
		ATermList term_list = factory.makeList();

		Iterator types = adt.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			Iterator alts = type.alternativeIterator();
			while (alts.hasNext()) {
				Alternative alt = (Alternative) alts.next();
				ATerm entry = makeDictEntry(type, alt);
				term_list = factory.makeList(entry, term_list);
			}
		}
		return term_list;
	}

	private ATerm makeDictEntry(Type type, Alternative alt) {
		String prefix = getGenerationParameters().getPrefix();
		return factory.make(
			"[<appl>,<term>]",
			prefix
				+ "pattern"
				+ StringConversions.makeIdentifier(type.getId())
				+ StringConversions.makeCapitalizedIdentifier(alt.getId()),
			alt.buildMatchPattern());
	}

	private ATermList makeAFunList() {
		ATermList afun_list = factory.makeList();
		Iterator afuns = afunRegister.aFunIterator();

		while (afuns.hasNext()) {
			AFun afun = (AFun) afuns.next();
			ATerm entry = makeDictEntry(afun);
			afun_list = afun_list.insert(entry);
		}
		return afun_list;
	}

	private ATerm makeDictEntry(AFun afun) {
		String prefix = getGenerationParameters().getPrefix();
		String name = afunRegister.lookup(afun);
		ATerm[] args = new ATerm[afun.getArity()];
		for (int j = 0; j < afun.getArity(); j++) {
			args[j] = factory.parse("x");
		}
		ATerm term = factory.makeAppl(afun, args);
		return factory.make("[" + prefix + name + ",<term>]", term);
	}
}
