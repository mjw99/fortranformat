//
//  FortranFormat Version 1.1, written by Kevin J. Theisen
//
//  Copyright (c) 2009 iChemLabs, LLC.  All rights reserved.
//
//  $Revision: 793 $
//  $Author: kevin $
//  $LastChangedDate: 2009-11-15 20:03:16 -0400 (Sun, 15 Nov 2009) $
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are met:
//
//  1. Redistributions of source code must retain the above copyright notice,
//	     this list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice,
//	     this list of conditions and the following disclaimer in the
//	     documentation and/or other materials provided with the distribution.
//  3. Neither the name of the iChemLabs nor the names of its contributors
//	     may be used to endorse or promote products derived from this software
//	     without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
//  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
//  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
//  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package name.mjw.fortranformat;

/**
 * Configuration options that control the behaviour of {@link FortranFormat}
 * during parsing and formatting operations.
 */
class Options {

	/** The char to use when skipping positions during write. */
	private char positioningChar = ' ';

	/** Whether to append a newline at the end of the generated string during write. */
	private boolean addReturn = false;

	/** Whether to return decimal values as {@code float} instead of {@code double}. */
	private boolean returnFloats = false;

	/** Whether to return zero instead of {@code null} when a blank is read for a numeric field. */
	private boolean returnZeroForBlanks = false;

	/** Whether character ({@code A}) fields are left-aligned rather than right-aligned. */
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
	 * Checks if a newline is appended at the end of a formatted string during write.
	 *
	 * @return true, if a newline is added at the end of the formatted string
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
	 * @param returnFloats {@code true} to return {@code float} values; {@code false} to return {@code double}
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
	 * @param returnZeroForBlanks {@code true} to return zero instead of {@code null} for blank numeric fields
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
	 * @param leftAlignCharacters {@code true} to left-align character ({@code A}) fields; {@code false} to right-align
	 */
	public void setLeftAlignCharacters(final boolean leftAlignCharacters) {
		this.leftAlignCharacters = leftAlignCharacters;
	}

}