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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Fortran edit descriptors that define how individual data items are read or written.
 *
 * <p>Each constant corresponds to a Fortran format edit descriptor (e.g. {@code A}, {@code I},
 * {@code F}, {@code E}) and provides the associated {@link #format} and {@link #parse} logic.
 * Non-repeatable descriptors (control descriptors such as {@code X}, {@code T}, {@code /}) are
 * flagged via {@link #isNonRepeatable()}.
 */
enum EditDescriptor {

	/** Character (string) edit descriptor — Fortran {@code A} descriptor. */
	CHARACTER("A", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			var use = o instanceof String s ? s : (o != null ? o.toString() : null);
			return format(
					o == null ? null
							: u.length() > 0 && use.length() > u.length() ? use.substring(0, u.length())
									: use,
					use != null && u.length() == 0 ? use.length() : u.length(),
					!options.isLeftAlignCharacters());
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return s.trim();
		}
	},

	/** Integer edit descriptor — Fortran {@code I} descriptor. */
	INTEGER("I", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = o == null ? null : Integer.toString((Integer) o);
			if (s != null && u.decimalLength() > 0) {
				final boolean neg = s.charAt(0) == '-';
				if (neg) {
					s = s.substring(1);
				}
				final int numzeros = u.decimalLength() - s.length();
				final var sb2 = new StringBuilder();
				if (neg) {
					sb2.append('-');
				}
				for (int j = 0; j < numzeros; j++) {
					sb2.append('0');
				}
				sb2.append(s);
				s = sb2.toString();
			}
			return format(s, u.length(), true);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			if (s.isEmpty()) {
				return options.isReturnZeroForBlanks() ? 0 : null;
			}
			return Integer.parseInt(s);
		}
	},

	/** Logical (boolean) edit descriptor — Fortran {@code L} descriptor. */
	LOGICAL("L", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = o == null ? null : (Boolean) o ? "T" : "F";
			if (s != null) {
				final var sb2 = new StringBuilder();
				for (int j = 0; j < u.length() - 1; j++) {
					sb2.append(' ');
				}
				sb2.append(s);
				s = sb2.toString();
			}
			return format(s, u.length(), false);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return s.isEmpty() ? null : s.charAt(0) == 'T' || s.charAt(0) == 't';
		}
	},

	/** Real (fixed-point decimal) edit descriptor — Fortran {@code F} descriptor. */
	REAL_DECIMAL("F", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = null;
			if (o != null) {
				double d = ((Number) o).doubleValue();
				final boolean neg = d < 0;
				if (neg) {
					d *= -1;
				}
				final var dfs = new StringBuilder();
				final int intLength = Integer.toString((int) d).length();
				for (int j = 0; j < intLength; j++) {
					dfs.append('0');
				}
				dfs.append('.');
				for (int j = 0; j < u.decimalLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				double bd = new BigDecimal(d).setScale(u.decimalLength(), RoundingMode.HALF_UP).doubleValue();
				s = (neg ? "-" : "") + new DecimalFormat(dfs.toString()).format(bd);
			}
			return format(s, u.length(), true);
		}

		@Override
		public Object parse(final Unit u, String s, final Options options) {
			Double returning;
			if (!s.contains("E")) {
				returning = s.isEmpty() ? null
						: Double.parseDouble(s) / (s.contains(".") ? 1 : Math.pow(10, u.decimalLength()));
			} else {
				var end = s.substring(s.indexOf('E') + 1);
				if (end.startsWith("+")) {
					end = end.substring(1);
				}
				s = s.substring(0, s.indexOf('E'));
				returning = s.isEmpty() ? null
						: Double.parseDouble(s) / (s.contains(".") ? 1 : Math.pow(10, u.decimalLength()))
								* Math.pow(10, Integer.parseInt(end));
			}
			if (returning == null && options.isReturnZeroForBlanks()) {
				returning = 0.0;
			}
			if (returning == null) {
				return null;
			}
			if (options.isReturnFloats() && !s.isEmpty()) {
				return returning.floatValue();
			}
			return returning;
		}
	},

	/** Generalised real edit descriptor — Fortran {@code G} descriptor. Delegates to {@link #REAL_DECIMAL}. */
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

	/** Double-precision real edit descriptor — Fortran {@code D} descriptor. Not supported for I/O. */
	REAL_DOUBLE("D", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new IOException("Output for the D edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			throw new IOException("Input for the D edit descriptor is not supported.");
		}
	},

	/** Engineering notation real edit descriptor — Fortran {@code EN} descriptor. */
	REAL_ENGINEERING("EN", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = null;
			if (o != null) {
				double d = ((Number) o).doubleValue();
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
				var dfs = new StringBuilder("0.");
				for (int j = 0; j < u.decimalLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				double bd = new BigDecimal(d).setScale(u.decimalLength(), RoundingMode.HALF_UP).doubleValue();
				s = (neg ? "-" : "") + new DecimalFormat(dfs.toString()).format(bd);

				dfs = new StringBuilder();
				for (int j = 0; j < u.exponentLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				double bd2 = new BigDecimal(exp).setScale(u.decimalLength(), RoundingMode.HALF_UP).doubleValue();
				s = s + "E" + (expneg ? "-" : "+") + new DecimalFormat(dfs.toString()).format(bd2);
			}
			return format(s, u.length(), true);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			return REAL_DECIMAL.parse(u, s, options);
		}
	},

	/** Exponential notation real edit descriptor — Fortran {@code E} descriptor. */
	REAL_EXPONENT("E", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = null;
			if (o != null) {
				double d = ((Number) o).doubleValue();
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
				var dfs = new StringBuilder("0.");
				for (int j = 0; j < u.decimalLength(); j++) {
					dfs.append('0');
				}
				s = (neg ? "-" : "") + new DecimalFormat(dfs.toString()).format(d);

				dfs = new StringBuilder();
				for (int j = 0; j < u.exponentLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				double bd = new BigDecimal(exp).setScale(u.decimalLength(), RoundingMode.HALF_UP).doubleValue();
				s = s + "E" + (expneg ? "-" : "+") + new DecimalFormat(dfs.toString()).format(bd);
			}
			return format(s, u.length(), true);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			return REAL_DECIMAL.parse(u, s, options);
		}
	},

	/** Scientific notation real edit descriptor — Fortran {@code ES} descriptor. */
	REAL_SCIENTIFIC("ES", false) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			String s = null;
			if (o != null) {
				double d = ((Number) o).doubleValue();
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
				var dfs = new StringBuilder("0.");
				for (int j = 0; j < u.decimalLength(); j++) {
					dfs.append('0');
				}
				s = (neg ? "-" : "") + new DecimalFormat(dfs.toString()).format(d);

				dfs = new StringBuilder();
				for (int j = 0; j < u.exponentLength(); j++) {
					dfs.append('0');
				}
				// Work around for JDK-7131459
				double bd = new BigDecimal(exp).setScale(u.decimalLength(), RoundingMode.HALF_UP).doubleValue();
				s = s + "E" + (expneg ? "-" : "+") + new DecimalFormat(dfs.toString()).format(bd);
			}
			return format(s, u.length(), true);
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			return REAL_DECIMAL.parse(u, s, options);
		}
	},

	/** Blank control (ignore blanks) edit descriptor — Fortran {@code BN} descriptor. Output not supported. */
	BLANK_CONTROL_REMOVE("BN", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new IOException("Output for the BN edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return null;
		}
	},

	/** Blank control (treat blanks as zeros) edit descriptor — Fortran {@code BZ} descriptor. Output not supported. */
	BLANK_CONTROL_ZEROS("BZ", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new IOException("Output for the BZ edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return null;
		}
	},

	/** Format scanning control descriptor — Fortran {@code :} descriptor. Terminates format scanning when no more data items remain. */
	FORMAT_SCANNING_CONTROL(":", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			return "";
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return null;
		}
	},

	/** Horizontal spacing edit descriptor — Fortran {@code X} descriptor. Skips {@code n} character positions. */
	POSITIONING_HORIZONTAL("X", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			return String.valueOf(options.getPositioningChar()).repeat(u.length());
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return null;
		}
	},

	/** Absolute tab positioning edit descriptor — Fortran {@code T} descriptor. Input not supported. */
	POSITIONING_TAB("T", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			return null;
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			throw new IOException("Input for the T edit descriptor is not supported.");
		}
	},

	/** Left-relative tab positioning edit descriptor — Fortran {@code TL} descriptor. Input not supported. */
	POSITIONING_TAB_LEFT("TL", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			return null;
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			throw new IOException("Input for the TL edit descriptor is not supported.");
		}
	},

	/** Right-relative tab positioning edit descriptor — Fortran {@code TR} descriptor. Input not supported. */
	POSITIONING_TAB_RIGHT("TR", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			return null;
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) throws IOException {
			throw new IOException("Input for the TR edit descriptor is not supported.");
		}
	},

	/** Vertical positioning (record separator) edit descriptor — Fortran {@code /} descriptor. Moves to a new record. */
	POSITIONING_VERTICAL("/", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) {
			return "\n";
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return null;
		}
	},

	/** Sign control (compiler default) edit descriptor — Fortran {@code S} descriptor. Output not supported. */
	SIGN_CONTROL_COMPILER("S", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new IOException("Output for the S edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return null;
		}
	},

	/** Sign control (always show plus sign) edit descriptor — Fortran {@code SP} descriptor. Output not supported. */
	SIGN_CONTROL_POSITIVE_ALWAYS("SP", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new IOException("Output for the SP edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return null;
		}
	},

	/** Sign control (suppress plus sign) edit descriptor — Fortran {@code SS} descriptor. Output not supported. */
	SIGN_CONTROL_POSITIVE_NEVER("SS", true) {
		@Override
		public String format(final Unit u, final Object o, final Options options) throws IOException {
			throw new IOException("Output for the SS edit descriptor is not supported.");
		}

		@Override
		public Object parse(final Unit u, final String s, final Options options) {
			return null;
		}
	};

	/** The Fortran format tag string used to identify this descriptor (e.g. {@code "A"}, {@code "I"}, {@code "F"}). */
	private final String tag;

	/**
	 * Whether this descriptor is non-repeatable (i.e. a control descriptor that does not
	 * consume a data item from the argument list).
	 */
	private final boolean nonRepeatable;

	/**
	 * Constructs an edit descriptor constant.
	 *
	 * @param tag            the Fortran format tag string (e.g. {@code "A"}, {@code "I"})
	 * @param nonRepeatable  {@code true} if this is a non-repeatable control descriptor
	 */
	EditDescriptor(final String tag, final boolean nonRepeatable) {
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
		final var sb = new StringBuilder();
		if (s == null) {
			sb.append(" ".repeat(length));
		} else if (length == -1) {
			sb.append(s);
		} else if (s.length() > length) {
			sb.append("*".repeat(length));
		} else {
			final int dif = length - s.length();
			if (rightAligned) {
				sb.append(" ".repeat(dif));
			}
			sb.append(s);
			if (!rightAligned) {
				sb.append(" ".repeat(dif));
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
