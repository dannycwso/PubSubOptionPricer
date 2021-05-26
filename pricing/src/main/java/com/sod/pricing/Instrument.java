package com.sod.pricing;
 
public class Instrument {

	final String ticker;
	final SecurityType type;
	
	protected double price;
	protected long updateTimeInMillis; //in milli-second.
	
	public Instrument(String ticker, SecurityType type, double s0) {
		this.ticker = ticker;
		this.type = type;
		this.price = s0;
		this.updateTimeInMillis = System.currentTimeMillis();
	}
	
	public void updatePrice(double price, long updateTimeInMillis) {
		this.price = price;
		this.updateTimeInMillis = updateTimeInMillis;
	}
	
	public void updatePrice(double price) {
		updatePrice(price, System.currentTimeMillis());
	}
	
	public double getPrice() { return price; }
	public long getUpdateTimeInMillis() {return updateTimeInMillis; }
	
}
