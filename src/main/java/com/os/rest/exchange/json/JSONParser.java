package com.os.rest.exchange.json;

/**
 * The JSON parser.
 *
 * @author uwe
 */
public class JSONParser {

	private final JSONTokenizer tokenizer;

	public JSONParser(JSONTokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	/**
	 * Parse the token stream for the JSON object
	 * @return the JSON object
	 * @throws JSONException
	 */
	public final JSONObject parseObject() throws JSONException {
		JSONObject jo = new JSONObject();
		char c;
		String key;

		if (tokenizer.nextClean() != '{') {
			throw tokenizer.syntaxError("A JSONObject text must begin with '{'");
		}
		for (;;) {
			c = tokenizer.nextClean();
			switch (c) {
				case 0:
					throw tokenizer.syntaxError("A JSONObject text must end with '}'");
				case '}':
					return jo;
				default:
					tokenizer.back();
					key = tokenizer.nextValue().toString();
			}

			/*
			 * The key is followed by ':'. We will also tolerate '=' or '=>'.
			 */

			c = tokenizer.nextClean();
			if (c == '=') {
				if (tokenizer.next() != '>') {
					tokenizer.back();
				}
			} else if (c != ':') {
				throw tokenizer.syntaxError("Expected a ':' after a key");
			}
			jo.putOnce(key, tokenizer.nextValue());

			/*
			 * Pairs are separated by ','. We will also tolerate ';'.
			 */

			switch (tokenizer.nextClean()) {
				case ';':
				case ',':
					if (tokenizer.nextClean() == '}') {
						return jo;
					}
					tokenizer.back();
					break;
				case '}':
					return jo;
				default:
					throw tokenizer.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	/**
	 * Parse the token stream for an array
	 * @return the JSON array
	 * @throws JSONException
	 */
	public final JSONArray parseArray() throws JSONException {
		JSONArray ja = new JSONArray();
		char c = tokenizer.nextClean();
		char q;
		if (c == '[') {
			q = ']';
		} else if (c == '(') {
			q = ')';
		} else {
			throw tokenizer.syntaxError("A JSONArray text must start with '['");
		}
		if (tokenizer.nextClean() == ']') {
			return ja;
		}
		tokenizer.back();
		for (;;) {
			if (tokenizer.nextClean() == ',') {
				tokenizer.back();
				ja.add(null);
			} else {
				tokenizer.back();
				ja.add(tokenizer.nextValue());
			}
			c = tokenizer.nextClean();
			switch (c) {
				case ';':
				case ',':
					if (tokenizer.nextClean() == ']') {
						return ja;
					}
					tokenizer.back();
					break;
				case ']':
				case ')':
					if (q != c) {
						throw tokenizer.syntaxError("Expected a '" + new Character(q) + "'");
					}
					return ja;
				default:
					throw tokenizer.syntaxError("Expected a ',' or ']'");
			}
		}
	}
}
