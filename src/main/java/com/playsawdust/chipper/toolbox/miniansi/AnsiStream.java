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

package com.playsawdust.chipper.toolbox.miniansi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class AnsiStream extends PrintStream {

	public AnsiStream(File file, String charsetName) throws FileNotFoundException, UnsupportedEncodingException {
		super(file, charsetName);
	}

	public AnsiStream(File file) throws FileNotFoundException {
		super(file);
	}

	public AnsiStream(OutputStream out, boolean autoFlush, String charsetName) throws UnsupportedEncodingException {
		super(out, autoFlush, charsetName);
	}

	public AnsiStream(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public AnsiStream(OutputStream out) {
		super(out);
	}

	public AnsiStream(String fileName, String charsetName) throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, charsetName);
	}

	public AnsiStream(String fileName) throws FileNotFoundException {
		super(fileName);
	}

	public void print(char[] chars, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.print(chars);
	}

	public void print(char c, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.print(c);
	}

	public void print(double d, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.print(d);
	}

	public void print(float f, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.print(f);
	}

	public void print(int i, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.print(i);
	}

	public void print(long l, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.print(l);
	}

	public void print(Object o, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.print(o);
	}

	public synchronized void print(String str, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.print(str);
	}

	public void print(boolean b, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.print(b);
	}

	public void print(Ansi... codes) {
		if (codes.length == 0) return;
		Ansi.Utils.toAppendableUnchecked(this, codes);
	}

	public void println(Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println();
	}

	public void println(char[] chars, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println(chars);
	}

	public void println(char c, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println(c);
	}

	public void println(double d, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println(d);
	}

	public void println(float f, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println(f);
	}

	public void println(int i, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println(i);
	}

	public void println(long l, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println(l);
	}

	public void println(Object o, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println(o);
	}

	public synchronized void println(String str, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println(str);
	}

	public void println(boolean b, Ansi... codes) {
		Ansi.Utils.toAppendableUnchecked(this, codes);
		super.println(b);
	}

	public void printPadded(String str, Ansi... codes) {
		print(codes);
		print(" ");
		print(str);
		print(" ");
	}

	public void cursorRight(int i) {
		print("\u001B[");
		print(i);
		print("C");
	}
	
	public void cursorUp(int i) {
		print("\u001B[");
		print(i);
		print("A");
	}
	
	public void cursorDown(int i) {
		print("\u001B[");
		print(i);
		print("B");
	}
	
	public void cursorLeft(int i) {
		print("\u001B[");
		print(i);
		print("D");
	}

	public void reset() {
		print(AnsiCode.RESET);
	}

}
