/*
 * Copyright (c) 2011 OutSmart Power Systems, Inc. -- All Rights Reserved
 */

package com.os.exchange.json;

/**
 * Interface indicates whether a class can create a JSON object.
 * @author uwe
 */
public interface JSONObjectSerializable {

	/**
	 * Get the name of the JSON object - typically the class name
	 * @return a string
	 */
	String getJSONObjectName();

	/**
	 * Serialize this class to a JSON object
	 * @return the JSON object
	 */
	JSONObject toJSON();

	/**
	 * Deserialize the object content from the JSON object
	 * @param jo
	 */
	void fromJSON(JSONObject jo);
}
