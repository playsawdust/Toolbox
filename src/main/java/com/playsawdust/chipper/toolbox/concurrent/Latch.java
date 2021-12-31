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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import com.playsawdust.chipper.toolbox.lipstick.MonotonicTime;

/**
 * Implements a very basic synchronization primitive that blocks threads
 * until another thread releases them. Thin abstraction for {@link Object#wait}
 * and {@link Object#notify} that deals with spurious wakeups.
 * <p>
 * If more than one thread waits on a Latch, an arbitrarily chosen thread will
 * be woken up for each call to {@link #release}, as a Latch is usually used to
 * synchronize only two threads. If you need to release multiple threads, use
 * {@link #releaseAll}.
 * <p>
 * To wait for more than one unit of work, use {@link CountDownLatch}, from
 * which this class borrows its name, as it behaves similarly to a CountDownLatch
 * with a {@code count} of 1. However, CountDownLatch is built on the heavier and
 * more robust {@link AbstractQueuedSynchronizer} class, rather than bare
 * {@code wait}/{@code notify}, which are only sufficient for single-unit latches.
 */
public class Latch {

	// we synchronize on this mutex rather than `this` to prevent users of this
	// class from accidentally using `wait()` and having almost-correct behavior
	private final Object mutex = new Object();
	private volatile boolean complete = false;

	/**
	 * Wait for this Latch to be released by another thread.
	 * @throws InterruptedException if the thread was interrupted while waiting
	 */
	public void await() throws InterruptedException {
		synchronized (mutex) {
			while (!complete) {
				mutex.wait();
			}
		}
	}
	
	/**
	 * Wait for this Latch to be released by another thread, for up to the given amount of time.
	 * @throws InterruptedException if the thread was interrupted while waiting
	 * @throws TimeoutException if the timeout expired before this latch was released
	 */
	public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		long start = MonotonicTime.nanos();
		long time = unit.toNanos(timeout);
		synchronized (mutex) {
			while (!complete) {
				TimeUnit.NANOSECONDS.timedWait(mutex, time);
				long waited = MonotonicTime.deltaNanos(start);
				if (waited > time) throw new TimeoutException();
				time -= waited;
			}
		}
	}

	/**
	 * Wait for this Latch to be released by another thread, and ignore any
	 * InterruptedExceptions.
	 */
	public void awaitUninterruptibly() {
		// this is a copy of #await instead of calling it and catching exceptions
		// as the while loop must be contained within the synchronized block for
		// correct behavior
		synchronized (mutex) {
			while (!complete) {
				try {
					mutex.wait();
				} catch (InterruptedException e) {}
			}
		}
	}

	/**
	 * Allow one arbitrarily chosen thread waiting on this Latch to continue
	 * running.
	 */
	public void release() {
		synchronized (mutex) {
			complete = true;
			mutex.notify();
		}
	}

	/**
	 * Allow all threads waiting on this Latch to continue running.
	 */
	public void releaseAll() {
		synchronized (mutex) {
			complete = true;
			mutex.notifyAll();
		}
	}
	
	/**
	 * @return {@code true} if this latch has been released
	 */
	public boolean isReleased() {
		return complete;
	}

}
