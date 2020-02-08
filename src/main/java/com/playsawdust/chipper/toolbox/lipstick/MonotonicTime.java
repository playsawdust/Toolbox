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

package com.playsawdust.chipper.toolbox.lipstick;

import static org.checkerframework.checker.units.qual.Prefix.*;
import org.checkerframework.checker.units.qual.s;

/**
 * Provides convenient access to a {@link System#nanoTime monotonic time source},
 * which will never jump around seemingly randomly, due to external changes such
 * as the system administrator changing the system clock. It may be susceptible
 * to leap second adjustments.
 */
public final class MonotonicTime {
	private static final long EPOCH = System.nanoTime();

	/**
	 * Return the number of nanoseconds that have elapsed since the game
	 * started.
	 */
	public static @s(nano) long nanos() {
		return (System.nanoTime()-EPOCH);
	}

	/**
	 * Return the number of milliseconds that have elapsed since the game
	 * started.
	 */
	public static @s(milli) long millis() {
		return nanos()/1000000L;
	}

	/**
	 * Return the number of seconds that have elapsed since the game started.
	 */
	public static @s double seconds() {
		return millis()/1000D;
	}


	/**
	 * Return the number of nanoseconds that have elapsed since the given time
	 * value as returned by {@link #nanos}.
	 * <p>
	 * Equivalent to {@code MonotonicTime.nanos()-nanos}. Convenience method.
	 */
	public static @s(nano) long deltaNanos(@s(nano) long nanos) {
		return nanos()-nanos;
	}

	/**
	 * Return the number of milliseconds that have elapsed since the given time
	 * value as returned by {@link #millis}.
	 * <p>
	 * Equivalent to {@code MonotonicTime.millis()-millis}. Convenience method.
	 */
	public static @s(milli) long deltaMillis(@s(milli) long millis) {
		return millis()-millis;
	}

	/**
	 * Return the number of seconds that have elapsed since the given time value
	 * as returned by {@link #seconds}.
	 * <p>
	 * Equivalent to {@code MonotonicTime.seconds()-seconds}. Convenience method.
	 */
	public static @s double deltaSeconds(@s double seconds) {
		return seconds()-seconds;
	}


	private MonotonicTime() {}

}
