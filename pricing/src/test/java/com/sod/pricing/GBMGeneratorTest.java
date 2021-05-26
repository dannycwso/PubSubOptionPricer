package com.sod.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.util.Precision;
import org.junit.Test;

public class GBMGeneratorTest {

	@Test
    public void testGetPriceNeverNegative() {
		
		assertEquals(10 * (1 + (0.8*2)+(0.03 * -10 * Math.sqrt(2))), StockPriceGBMGeneratorImpl.randomWalk(10, 2, 0.8, 0.03, -10), 0.00000001);
		double price = 10 * (1 + (-0.8*2)+(0.03 * -10 * Math.sqrt(2))); //drift is -0.8 as below
		assertTrue(price < 0D); //should be -ve
		price = StockPriceGBMGeneratorImpl.randomWalk(10, 2, -0.8, 0.03, -10); //
		assertEquals(0, price, Precision.EPSILON); //the function does perform -ve check and set it to 0
	}
}
