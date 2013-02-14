package com.os.exchange.json;

/*
 * Copyright (c) 2011 OutSmart Power Systems, Inc. -- All Rights Reserved.
 */

import com.os.exchange.ByteHelper;

import java.io.IOException;

/**
 * The JSON object/array factory
 * @author uwe
 */
public class DefaultJSONFactory implements JSONFactory<String> {

	private static final DefaultJSONFactory instance = new DefaultJSONFactory();

	public DefaultJSONFactory() {
	}

	/**
	 * Get an instance of this factory
	 * @return the instance
	 */
	public static DefaultJSONFactory getInstance() {
		return instance;
	}

	/**
	 * Create a JSON object from a string
	 * @param string is the JSON compatible string
	 * @return a JSON object
	 */
	public final JSONObject jsonObject(String string) throws JSONException {
		synchronized (instance) {
			JSONTokenizer t = new JSONTokenizer(string);
			JSONParser parser = new JSONParser(t);
			return parser.parseObject();
		}
	}

	/**
	 * Create a JSON array from a string
	 * @param string is a JSON compatible array string
	 * @return the JSON array
	 */
	public final JSONArray jsonArray(String string) throws JSONException {
		synchronized (instance) {
			JSONTokenizer t = new JSONTokenizer(string);
			JSONParser parser = new JSONParser(t);
			return parser.parseArray();
		}
	}

	/**
	 * Get a JSON array from a zipped/encoded string
	 * @param incoming is the incoming string
	 * @return the json array
	 */
	public final JSONArray jsonArrayFromZipped(String incoming) throws JSONException {
		try {
			byte[] incomingBuffer = ByteHelper.fromBase64String(incoming);
			byte[] unzipped = ByteHelper.gunzip(incomingBuffer);
			return jsonArray(new String(unzipped));
		} catch (IOException e) {
			throw new JSONException(e.getMessage());
		}
	}

	/**
	 * Get a JSON object from a zipped/encoded string
	 * @param incoming is the incoming string
	 * @return the json object
	 * @throws JSONException
	 */
	public final JSONObject jsonObjectFromZipped(String incoming) throws JSONException {
		try {
			byte[] incomingBuffer = ByteHelper.fromBase64String(incoming);
			byte[] unzipped = ByteHelper.gunzip(incomingBuffer);
			return jsonObject(new String(unzipped));
		} catch (IOException e) {
			throw new JSONException(e.getMessage());
		}
	}


	/**
	 * Convert a json thingy into a compressed/encoded string
	 * @param json the json thingy
	 * @return the compressed/encoded string
	 */
	public String toZipped(JSONBearer json) {
		String zippedString;
		try {
			byte[] zipped = ByteHelper.gzip(json.toString().getBytes());
			zippedString = ByteHelper.toBase64String(zipped);
		} catch (Exception e) {
			zippedString = "";
		}
		return zippedString;
	}
}
