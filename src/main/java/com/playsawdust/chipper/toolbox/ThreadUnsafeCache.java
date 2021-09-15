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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.playsawdust.chipper.toolbox.lipstick.MonotonicTime;

/**
 * Represents a Map-ish data structure where mappings can be automatically loaded upon request, and automatically discarded
 * on disuse.
 * 
 * <p>This class is explicitly thread-unsafe, which allows for two important qualities:
 * <ul>
 *   <li>Certain speed optimizations can be used in common time-sensitive scenarios, like once-per-frame eviction polling.
 *   <li>Unlike guava Cache, this class can be used safely with single-threaded libraries like opengl, as long as all
 *       access happens on the same thread the library was initialized on.
 * </ul>
 * 
 * <p>If you are using automatic eviction, objects of this class will need to be polled periodically with {@link #evict(long)}.
 * Ideally this happens once per frame or physics tick, at the end of the method so that objects can be refreshed last-minute.
 */
public class ThreadUnsafeCache<K, V> {
	
	private @Nullable Function<K, V> cacheLoader;
	private @Nullable BiConsumer<K, V> evictionListener;
	private long millisBeforeEviction = -1L;
	private long lastEviction = -1L;
	private long lastPoll = -1L;
	private boolean allowsNullValues;
	private Map<K, Entry> data = new HashMap<>();
	
	public ThreadUnsafeCache() {
		cacheLoader = null;
		evictionListener = null;
	}
	
	/**
	 * Creates a new empty ThreadUnsafeCache
	 * @param cacheLoader           A Function which will be called to determine the value of unmapped keys
	 * @param evictionListener      A function that will be called when mappings are evicted
	 * @param millisBeforeEviction  Duration in milliseconds of disuse after which cache mappings will be evicted.
	 * @param allowsNullValues      If true, keys may be mapped to null, and cacheLoaders may return null. If nulls are present in mappings, they can be evicted just like anything else.
	 */
	public ThreadUnsafeCache(@Nullable Function<K, V> cacheLoader, @Nullable BiConsumer<K, V> evictionListener, long millisBeforeEviction, boolean allowsNullValues) {
		this.cacheLoader = cacheLoader;
		this.evictionListener = evictionListener;
		this.millisBeforeEviction = millisBeforeEviction;
		this.allowsNullValues = allowsNullValues;
		
		lastPoll = MonotonicTime.millis();
	}
	
	/**
	 * @see #ThreadUnsafeCache(Function, BiConsumer, long, boolean)
	 */
	public ThreadUnsafeCache(@Nullable Function<K, V> cacheLoader, @Nullable BiConsumer<K, V> evictionListener, Duration timeBeforeEviction, boolean allowsNullValues) {
		this(cacheLoader, evictionListener, millis(timeBeforeEviction), allowsNullValues);
	}
	
	/**
	 * Creates a new, empty ThreadUnsafeCache with no automatic loading of cache lines and no notification on eviction.
	 * @param millisBeforeEviction Duration in milliseconds of disuse after which cache mappings will be evicted.
	 */
	public ThreadUnsafeCache(long millisBeforeEviction) {
		this(null, null, millisBeforeEviction, false);
	}
	
	/**
	 * Creates a new, empty ThreadUnsafeCache with no automatic loading of cache lines and no notification on eviction.
	 * @param timeBeforeEviction Duration of disuse after which cache mappings will be evicted.
	 */
	public ThreadUnsafeCache(Duration timeBeforeEviction) {
		this(null, null, millis(timeBeforeEviction), false);
	}
	
	private static long millis(Duration d) {
		try {
			return d.toMillis();
		} catch (Exception ex) {
			return Long.MAX_VALUE;
		}
	}
	
	/**
	 * Evicts old entries. Equivalent to {@code evict(MonotonicTime.millis())}
	 * 
	 * @see #evict(long)
	 */
	public int evict() {
		return evict(MonotonicTime.millis());
	}
	
	/**
	 * Evicts old entries. Accepts the current monotonic time in milliseconds, and removes entries if they're older than
	 * the previously-set millisBeforeEviction. Returns the number of evicted entries.
	 * 
	 * @return the number of evicted entries
	 */
	public int evict(long now) {
		lastPoll = now;
		
		if (now-lastEviction < millisBeforeEviction) return 0; //Don't waste our time iterating through the list faster than the eviction time
		lastEviction = now;
		
		int evicted = 0;
		var iterator = data.entrySet().iterator();
		while (iterator.hasNext()) {
			var cache = iterator.next();
			
			if (now - cache.getValue().lastAccess > millisBeforeEviction) {
				evicted++;
				iterator.remove();
				
				if (evictionListener != null) evictionListener.accept(cache.getKey(), cache.getValue().value);
			}
		}
		
		return evicted;
	}
	
	/**
	 * Immediately removes the mapping for the provided key. If the key is not present this method will successfully do nothing.
	 * @param key The key for the mapping to discard
	 */
	public void evictNow(K key) {
		data.remove(key);
	}
	
	/**
	 * Discards the existing mapping for the provided key and caches a fresh copy using the cache loader. If no existing
	 * mapping is present, the discard option is a successful no-op, and this method will successfully create a new mapping.
	 * @param key The key for the mapping to discard and refresh
	 */
	public void refresh(K key) {
		data.remove(key);
		get(key);
	}
	
	/**
	 * Sets a cache item manually
	 * @param key    The key for this mapping
	 * @param value  The value for this mapping
	 */
	public void put(K key, V value) {
		if (!allowsNullValues && value == null) throw new NullPointerException("A mapping was supplied with a null value, which is not allowed for this cache.");
		data.put(key, new Entry(value, lastPoll));
	}
	
	/**
	 * Gets or loads a cache item by its key.
	 * @param key The cache key to load this item by
	 * @return A cached value if available, or a newly-loaded value if a cache loader is present, otherwise null.
	 */
	public @Nullable V get(K key) {
		Entry result = data.get(key);
		if (result!=null) {
			result.lastAccess = lastPoll;
			return result.value;
		} else if (cacheLoader!=null) {
			result = new Entry(cacheLoader.apply(key), lastPoll);
			if (!allowsNullValues && result.value == null) throw new NullPointerException("The cacheLoader Function provided a null value, which is not allowed for this cache.");
			data.put(key, result);
			return result.value;
		} else {
			return null;
		}
	}
	
	/** Holds a value and its last access timestamp per MonotonicTime.millis() */
	private class Entry {
		public V value;
		public long lastAccess;
		
		public Entry(V value, long lastAccess) {
			this.value = value;
			this.lastAccess = lastAccess;
		}
	}
}
