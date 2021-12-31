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

package com.playsawdust.chipper.toolbox.collect.memo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.checkerframework.checker.guieffect.qual.SafeType;
import org.checkerframework.checker.lock.qual.GuardedBy;

/**
 * Backed by a HashMap and a supplier Function, this is a lazily-generated
 * mapping from Ks to Vs. This works great for registries of objects which
 * either aren't known in advance or are undesirable to generate all at once.
 * @see java.util.Map
 * @param <K> The type of keys maintained by this map
 * @param <V> The type of mapped values
 */
@SafeType
public class MemoMap<K,V> implements Map<K,V> {
	@GuardedBy("<self>")
	private final Map<K,V> memo;
	private final Function<K,V> supplier;

	public MemoMap(Function<K,V> supplier) {
		this.supplier = supplier;
		this.memo = getMemo();
	}

	protected Map<K,V> getMemo() {
		return new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		synchronized (memo) {
			if (memo.containsKey(key)) return memo.get(key);
			try {
				V v = supplier.apply((K)key);
				memo.put((K)key, v);
				return v;
			} catch (ClassCastException ex) {
				return null;
			}
		}
	}

	@Override
	public V put(K key, V value) {
		synchronized (memo) {
			return memo.put(key, value);
		}
	}

	@Override
	public Set<K> keySet() {
		synchronized (memo) {
			return memo.keySet();
		}
	}

	@Override
	public Collection<V> values() {
		synchronized (memo) {
			return memo.values();
		}
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		synchronized (memo) {
			return memo.entrySet();
		}
	}

	/**
	 * This method behaves differently from a standard HashMap, in that it determines
	 * whether there is a <em>currently memoized</em> value for the key. It is entirely
	 * possible to get false from <code>containsKey</code> and then retrieve a non-null value from
	 * {@link #get(Object) get} for the same key.
	 */
	@Override
	public boolean containsKey(Object key) {
		synchronized(memo) {
			return memo.containsKey(key);
		}
	}

	@Override
	public void clear() {
		synchronized (memo) {
			memo.clear();
		}
	}

	/**
	 * Contract is slightly different from {@link java.util.Map Map}.
	 * <p>
	 * This reports the number of currently memoized keys.
	 */
	@Override
	public int size() {
		return memo.size();
	}

	/**
	 * Contract is slightly different from {@link java.util.Map Map}.
	 * <p>
	 * This reports true if no keys are currently memoized.
	 */
	@Override
	public boolean isEmpty() {
		return memo.isEmpty();
	}

	/**
	 * Contract is slightly different from {@link java.util.Map Map}.
	 * <p>
	 * This reports true if the value is currently memoized.
	 */
	@Override
	public boolean containsValue(Object value) {
		return memo.containsValue(value);
	}

	/**
	 * Contract is slightly different from {@link java.util.Map Map}.
	 * <p>
	 * This removes any currently memoized value. However, when
	 * {@link #get(Object) get} is called, a new value may be generated!
	 */
	@Override
	public V remove(Object key) {
		synchronized (memo) {
			return memo.remove(key);
		}
	}

	/**
	 * Bypasses the generator function, filling in values.
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		synchronized (memo) {
			memo.putAll(m);
		}
	}
}