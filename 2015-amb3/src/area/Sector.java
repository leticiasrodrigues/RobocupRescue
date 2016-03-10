package area;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import rescuecore2.standard.entities.Area;
import rescuecore2.worldmodel.EntityID;

/*The Sector object, assigned to each Police Force*/
public class Sector implements Comparable<Sector> {
	int index;

	Set<EntityID> buildings;

	Map<EntityID, Set<EntityID>> locations;
	
	Rectangle rect;

	public Sector(int x, int y, int w, int h, int i) {
		rect = new Rectangle(x, y, w, h);
		index = i;
		locations = new HashMap<EntityID, Set<EntityID>>();
	}

	public Sector(Sector other) {
		rect = new Rectangle(other.rect);
		index = other.index;
		locations = other.locations;
	}

	/**
	 * Get the bounds of a sector.
	 * 
	 * @return
	 */
	public Rectangle2D getBounds2D() {
		return rect.getBounds2D();
	}
	
	public Rectangle getBounds() {
		return rect.getBounds();
	}

	public void setLocations(Map<EntityID, Set<EntityID>> other) {
		locations = other;
	}

	public Map<EntityID, Set<EntityID>> getLocations() {
		return locations;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int i) {
		index = i;
	}

	public void addVertex(EntityID v) {
		locations.put(v, null);
	}

	public Set<EntityID> getNeighbours(EntityID v) {
		return locations.get(v);
	}

	public Set<EntityID> getBuildings() {
		return buildings;
	}

	@Override
	public String toString() {
		return (new Integer(index)).toString();
	}

	public boolean containsCenter(Area entity) {
		Rectangle2D ent2D = entity.getShape().getBounds2D();
		return rect.contains(ent2D.getCenterX(), ent2D.getCenterY());
	}

	@Override
	public int compareTo(Sector o) {
		int i = o.getIndex();

		if (index < i) {
			return -1;
		}
		if (index > i) {
			return 1;
		}
		return 0;
	}
}
