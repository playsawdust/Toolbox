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

import com.playsawdust.chipper.toolbox.exception.RecycledObjectException;

public interface PooledObject extends AutoCloseable {

	/**
	 * Mark this object as recycled and place it back into the pool. The object
	 * must not be used after this method is called.
	 * <p>
	 * If the object is used anyway, on a <i>best effort basis</i>, a
	 * {@link RecycledObjectException} will be thrown. This behavior is for
	 * detecting bugs only and must not be relied upon. Due to the nature of
	 * object pooling, it is not always possible to detect when an object is
	 * improperly used after being recycled.
	 */
	void recycle();

	/**
	 * @deprecated Implemented to conform to AutoCloseable. Use {@link #recycle}
	 * 		instead.
	 */
	@Override
	@Deprecated
	default void close() {
		recycle();
	}

}
