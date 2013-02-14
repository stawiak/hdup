/*
 * Copyright (c) 2010 OutSmart Power Systems, Ic -- All Rights Reserved.
 */
package com.os.exchange.json;

import java.util.HashMap;
import java.util.Set;

/**
 * A JSON compatible object. This code is derived from the json.org code.
 * This is a much simpified version, much of the magical stuff has been removed.
 * @author uwe
 */
public class JSONObject extends HashMap<String, Object> implements JSONBearer {

	/**
	 * The null object
	 */
	public final static Object NULL = new Null();

	/**
	 * Create a new JSON object
	 */
	public JSONObject() {
	}

	// === GENERAL MANAGEMENT =================================================
	/**
	 * Get the length of this JSON object
	 * @return the length
	 */
	public int length() {
		return (super.size());
	}

	/**
	 * Get the set of keys in this JSON object
	 * @return
	 */
	public Set<String> keys() {
		return super.keySet();
	}

	// === OBJECT =============================================================
	/**
	 * Get an optional value associated with a key.
	 * @param key   A key string.
	 * @return      An object which is the value, or null if there is no value.
	 */
	public Object opt(String key) {
		return key == null ? null : super.get(key);
	}

	/**
	 * Get the value object associated with a key.
	 *
	 * @param key   A key string.
	 * @return      The object associated with the key.
	 * @throws   JSONException if the key is not found.
	 */
	public Object get(String key) throws JSONException {
		Object o = opt(key);
		if (o == null) {
			throw new JSONException("JSONObject[" + JSONHelper.quote(key) + "] not found.");
		}
		return o;
	}

	/**
	 * Put a key/value pair in the JSONObject. If the value is null,
	 * then the key will be removed from the JSONObject if it is present.
	 * @param key   A key string.
	 * @param value An object which is the value. It should be of one of these
	 *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
	 *  or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException If the value is non-finite number
	 *  or if the key is null.
	 */
	@Override
	public JSONObject put(String key, Object value) throws JSONException {
		if (key == null) {
			throw new JSONException("Null key.");
		}
		if (value != null) {
			JSONHelper.testValidity(value);
			super.put(key, value);
		} else {
			remove(key);
		}
		return this;
	}

	/**
	 * Put a key/value pair in the JSONObject, but only if the key and the
	 * value are both non-null, and only if there is not already a member
	 * with that name.
	 * @param key
	 * @param value
	 * @return his.
	 * @throws JSONException if the key is a duplicate
	 */
	public JSONObject putOnce(String key, Object value) throws JSONException {
		if (key != null && value != null) {
			if (opt(key) != null) {
				throw new JSONException("Duplicate key \"" + key + "\"");
			}
			put(key, value);
		}
		return this;
	}

	/**
	 * Append values to the array under a key. If the key does not exist in the
	 * JSONObject, then the key is put in the JSONObject with its value being a
	 * JSONArray containing the value parameter. If the key was already
	 * associated with a JSONArray, then the value parameter is appended to it.
	 * @param key   A key string.
	 * @param value An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException If the key is null or if the current value
	 *  associated with the key is not a JSONArray.
	 */
	public JSONObject append(String key, Object value) throws JSONException {
		JSONHelper.testValidity(value);
		Object o = opt(key);
		if (o == null) {
			put(key, new JSONArray().put(value));
		} else if (o instanceof JSONArray) {
			put(key, ((JSONArray) o).put(value));
		} else {
			throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
		}
		return this;
	}

	/**
	 * Remove a name and its value, if present.
	 * @param key The name to be removed.
	 * @return The value that was associated with the name, or null if there was no value.
	 */
	public Object remove(String key) {
		return super.remove(key);
	}
	
	/**
	 * Calculate the differences
	 * @param other is the other JSON object
	 * @return the difference object
	 */
	public JSONDifference diffTo(JSONObject other) {
		return new JSONDifference(this, other);
	}

	// === BOOLEAN ============================================================
	/**
	 * Get the boolean value associated with a key.
	 *
	 * @param key   A key string.
	 * @return      The truth.
	 * @throws   JSONException
	 *  if the value is not a Boolean or the String "true" or "false".
	 */
	public boolean getBoolean(String key) throws JSONException {
		Object o = get(key);
		if (o.equals(Boolean.FALSE) ||
				(o instanceof String &&
				((String) o).equalsIgnoreCase("false"))) {
			return false;
		} else if (o.equals(Boolean.TRUE) ||
				(o instanceof String &&
				((String) o).equalsIgnoreCase("true"))) {
			return true;
		}
		throw new JSONException("JSONObject[" + JSONHelper.quote(key) + "] is not a Boolean.");
	}

