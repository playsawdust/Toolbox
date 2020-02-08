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

import com.unascribed.random.RandomXoshiro256StarStar;

import com.google.common.collect.Lists;

/**
 * A random number generator based on {@link RandomXoshiro256StarStar}, with a friendlier
 * and more convenient API. The underlying Random can still be retrieved if it is needed
 * to interface with other APIs. Not thread safe.
 * <p>
 * For general-purpose unseeded use, please use {@link SharedRandom} instead, as a shared
 * generator results in overall higher quality randomness.
 */
public class BetterRandom {

	private final RandomXoshiro256StarStar rand;

	/**
	 * Create a BetterRandom with an arbitrarily seeded underlying random
	 * number generator.
	 */
	public BetterRandom() {
		this(new RandomXoshiro256StarStar());
	}

	/**
	 * Create a BetterRandom with the given seed. It will be stretched with
	 * an arbitrary algorithm to create a suitable state.
	 */
	public BetterRandom(long seed) {
		this(new RandomXoshiro256StarStar(seed));
	}

	/**
	 * Create a BetterRandom with the given random number generator.
	 * @param rand the RNG to use
	 */
	public BetterRandom(RandomXoshiro256StarStar rand) {
		this.rand = rand;
	}

	/**
	 * Shuffle the given list in-place.
	 * @param li the list to shuffle
	 * @see Collections#shuffle(List, java.util.Random)
	 * @return the input list, for convenience
	 */
	public <T, L extends List<T>> L shuffle(L li) {
		Collections.shuffle(li, rand);
		return li;
	}

	/**
	 * Make a copy of the given Iterable as a List, shuffle it, and return the list.
	 * @param iter the iterable to shuffle
	 * @return a new list
	 */
	public <T> ArrayList<T> shuffleCopy(Iterable<T> iter) {
		ArrayList<T> copy = Lists.newArrayList(iter);
		return shuffle(copy);
	}

