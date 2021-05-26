package com.sod.pricing;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SleepingRandomMillisIdleStrategyTest {

	@Test
    public void testRandomSleep() {
		SleepingRandomMillisIdleStrategy strategy = new SleepingRandomMillisIdleStrategy(SleepingRandomMillisIdleStrategy.DEFAULT_LOWER_BOUND_SLEEP_PERIOD_MS, SleepingRandomMillisIdleStrategy.DEFAULT_UPPER_BOUND_SLEEP_PERIOD_MS);
		for(int i = 0; i < 5; i++) { //tried 1000 times before, in real test just try 5 times to take 10 sec to run, at most
			long beforeSleep = System.currentTimeMillis();
			strategy.idle();
			long interval = System.currentTimeMillis() - beforeSleep;
			//minus 10 millis in case thread doesn't wake up sharp.
			assertTrue("SleepingRandomMillisIdleStrategy sleeps " + interval + " millis, less than expected", (interval - (SleepingRandomMillisIdleStrategy.DEFAULT_LOWER_BOUND_SLEEP_PERIOD_MS-10)) >= 0 );
			//add extra 10 millis in case thread doesn't wake up sharp.
			assertTrue("SleepingRandomMillisIdleStrategy sleeps " + interval + "millis, longer than expected", (SleepingRandomMillisIdleStrategy.DEFAULT_UPPER_BOUND_SLEEP_PERIOD_MS+10 - interval) >= 0 );

		}
	}
}
