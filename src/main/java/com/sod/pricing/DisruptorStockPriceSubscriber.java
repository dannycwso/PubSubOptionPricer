package com.sod.pricing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
 
public class DisruptorStockPriceSubscriber implements EventHandler<TickEvent> {
	private static final Logger LOG = LoggerFactory.getLogger(DisruptorStockPriceSubscriber.class);
	
	private final TickEventHandler tickHandler;
	private final TickEvent tickEvent = new TickEvent();
	
	public DisruptorStockPriceSubscriber (PositionManager positionManager) 
	{
		tickHandler = positionManager;
	}
	
	void onTick(TickEvent event)
	{
		tickHandler.onTick(tickEvent);
	}

	@Override
	public void onEvent(TickEvent event, long sequence, boolean endOfBatch) throws Exception {
		if(LOG.isDebugEnabled())
			LOG.debug("tick event received: symbol = {}, price = {}", event.getTicker(), event.getPrice());
		
		tickEvent.reset();
		event.copyTo(tickEvent);
		onTick(tickEvent);
	}
}
