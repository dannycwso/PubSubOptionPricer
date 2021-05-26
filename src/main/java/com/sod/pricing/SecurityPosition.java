package com.sod.pricing;

public abstract class SecurityPosition implements TickEventHandler {
	int qty;
	double value;
	double price;
	
	public SecurityPosition(int qty, double price)
	{
		this.qty = qty;
		this.price = price;
		this.value = qty * price;
	}
	
	public abstract void onTick(TickEvent tick);
	
	public abstract String getTicker();
}
