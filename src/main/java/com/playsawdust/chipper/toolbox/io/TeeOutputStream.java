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

public class TeeOutputStream extends OutputStream {

	private final OutputStream outA;
	private final OutputStream outB;

	public TeeOutputStream(OutputStream outA, OutputStream outB) {
		this.outA = outA;
		this.outB = outB;
	}

	@Override
	public void write(int b) throws IOException {
		outA.write(b);
		outB.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		outA.write(b);
		outB.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		outA.write(b, off, len);
		outB.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		outA.close();
		outB.close();
	}

	@Override
	public void flush() throws IOException {
		outA.flush();
		outB.flush();
	}

}
