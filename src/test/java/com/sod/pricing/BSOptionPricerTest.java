package com.sod.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BSOptionPricerTest {
	
	@Test
    public void testCalculateOption() {
		
		InstrumentPricer pricer = new BSOptionPricerImpl();
		TickEvent tick = new TickEvent();
		tick.updateEvent("TSLA",590D);
		
		//The expected price is gotten from the URL, https://www.mystockoptions.com/black-scholes.cfm?ticker=&s=590&x=550&t=0.5&r=1%25&v=40%25&calculate=Calculate
		double price = pricer.getPrice(tick, new OptionDef("TSLA-CALL", SecurityType.CALL_EUR, "TSLA", 550D, 0.4D, 0.5, 0.01, 550D));
		assertEquals("BSOption Call Price is incorrect", 87.55, price, 0.00999999);

		price = pricer.getPrice(tick, new OptionDef("TSLA-PUT", SecurityType.PUT_EUR, "TSLA", 550D, 0.4D, 0.5, 0.01, 550D));
		assertEquals("BSOption Put Price is incorrect", 44.81, price, 0.00999999);

		tick.updateEvent("TSLA",500.99);
		price = pricer.getPrice(tick, new OptionDef("TSLA-CALL", SecurityType.CALL_EUR, "TSLA", 550D, 0.4D, 0.5, 0.01, 550D));
		assertEquals("BSOption Call Price is incorrect", 38.64, price, 0.00999999);

		price = pricer.getPrice(tick, new OptionDef("TSAL-PUT", SecurityType.PUT_EUR, "TSLA", 550D, 0.4D, 0.5, 0.01, 550D));
		assertEquals("BSOption Put Price is incorrect", 84.91, price, 0.00999999);
		
		price = pricer.getPrice(tick, new OptionDef("TSLA-PUT", SecurityType.PUT_EUR, "TSLA", Double.NaN, 0.4D, 0.5, 0.01, 550D));
		assertTrue("Price is expected not to price because of invalid param", Double.isNaN(price));

		price = pricer.getPrice(tick, new OptionDef("TSLA-PUT", SecurityType.PUT_EUR, "TSLA", 550D, 0.4D, Double.NaN, 0.01, 550D));
		assertTrue("Price is expected not to price because of invalid param", Double.isNaN(price));

	}
}
