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

/**
 * Implements a fixed ticker with very good accuracy, adapting to the running host's sleep
 * inaccuracy and performing yielded busy-waits within the inaccuracy period.
 */
public class Ticker {
	public String id = "NONE";
	private int tickLength = 1000;
	private long lastTick = 0L;
	private long jitterGuard = 10; //Start assuming 10msec granularity, then reward good timing
	private long lastJitterTick = 0L;
	public Ticker() {
	}
	
	public Ticker(int tickLength) {
		this.tickLength = tickLength;
	}

	public void block() {
		long millis = millisToNextTick();
		while (millis>0) {
			if (millis>jitterGuard) {
				try {
					long preSleep = time();
					long expectedSleepDelta = millis-jitterGuard;
					Thread.sleep(millis-jitterGuard);
					long sleepDelta = time()-preSleep;
					long jitterTime = Math.max(0, sleepDelta-expectedSleepDelta);
					if (jitterTime<0) jitterTime=0;
					if (jitterTime>10) jitterTime=10;
					if (jitterTime>jitterGuard) {
						jitterGuard = jitterTime;
					}
					if (jitterTime<jitterGuard-1) {
						lastJitterTick++;
					} else {
						lastJitterTick = 0L;
					}
					if (lastJitterTick > 10) {
						lastJitterTick = 0L;
						jitterGuard--;
						if (jitterGuard<1) jitterGuard = 1;
					}
				} catch (InterruptedException ex) {}
			} else {
				Thread.yield(); //"It is rarely appropriate to use this method." --We'll see, Oracle. We'll see.
			}
			millis = millisToNextTick();
		}
		long cur = time();
		lastTick = cur;
	}
	
	/**
	 * Gets the approximate number of milliseconds until the next tick. This number will
	 * be negative if the tick should have already occurred, and can be used to measure
	 * how many ticks should have occurred by dividing the result by tickLength.
	 */
	public long millisToNextTick() {
		long curTick = time();
		long elapsed = curTick-lastTick;
		return tickLength - elapsed;
	}
	
	public void setTickLength(int len) {
		tickLength = len;
	}
	
	public static long time() {
		return System.nanoTime() / 1_000_000L;
	}
}
