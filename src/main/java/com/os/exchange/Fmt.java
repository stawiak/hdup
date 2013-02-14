package com.os.exchange;

/**
 *  Some simple single-arg sprintf-like routines. <P>
 *
 *  It is apparently impossible to declare a Java method that accepts variable
 *  numbers of any type of argument. You can declare it to take Objects, but
 *  numeric variables and constants are not in fact Objects. <P>
 *
 *  However, using the built-in string concatenation, it's almost as convenient
 *  to make a series of single-argument formatting routines. <P>
 *
 *  Fmt can format the following types: <BLOCKQUOTE><CODE>
 * byte short int long float double char String Object
 * </CODE></BLOCKQUOTE> For each type there is a set of overloaded methods, each
 *  returning a formatted String. There's the plain formatting version:
 *  <BLOCKQUOTE><PRE>
 * Fmt.fmt( x )
 * </PRE></BLOCKQUOTE> There's a version specifying a minimum field width:
 *  <BLOCKQUOTE><PRE>
 * Fmt.fmt( x, minWidth )
 * </PRE></BLOCKQUOTE> And there's a version that takes flags: <BLOCKQUOTE><PRE>
 * Fmt.fmt( x, minWidth, flags )
 * </PRE></BLOCKQUOTE> Currently available flags are: <BLOCKQUOTE><PRE>
 * Fmt.ZF - zero-fill
 * Fmt.LJ - left justify
 * Fmt.HX - hexadecimal
 * Fmt.OC - octal
 * </PRE></BLOCKQUOTE> The HX and OC flags imply unsigned output. <P>
 *
 *  For doubles and floats, there's a significant-figures parameter before the
 *  flags: <BLOCKQUOTE><PRE>
 * Fmt.fmt( d )
 * Fmt.fmt( d, minWidth )
 * Fmt.fmt( d, minWidth, sigFigs )
 * Fmt.fmt( d, minWidth, sigFigs, flags )
 * </PRE></BLOCKQUOTE> <P>
 *
 *  <A HREF="/resources/classes/Acme/Fmt.java">Fetch the software.</A> <BR>
 *  <A HREF="/resources/classes/Acme.tar.Z">Fetch the entire Acme package.</A>
 *  <HR> Similar classes:
 *  <UL>
 *    <LI> Andrew Scherpbier's <A HREF="http://www.sdsu.edu/doc/java-SDSU/sdsu.FormatString.html">
 *    FormatString</A> Tries to allow variable numbers of arguments by supplying
 *    overloaded routines with different combinations of parameters, but doesn't
 *    actually supply that many. The floating point conversion is described as
 *    "very incomplete".
 *    <LI> Core Java's <A HREF="http://www.apl.jhu.edu/~hall/java/CoreJava-Format.html">
 *    Format</A> . The design seems a little weird. They want you to create an
 *    instance, passing the format string to the constructor, and then call an
 *    instance method with your data to do the actual formatting. The extra
 *    steps are pointless; better to just use static methods.
 *  </UL>
 *
 *
 * @author     uwe
 * @created    January 22, 2007
 */
public class Fmt {

	// Flags.
	/**
	 *  Zero-fill.
	 */
	public final static int ZF = 1;
	/**
	 *  Left justify.
	 */
	public final static int LJ = 2;
	/**
	 *  Hexadecimal.
	 */
	public final static int HX = 4;
	/**
	 *  Octal.
	 */
	public final static int OC = 8;

	// Was a number - internal use.
	private final static int WN = 16;
	/**
	 *  Center (String only).
	 */
	public final static int CENTER = 32;


	/**
	 *  Format a byte
	 *
	 * @param  b  Byte to be formatted
	 * @return    a formatted string
	 */
	public static String fmt(byte b) {
		return fmt(b, 0, 0);
	}


	/**
	 *  Format a byte
	 *
	 * @param  b         Byte to be formatted
	 * @param  minWidth  Minimum width
	 * @return           a formatted string
	 */
	public static String fmt(byte b, int minWidth) {
		return fmt(b, minWidth, 0);
	}


	/**
	 *  Format a byte
	 *
	 * @param  b         Byte to be formatted
	 * @param  minWidth  Minimum width
	 * @param  flags     Formatting flags
	 * @return           a formatted string
	 */
	public static String fmt(byte b, int minWidth, int flags) {
		boolean hexadecimal = ((flags & HX) != 0);
		boolean octal = ((flags & OC) != 0);
		if(hexadecimal) {
			return fmt(Integer.toString(b & 0xff, 16), minWidth, flags | WN);
		} else if(octal) {
			return fmt(Integer.toString(b & 0xff, 8), minWidth, flags | WN);
		} else {
			return fmt(Integer.toString(b & 0xff), minWidth, flags | WN);
		}
	}


