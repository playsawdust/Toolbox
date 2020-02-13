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

import com.playsawdust.chipper.toolbox.function.ExceptableRunnable;

/**
 * Various "hacky" methods whose usage is a code smell, but can be convenient for quick prototyping.
 */
public class Hacks {
	/**
	 * Force the compiler to think an object is a different type. May explode at
	 * runtime, rarely is useful.
	 */
	public static <T> T shoehorn(Object o) {
		return (T) o;
	}

	/**
	 * Perform an operation that may throw an exception, silently discarding any
	 * resulting exceptions. Potentially dangerous, can hinder debugging if used
	 * in a bad place.
	 */
	public static void silently(ExceptableRunnable r) {
		try {
			r.run();
		} catch (Exception e) {
			// ...
		}
	}

	/**
	 * @return {@code true}, but in a way the compiler can't tell it's a constant value
	 */
	public static boolean alwaysTrue() {
		return Integer.valueOf(4).intValue() == 4;
	}

	/**
	 * @return {@code false}, but in a way the compiler can't tell it's a constant value
	 */
	public static boolean alwaysFalse() {
		return Integer.valueOf(4).intValue() == 5;
	}
}
