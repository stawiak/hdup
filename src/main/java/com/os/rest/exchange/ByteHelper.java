package com.os.rest.exchange;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Helper functions for bytes, byte arrays, and byte conversions.
 *
 * @author @author <a href="mailto:umeding@outsmartinc.com">Uwe B. Meding</a>
 */
public class ByteHelper {

	// Constants
	public static final byte[] BIT_VALUES = new byte[]{
			0, // 0000 0000
			1, // 0000 0001
			3, // 0000 0011
			7, // 0000 0111
			15, // 0000 1111
			31, // 0001 1111
			63, // 0011 1111
			127 // 0111 1111
	};
	public static final int BINARY_1 = 1;	// 00000001
	public static final int BINARY_2 = 2;	// 00000001
	private static final boolean DEBUG = false;
	private static final int MAX_BODY_RAM = 2097152; // 2MB

	/**
	 * Convert an unsigned byte to an int
	 */
	public static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	/**
	 * Convert a signed byte to an int
	 */
	public static int signedByteToInt(byte b) {
		return (int) b;
	}

	/**
	 * Converts a byte array that represents an unsigned 8-bit
	 * integer
	 */
	public static int getUInt8(byte b) {
		int value = b & 0xff;
		return value;
	}

	/**
	 * Converts a byte array that represents an unsigned 8-bit
	 * integer
	 */
	public static int getUInt8(byte[] bytes) {
		if (bytes.length != 1) {
			throw new IllegalArgumentException("Must be 1 byte");
		}
		int value = bytes[0] & 0xff;
		return value;
	}

	/**
	 * Converts a byte array that represents an unsigned 16-bit
	 * integer
	 */
	public static int getUInt16(byte[] bytes) {
		if (bytes.length != 2) {
			throw new IllegalArgumentException("Must be 2 bytes");
		}
		int value = bytes[0] & 0xff << 8;
		value |= bytes[1] & 0xff;
		return value;
	}

	/**
	 * Converts a byte array that represents an unsigned 32-bit
	 * integer
	 */
	public static long getUInt32(byte[] bytes) {
		if (bytes.length != 4) {
			throw new IllegalArgumentException("Must be 4 bytes");
		}
		long value = bytes[3] & 0xFF;
		value |= ((bytes[2] << 8) & 0xFF00);
		value |= ((bytes[1] << 16) & 0xFF0000);
		value |= (((bytes[0] & 0x7F) << 24) & 0xFF000000);
		if ((bytes[0] & 0x80) != 0) {
			value += 2147483647;
			value += 1;
		}
		if (value < 0) {
			throw new IllegalArgumentException("getUInt32 tried to return " + value);
		}
		return value;

	}

	/**
	 * Converts a byte array that represents an signed 32-bit
	 * integer
	 */
	public static int getInt32(byte[] bytes) {
		if (bytes.length != 4) {
			throw new IllegalArgumentException("Must be 4 bytes");
		}
		int value = bytes[3] & 0xFF;
		value |= ((bytes[2] << 8) & 0xFF00);
		value |= ((bytes[1] << 16) & 0xFF0000);
		value |= ((bytes[0] << 24) & 0xFF000000);
		return value;
	}

