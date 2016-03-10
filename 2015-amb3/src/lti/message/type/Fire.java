package lti.message.type;

import lti.message.Parameter;

public class Fire extends Parameter {

	private static final int BUILDING = 0;

	private static final int GROUND_AREA = 1;

	private static final int FLOORS = 2;

	private static final int INTENSITY = 3;

	/**
	 * Constructor
	 * 
	 * @param building
	 *            Building on fire
	 * @param intensity
	 *            Fire intensity
	 */
	public Fire(int building, int groundArea, int floors, int intensity) {
		super(Operation.FIRE, building, groundArea, floors, intensity);
	}

	/**
	 * Constructor
	 * 
	 * @param attributes
	 *            Fire position and intensity
	 */
	public Fire(byte[] attributes) {
		super(Operation.FIRE, attributes);
	}

	/**
	 * Get building on fire
	 * 
	 * @return Building on fire
	 */
	public int getBuilding() {
		return this.intAttributes[BUILDING];
	}

	/**
	 * @return the area
	 */
	public int getGroundArea() {
		return this.intAttributes[GROUND_AREA];
	}

	public int getFloors() {
		return this.intAttributes[FLOORS];
	}

	/**
	 * Get fire intensity
	 * 
	 * @return Fire intensity
	 */
	public int getIntensity() {
		return this.intAttributes[INTENSITY];
	}
}