	/**
	 * Shuffle the given array in-place.
	 * @param arr the array to shuffle
	 * @return the input array, for convenience
	 */
	@SafeVarargs // requires method be final
	public final <T> T[] shuffle(T... arr) {
		// somehow, there is no Arrays.shuffle
		// so... do it by hand
		// impl copied from Collections.shuffle (but it's so trivial it hardly matters)
		for (int i = arr.length; i > 1; i--) {
			T tmp = arr[i-1];
			int j = uniformInt(i);
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		return arr;
	}



	/**
	 * @return 1 in 2 (50%) chance of {@code true}, otherwise {@code false}
	 */
	public boolean chance() {
		return rand.nextBoolean();
	}

	/**
	 * @return 1 in {@code n} chance of {@code true}, otherwise {@code false}
	 */
	public boolean chance(int n) {
		if (n == 0) return false;
		return uniformInt(n) == 0;
	}

	/**
	 * @return (n*100)% chance of {@code true}, otherwise {@code false}
	 */
	public boolean chance(float n) {
		if (n <= 0) return false;
		if (n > 1) return true;
		return uniformFloat() < n;
	}

	/**
	 * @return (n*100)% chance of {@code true}, otherwise {@code false}
	 */
	public boolean chance(double n) {
		if (n <= 0) return false;
		if (n > 1) return true;
		return uniformDouble() < n;
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
	public double gaussianDouble() {
		return rand.nextGaussian();
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
	public double gaussianDouble(double stddev) {
		return gaussianDouble(stddev, 0);
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
	public double gaussianDouble(double stddev, double mean) {
		return (gaussianDouble()*stddev)+mean;
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
	public float gaussianFloat() {
		return (float)rand.nextGaussian();
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
	public float gaussianFloat(float stddev) {
		return gaussianFloat(stddev, 0);
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
	public float gaussianFloat(float stddev, float mean) {
		return (gaussianFloat()*stddev)+mean;
	}



	/**
	 * @return a triangularly distributed double, from -1 to 1, with values around
	 * 		0 being more common
	 */
	public double triangularDouble() {
		return uniformDouble()-uniformDouble();
	}

	/**
	 * @return a triangularly distributed double, from {@code -range} to {@code range},
	 * 		with values around 0 being more common
	 */
	public double triangularDouble(double range) {
		return triangularDouble(range, 0);
	}

	/**
	 * @return a triangularly distributed double, from {@code -(range-mid)} to {@code range+mid},
	 * 		with values around {@code mid} being more common
	 */
	public double triangularDouble(double range, double mid) {
		return (triangularDouble()*range)+mid;
	}



	/**
	 * @return a triangularly distributed float, from -1 to 1, with values around
	 * 		0 being more common
	 */
	public float triangularFloat() {
		return uniformFloat()-uniformFloat();
	}

	/**
	 * @return a triangularly distributed float, from {@code -range} to {@code range},
	 * 		with values around 0 being more common
	 */
	public float triangularFloat(float range) {
		return triangularFloat(range, 0);
	}

	/**
	 * @return a triangularly distributed float, from {@code -(range-mid)} to {@code range+mid},
	 * 		with values around {@code mid} being more common
	 */
	public float triangularFloat(float range, float mid) {
		return (triangularFloat()*range)+mid;
	}



	/**
	 * @return a uniformly (i.e. evenly) distributed float, from 0 to 1
	 */
	public float uniformFloat() {
		return rand.nextFloat();
	}

	/**
	 * @return a uniformly (i.e. evenly) distributed float from 0 to max
	 */
	public float uniformFloat(float max) {
		return uniformFloat()*max;
	}

	/**
	 * @return a uniformly (i.e. evenly) distributed float from min to max
	 */
	public float uniformFloat(float min, float max) {
		if (min > max) throw new IllegalArgumentException("min > max");
		return (uniformFloat()*(max-min))+min;
	}



	/**
	 * @return a uniformly (i.e. evenly) distributed double, from 0 to 1
	 */
	public double uniformDouble() {
		return rand.nextDouble();
	}

	/**
	 * @return a uniformly (i.e. evenly) distributed double from 0 to max
	 */
	public double uniformDouble(double max) {
		return uniformDouble()*max;
	}

	/**
	 * @return a uniformly (i.e. evenly) distributed double from min to max
	 */
	public double uniformDouble(double min, double max) {
		if (min > max) throw new IllegalArgumentException("min > max");
		return (uniformDouble()*(max-min))+min;
	}



	/**
	 * @return a uniformly (i.e. evenly) distributed int, from Integer.MIN_VALUE
	 * 		to Integer.MAX_VALUE
	 */
	public int uniformInt() {
		return rand.nextInt();
	}

	/**
	 * Note: unlike java.util.Random, 0 is valid for the maximum, and will just
	 * result in always getting 0 back.
	 * @return a uniformly (i.e. evenly) distributed int from 0 to max-1
	 */
	public int uniformInt(int max) {
		if (max == 0) return 0;
		return rand.nextInt(max);
	}

	/**
	 * Note: unlike java.util.Random, 0 is valid for the maximum, and will just
	 * result in always getting 0 back.
	 * @return a uniformly (i.e. evenly) distributed int from min to max-1
	 */
	public int uniformInt(int min, int max) {
		if (min > max) throw new IllegalArgumentException("min > max");
		return uniformInt(max-min)+min;
	}



	/**
	 * Note: Due to the higher quality random number generator used by
	 * BetterRandom, all long values are possible, unlike java.util.Random which
	 * will only return 0.001% of the possible values, due to its use of a 48-bit
	 * seed.
	 * @return a uniformly (i.e. evenly) distributed long, from Long.MIN_VALUE to
	 * 		Long.MAX_VALUE
	 */
	public long uniformLong() {
		return rand.nextLong();
	}

	/**
	 * Note: unlike java.util.Random, 0 is valid for the maximum, and will just
	 * result in always getting 0 back.
	 * @return a uniformly (i.e. evenly) distributed long from 0 to max-1
	 */
	public long uniformLong(long max) {
		if (max == 0) return 0;
		return rand.nextLong(max);
	}

	/**
	 * Note: unlike java.util.Random, 0 is valid for the maximum, and will just
	 * result in always getting 0 back.
	 * @return a uniformly (i.e. evenly) distributed long from min to max-1
	 */
	public long uniformLong(long min, long max) {
		if (min > max) throw new IllegalArgumentException("min > max");
		return uniformLong(max-min)+min;
	}

	/**
	 * Allocate a new byte array and populate it with uniformly random data.
	 * @param n the number of uniformly random bytes to generate
	 * @return a newly allocated array of size {@code n}, filled with random bytes
	 */
	public byte[] bytes(int n) {
		return bytes(new byte[n]);
	}

	/**
	 * Fill the given byte array with uniformly random data.
	 * @param buf the byte array to fill
	 * @return {@code buf}, for convenience
	 */
	public byte[] bytes(byte[] buf) {
		return bytes(buf, 0, buf.length);
	}

	/**
	 * Replace {@code len} bytes in {@code buf} starting at {@code ofs} with uniformly random data.
	 * @param buf the byte array to modify
	 * @param ofs the index of the first byte to replace; must be less than {@code buf.length}
	 * @param len the number of bytes to replace; must be less than {@code buf.length - ofs}
	 * @return {@code buf}, for convenience
	 */
	public byte[] bytes(byte[] buf, int ofs, int len) {
		rand.nextBytes(buf, ofs, len);
		return buf;
	}



	/**
	 * Return a random entry from the given list. If the list is empty, an exception is thrown.
	 * @param li the list to retrieve an element from
	 * @return the retrieved element
	 * @throws NoSuchElementException if the list is empty
	 */
	public <T> T choice(List<T> li) {
		if (li == null) throw new NullPointerException();
		if (li.size() == 0) throw new NoSuchElementException();
		return li.get(uniformInt(li.size()));
	}

	/**
	 * Always throws an exception.
	 * @deprecated Has no useful effect.
	 * @return nothing, always throws
	 * @throws NoSuchElementException always
	 */
	@Deprecated
	public <T> T choice() {
		throw new NoSuchElementException();
	}

	/**
	 * Return the argument.
	 * @deprecated Has no useful effect.
	 * @param only the argument to return
	 * @return the sole argument
	 */
	@Deprecated
	public <T> T choice(T only) {
		return only;
	}

	/**
	 * Return a random argument.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @return the chosen argument
	 */
	public <T> T choice(T a, T b) {
		return chance() ? a : b;
	}

	/**
	 * Return a random argument.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @return the chosen argument
	 */
	public <T> T choice(T a, T b, T c) {
		switch (uniformInt(3)) {
			case 0: return a;
			case 1: return b;
			case 2: return c;
			default: throw new AssertionError();
		}
	}

	/**
	 * Return a random argument.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @return the chosen argument
	 */
	public <T> T choice(T a, T b, T c, T d) {
		switch (uniformInt(4)) {
			case 0: return a;
			case 1: return b;
			case 2: return c;
			case 3: return d;
			default: throw new AssertionError();
		}
	}

	/**
	 * Return a random argument.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @param e the fifth possibility
	 * @return the chosen argument
	 */
	public <T> T choice(T a, T b, T c, T d, T e) {
		switch (uniformInt(5)) {
			case 0: return a;
			case 1: return b;
			case 2: return c;
			case 3: return d;
			case 4: return e;
			default: throw new AssertionError();
		}
	}



	/**
	 * Return a random argument.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @param e the fifth possibility
	 * @param f the sixth possibility
	 * @return the chosen argument
	 */
	public <T> T choice(T a, T b, T c, T d, T e, T f) {
		switch (uniformInt(6)) {
			case 0: return a;
			case 1: return b;
			case 2: return c;
			case 3: return d;
			case 4: return e;
			case 5: return f;
			default: throw new AssertionError();
		}
	}

	/**
	 * Return a random argument.
	 * @param a the first possibility
	 * @param b the second possibility
	 * @param c the third possibility
	 * @param d the fourth possibility
	 * @param e the fifth possibility
	 * @param f the sixth possibility
	 * @param g the seventh possibility
	 * @return the chosen argument
	 */
	public <T> T choice(T a, T b, T c, T d, T e, T f, T g) {
		switch (uniformInt(7)) {
			case 0: return a;
			case 1: return b;
			case 2: return c;
			case 3: return d;
			case 4: return e;
			case 5: return f;
			case 6: return g;
			default: throw new AssertionError();
		}
	}

	/**
	 * Return a random argument.
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
	public <T> T choice(T a, T b, T c, T d, T e, T f, T g, T h) {
		switch (uniformInt(8)) {
			case 0: return a;
			case 1: return b;
			case 2: return c;
			case 3: return d;
			case 4: return e;
			case 5: return f;
			case 6: return g;
			case 7: return h;
			default: throw new AssertionError();
		}
	}

	/**
	 * Return a random argument.
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
	public <T> T choice(T a, T b, T c, T d, T e, T f, T g, T h, T i) {
		switch (uniformInt(9)) {
			case 0: return a;
			case 1: return b;
			case 2: return c;
			case 3: return d;
			case 4: return e;
			case 5: return f;
			case 6: return g;
			case 7: return h;
			case 8: return i;
			default: throw new AssertionError();
		}
	}

	/**
	 * Return a random argument.
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
	public <T> T choice(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j) {
		switch (uniformInt(10)) {
			case 0: return a;
			case 1: return b;
			case 2: return c;
			case 3: return d;
			case 4: return e;
			case 5: return f;
			case 6: return g;
			case 7: return h;
			case 8: return i;
			case 9: return j;
			default: throw new AssertionError();
		}
	}

	/**
	 * Return a random entry from the given array, using SharedRandom's random
	 * number generator. If the array is empty, an exception is thrown.
	 * <p>
	 * This method results in an array construction, which may be costly. Various versions
	 * of this method with fixed numbers of arguments are offered for effectively zero-overhead
	 * convenient random choices of literals.
	 * @param arr the array to retrieve an element from
	 * @return the retrieved element
	 * @throws NoSuchElementException if the array is empty
	 */
	@SafeVarargs // requires method be final
	public final <T> T choice(T... arr) {
		if (arr == null) throw new NullPointerException();
		if (arr.length == 0) throw new NoSuchElementException();
		return arr[uniformInt(arr.length)];
	}

	/**
	 * @return the underlying RNG of this BetterRandom instance
	 */
	public RandomXoshiro256StarStar getUnderlying() {
		return rand;
	}

}
