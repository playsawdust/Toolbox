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

package com.playsawdust.chipper.toolbox.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import com.google.common.io.BaseEncoding;

/**
 * An immutable view into a byte array.
 */
public final class Slice {
	private static final BaseEncoding HEX = BaseEncoding.base16();
	private static final BaseEncoding B64 = BaseEncoding.base64();
	private static final BaseEncoding DEBUG_HEX = BaseEncoding.base16().withSeparator(" ", 2).lowerCase();

	private final byte[] arr;
	private final int ofs;
	private final int len;

	public Slice(byte[] arr) {
		this(arr, 0, arr.length);
	}

	public Slice(byte[] arr, int ofs, int len) {
		if (ofs < 0) throw new IllegalArgumentException("offset cannot be negative");
		if (ofs > arr.length) throw new IllegalArgumentException("offset cannot be > length");
		if (ofs+len > arr.length) throw new IllegalArgumentException("slice cannot extend past the end of the array");
		this.arr = arr;
		this.ofs = ofs;
		this.len = len;
	}

	public byte get(int idx) {
		if (idx >= len) throw new IndexOutOfBoundsException(idx+" >= "+len);
		return arr[ofs+idx];
	}

	public int size() {
		return len;
	}

	public Slice slice(int offset, int length) {
		return new Slice(arr, ofs+offset, length);
	}

	public Slice slice(int offset) {
		return new Slice(arr, ofs+offset, arr.length - (ofs+offset));
	}

	public byte[] toByteArray() {
		return of(arr, ofs, len);
	}

	public void writeTo(OutputStream os) throws IOException {
		os.write(arr, ofs, len);
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		for (int i = 0; i < len; i++) {
			hashCode = 31 * hashCode + get(i);
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Slice other = (Slice) obj;
		if (len != other.len) return false;
		for (int i = 0; i < len; i++) {
			if (get(i) != other.get(i)) return false;
		}
		return true;
	}

	public boolean equals(byte[] bys) {
		return equals(bys, 0, bys.length);
	}

	public boolean equals(byte[] bys, int ofs, int len) {
		if (ofs < 0 || len < 0 || ofs+len > bys.length) throw new IndexOutOfBoundsException();
		if (len != this.len) return false;
		for (int i = 0; i < len; i++) {
			if (get(i) != bys[ofs+i]) return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Slice ["+DEBUG_HEX.encode(arr, ofs, len)+"]";
	}

	public String toString(Charset charset) {
		return new String(arr, ofs, len, charset);
	}

	public String toString(String charset) throws UnsupportedCharsetException {
		return toString(Charset.forName(charset));
	}

	public String toHex() {
		return HEX.encode(arr, ofs, len);
	}

	public String toBase64() {
		return B64.encode(arr, ofs, len);
	}

	public static byte[] of(byte[] arr, int ofs, int len) {
		byte[] dst = new byte[len];
		System.arraycopy(arr, ofs, dst, 0, len);
		return dst;
	}

	public static Slice fromHex(String s) {
		return new Slice(HEX.decode(s));
	}

	public static Slice fromBase64(String s) {
		return new Slice(B64.decode(s));
	}

	public static Slice fromString(String s, Charset charset) {
		return new Slice(s.getBytes(charset));
	}

	public static Slice fromString(String s, String charset) {
		return fromString(s, Charset.forName(charset));
	}

}