	/**
	 *  Format a short
	 *
	 * @param  s  Short value to be formatted
	 * @return    a formatted string
	 */
	public static String fmt(short s) {
		return fmt(s, 0, 0);
	}


	/**
	 *  Format a short
	 *
	 * @param  s         Short value to be formatted
	 * @param  minWidth  Minimum width
	 * @return           a formatted string
	 */
	public static String fmt(short s, int minWidth) {
		return fmt(s, minWidth, 0);
	}


	/**
	 *  Format a short
	 *
	 * @param  s         Short value to be formatted
	 * @param  minWidth  Minimum width
	 * @param  flags     Formatting flags
	 * @return           a formatted string
	 */
	public static String fmt(short s, int minWidth, int flags) {
		boolean hexadecimal = ((flags & HX) != 0);
		boolean octal = ((flags & OC) != 0);
		if(hexadecimal) {
			return fmt(
					Integer.toString(s & 0xffff, 16), minWidth, flags | WN);
		} else if(octal) {
			return fmt(
					Integer.toString(s & 0xffff, 8), minWidth, flags | WN);
		} else {
			return fmt(Integer.toString(s), minWidth, flags | WN);
		}
	}


	/**
	 *  Format a integer.
	 *
	 * @param  i  Integer to be formatted
	 * @return    a formatted string
	 */
	public static String fmt(int i) {
		return fmt(i, 0, 0);
	}


	/**
	 *  Format a integer.
	 *
	 * @param  i         Integer to be formatted
	 * @param  minWidth  Minimum width
	 * @return           a formatted string
	 */
	public static String fmt(int i, int minWidth) {
		return fmt(i, minWidth, 0);
	}


	/**
	 *  Format a integer.
	 *
	 * @param  i         Integer to be formatted
	 * @param  minWidth  Minimum width
	 * @param  flags     Formatting flags
	 * @return           a formatted string
	 */
	public static String fmt(int i, int minWidth, int flags) {
		boolean hexadecimal = ((flags & HX) != 0);
		boolean octal = ((flags & OC) != 0);
		if(hexadecimal) {
			return fmt(
					Long.toString(i & 0xffffffffL, 16), minWidth, flags | WN);
		} else if(octal) {
			return fmt(
					Long.toString(i & 0xffffffffL, 8), minWidth, flags | WN);
		} else {
			return fmt(Integer.toString(i), minWidth, flags | WN);
		}
	}


	/**
	 *  Format a long.
	 *
	 * @param  l  Long value to be formatted
	 * @return    a formatted string
	 */
	public static String fmt(long l) {
		return fmt(l, 0, 0);
	}


	/**
	 *  Format a long.
	 *
	 * @param  l         Long value to be formatted
	 * @param  minWidth  Minimum width
	 * @return           a formatted string
	 */
	public static String fmt(long l, int minWidth) {
		return fmt(l, minWidth, 0);
	}


	/**
	 *  Format a long.
	 *
	 * @param  l         Long value to be formatted
	 * @param  minWidth  Minimum width
	 * @param  flags     Formatting flags
	 * @return           a formatted string
	 */
	public static String fmt(long l, int minWidth, int flags) {
		boolean hexadecimal = ((flags & HX) != 0);
		boolean octal = ((flags & OC) != 0);
		if(hexadecimal) {
			if((l & 0xf000000000000000L) != 0) {
				return fmt(
						Long.toString(l >>> 60, 16) +
								fmt(l & 0x0fffffffffffffffL, 15, HX | ZF),
						minWidth, flags | WN);
			} else {
				return fmt(Long.toString(l, 16), minWidth, flags | WN);
			}
		} else if(octal) {
			if((l & 0x8000000000000000L) != 0) {
				return fmt(
						Long.toString(l >>> 63, 8) +
								fmt(l & 0x7fffffffffffffffL, 21, OC | ZF),
						minWidth, flags | WN);
			} else {
				return fmt(Long.toString(l, 8), minWidth, flags | WN);
			}
		} else {
			return fmt(Long.toString(l), minWidth, flags | WN);
		}
	}


	/**
	 *  Format a float.
	 *
	 * @param  f  Float value to be formatted
	 * @return    a formatted string
	 */
	public static String fmt(float f) {
		return fmt(f, 0, 0, 0);
	}


