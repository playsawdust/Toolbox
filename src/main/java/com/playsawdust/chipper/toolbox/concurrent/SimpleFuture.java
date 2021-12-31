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

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Basically SettableFuture from Guava, but for use in Toolbox which only depends on the lightweight
 * ListenableFuture API rather than all of Guava.
 * <p>
 * TODO I haven't tested this. At all. I'm not really sure how to..? It's theoretically sound...
 */
public class SimpleFuture<T> implements ListenableFuture<T> {

	private static class Value {}
	private static class Errored extends Value {
		public final Throwable exception;
		public Errored(Throwable exception) {
			this.exception = exception;
		}
	}
	private static class Cancellation extends Errored {
		public Cancellation(Throwable exception) {
			super(exception);
		}
	}
	private static class Success<T> extends Value {
		public final T value;
		public Success(T value) {
			this.value = value;
		}
	}
	
	private final Latch latch = new Latch();
	private final AtomicReference<Value> value = new AtomicReference<>();
	private final ConcurrentLinkedDeque<Runnable> listeners = new ConcurrentLinkedDeque<>();
	
	/**
	 * Execute the given callable, calling {@code set} if it succeeds and {@code setException}
	 * otherwise.
	 * @param callable the Callable to execute
	 */
	public void execute(Callable<T> callable) {
		try {
			set(callable.call());
		} catch (Throwable t) {
			setException(t);
		}
	}
	
	/**
	 * Execute the given callable on the given Executor, calling {@code set} if it succeeds and
	 * {@code setException} otherwise.
	 * @param callable the Callable to execute
	 */
	public void executeOn(Callable<T> callable, Executor executor) {
		executor.execute(() -> {
			try {
				set(callable.call());
			} catch (Throwable t) {
				setException(t);
			}
		});
	}
	
	/**
	 * Attempt to mark this SimpleFuture as successful, yielding the given value.
	 * @param value the value to set
	 * @throws IllegalStateException if this SimpleFuture has already been set
	 */
	public void set(T value) {
		setInner(new Success<>(value));
	}
	
	/**
	 * Attempt to mark this SimpleFuture as errored, yielding the given exception.
	 * @param exc the exception to set
	 * @throws IllegalStateException if this SimpleFuture has already been set
	 */
	public void setException(Throwable exc) {
		setInner(new Errored(exc));
	}
	
	private void setInner(Value v) {
		if (value.compareAndSet(null, v)) {
			latch.releaseAll();
			Iterator<Runnable> iter = listeners.iterator();
			while (iter.hasNext()) {
				try {
					iter.next().run();
				} catch (Throwable t) {
					LoggerFactory.getLogger("Toolbox").error("Uncaught exception in future listener", t);
				}
				iter.remove();
			}
		} else {
			throw new IllegalStateException("Future has already been resolved");
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return value.compareAndSet(null, new Cancellation(new CancellationException()));
	}

	@Override
	public boolean isCancelled() {
		return value.get() instanceof Cancellation;
	}

	@Override
	public boolean isDone() {
		return value.get() != null;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		Value v = value.get();
		if (v == null) {
			latch.await();
			v = value.get();
		}
		return doGet(v);
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		Value v = value.get();
		if (v == null) {
			latch.await(timeout, unit);
			v = value.get();
		}
		return doGet(v);
	}

	private T doGet(Value v)  throws InterruptedException, ExecutionException {
		if (v instanceof Errored) {
			throw new ExecutionException(((Errored)v).exception);
		} else if (v instanceof Success) {
			return ((Success<T>) v).value;
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public void addListener(Runnable listener, Executor executor) {
		Runnable actual = () -> executor.execute(listener);
		listeners.add(actual);
		if (isDone()) {
			try {
				actual.run();
			} catch (Throwable t) {
				LoggerFactory.getLogger("Toolbox").error("Uncaught exception in future listener", t);
			}
		}
	}

}