	/**
	 * Converts a 32-bit signed int to a 4-byte array
	 */
	public static byte[] fromInt32(int value) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((value >>> 24) & 0xFF);
		bytes[1] = (byte) ((value >>> 16) & 0xFF);
		bytes[2] = (byte) ((value >>> 8) & 0xFF);
		bytes[3] = (byte) (value & 0xFF);
		return bytes;
	}

	/**
	 * Converts a 32-bit unsigned int to a 4-byte array
	 */
	public static byte[] fromUInt32(long value) {
		if (value > 4294967295l) {
			throw new IllegalArgumentException("Must be less than 2^32");
		}
		if (value < 0) {
			throw new IllegalArgumentException("Must be greater than 0");
		}
		byte[] bytes = new byte[4];
		bytes[3] = (byte) (value & 0xff);
		bytes[2] = (byte) ((value >> 8) & 0xff);
		bytes[1] = (byte) ((value >> 16) & 0xff);
		bytes[0] = (byte) ((value >> 24) & 0xff);
		return bytes;
	}

	/**
	 * Converts a 64-bit long into an 8-byte array
	 */
	public static byte[] fromLong(long value) {
		byte[] bytes = new byte[8];
		bytes[0] = (byte) ((value >>> 56) & 0xff);
		bytes[1] = (byte) ((value >>> 48) & 0xff);
		bytes[2] = (byte) ((value >>> 40) & 0xff);
		bytes[3] = (byte) ((value >>> 32) & 0xff);
		bytes[4] = (byte) ((value >>> 24) & 0xff);
		bytes[5] = (byte) ((value >>> 16) & 0xff);
		bytes[6] = (byte) ((value >>> 8) & 0xff);
		bytes[7] = (byte) ((value) & 0xff);
		return bytes;
	}

	/**
	 * Converts a byte array that represents an signed 32-bit
	 * integer
	 */
	public static long toLong(byte[] bytes) {
		if (bytes.length != 8) {
			throw new IllegalArgumentException("Must be 8 bytes");
		}

		long value1 = bytes[3] & 0xFF;
		value1 |= ((bytes[2] << 8) & 0xFF00);
		value1 |= ((bytes[1] << 16) & 0xFF0000);
		value1 |= ((bytes[0] << 24) & 0xFF000000);

		long value2 = bytes[7] & 0xFF;
		value2 |= ((bytes[6] << 8) & 0xFF00);
		value2 |= ((bytes[5] << 16) & 0xFF0000);
		value2 |= ((bytes[4] << 24) & 0xFF000000);
		return ((long) (value1 << 32) + (value2 & 0xFFFFFFFFL));

	}

	/**
	 * Converts a 16-bit unsigned int to a 2-byte array
	 */
	public static byte[] fromUInt16(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("Must be less greater than 0");
		}
		if (value > 65355) {
			throw new IllegalArgumentException("Must be less than 2^32");
		}
		byte[] bytes = new byte[2];
		bytes[0] = (byte) ((value >>> 8) & 0xFF);
		bytes[1] = (byte) (value & 0xFF);
		return bytes;
	}

	/**
	 * Converts a 8-bit unsigned int to a byte array
	 */
	public static byte[] fromUInt8(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("Must be less greater than 0");
		}
		return new byte[]{(byte) (value & 0xFF)};
	}

	/**
	 * Converts a byte array into an octal String like:
	 * \003\005\...
	 */
	public static String getOctalString(byte[] bytes) {

		if (null == bytes) {
			return "";
		}
		if (bytes.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(4 * bytes.length);

		// Convert each byte into an octal integer
		for (int i = 0; i < bytes.length; i++) {
			sb.append("\\");
			int intValue = unsignedByteToInt(bytes[i]);
			String s = Integer.toOctalString(intValue);
			// Pad zeros as necessary
			int zeros = 3 - s.length();
			for (int j = 0; j < zeros; j++) {
				sb.append("0");
			}

			// Add the rest of the number
			sb.append(s);
		}

		return sb.toString();

	}

	/**
	 * Converts a byte array that represents a boolean
	 */
	public static boolean getBoolean(byte[] bytes) {
		if (bytes.length != 1) {
			throw new IllegalArgumentException("Must be 1 byte");
		}
		if (bytes[0] == 0x00) {
			return false;
		}
		return true;
	}

	/**
	 * Converts a boolean to a 1-byte array
	 */
	public static byte[] fromBoolean(boolean value) {
		byte[] bytes = new byte[1];
		if (value) {
			bytes[0] = 0x01;
		} else {
			bytes[0] = 0x00;
		}
		return bytes;
	}

	/**
	 * Converts a byte array to ascii printable hex string
	 */
	public static String getHexString(byte[] bytes, String delim) {
		StringBuilder sb = new StringBuilder(bytes.length);
		String d = "";
		for (int i = 0; i < bytes.length; i++) {
			byte b = (byte) (bytes[i] & 0xff);
			sb.append(d).append(Fmt.fmt(b, 2, Fmt.HX | Fmt.ZF));
			d = delim;
		}
		return sb.toString();
	}

	public static String getHexString(byte[] bytes) {
		return (getHexString(bytes, ":"));
	}

	/**
	 * Converts a byte array to ascii printable decimal string
	 */
	public static String getDecimalString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			int ival = (int) bytes[i] & 0xff;
			sb.append(Integer.toString(ival));
			if ((i + 1) < bytes.length) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * Converts comma separated decimal values into a byte array.
	 */
	public static byte[] fromDecimalString(String s) {
		if (null == s) {
			return new byte[0];
		}
		try {
			// s = s.replaceAll("-",",");
			// s = s.replaceAll(":",",");
			String[] parts = s.split(",");
			byte[] bytes = new byte[parts.length];
			for (int i = 0; i < parts.length; i++) {
				int value = Integer.parseInt(parts[i], 10);
				bytes[i] = (byte) (value & 0xFF);
			}
			return bytes;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid decimal String: " + s);
		}
	}

	/**
	 * Converts a String to a byte array using ASCII translation. This
	 * method allocates a single byte per character so it is NOT
	 * possible to encode Unicode or non-ASCII characters!
	 */
	public static byte[] fromHexString(String s) {

		if (null == s) {
			return new byte[0];
		}

		// remove wsp
		s = s.replaceAll("\\s", "");

		// look for tokens
		boolean tokenized = true;

		if (s.contains("-")) {
			// default
		} else if (s.contains(",")) {
			s = s.replaceAll(",", "-");
		} else if (s.contains(".")) {
			s = s.replaceAll("\\.", "-");
		} else if (s.contains(":")) {
			s = s.replaceAll(":", "-");
		} else {
			tokenized = false;
		}

		try {

			if (!tokenized) {
				int charLength = s.length();
				if (charLength % 2 == 1) {
					throw new IllegalArgumentException("must pass in even number of letters");
				}
				int byteLength = charLength / 2;
				byte[] bytes = new byte[byteLength];
				for (int i = 0; i < byteLength; i++) {

					int pos = 2 * i;
					StringBuilder sb = new StringBuilder(2);
					sb.append(s.charAt(pos++));
					sb.append(s.charAt(pos));
					String octet = sb.toString();
					// System.out.println( "Octet " + i + ": " + octet );
					int value = Integer.parseInt(octet, 16);
					bytes[i] = (byte) (value & 0xFF);

				}
				return bytes;

			} else {

				String[] parts = s.split("-");
				byte[] bytes = new byte[parts.length];
				for (int i = 0; i < parts.length; i++) {
					int value = Integer.parseInt(parts[i], 16);
					bytes[i] = (byte) (value & 0xFF);
				}
				return bytes;

			}

		} catch (Exception e) {

			throw new IllegalArgumentException("Invalid Hex String: " + s);

		}

	}


	/**
	 * Converts a byte array that represents a String
	 * Assumes ASCII translation.
	 */
	public static String getString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0) {
				continue;
			}
			char c = (char) bytes[i];
			if (Character.isISOControl(c)) {
				sb.append(".");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Search the data byte array for the first occurrence
	 * of the byte array pattern. This implements the
	 * Knuth-Morris-Pratt pattern matching algorithm.
	 */
	public static int indexOf(byte[] data, byte[] pattern) {
		int[] failure = computeFailure(pattern);

		int j = 0;

		for (int i = 0; i < data.length; i++) {
			while (j > 0 && pattern[j] != data[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == data[i]) {
				j++;
			}
			if (j == pattern.length) {
				return i - pattern.length + 1;
			}
		}
		return -1;
	}

	/**
	 * Computes the failure function using a boot-strapping process,
	 * where the pattern is matched against itself.
	 */
	private static int[] computeFailure(byte[] pattern) {
		int[] failure = new int[pattern.length];

		int j = 0;
		for (int i = 1; i < pattern.length; i++) {
			while (j > 0 && pattern[j] != pattern[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == pattern[i]) {
				j++;
			}
			failure[i] = j;
		}

		return failure;
	}

	/**
	 * Concatenate the buffer into one buffer
	 * @param buffers are the buffers
	 * @return the mega buffer
	 */
	public static byte[] concatenate(byte[]... buffers) {
		int total = 0;
		for (byte[] buffer : buffers) {
			total += buffer.length;
		}

		int pos = 0;
		byte[] megaBuffer = new byte[total];
		for (byte[] buffer : buffers) {
			System.arraycopy(buffer, 0, megaBuffer, pos, buffer.length);
			pos += buffer.length;
		}
		return megaBuffer;
	}

	/**
	 * Returns a new byte array of the specified lenght using the contents
	 * provided. Pads and truncates as necessary.
	 */
	public static byte[] resize(byte[] data, int length) {
		byte[] newArray = new byte[length];
		int copyLength = Math.min(length, data.length);
		System.arraycopy(data, 0, newArray, 0, copyLength);
		return newArray;
	}

	/**
	 * Converts a String to a byte array using ASCII translation. This
	 * method allocates a single byte per character so it is NOT
	 * possible to encode Unicode or non-ASCII characters!
	 */
	public static byte[] fromString(String s) {
		if (null == s) {
			return new byte[0];
		}
		try {
			return s.getBytes("US-ASCII");
		} catch (java.io.UnsupportedEncodingException ioe) {
			return s.getBytes();
		}
	}

	/**
	 * Converts a byte array that represents an IP Address
	 */
	public static InetAddress getInetAddress(byte[] bytes) {
		try {
			return InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid IP bytes");
		}
	}

	/**
	 * Converts an InetAddress to a byte Array.
	 * IPv4 Addresses producte 4 bytes. IPv6 Addresses produce 16 bytes.
	 */
	public static byte[] fromInetAddress(InetAddress addr) {
		return addr.getAddress();
		/*
		if ( addr instanceof Inet4Address ) {
		Inet4Address ipv4 = (Inet4Address)addr;
		return ipv4.getAddress();
		}
		if ( addr instanceof Inet6Address ) {
		Inet6Address ipv6 = (Inet6Address)addr;
		return ipv6.getAddress();
		}
		 */
	}

	/**
	 * Print a byte array as a String
	 */
	public static String toString(byte[] bytes) {

		StringBuilder sb = new StringBuilder(4 * bytes.length);
		sb.append("[");

		for (int i = 0; i < bytes.length; i++) {
			sb.append(unsignedByteToInt(bytes[i]));
			if (i + 1 < bytes.length) {
				sb.append(",");
			}
		}

		sb.append("]");
		return sb.toString();
	}

	/**
	 * Print an octet string.  This can be used to encode bytes that may
	 * represent an unknown entity.  This will be in the form of:
	 *
	 *  00-11-22-33 ...
	 *
	 */
	public static String toOctetString(byte[] bytes) {

		StringBuffer sb = new StringBuffer(4 * bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			sb.append(unsignedByteToInt(bytes[i]));
			if (i + 1 < bytes.length) {
				sb.append("-");
			}
		}

		return sb.toString();

	}

	/**
	 * Returns the bytes from an octet string generated by
	 * ByteHelper.toOctetString()
	 */
	public static byte[] fromOctetString(String s) {

		String[] parts = s.split("-");
		byte[] bytes = new byte[parts.length];
		for (int i = 0; i < parts.length; i++) {
			bytes[i] = (byte) Integer.parseInt(parts[i], 16);
		}

		return bytes;

	}

	/**
	 *  Detailed dump, somewhat like "od -ax" but more compact
	 *  @param buffer is the byte buffer
	 *  @return a string
	 */
	public static String dump(byte[] buffer) {

		StringWriter sw = new StringWriter();
		PrintWriter fp = new PrintWriter(sw);

		fp.println("Total of " + buffer.length + "(0x" + Fmt.fmt(buffer.length, 4, Fmt.ZF | Fmt.HX) + ") bytes:");

		int addr = 0;
		StringBuffer hex = new StringBuffer();
		StringBuffer ascii = new StringBuffer();

		String delim = "";
		int null_counter = 0;
		int null_skip = 0;
		for (int i = 0; i < buffer.length; i++) {
			if (i > 0 && i % 16 == 0) {

				if (null_counter == 16) {
					null_skip++;
				} else {
					null_skip = 0;
				}

				if (null_skip <= 1) {

					fp.println(Fmt.fmt(addr, 4, Fmt.ZF)
							+ ": "
							+ hex
							+ "  "
							+ ascii);

				} else if (null_skip == 2) {
					fp.println("*");
				}

				hex = new StringBuffer();
				ascii = new StringBuffer();
				delim = "";
				addr += 16;
				null_counter = 0;
			}

			hex.append(delim);
			delim = " ";

			if (buffer[i] == 0) {
				null_counter++;
			}

			// show the byte
			hex.append(Fmt.fmt(0xff & buffer[i], 2, Fmt.ZF | Fmt.HX));

			// we assume ascii
			if (buffer[i] >= 0x20 && buffer[i] < 0x7f) {
				ascii.append(Character.toString((char) buffer[i]));
			} else {
				ascii.append(".");
			}
		}

		// print the remainder
		fp.println(Fmt.fmt(addr, 4, Fmt.ZF)
				+ ": "
				+ Fmt.fmt(hex.toString(), 47, Fmt.LJ)
				+ "  "
				+ Fmt.fmt(ascii.toString(), 16, Fmt.LJ));
		return (sw.toString());
	}

	/**
	 * Gunzip a byte array
	 */
	public static byte[] gunzip(byte[] src) throws IOException {

		InputStream is = null;
		ByteArrayOutputStream os = null;
		GZIPInputStream zipIn = null;

		try {

			is = new ByteArrayInputStream(src);
			zipIn = new GZIPInputStream(is);

			os = new ByteArrayOutputStream(src.length * 2);

			int len;
			byte[] buf = new byte[1024];

			while ((len = zipIn.read(buf, 0, 1024)) > 0) {
				os.write(buf, 0, len);
			}

			// close streams
			is.close();
			os.close();

			return os.toByteArray();

		} finally {

			if (null != os) {
				os.close();
			}
			is = null;
			os = null;
			zipIn = null;
		}
	}

	/**
	 * GZIP a byte array
	 */
	public static byte[] gzip(byte[] src) throws IOException {

		InputStream is = null;
		ByteArrayOutputStream os = null;
		GZIPOutputStream zipOut = null;

		try {

			is = new ByteArrayInputStream(src);
			os = new ByteArrayOutputStream(src.length);
			zipOut = new GZIPOutputStream(os);

			byte[] buf = new byte[1024];
			int len;
			while ((len = is.read(buf)) > 0) {
				zipOut.write(buf, 0, len);
			}
			is.close();

			// Complete the GZIP file
			zipOut.finish();
			zipOut.close();

			return os.toByteArray();

		} finally {
			if (null != os) {
				os.close();
			}
			is = null;
			os = null;
			zipOut = null;
		}
	}

	/**
	 *  Convert the content of a byte buffer to a string
	 *  @param buf is the byte buffer
	 *  @param charset indicates the character set encoding
	 *  @return a string
	 */
	public static String stringFromBuffer(ByteBuffer buf, Charset charset) {
		CharBuffer cb = charset.decode(buf.duplicate());
		return (cb.toString());
	}

	/**
	 *  Convert the content of a byte buffer to a string, using a
	 *  default char encoding
	 *  @param buf is the byte buffer
	 *  @return a string
	 */
	public static String stringFromBuffer(ByteBuffer buf) {
		Charset charset = Charset.forName("UTF-8");
		return (stringFromBuffer(buf, charset));
	}

	/**
	 * Convert the content of a byte buffer to a Bas64 encoded string
	 */
	public static String toBase64String(ByteBuffer buff) {

		ByteBuffer bb = buff.asReadOnlyBuffer();
		bb.position(0);
		byte[] b = new byte[bb.limit()];
		bb.get(b, 0, b.length);
		return Base64.encode(b);

	}

	public static String toBase64String(byte[] buff) {
		return (toBase64String(ByteBuffer.wrap(buff)));
	}

	public static String toBase64String(String string) {
		return (toBase64String(string.getBytes()));
	}

	/**
	 * Converts a Base64 encoded String to a byte array.
	 */
	public static byte[] fromBase64String(String s) {

		if (null == s) {
			return null; // null to match Utility.base64Decode impl
		}
		try {

			return Base64.decode(s);

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Returns an output stream for a ByteBuffer.
	 * The write() methods use the relative ByteBuffer put() methods.
	 */
	public static OutputStream newOutputStream(final ByteBuffer buf) {
		return new OutputStream() {

			public synchronized void write(int b) throws IOException {
				buf.put((byte) b);
			}

			public synchronized void write(byte[] bytes, int off, int len) throws IOException {
				buf.put(bytes, off, len);
			}
		};
	}

	/**
	 * Returns an input stream for a ByteBuffer.
	 * The read() methods use the relative ByteBuffer get() methods.
	 */
	public static InputStream newInputStream(final ByteBuffer buf) {
		return new BufferInputStream(buf);
	}

	/**
	 * Returns an input stream for a ByteBuffer.
	 * The read() methods use the relative ByteBuffer get() methods.
	 */
	private static class BufferInputStream extends InputStream {

		// Member variables
		private ByteBuffer buffer;

		private BufferInputStream(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		public int read() throws IOException {
			if (!buffer.hasRemaining()) {
				return -1;
			}
			return buffer.get();
		}

		// wrap to read
		public int read(byte[] bytes) throws IOException {
			return read(bytes, 0, bytes.length);
		}

		public int read(byte[] bytes, int off, int len) throws IOException {

			// close stream
			if (!buffer.hasRemaining()) {
				return -1;
			}

			// Read only what's left -- should return -1!!!
			len = Math.min(len, buffer.remaining());
			buffer.get(bytes, off, len);

			return len;
		}
	}

	/**
	 * Return ALL the content
	 */
	public static byte[] extractAllBytes(ByteBuffer bb) {
		ByteBuffer dupe = bb.duplicate();
		dupe.rewind();
		int size = dupe.limit();
		byte[] bytes = new byte[size];
		dupe.get(bytes);
		return bytes;
	}

	/**
	 * Extract content, position is assumed to be the end of the content
	 * @param bb is the  byte buffer
	 * @return a byte array
	 */
	public static byte[] extractBytes(ByteBuffer bb) {
		byte[] data = new byte[bb.position()];
		ByteBuffer dup = bb.duplicate();
		dup.flip();
		dup.get(data);
		return data;
	}

	/**
	 *  Create a byte buffer from a string
	 *  @param string is the the string
	 *  @return a byte buffer
	 */
	public static ByteBuffer newByteBuffer(String string) throws Exception {


		return (ByteBuffer.wrap(string.getBytes()));

		/*	ByteBuffer buffer = allocateBodyBuffer(string.length());*/
		/*	buffer.wrap(string.getBytes());*/
		/*	return(buffer);*/

	}

	/**
	 *  Create a byte buffer from a stream
	 *  @param in is the input stream
	 *  @return
	 */
	public static ByteBuffer newByteBuffer(InputStream in) throws Exception {

		if (false == in.markSupported()) {
			throw new Exception("Input stream must support mark");
		}

		int size = 0;
		in.mark(Integer.MAX_VALUE);
		while (in.read() != -1) {
			size++;
		}
		in.reset();
		return (newByteBuffer(in, size));
	}

	/**
	 *  Create a new byte buffer from a stream
	 *  @param in is the stream
	 *  @param size is the size
	 *  @return a byte buffer
	 */
	public static ByteBuffer newByteBuffer(InputStream in, int size) throws Exception {

		byte[] bytes = new byte[size];
		int n, total = 0;
		do {
			n = in.read(bytes, total, size);
			total += n;
		} while (total < size);

		return (ByteBuffer.wrap(bytes));
	}

	/**
	 * Allocate space for a Body in RAM
	 */
	private static ByteBuffer allocateBodyMemory(int size) throws Exception {
		return (ByteBuffer.allocate(size));
	}

	/**
	 * Allocate space for a Body in RAM
	 */
	private static ByteBuffer allocateBodyFile(int size) throws Exception {
		File file = null;
		RandomAccessFile raf = null;
		FileChannel fc = null;
		ByteBuffer buffer = null;
		try {
			file = createTempFile();
			raf = new RandomAccessFile(file, "rw");
			fc = raf.getChannel();
			buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
		} finally {
			file = null;
			if (null != fc) {
				fc.close();
			}
			if (null != raf) {
				raf.close();
			}
		}
		return (buffer);
	}

	/**
	 *  Allocate a body buffer, depending on size we will either do
	 *  this in memory or memory mapped file.
	 *  @param length is the size we need
	 */
	private static ByteBuffer allocateBodyBuffer(int length) throws Exception {
		// allocate space
		if (length < MAX_BODY_RAM) {
			return (allocateBodyMemory(length));
		} else {
			return (allocateBodyFile(length));
		}
	}

	/**
	 * Return a temp file
	 */
	public static File createTempFile() throws Exception {
		return File.createTempFile("mst_util", "tmp");
	}

	/**
	 * Transform an unsigned integer / long into the minimum number of necessary
	 * bytes. This method is only safe to use with positive numbers.
	 * @see fromFewestBytes
	 */
	public static byte[] toFewestBytes(long value) {

		if (value < 0) {
			throw new IllegalArgumentException("Can't use negative value");
		}
		if (value == 0) {
			return new byte[]{0};
		}

		byte[] bytes = fromLong(value);
		int i = 0;
		for (; i < 8; i++) {
			if (bytes[i] != 0) {
				break;
			}
		}
		int len = 8 - i;
		byte[] trimmed = new byte[len];
		System.arraycopy(bytes, i, trimmed, 0, trimmed.length);
		return trimmed;

	}

	/**
	 * Transform the given byte array into a long (unsinged int).
	 * @see toFewestBytes
	 */
	public static long fromFewestBytes(byte[] value) {

		if (null == value || value.length == 0) {
			throw new IllegalArgumentException("Can't use null or zero length value");
		}

		if (value.length > 7) {
			throw new IllegalArgumentException("Value too big");
		}

		// pad to 8 bytes
		byte[] bytes = new byte[8];
		int destPost = 8 - value.length;
		System.arraycopy(value, 0, bytes, destPost, value.length);
		return toLong(bytes);

	}

	/**
	 * Convert from Binary to a number
	 */
	public static int fromBinary(String s) {
		return Integer.parseInt(s, 2);
	}

	/**
	 * Convert a number to Binary
	 */
	public static String toBinary(int i) {
		return Integer.toString(i, 2);
	}

	/**
	 * Extract a value from inside a byte. Offset is a zero indexed value
	 * starting at the most significant bit (leftmost).
	 */
	public static int extractValue(byte b, int offset, int bits) {
		int mask = (int) BIT_VALUES[bits];	// number of adjacent 1s
		int shift = (8 - bits) - offset;	// determine shift distance
		mask = mask << shift;			// position to spot
		int value = (int) (b & mask);		// mask all other values
		value = value >> shift;			// shift back to the right
		return value;
	}

	/**
	 * Insert a value into a byte.
	 */
	public static byte insertValue(int number, int bits, byte b, int offset) {

		// make sure number will fit
		if (number > Math.pow(2, bits) - 1) {
			throw new IllegalArgumentException(
					"Can't fit " + number + " into " + bits + " bits");
		}

		// calcuate amount to shit
		int shift = (8 - bits) - offset;

		// clear position we will be setting (make all zeros)
		int mask = (int) BIT_VALUES[bits];	// digits
		mask = mask << shift;			// shift
		mask = ~mask;				// invert
		int value = (int) b & mask;		// zeros

		// merge value
		mask = number;
		number = (number << shift);
		value = value | number;			// add value
		return (byte) value;

	}

	/**
	 * Debug
	 */
	public static void debug(String s) {
		System.out.println(s);
	}

	/**
	 * Return the first bytes from a byte array
	 */
	public static byte[] head(byte[] bytes, int len) {
		if (len > bytes.length) {
			throw new IllegalArgumentException(
					"Can't ask for more data than is present");
		}
		byte[] buff = new byte[len];
		System.arraycopy(bytes, 0, buff, 0, buff.length);
		return buff;
	}

	/**
	 * Return the last bytes from a byte array
	 */
	public static byte[] tail(byte[] bytes, int len) {
		if (len > bytes.length) {
			throw new IllegalArgumentException("Can't ask for more data than is present");
		}
		byte[] buff = new byte[len];
		int start = bytes.length - len;
		System.arraycopy(bytes, start, buff, 0, len);
		return buff;
	}
}
