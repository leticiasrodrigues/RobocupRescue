package lti.message.type;

import lti.message.Parameter;

public class VictimRescued extends Parameter {
	private static final int VICTIM = 0;

	public VictimRescued(int victim) {
		super(Operation.VICTIM_RESCUED, victim);
	}

	public VictimRescued(byte[] attributes) {
		super(Operation.VICTIM_RESCUED, attributes);
	}

	public int getVictim() {
		return this.intAttributes[VICTIM];
	}
}
