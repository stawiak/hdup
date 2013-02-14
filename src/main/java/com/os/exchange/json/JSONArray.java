/*
 * Copyright (c) 2011 OutSmart Power Systems, Inc. -- All Rights Reserved.
 */
package com.os.exchange.json;

import java.util.ArrayList;

/**
 * The JSON array.
 *
 * @author uwe
 */
public class JSONArray extends ArrayList<Object> implements JSONBearer {

	public JSONArray() {
	}

	/**
	 * Get the length of the array
	 * @return the length
	 */
	public int length() {
		return super.size();
	}

	/**
	 * Get the optional value associated with an index
	 * @param index is the index
	 * @return the object
	 */
	public Object opt(int index) {
		if (index < 0 || index >= size()) {
			return null;
		} else {
			return super.get(index);
		}
	}

	/**
	 * Get the value at a particular index. 
	 * @param index of the value
	 * @return the object
	 * @throws JSONException If there is no value at index
	 */
	@Override
	public Object get(int index) throws JSONException {
		Object o = opt(index);
		if (o == null) {
			throw new JSONException("JSONArray[" + index + "] not found");
		}
		return o;
	}

	/**
	 * Add an object to the array
	 * @param o is the object
	 * @return the array
	 */
	public JSONArray put(Object o) {
		super.add(o);
		return this;
	}

	// === BOOLEAN ============================================================
	/**
	 * Get an optional value at a particular index
	 * @param index is the index
	 * @return the value
	 */
	public Boolean optBoolean(int index) {
		Object o = opt(index);
		if (o == null) {
			return null;
		}
		if (o instanceof Boolean) {
			return (Boolean) o;
		}
		if (o instanceof String) {
			if (((String) o).equalsIgnoreCase("false")) {
				return Boolean.FALSE;
			} else if (((String) o).equalsIgnoreCase("true")) {
				return Boolean.TRUE;
			}
		}
		return null;
	}

	/**
	 * Get an optional boolean value at a particular index
	 * @param index is the index
	 * @param defValue is the default value
	 * @return the boolean
	 */
	public Boolean optBoolean(int index, Boolean defValue) {
		Boolean value = optBoolean(index);
		return value == null ? defValue : value;
	}

	/**
	 * Get a boolean at a particular index
	 * @param index is the index
	 * @return the boolean
	 */
	public Boolean getBoolean(int index) throws JSONException {
		Boolean value = optBoolean(index);
		if (value == null) {
			throw new JSONException("JSONArray[" + index + "] is not a Boolean");
		}
		return value;
	}

	/**
	 * Put a boolean value into the array
	 * @param value the value
	 * @return the array
	 */
	public JSONArray putBoolean(Boolean value) {
		return put(value);
	}

	/**
	 * Put a boolean value into the array
	 * @param value the value
	 * @return the array
	 */
	public JSONArray put(Boolean value) {
		super.add(value);
		return this;
	}

	// === LONG ===============================================================
	/*
	 * Get an optional long value
	 * @param index is the index position
	 * @return the long value
	 */
	public Long optLong(int index) {
		Object o = opt(index);
		if (o == null) {
			return null;
		}
		if (o instanceof Number) {
			return ((Number) o).longValue();
		} else {
			Double d = optDouble(index);
			if (d == null) {
				return null;
			} else {
				return d.longValue();
			}
		}
	}

	/**
	 * Get an optional long value
	 * @param index is the index
	 * @param defValue is the default value
	 */
	public Long optLong(int index, Long defValue) {
		Long value = optLong(index);
		return value == null ? defValue : value;
	}

	/**
	 * Get a long value
	 * @param index is the index in the array
	 * @return the long value
	 */
	public Long getLong(int index) throws JSONException {
		Long value = optLong(index);
		if (value == null) {
			throw new JSONException("JSONArray[" + index + "] is not a long");
		}
		return value;
	}

	/**
	 * Put a long value into the array
	 * @param value is the long value
	 * @return the array
	 */
	public JSONArray putLong(Long value) {
		return put(value);
	}

	/**
	 * Put a long value into the array
	 * @param value is the long value
	 * @return the array
	 */
	public JSONArray put(Long value) {
		super.add(value);
		return this;
	}

	// === INTEGER ============================================================
	/*
	 * Get an optional long value
	 * @param index is the index position
	 * @return the long value
	 */
	public Integer optInt(int index) {
		Object o = opt(index);
		if (o == null) {
			return null;
		}
		if (o instanceof Number) {
			return ((Number) o).intValue();
		} else {
			Double d = optDouble(index);
			if (d == null) {
				return null;
			} else {
				return d.intValue();
			}
		}
	}

	/**
	 * Get an optional long value
	 * @param index is the index
	 * @param defValue is the default value
	 */
	public Integer optInt(int index, Integer defValue) {
		Integer value = optInt(index);
		return value == null ? defValue : value;
	}

