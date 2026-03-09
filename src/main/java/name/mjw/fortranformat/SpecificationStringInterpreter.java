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

import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Parses a Fortran format specification string into a list of {@link Unit} objects.
 *
 * <p>Pre-processing is performed in several stages: comma normalisation, repeat-count
 * expansion, and parenthesis removal. The final list of units is obtained via
 * {@link #getUnits()}.
 */
class SpecificationStringInterpreter {

	/** The original format specification string as supplied by the caller. */
	private final String original;

	/** The content extracted from inside the outermost parentheses, with whitespace removed. */
	private final String input;

	/** The format string after parenthesised groups have been flattened. */
	private final String withoutParenthesis;

	/** The format string after repeat-count notation (e.g. {@code 3I5}) has been expanded. */
	private final String multipliedOut;

	/** The format string after comma separators have been inserted in the correct positions. */
	private final String withCommas;

	/**
	 * Instantiates a new specification string interpreter.
	 *
	 * @param s the String to be pre-processed
	 *
	 * @throws ParseException the parse exception
	 */
	public SpecificationStringInterpreter(final String s) throws ParseException {
		Objects.requireNonNull(s, "The format specification string may not be null.");
		original = s;

		final int open = s.indexOf('(');
		if (open == -1) {
			throw new ParseException(
					"Fortran format specification strings must begin with an open parenthesis '(' and end with a close parenthesis ')'. Blank spaces are tolerated before an open parenthesis and any characters are tolerated after a close parenthesis. No characters outside of the root parenthesis affect the format specification.",
					0);
		}
		final int close = findClosingParenthesis(s, open);
		final var before = s.substring(0, open);
		if (!before.replace(" ", "").isEmpty()) {
			throw new ParseException("Only spaces may precede the root parenthesis.", 0);
		}

		input = s.substring(open + 1, close).replace(" ", "");
		withCommas = checkCommas(input);
		multipliedOut = multiplyOut(withCommas);
		withoutParenthesis = removeParenthesis(multipliedOut);
	}

	/**
	 * Adds the commas to the correct places.
	 *
	 * @param input the input
	 *
	 * @return the string
	 */
	final String checkCommas(final String input) {
		final var sb = new StringBuilder();
		boolean hitE = false;
		boolean lastWasChar = true;
		boolean foundNotNum = false;
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == '(' || c == ')' || c == ',') {
				sb.append(c);
			} else if (c == EditDescriptor.POSITIONING_HORIZONTAL.getTag().charAt(0)) {
				sb.append(c);
				if (i != input.length() - 1 && input.charAt(i + 1) != ')' && input.charAt(i + 1) != ',') {
					sb.append(',');
					lastWasChar = true;
				}
			} else if (c == '.' || Character.isDigit(c)) {
				sb.append(c);
				lastWasChar = false;
				if (i != 0 && input.charAt(i - 1) == ',') {
					foundNotNum = false;
				}
			} else {
				if (foundNotNum && !lastWasChar && i != 0 && sb.charAt(sb.length() - 1) != ','
						&& !(c == EditDescriptor.REAL_EXPONENT.getTag().charAt(0) && hitE)) {
					sb.append(',');
					hitE = false;
				}
				if (c == EditDescriptor.REAL_EXPONENT.getTag().charAt(0)) {
					hitE = true;
				}
				foundNotNum = true;
				lastWasChar = true;
				sb.append(c);
				if (c == '/') {
					sb.append(',');
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Multiplies out compound descriptors.
	 *
	 * @param input the input
	 *
	 * @return the string
	 *
	 * @throws ParseException the parse exception
	 */
	final String multiplyOut(final String input) throws ParseException {
		final var sb = new StringBuilder();
		final var current = new StringBuilder();
		final var number = new StringBuilder();
		int multiplier = 1;
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == '(') {
				if (number.length() > 0) {
					multiplier = Integer.parseInt(number.toString());
				}
				if (current.length() > 0) {
					for (int j = 0; j < multiplier; j++) {
						sb.append(current);
					}
					current.setLength(0);
					number.setLength(0);
				}
				final int closing = findClosingParenthesis(input, i);
				final var center = multiplyOut(input.substring(i + 1, closing));
				for (int j = 0; j < multiplier; j++) {
					sb.append('(');
					sb.append(center);
					sb.append(')');
				}
				i = closing;
				multiplier = 1;
				current.setLength(0);
				number.setLength(0);
			} else if (c == ',') {
				for (int j = 0; j < multiplier; j++) {
					sb.append(current);
					sb.append(',');
				}
				multiplier = 1;
				current.setLength(0);
			} else if (Character.isDigit(c) && current.length() == 0) {
				number.append(c);
			} else {
				if (c == EditDescriptor.POSITIONING_HORIZONTAL.getTag().charAt(0)) {
					sb.append(number);
					number.setLength(0);
					number.append('1');
				}
				if (number.length() > 0) {
					multiplier = Integer.parseInt(number.toString());
					number.setLength(0);
				}
				current.append(c);
			}
		}
		if (current.length() > 0) {
			for (int j = 0; j < multiplier; j++) {
				sb.append(current);
				if (j != multiplier - 1) {
					sb.append(',');
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Removes the parenthesis from the specification string.
	 *
	 * @param input the input
	 *
	 * @return the string
	 */
	final String removeParenthesis(final String input) {
		final var sb = new StringBuilder();
		boolean hitParenthesis = false;
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == '(' || c == ')') {
				hitParenthesis = true;
			} else {
				if (hitParenthesis && !sb.isEmpty() && sb.charAt(sb.length() - 1) != ',') {
					sb.append(',');
				}
				hitParenthesis = false;
				if (c != ',' || (!sb.isEmpty() && sb.charAt(sb.length() - 1) != ',')) {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Find the closing parenthesis to a given open parenthesis in a string.
	 *
	 * @param withParen is the String containing the open parenthesis in question.
	 * @param open      is the index of the open parenthesis
	 *
	 * @return the index of the corresponding close parenthesis
	 *
	 * @throws ParseException the parse exception
	 */
	private int findClosingParenthesis(final String withParen, final int open) throws ParseException {
		final var stack = new ArrayDeque<Integer>();
		for (int i = open + 1; i < withParen.length(); i++) {
			switch (withParen.charAt(i)) {
				case ')' -> {
					if (stack.isEmpty()) {
						return i;
					}
					stack.pop();
				}
				case '(' -> stack.push(i);
			}
		}
		throw new ParseException("Missing a close parenthesis.", open);
	}

	/**
	 * Parses the format specification string after pre-processing.
	 *
	 * @return the ArrayList of Units that correspond to the format
	 *
	 * @throws ParseException the parse exception
	 */
	public final ArrayList<Unit> getUnits() throws ParseException {
		final var tokens = getCompletedInterpretation().split(",");
		final var units = new ArrayList<Unit>(tokens.length);
		for (final var s : tokens) {
			if (s.isEmpty()) {
				continue;
			}
			boolean reachedType = false;
			boolean hasDecimal = false;
			boolean hasExponent = false;
			final var before = new StringBuilder();
			final var type = new StringBuilder();
			final var decimal = new StringBuilder();
			final var exponent = new StringBuilder();
			var after = new StringBuilder();
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) == '.') {
					hasDecimal = true;
				} else if (reachedType && s.charAt(i) == 'E') {
					hasExponent = true;
				} else if (Character.isLetter(s.charAt(i)) || s.charAt(i) == '/') {
					type.append(s.charAt(i));
					reachedType = true;
				} else {
					if (hasExponent) {
						exponent.append(s.charAt(i));
					} else if (hasDecimal) {
						decimal.append(s.charAt(i));
					} else if (reachedType) {
						after.append(s.charAt(i));
					} else {
						before.append(s.charAt(i));
					}
				}
			}
			int repeats = before.isEmpty() ? 1 : Integer.parseInt(before.toString());
			if (type.toString().equals(EditDescriptor.POSITIONING_HORIZONTAL.getTag())) {
				after = before;
				repeats = 1;
			}
			if (type.toString().equals(EditDescriptor.REAL_EXPONENT.getTag()) && exponent.isEmpty()) {
				exponent.append('2');
			}
			for (int i = 0; i < repeats; i++) {
				if (!FortranFormat.DESCRIPTOR_HASH.containsKey(type.toString())) {
					throw new ParseException("Unsupported Edit Descriptor: " + type,
							original.indexOf(type.toString()));
				}
				final int len = after.isEmpty() ? 0 : Integer.parseInt(after.toString());
				final int decLen = decimal.isEmpty() ? 0 : Integer.parseInt(decimal.toString());
				final int expLen = exponent.isEmpty() ? 0 : Integer.parseInt(exponent.toString());
				units.add(new Unit(FortranFormat.DESCRIPTOR_HASH.get(type.toString()), len, decLen, expLen));
			}
		}
		return units;
	}

	/**
	 * Returns the fully pre-processed format string: commas inserted, repeat counts
	 * expanded, and parentheses removed.
	 *
	 * @return the flattened, comma-separated descriptor string ready for tokenisation
	 */
	public String getCompletedInterpretation() {
		return withoutParenthesis;
	}
}
