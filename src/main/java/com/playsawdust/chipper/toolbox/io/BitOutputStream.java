/*
 * Chipper Toolbox - a somewhat opinionated collection of assorted utilities for Java
 * Copyright (c) 2019 - 2022 Una Thompson (unascribed), Isaac Ellingson (Falkreon)
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * API Note: Numeric types have specifically-named write methods because
 * compiler inference is fairly bad at parameters, even with marker casts
 * in place - in particular, Long tends to show up as Float (!)
 */
public class BitOutputStream extends FilterOutputStream {
	private int buffer = 0;
	private int index = 0;

	public BitOutputStream(OutputStream out) {
		super(out);
	}

	public void writeBit(int bit) throws IOException {
		buffer<<= 1;
		buffer |= (bit & 0x01);
		index++;
		if (index>=8) {
			out.write(buffer);
			index = 0;
			buffer = 0;
		}
	}

	@Override
	public void write(int value) throws IOException {
		if (index==-1) {
			out.write(value);
		} else {
			for(int i=7; i>=0; i--) {
				writeBit((value>>i) & 0xFF);
			}
		}
	}

	public void write(boolean value) throws IOException {
		writeBit(value ? 0x01 : 0x00);
	}

	public void writeFloat(float value) throws IOException {
		write(32, Float.floatToIntBits(value));
	}

	public void writeDouble(double value) throws IOException {
		write(64, Double.doubleToLongBits(value));
	}

	public void writeInt(int value) throws IOException {
		write(32, value);
	}

	public void writeLong(long value) throws IOException {
		write(64,value);
	}

	public void write(int bits, long value) throws IOException {
		if (bits < 1 || bits >= 64) {
			throw new IllegalArgumentException("bits(" + bits + ") is out of range");
		}

		long cur = Long.reverse(value) >> (64-bits);
		for(int i=0; i<bits; i++) {
			writeBit((int)cur);
			cur >>=1;
		}
	}



	@Override
	public void write(final byte[] value) throws IOException {
		if (index==0) {
			//Optimal case is optimal
			out.write(value);
		} else {
			for(byte b : value) {
				write(b);
			}
		}
	}

	/**
	 * Writes the 32-bit length and then the UTF-8 encoded String
	 */
	public void write(String value) throws IOException {
		write(32, value.length());
		write(value.getBytes(StandardCharsets.UTF_8));
	}

	public void align() throws IOException {
		while (index!=0) {
			writeBit(0);
		}
	}

	/**
	 * Close this Stream. Also closes the underlying Stream.
	 */
	@Override
	public void close() throws IOException {
		flush();
		out.close();
	}

	@Override
	public void flush() throws IOException {
		while (index!=0) {
			writeBit(0);
		}
	}


}

