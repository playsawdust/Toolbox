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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Convenience class for getting random numbers from a shared generator. Thread safe -
 * each thread gets its own non-thread-safe generator, similar to {@link ThreadLocalRandom}.
 * <p>
 * It is safe to share one random instance for all usages, and in fact is overall
 * better than having many separate random number generators, as the variety of
 * callers becomes a source of entropy itself. (This fact is why incredibly low-quality
 * primitive random number generators in older games work fine for normal gameplay.)
 * You are encouraged to always use SharedRandom for general-purpose random numbers that
 * do not need a seed.
 * <p>
 * The underlying implementation used by SharedRandom is {@link BetterRandom} - if you need
 * a seeded generator, you may use it instead. It has an identical API.
 */
public final class SharedRandom {

	private static final ThreadLocal<BetterRandom> rand = ThreadLocal.withInitial(BetterRandom::new);

	/**
	 * Shuffle the given list in-place using SharedRandom's random number
	 * generator.
	 * @param li the list to shuffle
	 * @see Collections#shuffle(List, java.util.Random)
	 * @return the input list, for convenience
	 */
	public static <T, L extends List<T>> L shuffle(L li) {
		return rand.get().shuffle(li);
	}

	/**
	 * Make a copy of the given Iterable as a List, shuffle it, and return the list.
	 * @param iter the iterable to shuffle
	 * @return a new list
	 */
	public static <T> ArrayList<T> shuffleCopy(Iterable<T> iter) {
		return rand.get().shuffleCopy(iter);
	}

	/**
	 * Shuffle the given array in-place using SharedRandom's random number
	 * generator.
	 * @param arr the array to shuffle
	 * @return the input array, for convenience
	 */
	@SafeVarargs
	public static <T> T[] shuffle(T... arr) {
		rand.get().shuffle(arr);
		return arr;
	}


	/**
	 * @return 1 in 2 (50%) chance of {@code true}, otherwise {@code false}
	 */
	public static boolean chance() {
		return rand.get().chance();
	}

	/**
	 * @return 1 in {@code n} chance of {@code true}, otherwise {@code false}
	 */
	public static boolean chance(int n) {
		return rand.get().chance(n);
	}

	/**
	 * @return (n*100)% chance of {@code true}, otherwise {@code false}
	 */
	public static boolean chance(float n) {
		return rand.get().chance(n);
	}

	/**
	 * @return (n*100)% chance of {@code true}, otherwise {@code false}
	 */
	public static boolean chance(double n) {
		return rand.get().chance(n);
	}



	/**
	 * Note: Gaussian-distributed numbers have a <em>standard deviation</em> of
	 * 1, <strong>not</strong> a range of 1! Values outside of the range -1 to
	 * 1 can and will be returned. This is fine for simulating natural chaotic
	 * phenomena, but if you need numbers in a specific range, use
	 * {@link #triangularDouble()} instead.
	 * @return a Gaussian (i.e. normal) distributed double with a mean of 0
	 * 		and a standard deviation of 1
	 */
	public static double gaussianDouble() {
		return rand.get().gaussianDouble();
	}

	/**
	 * Note: Gaussian-distributed numbers have a <em>standard deviation</em> of
	 * {@code stddev}, <strong>not</strong> a range of {@code stddev}! Values
	 * outside of the range -{@code stddev} to {@code stddev} can and will be
	 * returned. This is fine for simulating natural chaotic phenomena, but if
	 * you need numbers in a specific range, use {@link #triangularDouble(double)}
	 * instead.
	 * @return a Gaussian (i.e. normal) distributed double with a mean of 0
	 * 		and a standard deviation of {@code stddev}
	 */
	public static double gaussianDouble(double stddev) {
		return rand.get().gaussianDouble(stddev);
	}

