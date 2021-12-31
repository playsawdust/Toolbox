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

package com.playsawdust.chipper.toolbox.pool;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a thread-local pool of objects. Keeps object allocations down and
 * the GC happy.
 */
public class ObjectPool<T> implements Supplier<T> {
	private static final Logger log = LoggerFactory.getLogger(ObjectPool.class);

	private static boolean enableStats = false;
	private static List<ObjectPool<?>> allPools = null;

	private static final NumberFormat statsFormatter = NumberFormat.getIntegerInstance();

	static {
		statsFormatter.setGroupingUsed(true);
	}

	/**
	 * Enable printing statistics about ObjectPool savings.
	 * @param autoprint if true, automatically print stats every minute
	 */
	public static void enableStats(boolean autoprint) {
		if (enableStats) return;
		enableStats = true;
		allPools = new ArrayList<>();
		if (autoprint) {
			Thread t = new Thread(() -> {
				while (true) {
					try {
						TimeUnit.SECONDS.sleep(60);
					} catch (InterruptedException e) {
					}
					printStats();
				}
			}, "ObjectPool stats thread");
			t.setDaemon(true);
			t.start();
		}
	}

	/**
	 * Explicitly print ObjectPool saving statistics to ObjectPool's logger at info level right now.
	 * If stats have not been {@link #enableStats(boolean) enabled}, nothing will happen, as there
	 * will be no stats to print.
	 */
	public static void printStats() {
		if (allPools == null) return;
		log.info("-- ObjectPool stats --");
		log.info("There are {} pools", allPools.size());
		int totalSaved = 0;
		int totalOut = 0;
		int totalIn = 0;
		for (ObjectPool<?> pool : allPools) {
			totalSaved += pool.saved;
			totalOut += pool.out;
			totalIn += pool.in;
		}
		log.info("(High out numbers indicate leaks; low out numbers are flukes or long-lived objects)");
		log.info("In total,\n {} object allocations have been avoided.\n {} objects are in circulation (in).\n {} objects are unaccounted for (out)",
					pad(totalSaved), pad(totalIn), pad(totalOut));
		for (ObjectPool<?> pool : allPools) {
			log.info("For the {} pool,\n {} object allocations have been avoided.\n {} objects are in circulation (in).\n {} objects are unaccounted for (out)",
					pool.clazzForStats.getSimpleName(), pad(pool.saved), pad(pool.in), pad(pool.out));
		}
	}

	private static String pad(int i) {
		String s = statsFormatter.format(i);
		if (s.length() < 9) {
			s = " ".repeat(9-s.length())+s;
		}
		return s;
	}

	private final ThreadLocal<ArrayList<T>> pool = ThreadLocal.withInitial(ArrayList::new);
	private final Supplier<T> supplier;

	private boolean enrolledInStats = false;

	private Class<?> clazzForStats;

	// high out numbers indicate object leaks
	private int out;
	// high in numbers indicate occasional bulk allocation
	private int in;
	// how many object allocations have been avoided
	private int saved;

	public ObjectPool(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	/**
	 * @return the number of currently available objects in this pool
	 */
	public int available() {
		return pool.get().size();
	}

	/**
	 * Retrieve an object from this pool, if there is one; otherwise, construct a new one. The
	 * returned object is removed from the pool; to give it back, call {@link #recycle}.
	 */
	@Override
	public T get() {
		if (!enrolledInStats && enableStats) {
			enrolledInStats = true;
			allPools.add(this);
			if (pool.get().isEmpty()) {
				clazzForStats = supplier.get().getClass();
			} else {
				clazzForStats = pool.get().get(0).getClass();
			}
		}
		if (!pool.get().isEmpty()) {
			if (enableStats) {
				out++;
				in--;
				saved++;
			}
			return pool.get().remove(0);
		}
		if (enableStats) {
			out++;
		}
		return supplier.get();
	}

	/**
	 * Re-submit an object to the pool. The object must be cleaned and ready for re-use; often this
	 * is accomplished by zeroing/nulling-out all fields, and setting a "recycled" flag to true.
	 * (Remember to clear the recycled flag after retrieving an object if you use one.)
	 */
	public void recycle(T t) {
		if (enableStats) {
			out--;
			in++;
		}
		pool.get().add(t);
	}

	@Override
	public String toString() {
		if (enableStats) {
			return "ObjectPool[out="+out+",in="+in+",saved="+saved+"]";
		} else {
			return "ObjectPool[out=?,in=?,saved=?]";
		}
	}

}
