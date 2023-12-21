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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * The Enum EditDescriptor.
 */
enum EditDescriptor {

	/** The CHARACTER. */
	CHARACTER("A", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String use = null;
			if (o != null) {
				use = o instanceof String ? (String) o : o.toString();
			}
			return format(
					o == null ? null
							: u.getLength() > 0 && use.length() > u.getLength() ? use.substring(0, u.getLength())
									: use,
					use != null && u.getLength() == 0 ? use.length() : u.getLength(),
					!options.isLeftAlignCharacters());
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			return s.trim();
		}
	},

	/** The INTEGER. */
	INTEGER("I", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = o == null ? null : Integer.toString((Integer) o);
			if (s != null && u.getDecimalLength() > 0) {
				final boolean neg = s.charAt(0) == '-';
				if (neg) {
					s = s.substring(1);
				}
				final int numzeros = u.getDecimalLength() - s.length();
				final StringBuilder sb2 = new StringBuilder();
				if (neg) {
					sb2.append('-');
				}
				for (int j = 0; j < numzeros; j++) {
					sb2.append('0');
				}
				sb2.append(s);
				s = sb2.toString();
			}
			return format(s, u.getLength(), true);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			if (s.length() == 0) {
				if (options.isReturnZeroForBlanks()) {
					return 0;
				} else {
					return null;
				}
			} else {
				return Integer.parseInt(s);
			}
		}
	},

	/** The LOGICAL. */
	LOGICAL("L", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = o == null ? null : (Boolean) o ? "T" : "F";
			if (s != null) {
				final StringBuilder sb2 = new StringBuilder();
				for (int j = 0; j < u.getLength() - 1; j++) {
					sb2.append(' ');
				}
				sb2.append(s);
				s = sb2.toString();
			}
			return format(s, u.getLength(), false);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			return s.length() == 0 ? null : s.charAt(0) == 'T' || s.charAt(0) == 't';
		}
	},

	/** The REAL_DECIMAL. */
	REAL_DECIMAL("F", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = null;
			if (o != null) {
				Double d = o instanceof Double ? (Double) o : (Float) o;
				final boolean neg = d < 0;
				if (neg) {
					d *= -1;
				}
				final StringBuilder dfs = new StringBuilder();
				final int intLength = Integer.toString(d.intValue()).length();
				for (int j = 0; j < intLength; j++) {
					dfs.append('0');
				}
				dfs.append('.');
				for (int j = 0; j < u.getDecimalLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				Double bd = new BigDecimal(d).setScale(u.getDecimalLength(), RoundingMode.HALF_UP).doubleValue();

				s = (neg ? '-' : "") + new DecimalFormat(dfs.toString()).format(bd);
			}
			return format(s, u.getLength(), true);
		}

		@Override
		public Object parse(final Unit u, String s, final Options options) throws IOException {
			Double returning = null;
			if (s.indexOf('E') == -1) {
				returning = s.length() == 0 ? null
						: Double.parseDouble(s) / (s.indexOf('.') == -1 ? Math.pow(10, u.getDecimalLength()) : 1);
			} else {
				String end = s.substring(s.indexOf('E') + 1);
				if (end.startsWith("+")) {
					end = end.substring(1);
				}
				s = s.substring(0, s.indexOf('E'));
				returning = s.length() == 0 ? null
						: Double.parseDouble(s) / (s.indexOf('.') == -1 ? Math.pow(10, u.getDecimalLength()) : 1)
								* Math.pow(10, Integer.parseInt(end));
			}
			if (returning == null && options.isReturnZeroForBlanks()) {
				returning = (double) 0;
			}
			if (returning == null) {
				return null;
			}
			return options.isReturnFloats() && s.length() != 0 ? returning.floatValue() : returning;
		}
	},

	/** The REAL_DECIMAL_REDUNDANT. */
	REAL_DECIMAL_REDUNDANT("G", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			return REAL_DECIMAL.format(u, o, options);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			return REAL_DECIMAL.parse(u, s, options);
		}
	},

	/** The REAL_DOUBLE. */
	REAL_DOUBLE("D", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new java.io.IOException("Output for the D edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			throw new java.io.IOException("Input for the D edit descriptor is not supported.");
		}
	},

	/** The REAL_ENGINEERING. */
	REAL_ENGINEERING("EN", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = null;
			if (o != null) {
				Double d = o instanceof Double ? (Double) o : (Float) o;
				int exp = 0;
				final boolean neg = d < 0;
				if (neg) {
					d *= -1;
				}
				while (d > 10) {
					d /= 10;
					exp += 1;
				}
				while (d < 1) {
					d *= 10;
					exp -= 1;
				}
				while (exp % 3 != 0) {
					d *= 10;
					exp -= 1;
				}
				final boolean expneg = exp < 0;
				if (expneg) {
					exp *= -1;
				}
				StringBuilder dfs = new StringBuilder();
				dfs.append("0.");
				for (int j = 0; j < u.getDecimalLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				Double bd = new BigDecimal(d).setScale(u.getDecimalLength(), RoundingMode.HALF_UP).doubleValue();

				s = (neg ? "-" : "") + new DecimalFormat(dfs.toString()).format(bd);
				dfs = new StringBuilder();
				for (int j = 0; j < u.getExponentLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				Double bd2 = new BigDecimal(exp).setScale(u.getDecimalLength(), RoundingMode.HALF_UP).doubleValue();

				s = s + "E" + (expneg ? "-" : "+") + new DecimalFormat(dfs.toString()).format(bd2);
			}
			return format(s, u.getLength(), true);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			return REAL_DECIMAL.parse(u, s, options);
		}
	},

	/** The REAL_EXPONENT. */
	REAL_EXPONENT("E", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = null;
			if (o != null) {
				Double d = o instanceof Double ? (Double) o : (Float) o;
				int exp = 0;
				final boolean neg = d < 0;
				if (neg) {
					d *= -1;
				}
				while (d > 1) {
					d /= 10;
					exp += 1;
				}
				while (d < .1) {
					d *= 10;
					exp -= 1;
				}
				final boolean expneg = exp < 0;
				if (expneg) {
					exp *= -1;
				}
				StringBuilder dfs = new StringBuilder();
				dfs.append("0.");
				for (int j = 0; j < u.getDecimalLength(); j++) {
					dfs.append('0');
				}
				s = (neg ? '-' : "") + new DecimalFormat(dfs.toString()).format(d);
				dfs = new StringBuilder();
				for (int j = 0; j < u.getExponentLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				Double bd = new BigDecimal(exp).setScale(u.getDecimalLength(), RoundingMode.HALF_UP).doubleValue();

				s = s + 'E' + (expneg ? '-' : '+') + new DecimalFormat(dfs.toString()).format(bd);
			}
			return format(s, u.getLength(), true);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			return REAL_DECIMAL.parse(u, s, options);
		}
	},

	/** The REAL_SCIENTIFIC. */
	REAL_SCIENTIFIC("ES", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = null;
			if (o != null) {
				Double d = o instanceof Double ? (Double) o : (Float) o;
				int exp = 0;
				final boolean neg = d < 0;
				if (neg) {
					d *= -1;
				}
				while (d > 10) {
					d /= 10;
					exp += 1;
				}
				while (d < 1) {
					d *= 10;
					exp -= 1;
				}
				final boolean expneg = exp < 0;
				if (expneg) {
					exp *= -1;
				}
				StringBuilder dfs = new StringBuilder();
				dfs.append("0.");
				for (int j = 0; j < u.getDecimalLength(); j++) {
					dfs.append('0');
				}
				s = (neg ? "-" : "") + new DecimalFormat(dfs.toString()).format(d);
				dfs = new StringBuilder();
				for (int j = 0; j < u.getExponentLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				Double bd = new BigDecimal(exp).setScale(u.getDecimalLength(), RoundingMode.HALF_UP).doubleValue();

				s = s + "E" + (expneg ? "-" : "+") + new DecimalFormat(dfs.toString()).format(bd);
			}
			return format(s, u.getLength(), true);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			return REAL_DECIMAL.parse(u, s, options);
		}
	},

	/** The BLANK_CONTROL_REMOVE. */
	BLANK_CONTROL_REMOVE("BN", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new java.io.IOException("Output for the BN edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			// do nothing
			return null;
		}
	},

	/** The BLANK_CONTROL_ZEROS. */
	BLANK_CONTROL_ZEROS("BZ", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new java.io.IOException("Output for the BZ edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			// do nothing
			return null;
		}
	},

	/** The FORMAT_SCANNING_CONTROL. */
	FORMAT_SCANNING_CONTROL(":", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			// Do nothing
			return "";
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			// never called
			return null;
		}
	},

	/** The POSITIONING_HORIZONTAL. */
	POSITIONING_HORIZONTAL("X", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			final StringBuilder sb = new StringBuilder();
			for (int j = 0; j < u.getLength(); j++) {
				sb.append(options.getPositioningChar());
			}
			return sb.toString();
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			// do nothing
			return null;
		}
	},

	/** The POSITIONING_TAB. */
	POSITIONING_TAB("T", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			// never called
			return null;
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			throw new java.io.IOException("Input for the T edit descriptor is not supported.");
		}
	},

	/** The POSITIONING_TAB_LEFT. */
	POSITIONING_TAB_LEFT("TL", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			// never called
			return null;
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			throw new java.io.IOException("Input for the TL edit descriptor is not supported.");
		}
	},

	/** The POSITIONING_TAB_RIGHT. */
	POSITIONING_TAB_RIGHT("TR", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			// never called
			return null;
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			throw new java.io.IOException("Input for the TR edit descriptor is not supported.");
		}
	},

	/** The POSITIONING_VERTICAL. */
	POSITIONING_VERTICAL("/", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			return "\n";
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			// never called
			return null;
		}
	},

	/** The SIGN_CONTROL_COMPILER. */
	SIGN_CONTROL_COMPILER("S", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new java.io.IOException("Output for the S edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			// do nothing
			return null;
		}
	},

	/** The SIGN_CONTROL_POSITIVE_ALWAYS. */
	SIGN_CONTROL_POSITIVE_ALWAYS("SP", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new java.io.IOException("Output for the SP edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			// do nothing
			return null;
		}
	},

	/** The SIGN_CONTROL_POSITIVE_NEVER. */
	SIGN_CONTROL_POSITIVE_NEVER("SS", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new java.io.IOException("Output for the SS edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			// do nothing
			return null;
		}
	};

	/** The tag. */
	private final String tag;

	/** If non-repeatable. */
	private final boolean nonRepeatable;

	/**
	 * Instantiates a new edits the descriptor.
	 *
	 * @param tag           the edit descriptor tag
	 * @param nonRepeatable whether the edit descriptor is non-repeatable or not
	 */
	private EditDescriptor(final String tag, final boolean nonRepeatable) {
		this.tag = tag;
		this.nonRepeatable = nonRepeatable;
	}

	/**
	 * Gets the tag.
	 *
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Formats the object.
	 *
	 * @param u       the parent unit
	 * @param o       the object to be formatted
	 * @param options the options
	 *
	 * @return the formatted string
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract String format(Unit u, Object o, Options options) throws IOException;

	/**
	 * Parses the object.
	 *
	 * @param u       the parent unit
	 * @param s       the String to be parsed
	 * @param options the options
	 *
	 * @return the parsed object
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract Object parse(Unit u, String s, Options options) throws IOException;

	/**
	 * Helper method to add spaces and right align content.
	 *
	 * @param s            is the String to append
	 * @param length       is the desired length
	 * @param rightAligned specifies if the content should be right-aligned
	 *
	 * @return the formatted string
	 */
	String format(final String s, final int length, final boolean rightAligned) {
		final StringBuilder sb = new StringBuilder();
		if (s == null) {
			for (int i = 0; i < length; i++) {
				sb.append(' ');
			}
		} else if (length == -1) {
			sb.append(s);
		} else if (s.length() > length) {
			for (int i = 0; i < length; i++) {
				sb.append('*');
			}
		} else {
			final int dif = length - s.length();
			if (rightAligned) {
				for (int j = 0; j < dif; j++) {
					sb.append(' ');
				}
			}
			sb.append(s);
			if (!rightAligned) {
				for (int j = 0; j < dif; j++) {
					sb.append(' ');
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Checks if is non-repeatable.
	 *
	 * @return true, if is non-repeatable
	 */
	public boolean isNonRepeatable() {
		return nonRepeatable;
	}
}