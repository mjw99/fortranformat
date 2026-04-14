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

import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.StringTokenizer;

/**
 * Parses a Fortran format specification string into a list of {@link Unit} objects.
 *
 * <p>Pre-processing is performed in several stages: comma normalisation, repeat-count
 * expansion, and parenthesis removal. The final list of units is obtained via
 * {@link #getUnits()}.
 */
class SpecificationStringInterpreter {

	/** The original, unmodified format specification string as passed to the constructor. */
	private final String original;

	/** The content between the outermost parentheses with all spaces removed. */
	private final String input;

	/** The format string after all parentheses have been removed following repeat-expansion. */
	private final String withoutParenthesis;

	/** The format string after repeat-count groups have been expanded in full. */
	private final String multipliedOut;

	/** The format string after implicit commas have been inserted between adjacent descriptors. */
	private final String withCommas;

	/**
	 * Instantiates a new specification string interpreter.
	 *
	 * @param s the String to be pre-processed
	 *
	 * @throws ParseException the parse exception
	 */
	public SpecificationStringInterpreter(final String s) throws ParseException {
		if (s == null) {
			throw new NullPointerException("The format specification string may not be null.");
		}
		original = s;

		// check for malformatted root parenthesis
		final int open = s.indexOf('(');
		if (open == -1) {
			throw new ParseException(
					"Fortran format specification strings must begin with an open parenthesis '(' and end with a close parenthesis ')'. Blank spaces are tolerated before an open parenthesis and any characters are tolerated after a close parenthesis. No characters outside of the root parenthesis affect the format specification.",
					0);
		}
		final int close = findClosingParenthesis(s, open);
		final String before = s.substring(0, open);
		if (before.replace(" ", "").length() != 0) {
			throw new ParseException("Only spaces may precede the root parenthesis.", 0);
		}

		input = s.substring(open + 1, close).replace(" ", "");
		withCommas = checkCommas(input);
		multipliedOut = multiplyOut(withCommas);
		withoutParenthesis = removeParenthesis(multipliedOut);
	}

