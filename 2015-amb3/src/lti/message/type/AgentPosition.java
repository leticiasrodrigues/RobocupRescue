package lti.message.type;

import rescuecore2.standard.entities.StandardEntityURN;
import lti.message.Parameter;

public class AgentPosition extends Parameter{

	private static final int ID_AGENT = 0;

	private static final int POSITION = 1;
	
	private static final int URN = 2;


	/**
	 * Constructor
	 * 
	 * @param agente
	 *            Agent type
	 * @param position
	 *            Agent position
	 */
	public AgentPosition(int agent, int position, int urn) {
		super(Operation.AGENT_POSITION, agent, position, urn);
	}

	/**
	 * Constructor
	 * 
	 * @param attributes
	 *            Agent identification and position
	 *            attributes
	 */
	public AgentPosition(byte[] attributes) {
		super(Operation.AGENT_POSITION, attributes);
	}

	/**
	 * Get agent identification
	 * 
	 * @return Agent identification
	 */
	public int getAgent() {
		return this.intAttributes[ID_AGENT];
	}

	/**
	 * Get agent position
	 * 
	 * @return Agent position
	 */
	public int getPosition() {
		return this.intAttributes[POSITION];
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
