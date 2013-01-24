/*
 * Copyright (c) 2011 OutSmart Power Systems, Inc. -- All Rights Reserved.
 */
package com.os.rest.exchange.json.jsonClasses;

import com.os.rest.exchange.json.*;

/**
 * JSON helper functions.
 *
 * @author uwe
 */
public class JSONHelper {

	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, allowing JSON
	 * text to be delivered in HTML. In JSON text, a string cannot contain a
	 * control character or an unescaped quote or backslash.
	 * @param string A String
	 * @return  A String correctly formatted for insertion in a JSON text.
	 */
	public static final String quote(String string) {
		if (string == null || string.length() == 0) {
			return "\"\"";
		}

		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
				case '\\':
				case '"':
					sb.append('\\');
					sb.append(c);
					break;
				case '/':
					if (b == '<') {
						sb.append('\\');
					}
					sb.append(c);
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					if (c < ' ' || (c >= '\u0080' && c < '\u00a0') ||
							(c >= '\u2000' && c < '\u2100')) {
						t = "000" + Integer.toHexString(c);
						sb.append("\\u" + t.substring(t.length() - 4));
					} else {
						sb.append(c);
					}
			}
		}
		sb.append('"');
		return sb.toString();
	}

	/**
	 * Throw an exception if the object is an NaN or infinite number.
	 * @param o The object to test.
	 * @throws JSONException If o is a non-finite number.
	 */
	public final static void testValidity(Object o) throws JSONException {
		if (o != null) {
			if (o instanceof Double) {
				if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
					throw new JSONException(
							"JSON does not allow non-finite numbers.");
				}
			} else if (o instanceof Float) {
				if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
					throw new JSONException(
							"JSON does not allow non-finite numbers.");
				}
			}
		}
	}

	/**
	 * Make a JSON text of an Object value. If the object has an
	 * value.toJSONString() method, then that method will be used to produce
	 * the JSON text. The method is required to produce a strictly
	 * conforming text. If the object does not contain a toJSONString
	 * method (which is the most common case), then a text will be
	 * produced by other means. If the value is an array or Collection,
	 * then a JSONArray will be made from it and its toJSONString method
	 * will be called. If the value is a MAP, then a JSONObject will be made
	 * from it and its toJSONString method will be called. Otherwise, the
	 * value's toString method will be called, and the result will be quoted.
	 *
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param value The value to be serialized.
	 * @return a printable, displayable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException If the value is or contains an invalid number.
	 */
	public final static String valueToString(Object value) throws JSONException {
		if (value == null || value.equals(null)) {
			return "null";
		}
		if (value instanceof JSONString) {
			Object o;
			try {
				o = ((JSONString) value).toJSONString();
			} catch (Exception e) {
				throw new JSONException(e);
			}
			if (o instanceof String) {
				return (String) o;
			}
			throw new JSONException("Bad value from toJSONString: " + o);
		}
		if (value instanceof Number) {
			return numberToString((Number) value);
		}
		if (value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray) {
			return value.toString();
		}
		if (value instanceof JSONBearer) {
			return (value.toString());
		}
		return quote(value.toString());
	}

	/**
	 * Produce a string from a Number.
	 * @param  n A Number
	 * @return A String.
	 * @throws JSONException If n is a non-finite number.
	 */
	public final static String numberToString(Number n) throws JSONException {
		if (n == null) {
			throw new JSONException("Null pointer");
		}
		testValidity(n);

// Shave off trailing zeros and decimal point, if possible.

		String s = n.toString();
		if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
			while (s.endsWith("0")) {
				s = s.substring(0, s.length() - 1);
			}
			if (s.endsWith(".")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}

	/**
	 * Try to convert a string into a number, boolean, or null. If the string
	 * can't be converted, return the string.
	 * @param s A String.
	 * @return A simple JSON value.
	 */
	static public Object stringToValue(String s) {
		if (s.equals("")) {
			return s;
		}
		if (s.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if (s.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}
		if (s.equalsIgnoreCase("null")) {
			return JSONObject.NULL;
		}

		/*
		 * If it might be a number, try converting it. We support the 0- and 0x-
		 * conventions. If a number cannot be produced, then the value will just
		 * be a string. Note that the 0-, 0x-, plus, and implied string
		 * conventions are non-standard. A JSON parser is free to accept
		 * non-JSON forms as long as it accepts all correct JSON forms.
		 */

		char b = s.charAt(0);
		if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
			if (b == '0') {
				if (s.length() > 2 &&
						(s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
					try {
						return new Integer(Integer.parseInt(s.substring(2),
								16));
					} catch (Exception e) {
						/* Ignore the error */
					}
				} else {
					try {
						return new Integer(Integer.parseInt(s, 8));
					} catch (Exception e) {
						/* Ignore the error */
					}
				}
			}
			try {
				return new Integer(s);
			} catch (Exception e) {
				try {
					return new Long(s);
				} catch (Exception f) {
					try {
						return new Double(s);
					} catch (Exception g) {
						/* Ignore the error */
					}
				}
			}
		}
		return s;
	}
}