	/**
	 *  Format a float.
	 *
	 * @param  f         Float value to be formatted
	 * @param  minWidth  Minimum width
	 * @return           a formatted string
	 */
	public static String fmt(float f, int minWidth) {
		return fmt(f, minWidth, 0, 0);
	}


	/**
	 *  Format a float.
	 *
	 * @param  f         Float value to be formatted
	 * @param  minWidth  Minimum width
	 * @param  sigDigs   Short value to be formatted
	 * @return           a formatted string
	 */
	public static String fmt(float f, int minWidth, int sigDigs) {
		return fmt(f, minWidth, sigDigs, 0);
	}


	/**
	 *  Format a float.
	 *
	 * @param  f         Float value to be formatted
	 * @param  minWidth  Minimum width
	 * @param  sigDigs   Short value to be formatted
	 * @param  flags     Formatting flags
	 * @return           a formatted string
	 */
	public static String fmt(float f, int minWidth, int sigDigs, int flags) {
		return fmt(doubleToString((double) f, sigDigs), minWidth, flags | WN);
	}


	/**
	 *  Format a double.
	 *
	 * @param  d  Double value to be formatted
	 * @return    a formatted string
	 */
	public static String fmt(double d) {
		return fmt(d, 0, 16, 0);
	}


	/**
	 *  Format a double.
	 *
	 * @param  d         Double value to be formatted
	 * @param  minWidth  Minimum width
	 * @return           a formatted string
	 */
	public static String fmt(double d, int minWidth) {
		return fmt(d, minWidth, 16, 0);
	}


	/**
	 *  Format a double.
	 *
	 * @param  d         Double value to be formatted
	 * @param  minWidth  Minimum width
	 * @param  sigDigs   Short value to be formatted
	 * @return           a formatted string
	 */
	public static String fmt(double d, int minWidth, int sigDigs) {
		return fmt(d, minWidth, sigDigs, 0);
	}


	/**
	 *  Format a double.
	 *
	 * @param  d         Double value to be formatted
	 * @param  minWidth  Minimum width
	 * @param  sigDigs   Short value to be formatted
	 * @param  flags     Formatting flags
	 * @return           a formatted string
	 */
	public static String fmt(double d, int minWidth, int sigDigs, int flags) {
		return fmt(doubleToString(d, sigDigs), minWidth, flags | WN);
	}


	/**
	 *  Format a character.
	 *
	 * @param  c  Character value to be formatted
	 * @return    a formatted string
	 */
	public static String fmt(char c) {
		return fmt(c, 0, 0);
	}


	/**
	 *  Format a character.
	 *
	 * @param  c         Character value to be formatted
	 * @param  minWidth  Minimum width
	 * @return           a formatted string
	 */
	public static String fmt(char c, int minWidth) {
		return fmt(c, minWidth, 0);
	}


	/**
	 *  Format a character.
	 *
	 * @param  c         Character value to be formatted
	 * @param  minWidth  Minimum width
	 * @param  flags     Formatting flags
	 * @return           a formatted string
	 */
	public static String fmt(char c, int minWidth, int flags) {
		// Character currently lacks a static toString method.  Workaround
		// is to make a temporary instance and use the instance toString.
		return fmt(new Character(c).toString(), minWidth, flags);
	}


	/**
	 *  Format an object.
	 *
	 * @param  o  Object to be formatted
	 * @return    a formatted string
	 */
	public static String fmt(Object o) {
		return fmt(o, 0, 0);
	}


	/**
	 *  Format an object.
	 *
	 * @param  o         Object to be formatted
	 * @param  minWidth  Minimum width
	 * @return           a formatted string
	 */
	public static String fmt(Object o, int minWidth) {
		return fmt(o, minWidth, 0);
	}


	/**
	 *  Format an object.
	 *
	 * @param  o         Object to be formatted
	 * @param  minWidth  Minimum width
	 * @param  flags     Formatting flags
	 * @return           a formatted string
	 */
	public static String fmt(Object o, int minWidth, int flags) {
		return fmt(o.toString(), minWidth, flags);
	}


	/**
	 *  Format a string.
	 *
	 * @param  s  String value to be formatted
	 * @return    a formatted string
	 */
	public static String fmt(String s) {
		return fmt(s, 0, 0);
	}


