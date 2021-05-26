package com.sod.pricing;


import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.YieldingWaitStrategy;

/*
 * Main class to launch the implementation to use Disruptor ring buffer as the inter-thread communicatino channel between stock tick publisher and stock tick handler/subscriber  
 */
public class DiscruptorMain {
	private static final Logger LOG = LoggerFactory.getLogger(DiscruptorMain.class);
	

	public static void main(String[] args) throws Exception
	{

		final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
		
        SigInt.register(() ->
        {
        	barrier.signal();
        });

		RingBuffer<TickEvent> ringBuffer = RingBuffer.createSingleProducer(TickEvent.FACTORY, 32, new YieldingWaitStrategy());
		SequenceBarrier seqBarrier = ringBuffer.newBarrier();

        /*
         * Per requirement, can use SQLlite DB to store security definition. But the portfolio is stored in CSV.
         * Actually, the security definition table is not that complicated. There is no need to create 2 tables for stock and option definition respectively
         * Thus, can also model the security definition as another CSV file (This is my initial approach)
         * Below flag is to control loading security definition from SQLite or CSV. By default, it is to comply with the requirement
         * Pls note, the portfolio is always loaded from CSV regardless of below boolean flag
         */		
		boolean useSQLite2InitSecurity = true;
		PositionManager mgr = new PositionManager();
		mgr.initialize(useSQLite2InitSecurity);
		BatchEventProcessor<TickEvent> eventProcessor = new BatchEventProcessor<TickEvent>(ringBuffer, seqBarrier, new DisruptorStockPriceSubscriber(mgr));
		ringBuffer.addGatingSequences(eventProcessor.getSequence());  
		LOG.info("starting");
		new Thread(eventProcessor).start();
		
		//By default, send tick every 0.5 sec (in mills) to 2 sec.
		//TODO: change to accept runtime parameters to control tick frequency. Can be used to compare the performance of using Discruptor and Aeron IPC
		DisruptorStockPricePublisher publisher = new DisruptorStockPricePublisher(ringBuffer, SleepingRandomMillisIdleStrategy.DEFAULT_LOWER_BOUND_SLEEP_PERIOD_MS, SleepingRandomMillisIdleStrategy.DEFAULT_UPPER_BOUND_SLEEP_PERIOD_MS); 
		//DisruptorStockPricePublisher publisher = new DisruptorStockPricePublisher(ringBuffer, Integer.parseInt(args[0]), Integer.parseInt(args[1])); 
		publisher.initialize(useSQLite2InitSecurity);
		new Thread(publisher).start();;
		
		barrier.await();
		System.exit(0);
	}

}
