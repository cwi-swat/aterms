package apigen.adt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Location implements Cloneable {
	String altId;
	List<Step> path;

	public Location(String altId) {
		this.altId = altId;

		path = new LinkedList<Step>();
	}

	public void addStep(Step step) {
		path.add(step);
	}

	@Override
	public Object clone() {
		Location copy = new Location(altId);
		copy.path = (List<Step>) ((LinkedList<Step>) path).clone();

		return copy;
	}

	public String getAltId() {
		return altId;
	}

	public void makeTail() {
		Step step;

		step = path.get(path.size() - 1);
		step.makeTail();
	}

	public Iterator<Step> stepIterator() {
		return path.iterator();
	}

	@Override
	public String toString() {
		return "loc[" + altId + ", " + path + "]";
	}
}
