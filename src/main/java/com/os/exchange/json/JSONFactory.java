package com.os.exchange.json;

public interface JSONFactory<T> {

	/**
	 * Create a JSON object from a type
	 * @param t is the data from which to create a JSON object
	 * @return a JSON object
	 */
	JSONObject jsonObject(T t) throws JSONException;

	/**
	 * Create a JSON array from a type
	 * @param t is the data from which to create a JSON array
	 * @return the JSON array
	 * @throws JSONException
	 */
	JSONArray jsonArray(T t) throws JSONException;


	JSONObject jsonObjectFromZipped(T zipped) throws JSONException;
	JSONArray jsonArrayFromZipped(T zipped) throws JSONException;
	T toZipped(JSONBearer json);
}
