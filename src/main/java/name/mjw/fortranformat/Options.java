package name.mjw.fortranformat;

/**
 * The Class Options.
 */
class Options {

	/** The char to use when skipping positions during write. */
	private char positioningChar = ' ';

	/**
	 * Use this to set whether or not to append a return line to the end of the
	 * generated string during write.
	 */
	private boolean addReturn = false;

	/**
	 * Use this to choose whether to return decimals as Float or Double objects.
	 */
	private boolean returnFloats = false;

	/**
	 * Use this to choose whether to return zero if a blank is read for numbers.
	 */
	private boolean returnZeroForBlanks = false;

	/** Use this to choose whether character strings are left aligned. */
	private boolean leftAlignCharacters = false;

	/**
	 * Gets the positioning char. This is the character to use when skipping spaces
	 * during write.
	 *
	 * @return the positioning char
	 */
	public char getPositioningChar() {
		return positioningChar;
	}

	/**
	 * Sets the positioning char. This is the character to use when skipping spaces
	 * during write.
	 *
	 * @param positioningChar the new positioning character
	 */
	public void setPositioningChar(final char positioningChar) {
		this.positioningChar = positioningChar;
	}

	/**
	 * Specifies whether or not to add a new line at the end of a line during write.
	 *
	 * @param addReturn the new return line behavior
	 */
	public void setAddReturn(final boolean addReturn) {
		this.addReturn = addReturn;
	}

	/**
	 * Checks if returns are added at the end of lines during write.
	 *
	 * @return true, if is if new lines are added at the end of lines during write
	 */
	public boolean isAddReturn() {
		return addReturn;
	}

	/**
	 * Checks if floats are returned instead of doubles.
	 *
	 * @return true, if floats are returned
	 */
	public boolean isReturnFloats() {
		return returnFloats;
	}

	/**
	 * Set whether floats are returned instead of doubles.
	 *
	 * @param returnFloats the return floats
	 */
	public void setReturnFloats(final boolean returnFloats) {
		this.returnFloats = returnFloats;
	}

	/**
	 * Checks if zeros are returned for blanks.
	 *
	 * @return true, if zeros are returned
	 */
	public boolean isReturnZeroForBlanks() {
		return returnZeroForBlanks;
	}

	/**
	 * Set whether zeros are returned for blanks.
	 *
	 * @param returnZeroForBlanks the return zero for blanks
	 */
	public void setReturnZeroForBlanks(final boolean returnZeroForBlanks) {
		this.returnZeroForBlanks = returnZeroForBlanks;
	}

	/**
	 * Checks if characters are left aligned.
	 *
	 * @return true, if characters are left aligned
	 */
	public boolean isLeftAlignCharacters() {
		return leftAlignCharacters;
	}

	/**
	 * Set whether characters are left aligned.
	 *
	 * @param leftAlignCharacters the left align characters
	 */
	public void setLeftAlignCharacters(final boolean leftAlignCharacters) {
		this.leftAlignCharacters = leftAlignCharacters;
	}

}