	/**
	 * Inserts commas between adjacent edit descriptors where they are implicitly required.
	 *
	 * @param input the format string with spaces already removed
	 *
	 * @return the format string with commas inserted at all required positions
	 */
	final String checkCommas(final String input) {
		final StringBuilder sb = new StringBuilder();
		boolean hitE = false;
		boolean lastWasChar = true;
		boolean foundNotNum = false;
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == '(' || c == ')' || c == ',') {
				// skip over
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
	 * Expands all repeat-count prefixes recursively, so that for example {@code 3I4}
	 * becomes {@code I4,I4,I4}. Parentheses are preserved in the output; they are
	 * removed by the subsequent {@link #removeParenthesis} step.
	 *
	 * @param input the comma-normalised format string to expand
	 *
	 * @return the fully expanded format string with all repeat counts resolved, still containing parentheses
	 *
	 * @throws ParseException if a parenthesised group has no matching closing parenthesis
	 */
	final String multiplyOut(final String input) throws ParseException {
		final StringBuilder sb = new StringBuilder();
		final StringBuilder current = new StringBuilder();
		final StringBuilder number = new StringBuilder();
		int multiplier = 1;
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == '(') {
				if (number.length() > 0) {
					multiplier = Integer.parseInt(number.toString());
				}
				if (current.length() > 0) {
					for (int j = 0; j < multiplier; j++) {
						sb.append(current.toString());
					}
					current.delete(0, current.length());
					number.delete(0, number.length());
				}
				final int closing = findClosingParenthesis(input, i);
				final String center = multiplyOut(input.substring(i + 1, closing));
				for (int j = 0; j < multiplier; j++) {
					sb.append('(');
					sb.append(center);
					sb.append(')');
				}
				i = closing;
				multiplier = 1;
				current.delete(0, current.length());
				number.delete(0, number.length());
			} else if (c == ',') {
				for (int j = 0; j < multiplier; j++) {
					sb.append(current.toString());
					sb.append(',');
				}
				multiplier = 1;
				current.delete(0, current.length());
			} else if (Character.isDigit(c) && current.length() == 0) {
				number.append(c);
			} else {
				if (c == EditDescriptor.POSITIONING_HORIZONTAL.getTag().charAt(0)) {
					sb.append(number);
					number.delete(0, number.length());
					number.append('1');
				}
				if (number.length() > 0) {
					multiplier = Integer.parseInt(number.toString());
					number.delete(0, number.length());
				}
				current.append(c);
			}
		}
		if (current.length() > 0) {
			for (int j = 0; j < multiplier; j++) {
				sb.append(current.toString());
				if (j != multiplier - 1) {
					sb.append(',');
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Removes all parentheses from the fully-expanded format string, inserting
	 * commas at group boundaries as needed.
	 *
	 * @param input the expanded format string still containing parentheses
	 *
	 * @return the flat, comma-separated format string with no parentheses
	 */
	final String removeParenthesis(final String input) {
		final StringBuilder sb = new StringBuilder();
		boolean hitParenthesis = false;
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == '(' || c == ')') {
				hitParenthesis = true;
			} else {
				if (hitParenthesis && sb.length() != 0 && sb.charAt(sb.length() - 1) != ',') {
					sb.append(',');
				}
				hitParenthesis = false;
				if (c != ',' || (sb.length() != 0 && sb.charAt(sb.length() - 1) != ',')) {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Finds the index of the closing parenthesis that matches the opening parenthesis
	 * at position {@code open} in {@code withParen}.
	 *
	 * @param withParen the string containing the open parenthesis
	 * @param open      the index of the open parenthesis within {@code withParen}
	 *
	 * @return the index of the corresponding closing parenthesis
	 *
	 * @throws ParseException if no matching closing parenthesis is found
	 */
	private int findClosingParenthesis(final String withParen, final int open) throws ParseException {
		final Deque<Integer> s = new ArrayDeque<>();
		for (int i = open + 1; i < withParen.length(); i++) {
			final char c = withParen.charAt(i);
			switch (c) {
			case ')':
				if (s.isEmpty()) {
					return i;
				} else {
					s.pop();
				}
				break;
			case '(':
				s.push(i);
				break;
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
		final StringTokenizer st = new StringTokenizer(getCompletedInterpretation(), ",");
		final ArrayList<Unit> units = new ArrayList<>(st.countTokens());
		while (st.hasMoreTokens()) {
			final String s = st.nextToken();
			boolean reachedType = false;
			boolean hasDecimal = false;
			boolean hasExponent = false;
			final StringBuilder before = new StringBuilder();
			final StringBuilder type = new StringBuilder();
			final StringBuilder decimal = new StringBuilder();
			final StringBuilder exponent = new StringBuilder();
			StringBuilder after = new StringBuilder();
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
			int repeats = before.length() == 0 ? 1 : Integer.parseInt(before.toString());
			if (type.toString().equals(EditDescriptor.POSITIONING_HORIZONTAL.getTag())) {
				after = before;
				repeats = 1;
			}
			if (type.toString().equals(EditDescriptor.REAL_EXPONENT.getTag()) && exponent.length() == 0) {
				exponent.append('2');
			}
			for (int i = 0; i < repeats; i++) {
				if (!FortranFormat.DESCRIPTOR_HASH.containsKey(type.toString())) {
					throw new ParseException("Unsupported Edit Descriptor: " + type.toString(),
							original.indexOf(type.toString()));
				}
				final Unit u = new Unit(FortranFormat.DESCRIPTOR_HASH.get(type.toString()),
						after.length() == 0 ? 0 : Integer.parseInt(after.toString()));
				if (decimal.length() != 0) {
					u.decimalLength = Integer.parseInt(decimal.toString());
				}
				if (exponent.length() != 0) {
					u.exponentLength = Integer.parseInt(exponent.toString());
				}
				units.add(u);
			}
		}
		return units;
	}

	/**
	 * Returns the fully processed format string: parentheses removed, repeat counts
	 * expanded, and commas normalised. This is the string parsed by {@link #getUnits()}.
	 *
	 * @return the flat, comma-separated list of edit descriptor tokens
	 */
	public String getCompletedInterpretation() {
		return withoutParenthesis;
	}

}