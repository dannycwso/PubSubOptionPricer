package com.sod.pricing;

import org.junit.Test;

public class SecurityDefTest {

	@Test(expected = IllegalArgumentException.class)
    public void testNewStockDefWithEmptyTicker() {
    	new StockDef("", 0.0D, 0.0D, Double.NaN );
    }

	@Test(expected = IllegalArgumentException.class)
    public void testNewOptionDefWithEmptyTicker() {
    	new OptionDef("", SecurityType.CALL_EUR, "TSLA", 550D, 0.4D, 0.5, 0.01, Double.NaN);
    }

    public void testNewOptionDefWithEmptyUnderTicker() {
    	new OptionDef("TSLA-C", SecurityType.CALL_EUR, "", 550D, 0.4D, 0.5, 0.01, Double.NaN);
    }

    public void testNewOptionDefWithSameTickers() {
    	new OptionDef("TSLA", SecurityType.CALL_EUR, "TSLA", 550D, 0.4D, 0.5, 0.01, 0D);
    }

}
