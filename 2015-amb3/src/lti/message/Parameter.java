package lti.message;

public class Parameter {

	/**
	 * Message operation
	 */
	public enum Operation {
		// size == attributes.length * 4 + 1
		NONE(1), FIRE(17), VICTIM(25), BLOCKADE(21),
		TASK_PICKUP(5), TASK_DROP(5), BLOCKADE_CLEARED(5),
		VICTIM_DIED(5), VICTIM_RESCUED(5), FIRE_EXTINGUISHED(5),
		BUILDING_BURNT(5), HELP_CIVILIAN(13), BUILDING_ENTRANCE_CLEARED(5), AGENT_POSITION(13);

		// Operation array size
		private int size;

		/**
		 * Constructor
		 * 
		 * @param size
		 *            Operation array size
		 */
		private Operation(int size) {
			this.size = size;
		}

		/**
		 * Get operation byte
		 * 
		 * @return Operation byte
		 */
		public byte getByte() {
			return new Integer(this.ordinal()).byteValue();
		}

		/**
		 * Get operation array size
		 * 
		 * @return Operation array size
		 */
		public int getSize() {
			return size;
		}

		/**
		 * Get an operation represented by the operation number
		 * 
		 * @param operation
		 *            Operation number
		 * 
		 * @return Operation
		 */
		public static Operation ofOperation(byte numOperation) {
			Operation operation = NONE;

			if (numOperation == FIRE.getByte()) {
				operation = FIRE;
			} else if (numOperation == VICTIM.getByte()) {
				operation = VICTIM;
			} else if (numOperation == BLOCKADE.getByte()) {
				operation = BLOCKADE;
			} else if (numOperation == TASK_PICKUP.getByte()) {
				operation = TASK_PICKUP;
			} else if (numOperation == TASK_DROP.getByte()) {
				operation = TASK_DROP;
			} else if (numOperation == BLOCKADE_CLEARED.getByte()) {
				operation = BLOCKADE_CLEARED;
			} else if (numOperation == VICTIM_DIED.getByte()) {
				operation = VICTIM_DIED;
			} else if (numOperation == VICTIM_RESCUED.getByte()) {
				operation = VICTIM_RESCUED;
			} else if (numOperation == FIRE_EXTINGUISHED.getByte()) {
				operation = FIRE_EXTINGUISHED;
			} else if (numOperation == BUILDING_BURNT.getByte()) {
				operation = BUILDING_BURNT;
			} else if (numOperation == HELP_CIVILIAN.getByte()) {
				operation = HELP_CIVILIAN;
			} else if (numOperation == BUILDING_ENTRANCE_CLEARED.getByte()) {
				operation = BUILDING_ENTRANCE_CLEARED;
			}else if (numOperation == AGENT_POSITION.getByte()) {
				operation = AGENT_POSITION;
			}

			return operation;
		}
	};

	// Message operation
	protected Operation operation;

	// Integer attributes
	protected int[] intAttributes;

	// Byte attributes
	protected byte[] byteAttributes;

	/**
	 * Constructor
	 * 
	 * @param operation
	 *            Operation
	 */
	public Parameter(Operation operation) {
		this.operation = operation;
		this.intAttributes = new int[1];
		this.byteAttributes = toByte();
		this.intAttributes = toInt();
	}

	/**
	 * Constructor
	 * 
	 * @param operation
	 *            Operation
	 * @param attributes
	 *            Attributes
	 */
	public Parameter(Operation operation, int... attributes) {
		if (operation.getSize() == ((attributes.length * 4) + 1)) {
			this.operation = operation;
			this.intAttributes = attributes;
			this.byteAttributes = toByte();
		} else {
			this.operation = Operation.NONE;
			this.intAttributes = new int[1];
			this.byteAttributes = toByte();
			this.intAttributes = toInt();
		}
	}

	/**
	 * Constructor
	 * 
	 * @param operation
	 *            Operation
	 * @param attributes
	 *            Attributes
	 */
	public Parameter(Operation operation, byte[] attributes) {
		if (operation.getSize() == (attributes.length + 1)) {
			this.operation = operation;
			this.byteAttributes = attributes;
			this.intAttributes = toInt();
		} else {
			this.operation = Operation.NONE;
			this.intAttributes = new int[1];
			this.byteAttributes = toByte();
			this.intAttributes = toInt();
		}
	}

	/**
	 * Get the operation
	 * 
	 * @return Operation
	 */
	public Operation getOperation() {
		return this.operation;
	}

	/**
	 * Get parameter in bytes
	 * 
	 * @return Parameter in bytes
	 */
	public byte[] getByteAttributes() {
		return this.byteAttributes;
	}

	/**
	 * Get parameter in integers
	 * 
	 * @return Parameter in integers
	 */
	public int[] getIntAttributes() {
		return this.intAttributes;
	}

	/**
	 * Convert an integer into four bytes
	 * 
	 * @return Returns encrypted bytes
	 */
	private byte[] toByte() {
		byte[] x = new byte[4 * this.intAttributes.length];
		for (int j = 0; j < this.intAttributes.length; j++) {
			for (int i = 0; i < 4; i++) {
				x[(4 * j) + i] = (byte) ((this.intAttributes[j] << (8 * i)) >> 24);
			}
		}

		return x;
	}

	/**
	 * Convert an array of four bytes into an integer
	 * 
	 * @param encrypted
	 *            Array of 4 bytes
	 * @return Returns a integer
	 */
	private int[] toInt() {
		int[] x = new int[this.byteAttributes.length / 4];
		int tmp;
		for (int j = 0; j < x.length; j++) {
			for (int i = 0; i < 4; i++) {
				tmp = this.byteAttributes[4 * j + i];
				if (tmp < 0)
					tmp = 256 + tmp;
				x[j] |= tmp << (8 * (3 - i));
			}
		}

		return x;
	}

	/**
	 * Get string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		String str = "";

		str += this.operation.name() + " ";
		for (int attributes : this.intAttributes) {
			str += attributes + " ";
		}

		return str;
	}
}