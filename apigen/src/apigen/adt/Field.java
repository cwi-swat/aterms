package apigen.adt;

import java.util.*;

public class Field {
	String id;
	String type;

	List<Location> locations;

	public Field(String id, String type) {
		this.id = id;
		this.type = type;

		locations = new Vector<Location>();
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public void addLocation(Location loc) {
		locations.add(loc);
	}

	public boolean hasAltId(String altId) {
		return getLocation(altId) != null;
	}

	public Location getLocation(String altId) {
		Iterator<Location> locs = locations.iterator();
		while (locs.hasNext()) {
			Location loc = locs.next();
			if (loc.getAltId().equals(altId)) {
				return loc;
			}
		}

		return null;
	}

	public Iterator<Location> locationIterator() {
		return locations.iterator();
	}

	public String toString() {
		return "field[" + id + ", " + type + "," + locations + "]";
	}
}
