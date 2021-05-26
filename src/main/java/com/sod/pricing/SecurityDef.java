package com.sod.pricing;

public abstract class SecurityDef {
	
	final String ticker;
	final SecurityType type;
	final double openPrice;
	
	public SecurityDef(String ticker, SecurityType type, double openPrice) {
		this.ticker = ticker;
		this.type = type;
		this.openPrice = openPrice;
	}
}
