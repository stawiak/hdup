/*
 * Copyright (c) 2009 Meding Software Technik -- All Rights Reserved.
 */
package com.os.exchange.json;

import java.util.ArrayList;

/**
 * Calculate the difference between two JSONObject's
 * @author uwe
 */
public class JSONDifference extends ArrayList<JSONDifferenceItem> {

	/**
	 * Construct a JSON difference
	 * @param jo1 the first json object
	 * @param jo2 the second json object
	 */
	public JSONDifference(JSONObject jo1, JSONObject jo2) {

		// find the additions
		for (String name : jo1.keys()) {
			if (jo2.opt(name) == null) {
				add(JSONDifferenceItem.newAdded(name));
			}
		}

		// find the removals
		for (String name : jo2.keys()) {
			if (jo1.opt(name) == null) {
				add(JSONDifferenceItem.newRemoved(name));
			}
		}

		// find the mods
		for (String name : jo1.keys()) {
			String v1 = jo1.optString(name, null);
			String v2 = jo2.optString(name, null);
			if (v1 != null && v2 != null) {
				if (!v1.equals(v2)) {
					add(JSONDifferenceItem.newModified(name));
				}
			}
		}
	}

	/**
	 * Get a string representation of the difference list
	 * @return a string
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(JSONDifferenceItem item : this) {
			sb.append(item).append("\n");
		}
		return(sb.toString());
	}
}
