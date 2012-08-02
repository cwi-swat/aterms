package apigen.adt;

import java.util.Iterator;

import apigen.adt.api.Factory;
import apigen.adt.api.types.Separator;
import apigen.adt.api.types.Separators;
import apigen.util.FirstAndLastSkippingIterator;

public class SeparatedListType extends ListType {
	private Separators separators;

	public SeparatedListType(String id, String moduleName, String elementType,
			Separators separators, Factory factory) {
		super(id, moduleName, elementType, factory);
		this.separators = separators;

	}

	private String buildSeparatorPattern() {
		StringBuffer pattern = new StringBuffer();
		Separators runner = separators;

		while (!runner.isEmpty()) {
			Separator sep = runner.getHead();

			pattern.append(sep.toString());
			runner = runner.getTail();

			if (!runner.isEmpty()) {
				pattern.append(',');
			}
		}
		return pattern.toString();
	}

	public int countSeparatorFields() {
		Iterator<Field> iter = separatorFieldIterator();
		int count = 0;

		while (iter.hasNext()) {
			iter.next();
			count++;
		}

		return count;
	}

	@Override
	public Alternative getManyAlternative() {
		return getAlternative(MANY_LIST_ALT_NAME);
	}

	public Field getManyField(String fieldId) {
		return getAltField(MANY_LIST_ALT_NAME, fieldId);
	}

	public Separators getSeparators() {
		return separators;
	}

	@Override
	protected Alternative makeManyListConstructor() {
		String head = buildHeadPattern();
		String seps = buildSeparatorPattern();
		String tail = buildTailPattern();
		String pattern = "[" + head + "," + seps + "," + tail + "]";

		return new Alternative(MANY_LIST_ALT_NAME, getFactory()
				.getPureFactory().parse(pattern));
	}

	public Iterator<Field> manyFieldIterator() {
		return altFieldIterator(MANY_LIST_ALT_NAME);
	}

	public Iterator<Field> separatorFieldIterator() {
		return new FirstAndLastSkippingIterator<Field>(
				altFieldIterator(MANY_LIST_ALT_NAME));
	}

}
