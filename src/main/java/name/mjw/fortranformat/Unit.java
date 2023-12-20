package name.mjw.fortranformat;

/**
 * The Class Unit. Holds a single Edit Descriptor.
 */
class Unit {

	/** The Edit Descriptor type. */
	final EditDescriptor type;

	/** The length 'w'. */
	final int length;

	/** The decimal length 'd'. */
	int decimalLength;

	/** The exponent length 'e'. */
	int exponentLength;

	/**
	 * Instantiates a new unit.
	 *
	 * @param type   the type
	 * @param length the length 'w'
	 */
	public Unit(final EditDescriptor type, final int length) {
		this.type = type;
		this.length = length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return type.getTag() + length + (decimalLength > 0 ? "." + decimalLength : "")
				+ (exponentLength > 0 ? "E" + exponentLength : "") + " ";
	}

	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Gets the decimal length.
	 *
	 * @return the decimal length
	 */
	public int getDecimalLength() {
		return decimalLength;
	}

	/**
	 * Gets the exponent length.
	 *
	 * @return the exponent length
	 */
	public int getExponentLength() {
		return exponentLength;
	}

}