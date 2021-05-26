package com.sod.pricing;

import org.junit.Test;

public class StockInstrumentTest {
	@Test(expected = IllegalArgumentException.class)
    public void testNewStockInstrumentWithWrongTicker() {
    	new StockInstrument(null, 0, 0, 0);
    }

	@Test(expected = IllegalArgumentException.class)
    public void testNewStockInstrumentWithWrongOpenPrice() {
    	new StockInstrument("AAPL", Double.NaN, 0, 0);
    }

	@Test(expected = IllegalArgumentException.class)
    public void testNewStockInstrumentWithWrongDrift() {
    	new StockInstrument("AAPL", 0, Double.NaN, 0);
    }

	@Test(expected = IllegalArgumentException.class)
    public void testNewStockInstrumentWithWrongVol() {
    	new StockInstrument("AAPL", 0, 0, Double.NaN);
    }
	
}
