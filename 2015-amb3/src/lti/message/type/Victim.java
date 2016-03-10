package lti.message.type;

import rescuecore2.standard.entities.StandardEntityURN;
import lti.message.Parameter;

public class Victim extends Parameter {

	private static final int VICTIM = 0;

	private static final int POSITION = 1;

	private static final int HP = 2;

	private static final int DAMAGE = 3;

	private static final int BURIEDNESS = 4;

	private static final int URN = 5;

	/**
	 * Constructor
	 * 
	 * @param victim
	 *            Civilian identification
	 * @param position
	 *            Civilian position
	 * @param damage
	 *            Civilian damage
	 * @param buriedness
	 *            Civilian buriedness
	 */
	public Victim(int victim, int position, int hp, int damage, int buriedness, int urn) {
		super(Operation.VICTIM, victim, position, hp,damage, buriedness, urn);
	}

	/**
	 * Constructor
	 * 
	 * @param attributes
	 *            Civilian identification, position, damage and buriedness
	 *            attributes
	 */
	public Victim(byte[] attributes) {
		super(Operation.VICTIM, attributes);
	}

	/**
	 * Get civilian identification
	 * 
	 * @return Civilian identification
	 */
	public int getVictim() {
		return this.intAttributes[VICTIM];
	}

	/**
	 * Get civilian position
	 * 
	 * @return Civilian position
	 */
	public int getPosition() {
		return this.intAttributes[POSITION];
	}

	/**
	 * Get civilian HP
	 * 
	 * @return Civilian HP
	 */
	public int getHP() {
		return this.intAttributes[HP];
	}

	/**
	 * Get civilian damage
	 * 
	 * @return Civilian damage
	 */
	public int getDamage() {
		return this.intAttributes[DAMAGE];
	}

	/**
	 * Get civilian buriedness
	 * 
	 * @return Civilian buriedness
	 */
	public int getBuriedness() {
		return this.intAttributes[BURIEDNESS];
	}

	/**
	 * @return the urn
	 */
	public StandardEntityURN getURN() {
		switch (this.intAttributes[URN]) {
		case 0:
			return StandardEntityURN.AMBULANCE_TEAM;
		case 1:
			return StandardEntityURN.FIRE_BRIGADE;
		case 2:
			return StandardEntityURN.POLICE_FORCE;
		default:
			return StandardEntityURN.CIVILIAN;
		}
	}
}