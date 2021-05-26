package com.sod.pricing;

import org.apache.commons.math3.util.Precision;
/*
 * StockPriceGBMGeneratorImpl is to simulate stock price "random-walk" in Geometric Brownian motion 
 */
public class StockPriceGBMGeneratorImpl implements StockPriceGenerator{

	@Override
	public double getPrice(StockInstrument stock) { //random-walk stock price in dT seconds
		if(!Double.isFinite(stock.getPrice()) || !Double.isFinite(stock.driftInSec) || !Double.isFinite(stock.volInSec)) 
			throw new IllegalArgumentException("The stock parameter is not set properly, can't random-walk..." + stock);
		
		double deltaT = (System.currentTimeMillis() - stock.updateTimeInMillis)/1000;  //in sec
		return randomWalk(stock.getPrice(), deltaT, stock.driftInSec, stock.volInSec, stock.getRandomGenerator().nextGaussian());
	}

	public static double randomWalk(double s0, double deltaTInSec, double driftInSec, double volInSec, double gaussian) {
		double price = s0 * (1 + (driftInSec*deltaTInSec)+(volInSec * gaussian * Math.sqrt(deltaTInSec)));
		if(price < 0D)
			return Precision.EPSILON;
		
		return price;		
	}
}
