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

package com.playsawdust.chipper.toolbox.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A shared asynchronous thread pool, for slow tasks that should not
 * freeze an important thread.
 */
public class SharedThreadPool {
	private static final AtomicInteger threadNum = new AtomicInteger(1);
	
	private static boolean holderAccessed = false;
	private static Runnable initializer;
	private static Runnable finalizer;
	
	private static final class Holder {
		private static final ScheduledThreadPoolExecutor SVC  = new ScheduledThreadPoolExecutor(2, (r) -> {
			Thread t = new Thread(() -> {
				if (initializer != null) {
					initializer.run();
				}
				try {
					r.run();
				} finally {
					if (finalizer != null) {
						finalizer.run();
					}
				}
			}, "SharedThreadPool Worker #"+threadNum.getAndIncrement());
			t.setDaemon(true);
			return t;
		});
		
		static {
			// double the processor count for IO-bound tasks
			// (generally the most common kind of task submitted to a shared pool)
			SVC.setMaximumPoolSize(Runtime.getRuntime().availableProcessors()*2);
			// avoids a possible memory leak (exasperated by lambda capturing)
			SVC.setRemoveOnCancelPolicy(true);
		}
	}
	
	/**
	 * Sets an "initializer" that will run in every thread before it starts executing tasks. Useful
	 * for initializing a memory allocator or similar.
	 */
	public static void setInitializer(Runnable r) {
		if (holderAccessed) throw new IllegalStateException("SharedThreadPool already initialized");
		initializer = r;
	}
	/**
	 * Sets a "finalizer" that will run in every thread before it exits. Useful for deallocating
	 * native resources and such.
	 */
	public static void setFinalizer(Runnable r) {
		if (holderAccessed) throw new IllegalStateException("SharedThreadPool already initialized");
		finalizer = r;
	}

	/**
	 * Run the given Runnable on some worker thread as soon as possible,
	 * and populate the returned future with null, or an exception if one
	 * is thrown.
	 */
	public static ListenableFuture<Void> submit(Runnable r) {
		return submit(() -> {
			r.run();
			return null;
		});
	}

	/**
	 * Run the given Callable on some worker thread as soon as possible,
	 * and populate the returned future with its return value, or an exception
	 * if one is thrown.
	 */
	public static <T> ListenableFuture<T> submit(Callable<T> c) {
		return schedule(c, 0, TimeUnit.MILLISECONDS);
	}

	/**
	 * Run the given Runnable on some worker thread after at least the given
	 * amount of time has elapsed, and populate the returned future with null,
	 * or an exception if one is thrown.
	 */
	public static <T> ListenableFuture<T> schedule(Runnable r, long delay, TimeUnit unit) {
		return schedule(() -> {
			r.run();
			return null;
		}, delay, unit);
	}

	/**
	 * Run the given Callable on some worker thread after at least the given
	 * amount of time has elapsed, and populate the returned future with its
	 * return value.
	 */
	public static <T> ListenableFuture<T> schedule(Callable<T> c, long delay, TimeUnit unit) {
		SimpleFuture<T> future = new SimpleFuture<>();
		holderAccessed = true;
		Holder.SVC.schedule(() -> {
			try {
				future.set(c.call());
			} catch (Throwable t) {
				future.setException(t);
			}
		}, delay, unit);
		return future;
	}

}
