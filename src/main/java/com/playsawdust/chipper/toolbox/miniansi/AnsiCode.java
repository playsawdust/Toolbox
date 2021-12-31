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

import java.util.List;

public enum AnsiCode implements Ansi {
	FG_BLACK(30),
	FG_RED(31),
	FG_GREEN(32),
	FG_YELLOW(33),
	FG_BLUE(34),
	FG_MAGENTA(35),
	FG_CYAN(36),
	FG_WHITE(37),
	FG_DEFAULT(39),
	
	FG_BLACK_INTENSE(90),
	FG_RED_INTENSE(91),
	FG_GREEN_INTENSE(92),
	FG_YELLOW_INTENSE(93),
	FG_BLUE_INTENSE(94),
	FG_MAGENTA_INTENSE(95),
	FG_CYAN_INTENSE(96),
	FG_WHITE_INTENSE(97),
	
	BG_BLACK(40),
	BG_RED(41),
	BG_GREEN(42),
	BG_YELLOW(43),
	BG_BLUE(44),
	BG_MAGENTA(45),
	BG_CYAN(46),
	BG_WHITE(47),
	BG_DEFAULT(49),
	
	BG_BLACK_INTENSE(100),
	BG_RED_INTENSE(101),
	BG_GREEN_INTENSE(102),
	BG_YELLOW_INTENSE(103),
	BG_BLUE_INTENSE(104),
	BG_MAGENTA_INTENSE(105),
	BG_CYAN_INTENSE(106),
	BG_WHITE_INTENSE(107),
	
	BOLD(1),
	UNDERLINE(4),
	
	LEFT_SIDE_LINE(62),
	RIGHT_SIDE_LINE(60),
	
	NEGATIVE(7),
	POSITIVE(27),
	
	BLINK(5),
	
	RESET(0),
	;
	private final List<Integer> codes;
	private final String alone;
	AnsiCode(Integer... codes) {
		this.codes = List.of(codes);
		this.alone = Ansi.Utils.toString(this);
	}
	@Override
	public List<Integer> getCodes() {
		return codes;
	}
	@Override
	public String toString() {
		return alone;
	}
}