	/**
	 * Note: Gaussian-distributed numbers have a <em>standard deviation</em> of
	 * {@code stddev}, <strong>not</strong> a range of {@code stddev}! Values
	 * outside of the range -{@code stddev} to {@code stddev} can and will be
	 * returned. This is fine for simulating natural chaotic phenomena, but if
	 * you need numbers in a specific range, use {@link #triangularDouble(double, double)}
	 * instead.
	 * @return a Gaussian (i.e. normal) distributed double with a mean of
	 * 		{@code mean} and a standard deviation of {@code stddev}
	 */
	public static double gaussianDouble(double stddev, double mean) {
		return rand.get().gaussianDouble(stddev, mean);
	}



	/**
	 * Note: Gaussian-distributed numbers have a <em>standard deviation</em> of
	 * 1, <strong>not</strong> a range of 1! Values outside of the range -1 to
	 * 1 can and will be returned. This is fine for simulating natural chaotic
	 * phenomena, but if you need numbers in a specific range, use
	 * {@link #triangularFloat()} instead.
	 * @return a Gaussian (i.e. normal) distributed float with a mean of 0
	 * 		and a standard deviation of 1
	 */
	public static float gaussianFloat() {
		return rand.get().gaussianFloat();
	}

	/**
	 * Note: Gaussian-distributed numbers have a <em>standard deviation</em> of
	 * {@code stddev}, <strong>not</strong> a range of {@code stddev}! Values
	 * outside of the range -{@code stddev} to {@code stddev} can and will be
	 * returned. This is fine for simulating natural chaotic phenomena, but if
	 * you need numbers in a specific range, use {@link #triangularFloat(float)}
	 * instead.
	 * @return a Gaussian (i.e. normal) distributed float with a mean of 0
	 * 		and a standard deviation of {@code stddev}
	 */
	public static float gaussianFloat(float stddev) {
		return rand.get().gaussianFloat(stddev);
	}

	/**
	 * Note: Gaussian-distributed numbers have a <em>standard deviation</em> of
	 * {@code stddev}, <strong>not</strong> a range of {@code stddev}! Values
	 * outside of the range -{@code stddev} to {@code stddev} can and will be
	 * returned. This is fine for simulating natural chaotic phenomena, but if
	 * you need numbers in a specific range, use {@link #triangularFloat(float, float)}
	 * instead.
	 * @return a Gaussian (i.e. normal) distributed float with a mean of
	 * 		{@code mean} and a standard deviation of {@code stddev}
	 */
	public static float gaussianFloat(float stddev, float mean) {
		return rand.get().gaussianFloat(stddev, mean);
	}



	/**
	 * @return a triangularly distributed double, from -1 to 1, with values around
	 * 		0 being more common
	 */
	public static double triangularDouble() {
		return rand.get().triangularDouble();
	}

	/**
	 * @return a triangularly distributed double, from {@code -range} to {@code range},
	 * 		with values around 0 being more common
	 */
	public static double triangularDouble(double range) {
		return rand.get().triangularDouble(range);
	}

	/**
	 * @return a triangularly distributed double, from {@code -(range-mid)} to {@code range+mid},
	 * 		with values around {@code mid} being more common
	 */
	public static double triangularDouble(double range, double mid) {
		return rand.get().triangularDouble(range, mid);
	}



	/**
	 * @return a triangularly distributed float, from -1 to 1, with values around
	 * 		0 being more common
	 */
	public static float triangularFloat() {
		return rand.get().triangularFloat();
	}

	/**
	 * @return a triangularly distributed float, from {@code -range} to {@code range},
	 * 		with values around 0 being more common
	 */
	public static float triangularFloat(float range) {
		return rand.get().triangularFloat(range);
	}

	/**
	 * @return a triangularly distributed float, from {@code -(range-mid)} to {@code range+mid},
	 * 		with values around {@code mid} being more common
	 */
	public static float triangularFloat(float range, float mid) {
		return rand.get().triangularFloat(range, mid);
	}



	/**
	 * @return a uniformly (i.e. evenly) distributed float, from 0 to 1
	 */
	public static float uniformFloat() {
		return rand.get().uniformFloat();
	}

	/**
	 * @return a uniformly (i.e. evenly) distributed float from 0 to max
	 */
	public static float uniformFloat(float max) {
		return rand.get().uniformFloat(max);
	}

