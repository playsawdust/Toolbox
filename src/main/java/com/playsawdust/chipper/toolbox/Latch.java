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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Implements a very basic synchronization primitive that blocks one thread
 * until another thread releases it. Thin abstraction for {@link Object#wait}
 * and {@link Object#notify}, that deals with spurious wakeups.
 * <p>
 * If more than one thread waits on a Latch, an arbitrarily chosen thread will
 * be woken up for each call to {@link #release}.
 * <p>
 * To wait for more than one unit of work, use {@link CountDownLatch}, from
 * which this class borrows its name, as it behaves similarly to a CountDownLatch
 * with a {@code count} of 1. However, CountDownLatch is built on the more
 * robust {@link AbstractQueuedSynchronizer} class, rather than bare
 * {@code wait}/{@code notify}, which are only really sufficient for single-unit
 * latches with only two threads, which is what this class is designed for.
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

}
