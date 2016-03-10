package lti.utils;

import rescuecore2.worldmodel.EntityID;

public class BuildingPoint extends Point2D{
	private final EntityID building;
	
	/**
	 * Creates a BuildingPoint using the coordinates and a building
	 * @param x the x-coordinate of the point
	 * @param y the y-coordinate of the point
	 * @param building the building that contains the point
	 */
	public BuildingPoint(double x, double y, EntityID building) {
		super(x, y);
		
		this.building = building;
	}
	
	/**
	 * Returns the building
	 * @return the building of the point
	 */
	public EntityID building(){
		return this.building;
	}
}
