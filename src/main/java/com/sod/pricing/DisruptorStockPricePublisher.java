package com.sod.pricing;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.RingBuffer;

public class DisruptorStockPricePublisher extends StockPricePublisher implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(DisruptorStockPricePublisher.class);
	
	private final RingBuffer<TickEvent> eventRingBuffer;
	private final long eventTimeInMills1, eventTimeInMills2;
	
	public DisruptorStockPricePublisher(RingBuffer<TickEvent> ringBuffer, long eventTimeInMills1, long eventTimeInMills2) 
	{
		eventRingBuffer = ringBuffer;	
		this.eventTimeInMills1 = eventTimeInMills1;
		this.eventTimeInMills2 = eventTimeInMills2;
        if(eventTimeInMills1 >= eventTimeInMills2) {
        	throw new IllegalArgumentException("sleep upper bound is smaller or equal to lower bound");
        }

	}
 	
	@Override
	public void run() 
	{
		try
		{
			for(;;) 
			{
				//Randomly sleep for a range of time in milli-seconds
				Thread.sleep(ThreadLocalRandom.current().nextLong(eventTimeInMills1, eventTimeInMills2));
				//Randomly select a stock to random-walk and then publish tick event
				sendTickEvent(doStockRandomWalk());
			}
		} catch (InterruptedException ex) {
			LOG.error("-------- Unexpected interrupt to end the DisruptorStockPricePublisher --------------");
		}
	}
	
	@Override
	protected int sendTickEvent(StockInstrument tick) {
		long sequence = eventRingBuffer.next();
		TickEvent event = eventRingBuffer.get(sequence);
		event.updateEvent(tick.ticker, tick.getPrice());
		// make the event available to EventProcessors
		eventRingBuffer.publish(sequence);
		return 0;
	}
}
