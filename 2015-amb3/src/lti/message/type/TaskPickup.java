package lti.message.type;

import lti.message.Parameter;

public class TaskPickup extends Parameter {
	private static final int TASK = 0;

	/**
	 * Constructor
	 * 
	 * @param taskID
	 *            The ID of the task the agent has picked up
	 */
	public TaskPickup(int taskID) {
		super(Operation.TASK_PICKUP, taskID);
	}

	/**
	 * Constructor
	 * 
	 * @param attributes
	 *            The byte vector containing the atributes
	 */
	public TaskPickup(byte[] attributes) {
		super(Operation.TASK_PICKUP, attributes);
	}

	/**
	 * Get the ID of the task the agent has picked up
	 * 
	 * @return The ID of the task the agent has picked up
	 */
	public int getTask() {
		return this.intAttributes[TASK];
	}
}
