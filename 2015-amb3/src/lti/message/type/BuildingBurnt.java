package lti.message.type;

import lti.message.Parameter;

public class BuildingBurnt extends Parameter {
	private static final int BUILDING = 0;

	public BuildingBurnt(int building) {
		super(Operation.BUILDING_BURNT, building);
	}

	public BuildingBurnt(byte[] attributes) {
		super(Operation.BUILDING_BURNT, attributes);
	}

	public int getBuilding() {
		return this.intAttributes[BUILDING];
	}
}
