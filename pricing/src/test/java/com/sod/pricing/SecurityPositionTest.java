package com.sod.pricing;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.util.Precision;
import org.junit.Test;

public class SecurityPositionTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void testStockPositionUpdate() {
		StockPosition sp = new StockPosition(new StockDef("XYZ", 0.23D, 0.3D, 10D), 1000, 10D);
		assertEquals("Invalid init position value", sp.price * sp.qty, sp.value, Precision.EPSILON);
		
		TickEvent tick = new TickEvent();
		tick.updateEvent("XYZ", 11.9);
		sp.onTick(tick);
		assertEquals("Invalid updated position price", 11.9, sp.price, Precision.EPSILON);
		assertEquals("Invalid updated position value", sp.price * sp.qty, sp.value, Precision.EPSILON);
		
		tick.reset();
		tick.updateEvent("ABC", 11.9);
		sp.onTick(tick);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOptionPositionUpdate() {
		EurOptionPosition sp = new EurOptionPosition(new OptionDef("TSLA-CALL", SecurityType.CALL_EUR, "TSLA", 550D, 0.4D, 0.5, 0.01, Double.NaN), 1000, 87.50);
		assertEquals("Invalid init position value", sp.price * sp.qty, sp.value, Precision.EPSILON);
		
		TickEvent tick = new TickEvent();
		tick.updateEvent("TSLA", 590D); //reference the BSOptionPricerTest.java, option price come from that case
		sp.onTick(tick);
		assertEquals("Invalid updated position price", 87.55, sp.price, 0.00999999);
		assertEquals("Invalid updated position value", sp.price * sp.qty, sp.value, Precision.EPSILON);
		
		tick.reset();
		tick.updateEvent("ABC", 11.9);
		sp.onTick(tick);
	}

}
