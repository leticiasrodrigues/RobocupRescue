package lti.message.type;

import lti.message.Parameter;

public class Blockade extends Parameter {

	private static final int BLOCKADE = 0;

	private static final int ROAD = 1;

	private static final int X = 2;

	private static final int Y = 3;

	private static final int COST = 4;

	/**
	 * Constructor
	 * 
	 * @param blockade
	 *            Blockade identification
	 * @param road
	 *            Road identification
	 * @param x
	 *            X coordinate
	 * @param y
	 *            y coordinate
	 * @param cost
	 *            Repair cost
	 */
	public Blockade(int blockade, int road, int x, int y, int cost) {
		super(Operation.BLOCKADE, blockade, road, x, y, cost);
	}

	/**
	 * Constructor
	 * 
	 * @param blockade
	 *            Blockade identification
	 * @param road
	 *            Road identification
	 * @param x
	 *            X coordinate
	 * @param y
	 *            y coordinate
	 * @param cost
	 *            Repair cost
	 */
	public Blockade(byte[] attributes) {
		super(Operation.BLOCKADE, attributes);
	}

	/**
	 * Get blockade identification
	 * 
	 * @return Blockade identification
	 */
	public int getBlockade() {
		return this.intAttributes[BLOCKADE];
	}

	/**
	 * Get road identification
	 * 
	 * @return Road identification
	 */
	public int getRoad() {
		return this.intAttributes[ROAD];
	}

	/**
	 * Get X coordinate
	 * 
	 * @return X coordinate
	 */
	public int getX() {
		return this.intAttributes[X];
	}

	/**
	 * Get Y coordinate
	 * 
	 * @return Y coordinate
	 */
	public int getY() {
		return this.intAttributes[Y];
	}

	/**
	 * Get repair cost
	 * 
	 * @return Repair cost
	 */
	public int getCost() {
		return this.intAttributes[COST];
	}
}