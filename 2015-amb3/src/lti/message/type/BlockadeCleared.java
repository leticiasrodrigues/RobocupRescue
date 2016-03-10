package lti.message.type;

import lti.message.Parameter;

public class BlockadeCleared extends Parameter {
	private static final int BLOCKADE = 0;
	
	public BlockadeCleared(int blockade){
		super(Operation.BLOCKADE_CLEARED, blockade);
	}
	
	public BlockadeCleared(byte[] attributes){
		super(Operation.BLOCKADE_CLEARED, attributes);
	}
	
	public int getBlockade(){
		return this.intAttributes[BLOCKADE];
	}
}
