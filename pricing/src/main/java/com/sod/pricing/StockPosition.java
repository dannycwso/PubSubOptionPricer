package com.sod.pricing;

public class StockPosition extends SecurityPosition {
	final StockDef def;
	
	public StockPosition(StockDef def, int qty, double price)
	{
		super(qty, price);
		this.def = def;
	}
	
	@Override
	public void onTick(TickEvent tick) 
	{
		if(!def.ticker.equals(tick.getTicker()))
			throw new IllegalArgumentException("TickEvent's ticker doesn't match Instrument's, can't price the position");
		
		this.price = tick.getPrice();
		this.value = this.qty * tick.getPrice();
	}

	@Override
	public String getTicker() {return def.ticker;}
	
}
