package com.sod.pricing;

import static org.apache.commons.math3.util.Precision.EPSILON;

import org.apache.commons.math3.util.Precision;

public class StockDef extends SecurityDef{
	
	final double drift;
	final double vol;
	
	public StockDef(String ticker, double drift, double vol, double openPrice) {
		super(ticker, SecurityType.STOCK, openPrice);
		this.drift = drift;
		this.vol = vol;
		
		if(ticker == null || ticker.length() == 0)
			throw new IllegalArgumentException("ticker: " + ticker + " is invalid");
		
		if(!(Precision.compareTo(openPrice, 0, EPSILON) > 0) )
			throw new IllegalArgumentException("open price should be > 0");
		
	}
}
