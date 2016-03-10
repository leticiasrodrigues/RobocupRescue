package lti.utils;

import java.util.Comparator;

import rescuecore2.worldmodel.EntityID;

public class EntityIDComparator implements Comparator<EntityID> {

	public EntityIDComparator() {}

	@Override
	public int compare(EntityID a, EntityID b) {

		if (a.getValue() < b.getValue()) {
			return -1;
		}
		if (a.getValue() > b.getValue()) {
			return 1;
		}
		return 0;
	}
}