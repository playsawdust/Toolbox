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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.FilterInputStream;

public class BitInputStream extends FilterInputStream {
	private int data;
	private int index = -1;

	public BitInputStream(final InputStream input) {
		super(input);
	}

	public int readBit() throws IOException {
		if (index<0) {
			data = in.read();
			if (data<0) return -1;
			index = 6;
			return (data >> 7) & 0x01;
		}
		index--;
		return (data >> index+1) & 0x01;
	}

	public boolean readBoolean() throws IOException {
		return readBit()==1;
	}

	/**
	 * Reads the next byte of data from the stream. As per the InputStream
	 * contract, if the end of the stream has been reached, -1 is returned.
	 * No guarantees are made about byte <em>alignment</em> unless {@link #align}
	 * has been called since the last bitwise read operation.
	 */
	@Override
	public int read() throws IOException {
		int result = 0;
		for(int i=0; i<8; i++) {
			int cur = readBit();
			if (cur<0) return -1;
			result <<= 1;
			result |= cur;
		}
		return result;
	}

	public long read(final int bits) throws IOException {
		if (bits<1 || bits>64) throw new IllegalArgumentException("Invalid number of bits for the destination type.");
		long result = 0L;
		for(int i=0; i<bits; i++) {
			int cur = readBit();
			if (cur<0) return -1;
			result <<= 1;
			result |= cur;
		}
		return result;
	}

	public int readInt() throws IOException {
		return (int)read(32);
	}

	public long readLong() throws IOException {
		return read(64);
	}

	public float readFloat() throws IOException {
		int result = readInt();
		if (result==-1) return -1.0f;
		return Float.intBitsToFloat(result);
	}

	public double readDouble() throws IOException {
		long result = readLong();
		if (result==-1L) return -1.0;
		return Double.longBitsToDouble(result);
	}

	/**
	 * Read a 32-bit length and then a UTF-8 encoded String from the stream.
	 */
	public String readString() throws IOException {
		int len = readInt();
		return new String(readBytes(len),StandardCharsets.UTF_8);
	}

	/**
	 * Allocates a new byte[] and fills it with <code>numBytes</code>
	 * bytes from the underlying Stream. If an EOF happens during the
	 * read operation, the remaining bytes will be filled with -1.
	 */
	public byte[] readBytes(int numBytes) throws IOException {
		byte[] result = new byte[numBytes];
		for(int i=0; i<numBytes; i++) {
			result[i] = (byte)read();
		}
		return result;
	}

	/**
	 * Aligns the read marker to the start of the next byte. If
	 * the marker is already at the beginning of a byte, this
	 * method does nothing.
	 */
	public void align() throws IOException {
		while(index>0) readBit();
	}

	/**
	 * Closes this Stream. Also closes the underlying Stream.
	 */
	@Override
	public void close() throws IOException {
		in.close();
	}

}

