package apigen.gen.c;

import java.io.IOException;
import java.util.Iterator;

import apigen.adt.ADT;
import apigen.gen.GenerationParameters;
import apigen.gen.Generator;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermFactory;
import aterm.ATermList;

public class CDictionaryGenerator extends Generator {
	private ADT adt;
	private AFunRegister afunRegister;
	private ATermFactory factory;

	public CDictionaryGenerator(ADT adt, GenerationParameters params,
			ATermFactory factory, AFunRegister afunRegister) {
		super(params);
		this.adt = adt;
		this.factory = factory;
		this.afunRegister = afunRegister;
		setDirectory(params.getOutputDirectory());
		setExtension(".dict");
		setFileName(params.getApiName());
	}

	protected void generate() {
		try {
			buildDictionary(adt).writeToTextFile(getStream());
		} catch (IOException e) {
			System.out.println("Could not write to dictionary file.");
			System.exit(1);
		}
	}

	private ATerm buildDictionary(ADT adt) {
		ATermList afun_list = makeAFunList();
		ATermList term_list = factory.makeList();

		return factory.make("[afuns(<term>),terms(<term>)]", afun_list,
				term_list);
	}

	private ATermList makeAFunList() {
		ATermList afun_list = factory.makeList();
		Iterator<AFun> afuns = afunRegister.aFunIterator();

		while (afuns.hasNext()) {
			AFun afun = afuns.next();
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
