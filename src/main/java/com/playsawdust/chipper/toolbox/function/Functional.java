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

package com.playsawdust.chipper.toolbox.function;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Functional {

	@Nullable
	public static <T, U> U safely(@Nullable T maybe, @Nonnull Function<T, U> proc) {
		return (maybe == null) ? null : proc.apply(maybe);
	}

	public static <T, U> U safely(@Nullable T maybe, @Nonnull Function<T, U> proc, @Nonnull Supplier<U> fallback) {
		return (maybe == null) ? fallback.get() : proc.apply(maybe);
	}

	public static <T, U, V> V safely(@Nullable T maybe, @Nonnull Function<T, U> proc, @Nonnull Function<U, V> proc2, @Nonnull Supplier<V> fallback) {
		return (maybe == null) ? fallback.get() : safely(proc.apply(maybe), proc2, fallback);
	}

	public static <T, U, V, W> W safely(@Nullable T maybe, @Nonnull Function<T, U> proc, @Nonnull Function<U, V> proc2, @Nonnull Function<V, W> proc3, @Nonnull Supplier<W> fallback) {
		return (maybe == null) ? fallback.get() : safely(proc.apply(maybe), proc2, proc3, fallback);
	}

	public static <T> void safely(@Nullable T maybe, Consumer<T> proc) {
		if (maybe != null)
			proc.accept(maybe);
	}

	public static <T> void forEach(Iterable<T> iter, Consumer<T> consumer) {
		for (T t : iter) {
			consumer.accept(t);
		}
	}

	public static <T> void forEach(T[] arr, Consumer<T> consumer) {
		for (T t : arr) {
			consumer.accept(t);
		}
	}

	public static void forEach(int[] arr, IntConsumer consumer) {
		for (int i : arr) {
			consumer.accept(i);
		}
	}

	public static void forEach(long[] arr, LongConsumer consumer) {
		for (long l : arr) {
			consumer.accept(l);
		}
	}

	public static void forEach(double[] arr, DoubleConsumer consumer) {
		for (double d : arr) {
			consumer.accept(d);
		}
	}

}