	/**
	 * Get an optional boolean associated with a key.
	 * It returns false if there is no such key, or if the value is not
	 * Boolean.TRUE or the String "true".
	 *
	 * @param key   A key string.
	 * @return      The truth.
	 */
	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}

	/**
	 * Get an optional boolean associated with a key.
	 * It returns the defaultValue if there is no such key, or if it is not
	 * a Boolean or the String "true" or "false" (case insensitive).
	 *
	 * @param key              A key string.
	 * @param defaultValue     The default.
	 * @return      The truth.
	 */
	public boolean optBoolean(String key, boolean defaultValue) {
		try {
			return getBoolean(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Put a boolean pair into the JSONObject
	 * @param key key string
	 * @param value the boolean
	 * @return this
	 * @throws JSONException when the key is null
	 */
	public JSONObject put(String key, Boolean value) throws JSONException {
		super.put(key, value);
		return this;
	}

	// === LONG ===============================================================
	/**
	 * Get the long value associated with a key. If the number value is too
	 * long for a long, it will be clipped.
	 *
	 * @param key   A key string.
	 * @return      The long value.
	 * @throws   JSONException if the key is not found or if the value cannot
	 *  be converted to a long.
	 */
	public long getLong(String key) throws JSONException {
		Object o = get(key);
		return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(key);
	}

	/**
	 * Get an optional long value associated with a key,
	 * or zero if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @return      An object which is the value.
	 */
	public long optLong(String key) {
		return optLong(key, 0);
	}

	/**
	 * Get an optional long value associated with a key,
	 * or the default if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public long optLong(String key, long defaultValue) {
		try {
			return getLong(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Store a long value
	 * @param key is key
	 * @param value is the value
	 */
	public JSONObject put(String key, Long value) throws JSONException {
		super.put(key, value);
		return (this);
	}

	// === INT ================================================================
	/**
	 * Get the int value associated with a key. If the number value is too
	 * large for an int, it will be clipped.
	 *
	 * @param key   A key string.
	 * @return      The integer value.
	 * @throws   JSONException if the key is not found or if the value cannot
	 *  be converted to an integer.
	 */
	public int getInt(String key) throws JSONException {
		Object o = get(key);
		return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(key);
	}

	/**
	 * Get an optional int value associated with a key,
	 * or zero if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @return      An object which is the value.
	 */
	public int optInt(String key) {
		return optInt(key, 0);
	}

	/**
	 * Get an optional int value associated with a key,
	 * or the default if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public int optInt(String key, int defaultValue) {
		try {
			return getInt(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Store an integer value
	 * @param key is the key
	 * @param value is the integer value
	 */
	public JSONObject put(String key, Integer value) throws JSONException {
		super.put(key, value);
		return this;
	}

	// === SHORT ==============================================================
	/**
	 * Get the int value associated with a key. If the number value is too
	 * large for an int, it will be clipped.
	 *
	 * @param key   A key string.
	 * @return      The integer value.
	 * @throws   JSONException if the key is not found or if the value cannot
	 *  be converted to an integer.
	 */
	public short getShort(String key) throws JSONException {
		Object o = get(key);
		return o instanceof Number ? ((Number) o).shortValue() : (short) getDouble(key);
	}

	public short optShort(String key) {
		return (optShort(key, (short) 0));
	}

	public short optShort(String key, short defaultValue) {
		try {
			return ((short) getInt(key));
		} catch (Exception e) {
			return (defaultValue);
		}
	}

	/**
	 * Store a short value
	 * @param key is the key
	 * @param value is the value
	 * @return this
	 * @throws JSONException
	 */
	public JSONObject put(String key, Short value) throws JSONException {
		super.put(key, value);
		return this;
	}

	// === DOUBLE =============================================================
	/**
	 * Get the double value associated with a key.
	 * @param key   A key string.
	 * @return      The numeric value.
	 * @throws JSONException if the key is not found or
	 *  if the value is not a Number object and cannot be converted to a number.
	 */
	public double getDouble(String key) throws JSONException {
		Object o = get(key);
		try {
			return o instanceof Number ? ((Number) o).doubleValue() : Double.valueOf((String) o).doubleValue();
		} catch (Exception e) {
			throw new JSONException("JSONObject[" + JSONHelper.quote(key) + "] is not a number.");
		}
	}

	/**
	 * Get an optional double associated with a key,
	 * or NaN if there is no such key or if its value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A string which is the key.
	 * @return      An object which is the value.
	 */
	public double optDouble(String key) {
		return optDouble(key, Double.NaN);
	}

	/**
	 * Get an optional double associated with a key, or the
	 * defaultValue if there is no such key or if its value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public double optDouble(String key, double defaultValue) {
		try {
			Object o = opt(key);
			return o instanceof Number ? ((Number) o).doubleValue() : new Double((String) o).doubleValue();
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Store  a double value
	 * @param key is the key
	 * @param value is the value
	 * @return this
	 */
	public JSONObject put(String key, Double value) throws JSONException {
		super.put(key, value);
		return this;
	}

	// === STRING =============================================================
	/**
	 * Get the string associated with a key.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 * @throws   JSONException if the key is not found.
	 */
	public String getString(String key) throws JSONException {
		return get(key).toString();
	}

	/**
	 * Get an optional string associated with a key.
	 * It returns an empty string if there is no such key. If the value is not
	 * a string and is not null, then it is coverted to a string.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 */
	public String optString(String key) {
		return optString(key, "");
	}

	/**
	 * Get an optional string associated with a key.
	 * It returns the defaultValue if there is no such key.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      A string which is the value.
	 */
	public String optString(String key, String defaultValue) {
		Object o = opt(key);
		return o != null ? o.toString() : defaultValue;
	}

	// === JSONObject =========================================================
	/**
	 * Get the JSONObject associated with a key.
	 *
	 * @param key   A key string.
	 * @return      a JSON object
	 * @throws   JSONException if the key is not found.
	 */
	public JSONObject getJSONObject(String key) throws JSONException {
		Object o = get(key);
		if (o instanceof JSONObject) {
			return (JSONObject) o;
		} else {
			throw new JSONException("JSONObject[" + key + "] not a JSONObject");
		}
	}

	/**
	 * Get an optional string associated with a key.
	 * It returns an empty string if there is no such key. If the value is not
	 * a string and is not null, then it is coverted to a string.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 */
	public JSONObject optJSONObject(String key) {
		Object o = opt(key);
		if (o instanceof JSONObject) {
			return (JSONObject) o;
		} else {
			return null;
		}
	}

	// === JSONArray =========================================================
	/**
	 * Get the JSONArray associated with a key.
	 *
	 * @param key   A key string.
	 * @return      a JSON object
	 * @throws   JSONException if the key is not found.
	 */
	public JSONArray getJSONArray(String key) throws JSONException {
		Object o = get(key);
		if (o instanceof JSONArray) {
			return (JSONArray) o;
		} else {
			throw new JSONException("JSONArray[" + key + "] not a JSONArray");
		}
	}

	/**
	 * Get an optional string associated with a key.
	 * It returns an empty string if there is no such key. If the value is not
	 * a string and is not null, then it is coverted to a string.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 */
	public JSONArray optJSONArray(String key) {
		Object o = opt(key);
		if (o instanceof JSONArray) {
			return (JSONArray) o;
		} else {
			return null;
		}
	}

	/**
	 * Make a JSON text of this JSONObject. For compactness, no whitespace
	 * is added. If this would not result in a syntactically correct JSON text,
	 * then null will be returned instead.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a printable, displayable, portable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 */
	@Override
	public String toString() {
		try {
			StringBuilder sb = new StringBuilder("{");
			String delim = "";
			for (String key : keys()) {
				sb.append(delim).append(JSONHelper.quote(key)).append(':').append(JSONHelper.valueToString(super.get(key)));
				delim = ",";
			}
			sb.append('}');
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * JSONObject.NULL is equivalent to the value that JavaScript calls null,
	 * whilst Java's null is equivalent to the value that JavaScript calls
	 * undefined.
	 */
	private static final class Null {

		/**
		 * There is only intended to be a single instance of the NULL object,
		 * so the clone method returns itself.
		 * @return     NULL.
		 */
		@Override
		protected final Object clone() {
			return this;
		}

		/**
		 * A Null object is equal to the null value and to itself.
		 * @param object    An object to test for nullness.
		 * @return true if the object parameter is the JSONObject.NULL object
		 *  or null.
		 */
		@Override
		public boolean equals(Object object) {
			return object == null || object == this;
		}

		/**
		 * Get the "null" string value.
		 * @return The string "null".
		 */
		@Override
		public String toString() {
			return "null";
		}
	}
}
