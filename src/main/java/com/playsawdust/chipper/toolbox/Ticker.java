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

import java.util.concurrent.TimeUnit;

import com.playsawdust.chipper.toolbox.lipstick.MonotonicTime;

/**
 * Implements a fixed ticker with very good accuracy, adapting to the running host's sleep
 * inaccuracy and performing yielded busy-waits within the inaccuracy period.
 */
public class Ticker {
	public String id = "NONE";
	
	private long tickLength = TimeUnit.SECONDS.toNanos(1);
	private long lastTick = 0L;
	private long jitterGuard = 10; //Start assuming 10msec granularity, then reward good timing
	private long lastJitterTick = 0L;
	
	public Ticker() {}
	
	/**
	 * @deprecated Assumes milliseconds. Use {@link #Ticker(long, TimeUnit)} instead.
	 */
	@Deprecated
	public Ticker(int tickLength) {
		this(tickLength, TimeUnit.MILLISECONDS);
	}
	
	public Ticker(long tickLength, TimeUnit unit) {
		this.tickLength = unit.toNanos(tickLength);
	}

	public void block() {
		long nanos = nanosToNextTick();
		while (nanos > 0) {
			if (nanos > TimeUnit.MILLISECONDS.toNanos(jitterGuard)) {
				try {
					long preSleep = MonotonicTime.nanos();
					long expectedSleepDelta = nanos-TimeUnit.MILLISECONDS.toNanos(jitterGuard);
					TimeUnit.NANOSECONDS.sleep(expectedSleepDelta);
					long sleepDelta = MonotonicTime.nanos()-preSleep;
					long jitterTime = TimeUnit.NANOSECONDS.toMillis(Math.max(0, sleepDelta-expectedSleepDelta));
					if (jitterTime < 0) jitterTime = 0;
					if (jitterTime > 10) jitterTime=10;
					if (jitterTime > jitterGuard) {
						jitterGuard = jitterTime;
					}
					if (jitterTime < jitterGuard-1) {
						lastJitterTick++;
					} else {
						lastJitterTick = 0L;
					}
					if (lastJitterTick > 10) {
						lastJitterTick = 0L;
						jitterGuard--;
						if (jitterGuard < 0) jitterGuard = 0;
					}
				} catch (InterruptedException ex) {}
			} else {
				Thread.yield(); //"It is rarely appropriate to use this method." --We'll see, Oracle. We'll see.
			}
			nanos = nanosToNextTick();
		}
		long cur = MonotonicTime.nanos();
		lastTick = cur;
	}
	
	/**
	 * Gets the approximate number of nanoseconds until the next tick. This number will
	 * be negative if the tick should have already occurred, and can be used to measure
	 * how many ticks should have occurred by dividing the result by tickLength.
	 */
	public long nanosToNextTick() {
		long curTick = MonotonicTime.nanos();
		long elapsed = curTick-lastTick;
		return tickLength - elapsed;
	}
	
	/**
	 * Gets the approximate number of milliseconds until the next tick. This number will
	 * be negative if the tick should have already occurred, and can be used to measure
	 * how many ticks should have occurred by dividing the result by tickLength.
	 */
	public long millisToNextTick() {
		return TimeUnit.NANOSECONDS.toMillis(nanosToNextTick());
	}
	
	/**
	 * @deprecated Assumes milliseconds. Use {@link #setTickLength(long, TimeUnit)} instead.
	 */
	@Deprecated
	public void setTickLength(int len) {
		setTickLength(len, TimeUnit.MILLISECONDS);
	}
	
	public void setTickLength(long len, TimeUnit unit) {
		tickLength = unit.toNanos(len);
	}
	
	/**
	 * @deprecated Use {@link MonotonicTime#millis} instead.
	 */
	@Deprecated
	public static long time() {
		return MonotonicTime.millis();
	}
}
