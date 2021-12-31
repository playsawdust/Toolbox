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

package com.playsawdust.chipper.toolbox.lipstick;

import org.slf4j.helpers.MessageFormatter;

/**
 * Wrapper around SLF4j's {@link MessageFormatter} with a more convenient API, as that class is
 * considered a low-level implementation class there. The API provided here is closer to the one
 * exposed by SLF4j loggers.
 * <p>
 * Useful for formatting long strings with lots of inline data.
 */
public final class BraceFormatter {

	/**
	 * Returns the input.
	 * @deprecated Not useful.
	 */
	@Deprecated
	public static String format(String format) {
		return format;
	}

	/**
	 * Replace the first {} in the given format string with {@code arg}'s string representation.
	 * <p>
	 * For example, {@code BraceFormatter.format("Hi {}", "there")} will return {@code "Hi there"}.
	 */
	public static String format(String format, Object arg) {
		return MessageFormatter.format(format, arg).getMessage();
	}

	/**
	 * Replace the first {} in the given format string with {@code arg1}'s string representation,
	 * and the second with {@code arg2}'s string representation.
	 * <p>
	 * For example, {@code BraceFormatter.format("Hi {}, {}", "there", "Bob")} will return
	 * {@code "Hi there, Bob"}.
	 */
	public static String format(String format, Object arg1, Object arg2) {
		return MessageFormatter.format(format, arg1, arg2).getMessage();
	}

	/**
	 * Replace any {} in the given format string with the string representations of successive
	 * objects.
	 * <p>
	 * For example,
	 * {@code BraceFormatter.format("Hi {}, {}. {} {}?", "there", "Bob", "How's it", "going")} will
	 * return {@code "Hi there, Bob. How's it going?"}.
	 */
	public static String format(String format, Object... args) {
		return MessageFormatter.arrayFormat(format, args).getMessage();
	}

	private BraceFormatter() {}

}
