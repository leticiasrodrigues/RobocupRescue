package lti.message.type;

import rescuecore2.standard.entities.StandardEntityURN;
import lti.message.Parameter;

public class Help extends Parameter {

	private static final int IS_DAMAGE = 0;

	private static final int IS_BURIEDNESS = 1;

	private static final int URN = 2;
	
	/**
	 * Constructor
	 * 
	 * @param victim
	 *            Civilian identification
	 * @param is_buriedness
	 *            Civilian buriedness
	 * @param urn
	 * 			  Type = Civilian
	 */
	public Help(int is_damage, int is_buriedness) {
		super(Operation.HELP_CIVILIAN, is_damage, is_buriedness, 3);
	}

	/**
	 * Constructor
	 * 
	 * @param attributes
	 *            is_damage, is_buriedness, urn
	 */
	public Help(byte[] attributes) {
		super(Operation.HELP_CIVILIAN, attributes);
	}

	/**
	 * Get civilian damage
	 * 
	 * @return Civilian damage
	 */
	public int isDamage() {
		return this.intAttributes[IS_DAMAGE];
	}

	/**
	 * Get civilian buriedness
	 * 
	 * @return Civilian buriedness
	 */
	public int isBuriedness() {
		return this.intAttributes[IS_BURIEDNESS];
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
		case 3:
			return StandardEntityURN.CIVILIAN;
		default:
			return StandardEntityURN.CIVILIAN;
		}
	}

}