	/**
	 *  Format a string.
	 *
	 * @param  s         String value to be formatted
	 * @param  minWidth  Minimum width
	 * @return           a formatted string
	 */
	public static String fmt(String s, int minWidth) {
		return fmt(s, minWidth, 0);
	}


	/**
	 *  Format a string.
	 *
	 * @param  s         String value to be formatted
	 * @param  minWidth  Minimum width
	 * @param  flags     Formatting flags
	 * @return           a formatted string
	 */
	public static String fmt(String s, int minWidth, int flags) {
		boolean zeroFill = ((flags & ZF) != 0);
		if(zeroFill) {
			return (fmt(s, minWidth, flags, '0'));
		} else {
			return (fmt(s, minWidth, flags, ' '));
		}
	}


	/**
	 *  Format a string.
	 *
	 * @param  s         String value to be formatted
	 * @param  minWidth  Minimum width
	 * @param  flags     Formatting flags
	 * @param  fillChar  Fill character
	 * @return           a formatted string
	 */
	public static String fmt(String s, int minWidth, int flags, char fillChar) {
		int len = s.length();
		boolean zeroFill = ((flags & ZF) != 0);
		boolean leftJustify = ((flags & LJ) != 0);
		boolean hexadecimal = ((flags & HX) != 0);
		boolean octal = ((flags & OC) != 0);
		boolean wasNumber = ((flags & WN) != 0);
		boolean center = ((flags & CENTER) != 0);
		if((hexadecimal || octal || zeroFill) && !wasNumber) {
			throw new InternalError("number flag on a non-number");
		}
		if(zeroFill && leftJustify) {
			throw new InternalError("zero-fill left-justify is silly");
		}
		if(hexadecimal && octal) {
			throw new InternalError("can't do both hex and octal");
		}
		if(center && leftJustify) {
			throw new InternalError("can't do both center and left-justify");
		}
		if(len >= minWidth) {
			return s;
		}
		int fillWidth = minWidth - len;
		StringBuffer fill = new StringBuffer(fillWidth);
		for(int i = 0; i < fillWidth; ++i) {
			fill.append(fillChar);
		}
		if(center) {
			int snip = fillWidth / 2;
			return (fill.substring(0, snip) + s + fill.substring(0, fillWidth - snip));
		} else if(leftJustify) {
			return s + fill;
		} else if(zeroFill && s.startsWith("-")) {
			return "-" + fill + s.substring(1);
		} else {
			return fill + s;
		}
	}



	/**
	 *  Internal routines.
	 *
	 * @param  s        the string to be formatted
	 * @param  sigFigs  sign figure
	 * @return          formatted string
	 */
	private static String sigFigFix(String s, int sigFigs) {
		// First dissect the floating-point number string into sign,
		// integer part, fraction part, and exponent.
		String sign;
		String unsigned;
		if(s.startsWith("-") || s.startsWith("+")) {
			sign = s.substring(0, 1);
			unsigned = s.substring(1);
		} else {
			sign = "";
			unsigned = s;
		}
		String mantissa;
		String exponent;
		int eInd = unsigned.indexOf('e');
		if(eInd == -1) {
			mantissa = unsigned;
			exponent = "";
		} else {
			mantissa = unsigned.substring(0, eInd);
			exponent = unsigned.substring(eInd);
		}
		StringBuffer number;
		StringBuffer fraction;
		int dotInd = mantissa.indexOf('.');
		if(dotInd == -1) {
			number = new StringBuffer(mantissa);
			fraction = new StringBuffer("");
		} else {
			number = new StringBuffer(mantissa.substring(0, dotInd));
			fraction = new StringBuffer(mantissa.substring(dotInd + 1));
		}

		int numFigs = number.length();
		int fracFigs = fraction.length();
		if((numFigs == 0 || number.equals("0")) && fracFigs > 0) {
			// Don't count leading zeros in the fraction.
			numFigs = 0;
			for(int i = 0; i < fraction.length(); ++i) {
				if(fraction.charAt(i) != '0') {
					break;
				}
				--fracFigs;
			}
		}
		int mantFigs = numFigs + fracFigs;
		if(sigFigs > mantFigs) {
			// We want more figures; just append zeros to the fraction.
			for(int i = mantFigs; i < sigFigs; ++i) {
				fraction.append('0');
			}
		} else if(sigFigs < mantFigs && sigFigs >= numFigs) {
			// Want fewer figures in the fraction; chop.
			fraction.setLength(
					fraction.length() - (fracFigs - (sigFigs - numFigs)));
		}
		// Round?
		else if(sigFigs < numFigs) {
			// Want fewer figures in the number; turn them to zeros.
			fraction.setLength(0);
			// should already be zero, but make sure
			for(int i = sigFigs; i < numFigs; ++i) {
				number.setCharAt(i, '0');
			}
			// Round?
		}
		// Else sigFigs == mantFigs, which is fine.

		if(fraction.length() == 0) {
			return sign + number + exponent;
		} else {
			return sign + number + "." + fraction + exponent;
		}
	}


