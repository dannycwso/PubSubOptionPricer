package com.sod.pricing;

public interface StockPriceGenerator {
	public static final int NUMBER_TRADING_SECONDS = 7257600;
	public static final double SQR_NUMBER_TRADING_SECONDS = Math.sqrt(NUMBER_TRADING_SECONDS);
	
	double getPrice(StockInstrument stock);
}
