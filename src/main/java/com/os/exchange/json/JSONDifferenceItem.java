/*
 * Copyright (c) 2009 Meding Software Technik -- All Rights Reserved.
 */

package com.os.exchange.json;


/**
 * A difference item
 * @author uwe
 */
public class JSONDifferenceItem {

	enum DiffType { ADDED, MODIFIED, REMOVED };
	
	private final DiffType type;
	private final String name;

	private JSONDifferenceItem(DiffType type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * This item was added
	 * @param name is the name of the property
	 * @return the difference item
	 */
	public static JSONDifferenceItem newAdded(String name) {
		JSONDifferenceItem diff = new JSONDifferenceItem(DiffType.ADDED, name);
		return(diff);
	}

	/**
	 * This item was modified
	 * @param name is the name of the property
	 * @return the difference item
	 */
	public static JSONDifferenceItem newModified(String name) {
		JSONDifferenceItem diff = new JSONDifferenceItem(DiffType.MODIFIED, name);
		return(diff);
	}

	/**
	 * This item was removed
	 * @param name the name of the property
	 * @return the difference object
	 */
	public static JSONDifferenceItem newRemoved(String name) {
		JSONDifferenceItem diff = new JSONDifferenceItem(DiffType.REMOVED, name);
		return(diff);
	}

	/**
	 * See if the item was added
	 * @return true or false
	 */
	public boolean wasAdded() {
		return(type == DiffType.ADDED);
	}

	/**
	 * See if the item was modified
	 * @return true or false
	 */
	public boolean wasModified() {
		return(type == DiffType.MODIFIED);
	}
	/**
	 * See if the item was deleted
	 * @return
	 */
	public boolean wasRemoved() {
		return(type == DiffType.REMOVED);
	}

	/**
	 * Get the name of the object
	 * @return the name
	 */
	public String getName() {
		return(name);
	}

	/**
	 * Get a string representation for this difference
	 * @return the string
	 */
	@Override
	public String toString() {
		return(type+": "+name);
	}
}
