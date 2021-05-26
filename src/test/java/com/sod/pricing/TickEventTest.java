package com.sod.pricing;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TickEventTest {

	@Test
	public void testTickerLen() {
		byte[] tick = new byte[TickEvent.MAX_TICKER_SIZE_IN_BYTE];
		String test = new String(tick);
		assertTrue(!TickEvent.isTickerTooLong(test));
		
		tick = new byte[TickEvent.MAX_TICKER_SIZE_IN_BYTE+1];
		test = new String(tick);
		assertTrue(TickEvent.isTickerTooLong(test));
    }

}
