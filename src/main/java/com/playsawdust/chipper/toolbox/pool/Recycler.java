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

package com.playsawdust.chipper.toolbox.pool;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Convenience class for managing many {@link PooledObject}s at once
 * using a try-with-resources. For example:
 * <pre>
 * try (Recycler r = Recycler.get()) {
 *     ProtoColor color = r.add(ProtoColor.fromRGB(0xFFFFFF));
 *     Rect r = r.add(Rect.fromEdges(5, 5, 25, 25));
 *     // do something
 * }
 * // the color and rect are recycled automatically
 * </pre>
 * @see {@link com.google.common.io.Closer Closer}, the Closeable equivalent from Guava that
 * 		inspired this class
 */
public class Recycler implements PooledObject {
	private static final ObjectPool<Recycler> pool = new ObjectPool<>(Recycler::new);

	private List<PooledObject> list = Lists.newArrayList();

	private Recycler() {}

	@Override
	public void recycle() {
		for (PooledObject o : list) o.recycle();
		list.clear();
		pool.recycle(this);
	}

	public <T extends PooledObject> T add(T t) {
		list.add(t);
		return t;
	}

	public static Recycler get() {
		return pool.get();
	}

}
