package com.sod.pricing;
 
import java.nio.ByteBuffer;

import org.agrona.concurrent.Agent;
import org.agrona.concurrent.UnsafeBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.aeron.Publication;

public class AeronAgentStockPricePublisher extends StockPricePublisher implements Agent {
	private static final Logger LOG = LoggerFactory.getLogger(AeronAgentStockPricePublisher.class);

	private final Publication publication;
    private final UnsafeBuffer unsafeBuffer;
    	
	private final TickEvent currrentEvent = new TickEvent();
	
	public AeronAgentStockPricePublisher(final Publication publication)
	{
        this.publication = publication;
        this.unsafeBuffer = new UnsafeBuffer(ByteBuffer.allocate(TickEvent.MAX_BYTE_BUFFER_SIZE));		
	}
	
	public int sendTickEvent(StockInstrument tick) {
		currrentEvent.reset();
		currrentEvent.updateEvent(tick.ticker, tick.getPrice());
		
        if (publication.isConnected())
        {
            if (publication.offer(unsafeBuffer) > 0)
            {
            	currrentEvent.encode(unsafeBuffer);
            }
        }
        return 0;

	}

	@Override
	public int doWork() throws Exception {
		return sendTickEvent(doStockRandomWalk());
	}

	@Override
	public String roleName() {
		return "StockTickPublisher";
	}
}