	/**
	 * @return a uniformly (i.e. evenly) distributed float from min to max
	 */
	public static float uniformFloat(float min, float max) {
		return rand.get().uniformFloat(min, max);
	}



	/**
	 * @return a uniformly (i.e. evenly) distributed double, from 0 to 1
	 */
	public static double uniformDouble() {
		return rand.get().uniformDouble();
	}

	/**
	 * @return a uniformly (i.e. evenly) distributed double from 0 to max
	 */
	public static double uniformDouble(double max) {
		return rand.get().uniformDouble(max);
	}

	/**
	 * @return a uniformly (i.e. evenly) distributed double from min to max
	 */
	public static double uniformDouble(double min, double max) {
		return rand.get().uniformDouble(min, max);
	}



	/**
	 * @return a uniformly (i.e. evenly) distributed int, from Integer.MIN_VALUE
	 * 		to Integer.MAX_VALUE
	 */
	public static int uniformInt() {
		return rand.get().uniformInt();
	}

	/**
	 * Note: unlike java.util.Random, 0 is valid for the maximum, and will just
	 * result in always getting 0 back.
	 * @return a uniformly (i.e. evenly) distributed int from 0 to max-1
	 */
	public static int uniformInt(int max) {
		return rand.get().uniformInt(max);
	}

	/**
	 * Note: unlike java.util.Random, 0 is valid for the maximum, and will just
	 * result in always getting 0 back.
	 * @return a uniformly (i.e. evenly) distributed int from min to max-1
	 */
	public static int uniformInt(int min, int max) {
		return rand.get().uniformInt(min, max);
	}



	/**
	 * Note: Due to the higher quality random number generator used by
	 * SharedRandom, all long values are possible, unlike java.util.Random which
	 * will only return 0.001% of the possible values, due to its use of a 48-bit
	 * seed.
	 * @return a uniformly (i.e. evenly) distributed long, from Long.MIN_VALUE to
	 * 		Long.MAX_VALUE
	 */
	public static long uniformLong() {
		return rand.get().uniformLong();
	}

	/**
	 * Note: unlike java.util.Random, 0 is valid for the maximum, and will just
	 * result in always getting 0 back.
	 * @return a uniformly (i.e. evenly) distributed long from 0 to max-1
	 */
	public static long uniformLong(long max) {
		return rand.get().uniformLong(max);
	}

	/**
	 * Note: unlike java.util.Random, 0 is valid for the maximum, and will just
	 * result in always getting 0 back.
	 * @return a uniformly (i.e. evenly) distributed long from min to max-1
	 */
	public static long uniformLong(long min, long max) {
		return rand.get().uniformLong(min, max);
	}

	/**
	 * Allocate a new byte array and populate it with uniformly random data.
	 * @param n the number of uniformly random bytes to generate
	 * @return a newly allocated array of size {@code n}, filled with random bytes
	 */
	public static byte[] bytes(int n) {
		return rand.get().bytes(n);
	}

	/**
	 * Fill the given byte array with uniformly random data.
	 * @param buf the byte array to fill
	 * @return {@code buf}, for convenience
	 */
	public static byte[] bytes(byte[] buf) {
		return rand.get().bytes(buf);
	}

	/**
	 * Replace {@code len} bytes in {@code buf} starting at {@code ofs} with uniformly random data.
	 * @param buf the byte array to modify
	 * @param ofs the index of the first byte to replace; must be less than {@code buf.length}
	 * @param len the number of bytes to replace; must be less than {@code buf.length - ofs}
	 * @return {@code buf}, for convenience
	 */
	public static byte[] bytes(byte[] buf, int ofs, int len) {
		return rand.get().bytes(buf, ofs, len);
	}



	/**
	 * Return a random entry from the given list, using SharedRandom's random
	 * number generator. If the list is empty, an exception is thrown.
	 * @param li the list to retrieve an element from
	 * @return the retrieved element
	 * @throws NoSuchElementException if the list is empty
	 */
	public static <T> T choice(List<T> li) {
		return rand.get().choice(li);
	}

