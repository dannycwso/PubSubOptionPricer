package com.sod.pricing;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aeron.Subscription;
import io.aeron.logbuffer.Header;
 
public class AeronAgentStockPriceSubscriber implements Agent {
	private static final Logger LOG = LoggerFactory.getLogger(AeronAgentStockPriceSubscriber.class);
	
    private final Subscription subscription;
    
	private final TickEventHandler tickHandler;
	private final TickEvent tickEvent = new TickEvent();
	
	public AeronAgentStockPriceSubscriber (PositionManager positionManager, Subscription subscription) 
	{
		tickHandler = positionManager;
		this.subscription = subscription;
	}
	
	void onTick(TickEvent event)
	{
		tickHandler.onTick(tickEvent);
	}

    @Override
    public int doWork() throws Exception
    {
        subscription.poll(this::handler, 10);
        return 0;
    }

    private void handler(DirectBuffer buffer, int offset, int length, Header header)
    {
		tickEvent.reset();

		tickEvent.decode(buffer, offset);		
		if(LOG.isDebugEnabled())
			LOG.debug("tick event received: symbol = {}, price = {}", tickEvent.getTicker(), tickEvent.getPrice());
		
		onTick(tickEvent);
    }
 
	@Override
	public String roleName() {
		return "StockTickReceiver";
	}
}
