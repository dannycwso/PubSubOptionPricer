package com.sod.pricing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EurOptionPosition extends SecurityPosition {
	private static final Logger LOG = LoggerFactory.getLogger(EurOptionPosition.class);

	final OptionDef def;
	final InstrumentPricer optionPricer = new BSOptionPricerImpl();	
	
	public EurOptionPosition(OptionDef def, int qty, double price)
	{
		super(qty, price);
		this.def = def;
	}

	@Override
	public void onTick(TickEvent tick) {
		if(!def.underlyingTicker.equals(tick.getTicker()))
			throw new IllegalArgumentException("TickEvent's ticker doesn't match Option's underlying ticker, can't price the position");

		this.price = optionPricer.getPrice(tick, def);
		if(LOG.isDebugEnabled())
			LOG.debug("Option price was calculated for {} = {}", def.ticker, price);
		
		this.value = this.price * this.qty;
	}

	@Override
	public String getTicker() {
		return def.ticker;
	}

}
