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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executors.newSingleThreadExecutor returns a full-blown
 * {@link ThreadPoolExecutor}. This is a simple implementation of a
 * single-thread executor with a queue.
 */
public class SimpleSingleThreadExecutor extends AbstractExecutorService {

	private static final AtomicInteger nextDefaultThreadId = new AtomicInteger();
	private static final ThreadFactory DEFAULT_THREAD_FACTORY = r -> {
		return new Thread(r, "Anonymous SimpleSingleThreadExecutor #"+nextDefaultThreadId.getAndIncrement());
	};
	
	private final LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();
	private boolean shutdown = false;

	private long operationDelay = 0;

	private final Thread thread;
	
	public SimpleSingleThreadExecutor() {
		this(DEFAULT_THREAD_FACTORY);
	}
	
	public SimpleSingleThreadExecutor(String threadName) {
		this(r -> new Thread(r, threadName));
	}
	
	public SimpleSingleThreadExecutor(ThreadFactory threadFactory) {
		this.thread = threadFactory.newThread(() -> {
			while (!shutdown || !queue.isEmpty()) {
				try {
					queue.take().run();
					Thread.sleep(operationDelay);
				} catch (InterruptedException e) {
				}
			}
		});
	}

	public SimpleSingleThreadExecutor withOperationDelay(long operationDelay) {
		this.operationDelay = operationDelay;
		return this;
	}

	@Override
	public void execute(Runnable command) {
		if (shutdown) throw new IllegalStateException("Executor is shut down");
		if (!thread.isAlive()) {
			thread.start();
		}
		queue.add(command);
	}

	@Override
	public void shutdown() {
		shutdown = true;
	}

	@Override
	public List<Runnable> shutdownNow() {
		shutdown = true;
		List<Runnable> li = new ArrayList<>();
		queue.drainTo(li);
		thread.interrupt();
		return li;
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

	@Override
	public boolean isTerminated() {
		return shutdown && !thread.isAlive();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		unit.timedJoin(thread, timeout);
		return !thread.isAlive();
	}

}
