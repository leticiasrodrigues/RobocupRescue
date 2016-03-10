package lti.message.type;

import lti.message.Parameter;

public class FireExtinguished extends Parameter {
	private static final int BUILDING = 0;

	public FireExtinguished(int building) {
		super(Operation.FIRE_EXTINGUISHED, building);
	}

	public FireExtinguished(byte[] attributes) {
		super(Operation.FIRE_EXTINGUISHED, attributes);
	}

	public int getBuilding() {
		return this.intAttributes[BUILDING];
	}
}
