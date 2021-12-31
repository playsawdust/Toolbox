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

package com.playsawdust.chipper.toolbox;

import java.util.Locale;
import java.util.regex.Pattern;

public class MoreStrings {
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\p{javaWhitespace}");
	
	private MoreStrings() {}
	
	/**
	 * Get the "first word" in the given String; that is, all the characters occuring before the
	 * first {@link Character#isWhitespace whitespace}.
	 */
	public static String firstWord(String s) {
		return WHITESPACE_PATTERN.split(s, 2)[0];
	}

	/**
	 * Get everything <em>except</em> the "first word" in the given String; that is, all the
	 * characters occuring after the first {@link Character#isWhitespace whitespace}.
	 */
	public static String exceptFirstWord(String s) {
		String[] split = WHITESPACE_PATTERN.split(s, 2);
		return split[split.length-1];
	}

	/**
	 * Remove {@code prefixLen} chars from the beginning of {@code s}.
	 * @param s the string to truncate
	 * @param prefixLen how many chars to remove
	 * @return the truncated string
	 */
	public static String removePrefix(String s, int prefixLen) {
		if (prefixLen<=0) return s;
		if (prefixLen>=s.length()) return "";
		return s.substring(prefixLen);
	}

	/**
	 * Verify that {@code s} begins with {@code prefix}, then remove {@code prefix.length()} chars
	 * from the beginning as if by {@code removePrefix(s, prefix.length())}.
	 * @param s the string to truncate
	 * @param prefix the prefix to remove
	 * @return the truncated string
	 * @throws IllegalArgumentException if the string does not have that prefix
	 */
	public static String removePrefix(String s, String prefix) {
		if (!s.startsWith(prefix)) throw new IllegalArgumentException("Cannot remove nonexistent prefix '"+prefix+"' from '"+s+"'");
		return removePrefix(s, prefix.length());
	}
	
	/**
	 * Remove {@code suffixLen} chars from the end of {@code s}.
	 * @param s the string to truncate
	 * @param suffixLen how many chars to remove
	 * @return the truncated string
	 */
	public static String removeSuffix(String s, int suffixLen) {
		if (suffixLen<=0) return s;
		if (suffixLen>=s.length()) return "";
		return s.substring(0, s.length()-suffixLen);
	}

	/**
	 * Verify that {@code s} ends with {@code suffix}, then remove {@code suffix.length()} chars
	 * from the end as if by {@code removeSuffix(s, suffix.length())}.
	 * @param s the string to truncate
	 * @param suffix the suffix to remove
	 * @return the truncated string
	 * @throws IllegalArgumentException if the string does not have that suffix
	 */
	public static String removeSuffix(String s, String suffix) {
		if (!s.endsWith(suffix)) throw new IllegalArgumentException("Cannot remove nonexistent suffix '"+suffix+"' from '"+s+"'");
		return removeSuffix(s, suffix.length());
	}

	/**
	 * Takes either a set of words (e.g. "set of words") or an enum constant (e.g. "SET_OF_WORDS"),
	 * and formats it as one word in TitleCase, where the first letter of each "source" word is
	 * capitalized (e.g. "SetOfWords").
	 * @param in the name of an enum constant, or a set of words
	 * @return one word, capitalized for legibility and pretty printing
	 */
	public static String formatTitleCase(String in) {
		String[] pieces = new String[] { in };
		if (in.contains(" ")) {
			pieces = in.toLowerCase().split(" ");
		} else if (in.contains("_")) {
			pieces = in.toLowerCase().split("_");
		}

		StringBuilder result = new StringBuilder();
		for (String s : pieces) {
			if (s == null)
				continue;
			String t = s.trim().toLowerCase(Locale.ROOT);
			if (t.isEmpty())
				continue;
			result.append(Character.toUpperCase(t.charAt(0)));
			if (t.length() > 1)
				result.append(t.substring(1));
		}
		return result.toString();
	}
	
}
