package lti.message.type;

import lti.message.Parameter;

public class VictimDied extends Parameter {
	private static final int VICTIM = 0;
	
	public VictimDied(int victim){
		super(Operation.VICTIM_DIED, victim);
	}
	
	public VictimDied(byte[] attributes){
		super(Operation.VICTIM_DIED, attributes);
	}
	
	public int getVictim(){
		return this.byteAttributes[VICTIM];
	}
}
