package com.sod.pricing;


/*
 * Main class to launch the implementation to use Aeron IPC as the inter-thread communicatino channel between stock tick publisher and stock tick handler/subscriber  
 */
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.agrona.concurrent.YieldingIdleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

public class AeronAgentRunnerMain {
	
	private static final Logger LOG = LoggerFactory.getLogger(AeronAgentRunnerMain.class);

	public static void main(String[] args) throws Exception
	{		
        final String channel = "aeron:ipc";
        final int stream = 10;

		//By default, send tick every 0.5 sec (in mills) to 2 sec.
		//TODO: change to accept runtime parameters to control tick frequency. Can be used to compare the performance of using Discruptor and Aeron IPC
        final IdleStrategy idleStrategySend = new SleepingRandomMillisIdleStrategy(SleepingRandomMillisIdleStrategy.DEFAULT_LOWER_BOUND_SLEEP_PERIOD_MS, SleepingRandomMillisIdleStrategy.DEFAULT_UPPER_BOUND_SLEEP_PERIOD_MS);
        //final IdleStrategy idleStrategySend = new SleepingRandomMillisIdleStrategy(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        final IdleStrategy idleStrategyReceive = new YieldingIdleStrategy();
        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

        final MediaDriver.Context mediaDriverCtx = new MediaDriver.Context()
                .dirDeleteOnStart(true)
                .threadingMode(ThreadingMode.SHARED)
                .sharedIdleStrategy(new BusySpinIdleStrategy())
                .dirDeleteOnShutdown(true);
        final MediaDriver mediaDriver = MediaDriver.launchEmbedded(mediaDriverCtx);

        final Aeron.Context aeronCtx = new Aeron.Context()
                .aeronDirectoryName(mediaDriver.aeronDirectoryName());
        final Aeron aeron = Aeron.connect(aeronCtx);

        LOG.info("Dir {}", mediaDriver.aeronDirectoryName());

        //construct the subs and pubs
        final Subscription subscription = aeron.addSubscription(channel, stream);
        final Publication publication = aeron.addPublication(channel, stream);
 
        /*
         * Per requirement, can use SQLlite DB to store security definition. But the portfolio is stored in CSV.
         * Actually, the security definition table is not that complicated. There is no need to create 2 tables for stock and option definition respectively
         * Thus, can also model the security definition as another CSV file (This is my initial approach)
         * Below flag is to control loading security definition from SQLite or CSV. By default, it is to comply with the requirement
         * Pls note, the portfolio is always loaded from CSV regardless of below boolean flag. 
         */
        boolean useSQLite2InitSecurity = true; 
        //construct the agents
        final AeronAgentStockPricePublisher publisher = new AeronAgentStockPricePublisher(publication);
        publisher.initialize(useSQLite2InitSecurity);
        
		//PositionManager mgr = new PositionManager();
        PositionManager mgr = new PositionManager();
		mgr.initialize(useSQLite2InitSecurity);
        final AeronAgentStockPriceSubscriber subscriber = new AeronAgentStockPriceSubscriber(mgr, subscription);

        SigInt.register(() ->
        {
        	barrier.signal();
        });
        
        final AgentRunner pubAgentRunner = new AgentRunner(idleStrategySend,
                Throwable::printStackTrace, null, publisher);
        final AgentRunner subAgentRunner = new AgentRunner(idleStrategyReceive,
                Throwable::printStackTrace, null, subscriber);
        LOG.info("starting");
        //start the runners
        AgentRunner.startOnThread(pubAgentRunner);
        AgentRunner.startOnThread(subAgentRunner);

        //wait for the final item to be received before closing
        barrier.await();

        //close the resources
        subAgentRunner.close();
        pubAgentRunner.close();
        aeron.close();
        mediaDriver.close();	
		
	}

}
