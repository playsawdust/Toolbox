/*
 * Chipper Toolbox - a somewhat opinionated collection of assorted utilities for Java
 * Copyright (c) 2019 - 2020 Una Thompson (unascribed), Isaac Ellingson (Falkreon)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.playsawdust.chipper.toolbox;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

import com.google.common.base.Ascii;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

/**
 * Utility class for encoding binary data in "canonical" hexdump format, as output by
 * {@code hexdump -C}.
 */
public final class Hexdump {

	private static final BaseEncoding HEX_BYTES = BaseEncoding.base16().lowerCase().withSeparator(" ", 2);

	/**
	 * @see #encode(byte[])
	 */
	public static String encode(ByteBuffer buf) {
		if (buf.hasArray()) {
			return encode(buf.array(), buf.arrayOffset()+buf.position(), buf.remaining());
		} else {
			byte[] bys = new byte[buf.remaining()];
			buf.duplicate().get(bys);
			return encode(bys);
		}
	}

	/**
	 * Convert the given binary data to "canonical" hexdump format, identical to the output
	 * from {@code hexdump -C} on Linux.
	 * <p>
	 * Example:
	 * <pre>
	 * 00000000  53 61 77 64 75 73 74 53  61 76 65 46 69 6c 65 21  |SawdustSaveFile!|
	 * 00000010  0d 0a 48 3a 32 2c 62 6c  6f 63 6b 53 69 7a 65 3a  |..H:2,blockSize:|
	 * 00000020  31 30 30 30 2c 63 72 65  61 74 65 64 3a 31 36 62  |1000,created:16b|
	 * 00000030  61 37 63 64 36 35 33 30  2c 66 6f 72 6d 61 74 3a  |a7cd6530,format:|
	 * 00000040  31 2c 66 6c 65 74 63 68  65 72 3a 64 63 63 64 39  |1,fletcher:dccd9|
	 * 00000050  61 34 39 0a 00 00 00 00  00 00 00 00 00 00 00 00  |a49.............|
	 * 00000060  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  |................|
	 * 00000070
	 * </pre>
	 */
	public static String encode(byte[] bys) {
		return encode(bys, 0, bys.length);
	}

	/**
	 * Convert the given binary data to "canonical" hexdump format, identical to the output
	 * from {@code hexdump -C} on Linux.
	 * <p>
	 * Example:
	 * <pre>
	 * 00000000  53 61 77 64 75 73 74 53  61 76 65 46 69 6c 65 21  |SawdustSaveFile!|
	 * 00000010  0d 0a 48 3a 32 2c 62 6c  6f 63 6b 53 69 7a 65 3a  |..H:2,blockSize:|
	 * 00000020  31 30 30 30 2c 63 72 65  61 74 65 64 3a 31 36 62  |1000,created:16b|
	 * 00000030  61 37 63 64 36 35 33 30  2c 66 6f 72 6d 61 74 3a  |a7cd6530,format:|
	 * 00000040  31 2c 66 6c 65 74 63 68  65 72 3a 64 63 63 64 39  |1,fletcher:dccd9|
	 * 00000050  61 34 39 0a 00 00 00 00  00 00 00 00 00 00 00 00  |a49.............|
	 * 00000060  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  |................|
	 * 00000070
	 * </pre>
	 */
	public static String encode(byte[] bys, int ofs, int len) {
		byte[] prevBuf = new byte[16];
		byte[] buf = new byte[16];
		int printed = 0;
		StringBuilder sb = new StringBuilder();
		CharsetDecoder dec = Charsets.US_ASCII.newDecoder()
			.onUnmappableCharacter(CodingErrorAction.REPLACE)
			.onMalformedInput(CodingErrorAction.REPLACE)
			.replaceWith(".");
		ByteBuffer bufBuf = ByteBuffer.wrap(buf);
		char[] decOutArr = new char[16];
		CharBuffer decOut = CharBuffer.wrap(decOutArr);
		boolean omitting = false;
		while (printed < len) {
			System.arraycopy(buf, 0, prevBuf, 0, 16);
			int count = Math.min(len-printed, 16);
			System.arraycopy(bys, ofs+printed, buf, 0, count);
			if (count == 16) {
				if (Arrays.equals(buf, prevBuf)) {
					if (!omitting) {
						omitting = true;
						sb.append("*\n");
					}
					printed += 16;
					continue;
				}
			}
			sb.append(Strings.padStart(Integer.toHexString(printed), 8, '0'));
			sb.append("  ");
			if (count < 8) {
				sb.append(HEX_BYTES.encode(buf, 0, count));
				for (int i = 0; i < 16-count; i++) {
					sb.append("   ");
				}
				sb.append(" ");
			} else if (count < 16) {
				sb.append(HEX_BYTES.encode(buf, 0, 8));
				sb.append("  ");
				sb.append(HEX_BYTES.encode(buf, 8, count-8));
				for (int i = 0; i < 8-(count-8); i++) {
					sb.append("   ");
				}
			} else {
				sb.append(HEX_BYTES.encode(buf, 0, 8));
				sb.append("  ");
				sb.append(HEX_BYTES.encode(buf, 8, 8));
			}
			sb.append("  |");
			decOut.rewind();
			decOut.limit(16);
			bufBuf.rewind();
			bufBuf.limit(count);
			dec.decode(bufBuf, decOut, true);
			for (int i = 0; i < count; i++) {
				char c = decOutArr[i];
				if (c <= Ascii.US || c >= Ascii.DEL) {
					decOutArr[i] = '.';
				}
			}
			sb.append(decOutArr, 0, count);
			sb.append("|\n");
			dec.reset();
			printed += count;
		}
		sb.append(Strings.padStart(Integer.toHexString(printed), 8, '0'));
		return sb.toString();
	}

	private Hexdump() {}

}
