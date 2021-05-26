package com.sod.pricing;

import org.apache.commons.math3.util.Precision;
import static org.apache.commons.math3.util.Precision.EPSILON;;

public class OptionDef extends SecurityDef{
	final String underlyingTicker;
	final double strike;
	final double vol;
	final double maturity;
	final double rate;
	
	public OptionDef(String ticker, SecurityType type, String underlyingTicker, double strike, double vol, double maturity, double rate, double openPrice) {
		super(ticker, type, openPrice);
		this.underlyingTicker = underlyingTicker;
		this.strike = strike;
		this.vol = vol;
		this.maturity = maturity;
		this.rate = rate;
		
		if(type != SecurityType.CALL_EUR && type != SecurityType.PUT_EUR )
			throw new IllegalArgumentException("SecurityType: " + type + " is invalid");
		
		if(ticker == null || ticker.length() == 0 || underlyingTicker == null || underlyingTicker.length() == 0)
			throw new IllegalArgumentException("ticker: " + ticker + " or underlying ticker: " + underlyingTicker + " is invalid");
		else if(ticker.equalsIgnoreCase(underlyingTicker))
			throw new IllegalArgumentException("ticker: " + ticker + " and underlying ticker: " + underlyingTicker + " are same");
		
		if(!(Precision.compareTo(strike, 0, EPSILON) > 0))
			throw new IllegalArgumentException("strike should be > 0");
		
		if(!(Precision.compareTo(vol, 0, EPSILON) > 0))
			throw new IllegalArgumentException("implied vol should be > 0");
		
		if(!(Precision.compareTo(maturity, 0, EPSILON) > 0))
			throw new IllegalArgumentException("maturity should be > 0");
		
		if(!(Precision.compareTo(openPrice, 0, EPSILON) > 0))
			throw new IllegalArgumentException("open price should be > 0");
	}
}
