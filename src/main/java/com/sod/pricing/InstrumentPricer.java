package com.sod.pricing;

public interface InstrumentPricer {

	double getPrice(TickEvent event, SecurityDef instrument);
}
