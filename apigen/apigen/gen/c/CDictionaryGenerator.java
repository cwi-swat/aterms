package apigen.gen.c;

import java.io.IOException;
import java.util.Iterator;
import apigen.adt.*;
import apigen.gen.Generator;
import apigen.gen.StringConversions;
import aterm.*;

public class CDictionaryGenerator extends Generator {
	private ADT adt;
	private String prefix;
	private String apiName;

    private AFunRegister afunRegister;

	private ATermFactory factory;

	public CDictionaryGenerator(
		ATermFactory factory,
		ADT adt,
		String directory,
		String apiName,
		String prefix,
		AFunRegister afunRegister,
		boolean verbose,
		boolean folding) {
		super(directory, apiName, ".dict", verbose, folding);
		this.adt = adt;
		this.apiName = apiName;
		this.prefix = prefix;
		this.factory = factory;

		this.afunRegister = afunRegister;
	}

	protected void generate() {
		info("generating " + apiName + ".dict");

		try {
			buildDictionary(adt).writeToTextFile(stream);
		} catch (IOException e) {
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
		return factory.make(
			"[<appl>,<term>]",
			prefix + "pattern" + StringConversions.makeIdentifier(type.getId()) + StringConversions.makeCapitalizedIdentifier(alt.getId()),
			alt.buildMatchPattern());
	}

	private ATermList makeAFunList() {
		ATermList afun_list = factory.makeList();
        Iterator afuns = afunRegister.aFunIterator();
     
       while(afuns.hasNext()) {
			AFun afun = (AFun) afuns.next();
			ATerm entry = makeDictEntry(afun);
			afun_list = afun_list.insert(entry);
       }
		return afun_list;
	}

	private ATerm makeDictEntry(AFun afun) {
		String name = afunRegister.lookup(afun);
		ATerm[] args = new ATerm[afun.getArity()];
		for (int j = 0; j < afun.getArity(); j++) {
			args[j] = factory.parse("x");
		}
		ATerm term = factory.makeAppl(afun, args);
		return factory.make("[" + prefix + name + ",<term>]", term);
	}
}
