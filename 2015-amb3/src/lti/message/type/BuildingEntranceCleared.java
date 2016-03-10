package lti.message.type;

import lti.message.Parameter;

public class BuildingEntranceCleared extends Parameter {
	private static final int BUILDINGID = 0;
	
	public BuildingEntranceCleared(int buildingid){
		super(Operation.BUILDING_ENTRANCE_CLEARED, buildingid);
	}
	
	public BuildingEntranceCleared(byte[] attributes){
		super(Operation.BUILDING_ENTRANCE_CLEARED, attributes);
	}
	
	public int getBuildingID(){
		return this.intAttributes[BUILDINGID];
	}
}
