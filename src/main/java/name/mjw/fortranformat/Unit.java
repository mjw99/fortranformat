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