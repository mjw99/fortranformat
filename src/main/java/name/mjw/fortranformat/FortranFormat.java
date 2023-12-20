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

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * The Class FortranFormat.
 */
public class FortranFormat {

	/** A hash of the descriptors for easy access. */
	static final HashMap<String, EditDescriptor> DESCRIPTOR_HASH = new HashMap<>(
			EditDescriptor.values().length);

	static {
		for (final EditDescriptor ed : EditDescriptor.values()) {
			DESCRIPTOR_HASH.put(ed.getTag(), ed);
		}
	}

	/**
	 * Static read function similar to Fortran implementation.
	 *
	 * @param data   is the data to be parsed
	 * @param format is the format specification
	 *
	 * @return ArrayList of all the parsed data as Java objects
	 *
	 * @throws ParseException the parse exception
	 * @throws IOException    Signals that an I/O exception has occurred.
	 */
	public static ArrayList<Object> read(final String data, final String format) throws ParseException, IOException {
		final FortranFormat ff = new FortranFormat(format);
		return ff.parse(data);
	}

	/**
	 * Static write function similar to the Fortran implementation.
	 *
	 * @param objects is the vector of objects to be formatted
	 * @param format  is the format specification
	 *
	 * @return the formatted string
	 *
	 * @throws ParseException the parse exception
	 * @throws IOException    Signals that an I/O exception has occurred.
	 */
	public static String write(final ArrayList<Object> objects, final String format)
			throws ParseException, IOException {
		final FortranFormat ff = new FortranFormat(format);
		return ff.format(objects);
	}

	/** The parsed Edit Descriptors. */
	private final ArrayList<Unit> units;

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
	 * Parses the input.
	 *
	 * @param s is the input string
	 *
	 * @return all the parsed data as Java Objects
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ArrayList<Object> parse(final String s) throws IOException {
		final StringTokenizer st = new StringTokenizer(s, "\n");
		final ArrayList<Object> returning = new ArrayList<>(units.size());
		StringReader sr = new StringReader(st.hasMoreTokens() ? st.nextToken() : "");
		for (final Unit u : units) {
			final char[] chars = new char[u.length];
			sr.read(chars, 0, u.length);
			final StringBuilder sb = new StringBuilder(chars.length);
			for (final char c : chars) {
				if ((u.type == EditDescriptor.CHARACTER || c != ' ') && c != 0) {
					sb.append(c);
				}
			}
			final String complete = sb.toString();
			if (u.type == EditDescriptor.FORMAT_SCANNING_CONTROL) {
				break;
			} else if (u.type == EditDescriptor.POSITIONING_VERTICAL) {
				sr = new StringReader(st.hasMoreTokens() ? st.nextToken() : "");
			} else {
				if (!u.type.isNonRepeatable()) {
					returning.add(u.type.parse(u, complete, options));
				}
			}
		}
		return returning;
	}

	/**
	 * Formats the given object.
	 *
	 * @param object is the Java Object to be formatted
	 *
	 * @return the formatted string
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String format(final Object object) throws IOException {
		final ArrayList<Object> input = new ArrayList<>(1);
		input.add(object);
		return format(input);
	}

	/**
	 * Formats the given objects.
	 *
	 * @param objects are the Java Objects to be formatted
	 *
	 * @return the formatted string
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	String format(final ArrayList<Object> objects) throws IOException {
		int minus = 0;
		StringBuilder sb = new StringBuilder();
		int place = -1;
		StringBuilder save = null;
		for (int i = 0; i < objects.size() + minus; i++) {
			final Unit u = units.get(i);
			final Object o = objects.get(i - minus);
			if (u.type == EditDescriptor.POSITIONING_TAB || u.type == EditDescriptor.POSITIONING_TAB_LEFT
					|| u.type == EditDescriptor.POSITIONING_TAB_RIGHT) {
				if (save == null) {
					save = sb;
				} else {
					while (place - 1 + sb.length() > save.length()) {
						save.append(' ');
					}
					save.replace(place - 1, place - 1 + sb.length(), sb.toString());
				}
				switch (u.type) {
				case POSITIONING_TAB:
					place = u.length;
					break;
				case POSITIONING_TAB_LEFT:
					place -= u.length - sb.length();
					break;
				case POSITIONING_TAB_RIGHT:
					place += u.length + sb.length();
					break;
				default:
					break;
				}
				sb = new StringBuilder();
			} else {
				sb.append(u.type.format(u, o, options));
			}
			if (u.type.isNonRepeatable()) {
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