	/**
	 * Get a long value
	 * @param index is the index in the array
	 * @return the long value
	 */
	public Integer getInt(int index) throws JSONException {
		Integer value = optInt(index);
		if (value == null) {
			throw new JSONException("JSONArray[" + index + "] is not a long");
		}
		return value;
	}

	/**
	 * Put an integer into the array
	 * @param value is the value
	 * @return the array
	 */
	public JSONArray putInt(Integer value) {
		return put(value);
	}

	/**
	 * Put an integer into the array
	 * @param value is the value
	 * @return the array
	 */
	public JSONArray put(Integer value) {
		super.add(value);
		return this;
	}

	// === DOUBLE =============================================================
	/**
	 * Get an optional double value
	 * @param index is the index
	 * @return the double value
	 */
	public Double optDouble(int index) {
		Object o = opt(index);
		if (o == null) {
			return null;
		}
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		} else {
			return Double.valueOf(o.toString());
		}
	}

	public Double optDouble(int index, Double defValue) {
		Double value = optDouble(index);
		if (value == null) {
			return defValue;
		} else {
			return (value);
		}
	}

	public Double getDouble(int index) throws JSONException {
		Double value = getDouble(index);
		if (value == null) {
			throw new JSONException("JSONArray[" + index + "] is not a double");
		} else {
			return value;
		}
	}

	/**
	 * Put a double value in the array
	 * @param value is the double value
	 * @return the array
	 */
	public JSONArray putDouble(Double value) {
		return put(value);
	}

	/**
	 * Put a double value in the array
	 * @param value is the double value
	 * @return the array
	 */
	public JSONArray put(Double value) {
		super.add(value);
		return this;
	}
	// === STRING =============================================================

	/**
	 * Get an optional string
	 * @param index index of desired position
	 * @return the string
	 */
	public String optString(int index) {
		Object o = opt(index);
		return o == null ? null : o.toString();
	}

	/**
	 * Get an optional string
	 * @param index the index into the array
	 * @param defValue the deffault value
	 * @return the string
	 */
	public String optString(int index, String defValue) {
		String value = optString(index);
		return value == null ? defValue : value;
	}

	public String getString(int index) throws JSONException {
		String value = optString(index);
		if (value == null) {
			throw new JSONException("JSONArray[" + index + "] is not a string");
		} else {
			return value;
		}
	}

	/**
	 * Put a string into the array
	 * @param value the string value
	 * @return the array
	 */
	public JSONArray putString(String value) {
		return put(value);
	}

	/**
	 * Put a string into the array
	 * @param value the string value
	 * @return the array
	 */
	public JSONArray put(String value) {
		super.add(value);
		return this;
	}

	// === JSONObject =========================================================
	/**
	 * Get an optional JSON object
	 * @param index is the index in the array
	 * @return the JSON object
	 */
	public JSONObject optJSONObject(int index) {
		Object o = get(index);
		return o instanceof JSONObject ? (JSONObject) o : null;
	}

	/**
	 * Get a JSON object
	 * @param index in the array
	 * @return the JSON object
	 */
	public JSONObject getJSONObject(int index) {
		JSONObject value = optJSONObject(index);
		if (value == null) {
			throw new JSONException("JSONArray[" + index + "] is not a JSON object");
		}
		return value;
	}

	/**
	 * Add a JSON object to the array
	 * @param value is the value
	 * @return the array
	 */
	public JSONArray putJSONObject(JSONObject value) {
		return put(value);
	}

	/**
	 * Add a JSON object to the array
	 * @param value is the value
	 * @return the array
	 */
	public JSONArray put(JSONObject value) {
		super.add(value);
		return this;
	}

	// === JSONArray ==========================================================
	/**
	 * Get an optional JSON object
	 * @param index is the index in the array
	 * @return the JSON object
	 */
	public JSONArray optJSONArray(int index) {
		Object o = get(index);
		return o instanceof JSONArray ? (JSONArray) o : null;
	}

	/**
	 * Get a JSON object
	 * @param index in the array
	 * @return the JSON object
	 */
	public JSONArray getJSONArray(int index) {
		JSONArray value = optJSONArray(index);
		if (value == null) {
			throw new JSONException("JSONArray[" + index + "] is not a JSON object");
		}
		return value;
	}

	/**
	 * Add a JSON object to the array
	 * @param value is the value
	 * @return the array
	 */
	public JSONArray putJSONArray(JSONArray value) {
		return put(value);
	}

	/**
	 * Add a JSON object to the array
	 * @param value is the value
	 * @return the array
	 */
	public JSONArray put(JSONArray value) {
		super.add(value);
		return this;
	}

	/**
	 * Create a JSON compatible string.
	 * @return a string
	 */
	@Override
	public String toString() {
		try {
			StringBuilder sb = new StringBuilder("[");
			String delim = "";
			for (Object value : this) {
				sb.append(delim).append(JSONHelper.valueToString(value));
				delim = ",";
			}
			sb.append(']');
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

}