	/**
	 * Always throws an exception.
	 * @deprecated Has no useful effect.
	 * @return nothing, always throws
	 * @throws NoSuchElementException always
	 */
	@Deprecated
	public static <T> T choice() {
		throw new NoSuchElementException();
	}

	/**
	 * Return the argument.
	 * @deprecated Has no useful effect.
	 * @param only the argument to return
	 * @return the sole argument
	 */
	@Deprecated
	public static <T> T choice(T only) {
		return only;
	}

	/**
	 * Return a random argument, using SharedRandom's random number generator.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @return the chosen argument
	 */
	public static <T> T choice(T a, T b) {
		return rand.get().choice(a, b);
	}

	/**
	 * Return a random argument, using SharedRandom's random number generator.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @return the chosen argument
	 */
	public static <T> T choice(T a, T b, T c) {
		return rand.get().choice(a, b, c);
	}

	/**
	 * Return a random argument, using SharedRandom's random number generator.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @return the chosen argument
	 */
	public static <T> T choice(T a, T b, T c, T d) {
		return rand.get().choice(a, b, c, d);
	}

	/**
	 * Return a random argument, using SharedRandom's random number generator.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @param e the fifth possibility
	 * @return the chosen argument
	 */
	public static <T> T choice(T a, T b, T c, T d, T e) {
		return rand.get().choice(a, b, c, d, e);
	}

	/**
	 * Return a random argument, using SharedRandom's random number generator.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @param e the fifth possibility
	 * @param f the sixth possibility
	 * @return the chosen argument
	 */
	public static <T> T choice(T a, T b, T c, T d, T e, T f) {
		return rand.get().choice(a, b, c, d, e, f);
	}

	/**
	 * Return a random argument, using SharedRandom's random number generator.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @param e the fifth possibility
	 * @param f the sixth possibility
	 * @param g the seventh possibility
	 * @return the chosen argument
	 */
	public static <T> T choice(T a, T b, T c, T d, T e, T f, T g) {
		return rand.get().choice(a, b, c, d, e, f, g);
	}

	/**
	 * Return a random argument, using SharedRandom's random number generator.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @param e the fifth possibility
	 * @param f the sixth possibility
	 * @param g the seventh possibility
	 * @param h the eighth possibility
	 * @return the chosen argument
	 */
	public static <T> T choice(T a, T b, T c, T d, T e, T f, T g, T h) {
		return rand.get().choice(a, b, c, d, e, f, g, h);
	}

	/**
	 * Return a random argument, using SharedRandom's random number generator.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @param e the fifth possibility
	 * @param f the sixth possibility
	 * @param g the seventh possibility
	 * @param h the eighth possibility
	 * @param i the ninth possibility
	 * @return the chosen argument
	 */
	public static <T> T choice(T a, T b, T c, T d, T e, T f, T g, T h, T i) {
		return rand.get().choice(a, b, c, d, e, f, g, h, i);
	}

	/**
	 * Return a random argument, using SharedRandom's random number generator.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @param e the fifth possibility
	 * @param f the sixth possibility
	 * @param g the seventh possibility
	 * @param h the eighth possibility
	 * @param i the ninth possibility
	 * @param j the tenth possibility
	 * @return the chosen argument
	 */
	public static <T> T choice(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j) {
		return rand.get().choice(a, b, c, d, e, f, g, h, i, j);
	}

	/**
	 * Return a random entry from the given array, using SharedRandom's random
	 * number generator. If the array is empty, an exception is thrown.
	 * <p>
	 * This method results in an array construction, which may be costly depending
	 * on the caller. Various versions of this method with fixed numbers of arguments
	 * are offered for effectively zero-overhead convenient random choices of literals.
	 * @param arr the array to retrieve an element from
	 * @return the retrieved element
	 * @throws NoSuchElementException if the array is empty
	 */
	@SafeVarargs
	public static <T> T choice(T... arr) {
		return rand.get().choice(arr);
	}

	private SharedRandom() {}

}
