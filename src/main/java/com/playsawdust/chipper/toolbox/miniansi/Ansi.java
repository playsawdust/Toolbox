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

package com.playsawdust.chipper.toolbox.miniansi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public interface Ansi {
	List<Integer> getCodes();

	public static final class Utils {
		public static String toString(Ansi... codes) {
			StringBuilder sb = new StringBuilder();
			toAppendableUnchecked(sb, codes);
			return sb.toString();
		}
	
		public static void toAppendableUnchecked(Appendable a, Ansi... codes) {
			try {
				toAppendable(a, codes);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	
		public static void toAppendable(Appendable a, Ansi... codes) throws IOException {
			a.append("\u001B[");
			boolean first = true;
			for (Ansi code : codes) {
				for (Integer i : code.getCodes()) {
					if (!first) {
						a.append(';');
					}
					a.append(i.toString());
					first = false;
				}
			}
			a.append('m');
		}
	}
}
