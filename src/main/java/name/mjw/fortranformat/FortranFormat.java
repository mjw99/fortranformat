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

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses and formats strings according to Fortran format specifications.
 *
 * <p>Provides static convenience methods {@link #read(String, String)} and
 * {@link #write(ArrayList, String)} analogous to Fortran READ/WRITE statements,
 * as well as instance methods for repeated use of the same format specification.
 */
public class FortranFormat {

	/** A hash of the descriptors for easy access. */
	static final Map<String, EditDescriptor> DESCRIPTOR_HASH;

	static {
		var map = new HashMap<String, EditDescriptor>(EditDescriptor.values().length);
		for (var ed : EditDescriptor.values()) {
			map.put(ed.getTag(), ed);
		}
		DESCRIPTOR_HASH = Map.copyOf(map);
	}

	/**
	 * Static read function similar to the Fortran READ statement.
	 *
	 * @param data   the data string to be parsed
	 * @param format the Fortran format specification string
	 *
	 * @return list of parsed data items as Java objects, in the order defined by the format
	 *
	 * @throws ParseException if the format specification string is invalid
	 * @throws IOException    if an I/O error occurs during parsing
	 */
	public static ArrayList<Object> read(final String data, final String format) throws ParseException, IOException {
		return new FortranFormat(format).parse(data);
	}

	/**
	 * Static write function similar to the Fortran WRITE statement.
	 *
	 * @param objects the list of objects to be formatted
	 * @param format  the Fortran format specification string
	 *
	 * @return the formatted string
	 *
	 * @throws ParseException if the format specification string is invalid
	 * @throws IOException    if an I/O error occurs during formatting
	 */
	public static String write(final ArrayList<Object> objects, final String format)
			throws ParseException, IOException {
		return new FortranFormat(format).format(objects);
	}

	/** The parsed Edit Descriptors. */
	private final List<Unit> units;

	/** The options. */
	private final Options options = new Options();

	/**
	 * Instantiates a new FortranFormat object.
	 *
	 * @param specificationString is the format specification string
	 *
	 * @throws ParseException the parse exception
	 */
	public FortranFormat(final String specificationString) throws ParseException {
		units = new SpecificationStringInterpreter(specificationString).getUnits();
	}

	/**
	 * Parses the input string according to this instance's format specification.
	 * Multi-line input is supported; newline characters advance to the next record.
	 *
	 * @param s the input string to parse
	 *
	 * @return list of parsed data items as Java objects, in the order defined by the format
	 *
	 * @throws IOException if an I/O error occurs during parsing
	 */
	public ArrayList<Object> parse(final String s) throws IOException {
		var lines = s.split("\n", -1);
		var lineIdx = 0;
		final var returning = new ArrayList<Object>(units.size());
		var sr = new StringReader(lines.length > 0 ? lines[lineIdx] : "");
		for (final var u : units) {
			final var chars = new char[u.length()];
			sr.read(chars, 0, u.length());
			final var sb = new StringBuilder(chars.length);
			for (final char c : chars) {
				if ((u.type() == EditDescriptor.CHARACTER || c != ' ') && c != 0) {
					sb.append(c);
				}
			}
			final var complete = sb.toString();
			if (u.type() == EditDescriptor.FORMAT_SCANNING_CONTROL) {
				break;
			} else if (u.type() == EditDescriptor.POSITIONING_VERTICAL) {
				lineIdx++;
				sr = new StringReader(lineIdx < lines.length ? lines[lineIdx] : "");
			} else {
				if (!u.type().isNonRepeatable()) {
					returning.add(u.type().parse(u, complete, options));
				}
			}
		}
		return returning;
	}

	/**
	 * Formats a single object according to this instance's format specification.
	 * Convenience overload of {@link #format(ArrayList)}.
	 *
	 * @param object the Java object to be formatted
	 *
	 * @return the formatted string
	 *
	 * @throws IOException if an I/O error occurs during formatting
	 */
	public String format(final Object object) throws IOException {
		final var input = new ArrayList<Object>(1);
		input.add(object);
		return format(input);
	}

	/**
	 * Formats a list of objects according to this instance's format specification.
	 *
	 * @param objects the Java objects to be formatted, in the order required by the format
	 *
	 * @return the formatted string
	 *
	 * @throws IOException if an I/O error occurs during formatting
	 */
	String format(final ArrayList<Object> objects) throws IOException {
		int minus = 0;
		var sb = new StringBuilder();
		int place = -1;
		StringBuilder save = null;
		for (int i = 0; i < objects.size() + minus && i < units.size(); i++) {
			final var u = units.get(i);
			final var o = objects.get(i - minus);
			if (u.type() == EditDescriptor.POSITIONING_TAB
					|| u.type() == EditDescriptor.POSITIONING_TAB_LEFT
					|| u.type() == EditDescriptor.POSITIONING_TAB_RIGHT) {
				if (save == null) {
					save = sb;
				} else {
					while (place - 1 + sb.length() > save.length()) {
						save.append(' ');
					}
					save.replace(place - 1, place - 1 + sb.length(), sb.toString());
				}
				place = switch (u.type()) {
					case POSITIONING_TAB -> u.length();
					case POSITIONING_TAB_LEFT -> place - (u.length() - sb.length());
					case POSITIONING_TAB_RIGHT -> place + (u.length() + sb.length());
					default -> place;
				};
				sb = new StringBuilder();
			} else {
				sb.append(u.type().format(u, o, options));
			}
			if (u.type().isNonRepeatable()) {
				minus++;
			}
		}
		if (save != null) {
			while (place - 1 + sb.length() > save.length()) {
				save.append(' ');
			}
			save.replace(place - 1, place - 1 + sb.length(), sb.toString());
			sb = save;
		}
		if (options.isAddReturn()) {
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Gets the Options object.
	 *
	 * @return the options object
	 */
	public Options getOptions() {
		return options;
	}
}
