package lti.message.type;

import lti.message.Parameter;

public class TaskDrop extends Parameter {
	private static int TASK = 0;

	public TaskDrop(int task) {
		super(Operation.TASK_DROP, task);
	}

	public TaskDrop(byte[] attributes) {
		super(Operation.TASK_DROP, attributes);
	}

	public int getTask() {
		return this.intAttributes[TASK];
	}
}
