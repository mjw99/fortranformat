//
//  FortranFormat Version 1.1, written by Kevin J. Theisen
//
//  Copyright (c) 2009 iChemLabs, LLC.  All rights reserved.
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
 * Holds a single parsed Fortran edit descriptor together with its width ({@code w}),
 * decimal ({@code d}), and exponent ({@code e}) parameters.
 *
 * @param type           the edit descriptor constant
 * @param length         the total field width {@code w}
 * @param decimalLength  the number of digits after the decimal point {@code d}
 * @param exponentLength the number of digits in the exponent field {@code e}
 */
record Unit(EditDescriptor type, int length, int decimalLength, int exponentLength) {

	/**
	 * Convenience constructor for descriptors that require only a field width.
	 * Sets {@code decimalLength} and {@code exponentLength} to zero.
	 *
	 * @param type   the edit descriptor constant
	 * @param length the total field width {@code w}
	 */
	Unit(EditDescriptor type, int length) {
		this(type, length, 0, 0);
	}

	/**
	 * Returns a string representation of this unit in Fortran edit descriptor notation,
	 * e.g. {@code F10.4} or {@code E12.6E2}.
	 */
	@Override
	public String toString() {
		return type.getTag() + length
				+ (decimalLength > 0 ? "." + decimalLength : "")
				+ (exponentLength > 0 ? "E" + exponentLength : "")
				+ " ";
	}
}
