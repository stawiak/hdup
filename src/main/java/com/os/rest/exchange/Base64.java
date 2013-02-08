package com.os.rest.exchange;

/**
 * This class provides methods for encoding and decoding data in MIME base64
 * format.
 *
 * The input to the encode methods is always a byte array.
 * Strictly speaking the output represents a sequence of characters, but
 * since these characters are from a subset of both the Unicode and ASCII
 * character repertoires, it is possible to express the output either as
 * a String or as a byte array.
 *
 * See also:
 *        RFC 2045, Multipurpose Internet Mail Extensions (MIME) Part One,
 *        Format of Internet Message Bodies,
 *        "Section 6.8 Base64 Content-Transfer-Encoding,"
 *        http://www.ietf.org/rfc/rfc2045.txt?number=2045
 *
 * @author <a href="mailto:umeding@outsmartinc.com">Uwe B. Meding</a>
 */
public class Base64 {

	/**
	 * This is the maximum number of input bytes represented on each line
	 * (not the number of output characters).
	 */
	private static final int LINEBREAK_LENGTH = 48;
	/**
	 * A static array that maps 6-bit integers to a specific char.
	 */
	protected final static char[] enc_table = {
			//   0   1   2   3   4   5   6   7
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', // 0
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', // 1
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', // 2
			'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', // 3
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', // 4
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', // 5
			'w', 'x', 'y', 'z', '0', '1', '2', '3', // 6
			'4', '5', '6', '7', '8', '9', '+', '/'  // 7
	};
	/**
	 * A static array that maps ASCII code points to a 6-bit integer,
	 * or -1 for an invalid code point.
	 */
	protected final static byte[] dec_table = {
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
			52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
			-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
			15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
			-1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
			41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	};

	/**
	 * Creates a Base64 transfer-encoding object.
	 */
	public Base64() {
	}

	/**
	 * Encodes data as a String using base64 encoding. Line breaks
	 * in the output are represented as CR LF.
	 */
	public static String encode(byte[] data) {
		return ISOLatin1.toString(encodeAsByteArray(data));
	}

	/**
	 * Encodes data as a byte array using base64 encoding. The characters
	 * 'A'-'Z', 'a'-'z', '0'-'9', '+', '/', and '=' in the output are mapped to
	 * their ASCII code points. Line breaks in the output are represented as
	 * CR LF (codes 13 and 10).
	 */
	public static byte[] encodeAsByteArray(byte[] data) {
		int i = 0, j = 0;
		int len = data.length;
		int delta = len % 3;
		int outlen = ((len + LINEBREAK_LENGTH - 1) / LINEBREAK_LENGTH) * 2 +
				((len + 2) / 3) * 4 + (len == 0 ? 2 : 0);
		byte[] output = new byte[outlen];

		byte a, b, c;
		for (int count = len / 3; count > 0; count--) {
			a = data[i++];
			b = data[i++];
			c = data[i++];
			output[j++] = (byte) (enc_table[(a >>> 2) & 0x3F]);
			output[j++] = (byte) (enc_table[((a << 4) & 0x30) + ((b >>> 4) & 0x0F)]);
			output[j++] = (byte) (enc_table[((b << 2) & 0x3C) + ((c >>> 6) & 0x03)]);
			output[j++] = (byte) (enc_table[c & 0x3F]);

			if (i % LINEBREAK_LENGTH == 0) {
				output[j++] = (byte) '\r';
				output[j++] = (byte) '\n';
			}
		}

		if (delta == 1) {
			a = data[i++];
			output[j++] = (byte) (enc_table[(a >>> 2) & 0x3F]);
			output[j++] = (byte) (enc_table[((a << 4) & 0x30)]);
			output[j++] = (byte) '=';
			output[j++] = (byte) '=';
		} else if (delta == 2) {
			a = data[i++];
			b = data[i++];
			output[j++] = (byte) (enc_table[(a >>> 2) & 0x3F]);
			output[j++] = (byte) (enc_table[((a << 4) & 0x30) + ((b >>> 4) & 0x0F)]);
			output[j++] = (byte) (enc_table[((b << 2) & 0x3C)]);
			output[j++] = (byte) '=';
		}
		if (i == 0 || i % LINEBREAK_LENGTH != 0) {
			output[j++] = (byte) '\r';
			output[j++] = (byte) '\n';
		}

		if (j != outlen) {
			throw new InternalError("Problem in base64 encoding: incorrect length calculated for base64 output");
		}

		return output;
	}

	/**
	 * Decodes a byte array containing base64-encoded ASCII. Characters with
	 * ASCII code points <= 32 (this includes whitespace and newlines) are
	 * ignored.
	 */
	public static byte[] decode(byte[] data) {
		int padCount = 0;
		int i, len = data.length;
		int real_len = 0;

		for (i = len - 1; i >= 0; --i) {
			if (data[i] > ' ') {
				real_len++;
			}

			if (data[i] == 0x3D) // ASCII '='
			{
				padCount++;
			}
		}

		if (real_len % 4 != 0) {
			throw new IllegalArgumentException("Length " + real_len + " not a multiple of 4");
		}

		int ret_len = (real_len / 4) * 3 - padCount;
		byte[] ret = new byte[ret_len];

		i = 0;
		byte[] t = new byte[4];
		int output_index = 0;
		int j = 0;
		t[0] = t[1] = t[2] = t[3] = 0x3D; // ASCII '='
		while (i < len) {
			byte c = data[i++];
			if (c > ' ') {
				t[j++] = c;
			}

			if (j == 4) {
				output_index += decode(ret, output_index, t[0], t[1], t[2], t[3]);
				j = 0;
				t[0] = t[1] = t[2] = t[3] = 0x3D; // ASCII '='
			}
		}
		if (j > 0) {
			decode(ret, output_index, t[0], t[1], t[2], t[3]);
		}

		return ret;
	}

	/**
	 * Decodes a base64-encoded String. Characters with ASCII code points <= 32
	 * (this includes whitespace and newlines) are ignored.
	 */
	public static byte[] decode(String msg) throws IllegalArgumentException {
		return decode(ISOLatin1.toByteArrayLossless(msg));
	}

	/**
	 * Given a block of 4 encoded bytes { a, b, c, d }, this method
	 * decodes up to 3 bytes of output, and stores them starting at
	 * ret[ret_offset].
	 */
	private static int decode(byte[] ret, int ret_off, byte a, byte b, byte c, byte d) {
		byte da = dec_table[a];
		byte db = dec_table[b];
		byte dc = dec_table[c];
		byte dd = dec_table[d];

		if (da == -1 || db == -1 || (dc == -1 && c != 0x3D) || (dd == -1 && d != 0x3D)) {
			throw new IllegalArgumentException("Invalid character [" +
					(a & 0xFF) + ", " + (b & 0xFF) + ", " + (c & 0xFF) + ", " + (d & 0xFF) + "]");
		}

		ret[ret_off++] = (byte) (da << 2 | db >>> 4);
		if (c == 0x3D) // ASCII '='
		{
			return 1;
		}
		ret[ret_off++] = (byte) (db << 4 | dc >>> 2);
		if (d == 0x3D) // ASCII '='
		{
			return 2;
		}
		ret[ret_off++] = (byte) (dc << 6 | dd);
		return 3;
	}
}
