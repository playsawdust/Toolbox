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

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

/**
 * An implementation of PrintStream that delegates to a SLF4j logger, and assigns blame based on
 * performing stack traces.
 */
public class LoggerPrintStream extends PrintStream {
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n", Pattern.LITERAL);

	private static final Set<String> SKIP_CLASSES = Set.of(
			"java.lang.Throwable" // to correctly implicate printStackTrace
		);

	private final Logger log;
	private final boolean warn;

	private final StringBuilder accum = new StringBuilder();

	/**
	 * Set System.out to a LoggerPrintStream called STDOUT at info level, and System.err to a
	 * LoggerPrintStream called STDERR at warning level.
	 */
	public static void initializeDefault() {
		System.setOut(new LoggerPrintStream("STDOUT", false));
		System.setErr(new LoggerPrintStream("STDERR", true));
	}

	public LoggerPrintStream(String label, boolean warn) {
		super(NullOutputStream.INSTANCE);
		this.warn = warn;
		log = LoggerFactory.getLogger(label);
	}

	@Override
	public void println(String x) {
		flush();
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		String caller = "<unknown caller>";
		boolean first = true;
		for (StackTraceElement ste : st) {
			if (first) {
				first = false;
				continue;
			}
			if (!getClass().getName().equals(ste.getClassName()) && !SKIP_CLASSES.contains(ste.getClassName())) {
				caller = ste.getFileName()+":"+ste.getLineNumber();
				break;
			}
		}
		if (warn) {
			log.warn("[{}] {}", caller, x);
		} else {
			log.info("[{}] {}", caller, x);
		}
	}

	@Override
	public void print(String s) {
		synchronized (accum) {
			if (s.contains("\n")) {
				for (String line : NEWLINE_PATTERN.split(s)) {
					accum.append(line);
					flush();
				}
			} else {
				accum.append(s);
			}
		}
	}

	@Override
	public void println(boolean x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(char x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(int x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(long x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(float x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(double x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(char[] x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(Object x) {
		println(String.valueOf(x));
	}

	@Override
	public void flush() {
		String str = null;
		synchronized (accum) {
			if (accum.length() > 0) {
				str = accum.toString();
				accum.setLength(0);
			}
		}
		if (str != null) {
			println(str);
		}
	}

	@Override
	public void close() {
	}

	@Override
	public boolean checkError() {
		return false;
	}

	@Override
	protected void setError() {
	}

	@Override
	protected void clearError() {
	}

	@Override
	public void write(int b) {
		print((char)(b&0xFF));
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		print(new String(buf, off, len, StandardCharsets.UTF_8));
	}

	@Override
	public void print(boolean b) {
		print(String.valueOf(b));
	}

	@Override
	public void print(char c) {
		print(String.valueOf(c));
	}

	@Override
	public void print(int i) {
		print(String.valueOf(i));
	}

	@Override
	public void print(long l) {
		print(String.valueOf(l));
	}

	@Override
	public void print(float f) {
		print(String.valueOf(f));
	}

	@Override
	public void print(double d) {
		print(String.valueOf(d));
	}

	@Override
	public void print(char[] s) {
		print(String.valueOf(s));
	}

	@Override
	public void print(Object obj) {
		print(String.valueOf(obj));
	}

	@Override
	public void println() {
		flush();
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		println(String.format(format, args));
		return this;
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		return super.printf(l, format, args);
	}

	@Override
	public PrintStream format(String format, Object... args) {
		return super.format(format, args);
	}

	@Override
	public PrintStream format(Locale l, String format, Object... args) {
		return super.format(l, format, args);
	}

	@Override
	public PrintStream append(CharSequence csq) {
		synchronized (accum) {
			accum.append(csq);
		}
		return this;
	}

	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		synchronized (accum) {
			accum.append(csq, start, end);
		}
		return this;
	}

	@Override
	public PrintStream append(char c) {
		synchronized (accum) {
			accum.append(c);
		}
		return this;
	}

}