	/**
	 *  Improved version of Double.toString(), returns more decimal places. <P>
	 *
	 *  The JDK 1.0.2 version of Double.toString() returns only six decimal
	 *  places on some systems. In JDK 1.1 full precision is returned on all
	 *  platforms.
	 *
	 * @param  d       Description of the Parameter
	 * @param  sigDig  Description of the Parameter
	 * @return         Description of the Return Value
	 * @deprecated
	 * @see            java.lang.Double#toString
	 */
	public static String doubleToString(double d, int sigDig) {
		// Handle special numbers first, to avoid complications.
		if(Double.isNaN(d)) {
			return "NaN";
		}
		if(d == Double.NEGATIVE_INFINITY) {
			return "-Inf";
		}
		if(d == Double.POSITIVE_INFINITY) {
			return "Inf";
		}

		// Grab the sign, and then make the number positive for simplicity.
		boolean negative = false;
		if(d < 0.0D) {
			negative = true;
			d = -d;
		}

		// Get the native version of the unsigned value, as a template.
		String unsStr = Double.toString(d);

		// Dissect out the exponent.
		String mantStr;

		// Dissect out the exponent.
		String expStr;
		int exp;
		int eInd = unsStr.indexOf('e');
		if(eInd == -1) {
			mantStr = unsStr;
			expStr = "";
			exp = 0;
		} else {
			mantStr = unsStr.substring(0, eInd);
			expStr = unsStr.substring(eInd + 1);
			if(expStr.startsWith("+")) {
				exp = Integer.parseInt(expStr.substring(1));
			} else {
				exp = Integer.parseInt(expStr);
			}
		}

		// Dissect out the number part.
		String numStr;
		int dotInd = mantStr.indexOf('.');
		if(dotInd == -1) {
			numStr = mantStr;
		} else {
			numStr = mantStr.substring(0, dotInd);
		}
		long num;
		if(numStr.length() == 0) {
			num = 0;
		} else {
			num = Integer.parseInt(numStr);
		}

		// Build the new mantissa.
		StringBuffer newMantBuf = new StringBuffer(numStr + ".");
		double p = Math.pow(10, exp);
		double frac = d - num * p;
		String digits = "0123456789";
		//int nDigits = 16 - numStr.length(); // about 16 digits in a double
		int nDigits = sigDig;
		//+ numStr.length(); // about 16 digits in a double
		for(int i = 0; i < nDigits; ++i) {
			p /= 10.0D;
			int dig = (int) (frac / p);
			if(dig < 0) {
				dig = 0;
			}
			if(dig > 9) {
				dig = 9;
			}
			newMantBuf.append(digits.charAt(dig));
			frac -= dig * p;
		}

		if((int) (frac / p + 0.5D) == 1) {
			// Round up.
			boolean roundMore = true;
			do {
				for(int i = newMantBuf.length() - 1; i >= 0; --i) {
					int dig = digits.indexOf(newMantBuf.charAt(i));
					if(dig == -1) {
						continue;
					}
					++dig;
					if(dig == 10) {
						newMantBuf.setCharAt(i, '0');
						continue;
					}
					newMantBuf.setCharAt(i, digits.charAt(dig));
					roundMore = false;
					break;
				}
				if(roundMore) {
					// If this happens, we need to prepend a 1.  But I haven't
					// found a test case yet, so I'm leaving it out for now.
					// But if you get this message, please let me know!
					//newMantBuf.append("ROUNDMORE");
					newMantBuf.insert(0, '1');
				}
			} while (roundMore);
		}

		// Chop any trailing zeros.
		int len = newMantBuf.length();
		while(newMantBuf.charAt(len - 1) == '0') {
			newMantBuf.setLength(--len);
		}
		// And chop a trailing dot, if any.
		if(newMantBuf.charAt(len - 1) == '.') {
			newMantBuf.setLength(--len);
		}

		// Done.
		return (negative ? "-" : "") +
				newMantBuf +
				(expStr.length() != 0 ? ("e" + expStr) : "");
	}

}
