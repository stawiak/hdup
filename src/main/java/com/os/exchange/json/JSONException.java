/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.os.exchange.json;



/**
 * A JSON exception, we get this is case stuff in seriously bad.
 *
 * @author uwe
 */
public class JSONException extends RuntimeException {

	private Throwable t;

	/**
	 * Construct an exception from a string
	 * @param message is the string
	 */
	public JSONException(String message) {
		super(message);
	}

	/**
	 * Construct an exception from an exception
	 * @param t is the throwable
	 */
	public JSONException(Throwable t) {
		this.t = t;
	}

	/**
	 * Get the cause of the exception
	 * @return the cause
	 */
	@Override
	public Throwable getCause() {
		return t==null ? super.getCause() : t.getCause();
	}
}
