package com.sod.pricing;


import static org.apache.commons.math3.util.Precision.EPSILON;

import java.util.Random;

import org.apache.commons.math3.util.Precision;
 

public class StockInstrument extends Instrument {
	
	private Random rnd; //each stock has its own pseudo-series 
	
	//param for GBM price gen
	final double driftInSec;
	final double volInSec;
 
	public StockInstrument (String ticker, double s0, double driftInYear, double volInYear) 
	{		
		super(ticker, SecurityType.STOCK, s0);
		this.driftInSec = driftInYear / StockPriceGenerator.NUMBER_TRADING_SECONDS;
		this.volInSec = volInYear / StockPriceGenerator.SQR_NUMBER_TRADING_SECONDS;
		
		if(ticker == null || ticker.length() == 0)
			throw new IllegalArgumentException();
		
		if(!(Precision.compareTo(s0, 0, EPSILON) > 0))
			throw new IllegalArgumentException("open price should be > 0");

		if(!(Precision.compareTo(volInSec, 0, EPSILON) > 0) )
			throw new IllegalArgumentException("volInSec should be > 0");
		
		if(!Double.isFinite(driftInSec))
			throw new IllegalArgumentException("expected return must be a number");

	}
	
	public void setRandomGenerator(Random rnd) 
	{
		this.rnd = rnd;
	}

	public Random getRandomGenerator() 
	{
		return rnd;
	}
	
}
