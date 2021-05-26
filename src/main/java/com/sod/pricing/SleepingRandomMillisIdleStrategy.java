package com.sod.pricing;

import java.util.concurrent.ThreadLocalRandom;

import org.agrona.concurrent.IdleStrategy;

/**
 * When idle this strategy is to sleep for a random period time in milliseconds. The range of period time depends on the parameters passed in
 * This implementation is only for AeronAgentRunnerMain implementation.
 * <p>
 * This class uses {@link Thread#sleep(long)} to idle.
 */
public final class SleepingRandomMillisIdleStrategy implements IdleStrategy
{
    /**
     * Name to be returned from {@link #alias()}.
     */
    public static final String ALIAS = "sleep-ms";

    /**
     * Default sleep period when the default constructor is used.
     */
    public static final long DEFAULT_LOWER_BOUND_SLEEP_PERIOD_MS = 500;
    public static final long DEFAULT_UPPER_BOUND_SLEEP_PERIOD_MS = 2000;

    
    private final long sleepPeriodMs;
    private final long sleepPeriodMs2;

    
    /**
     * Constructed a new strategy that will sleep randomly for a given range period when idle.
     *
     * @param sleepPeriodMs the lower-bound time(inclusive) milliseconds for which the strategy will sleep when work count is 0.
     * @param sleepPeriodMs2 the upper-bound time(exclusive) in milliseconds for which the strategy will sleep when work count is 0.
     */
    public SleepingRandomMillisIdleStrategy(final long sleepPeriodMs, final long sleepPeriodMs2)
    {
        this.sleepPeriodMs = sleepPeriodMs;
        this.sleepPeriodMs2 = sleepPeriodMs2;
        if(sleepPeriodMs >= sleepPeriodMs2) {
        	throw new IllegalArgumentException("sleep upper bound is smaller or equal to lower bound");
        }
    }

    /**
     *  {@inheritDoc}
     */
    public void idle(final int workCount)
    {
        if (workCount > 0)
        {
            return;
        }

        try
        {
        	Thread.sleep(ThreadLocalRandom.current().nextLong(sleepPeriodMs, sleepPeriodMs2));
        }
        catch (final InterruptedException ignore)
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     *  {@inheritDoc}
     */
    public void idle()
    {
        try
        {
        	Thread.sleep(ThreadLocalRandom.current().nextLong(sleepPeriodMs, sleepPeriodMs2));
        }
        catch (final InterruptedException ignore)
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     *  {@inheritDoc}
     */
    public void reset()
    {
    }

    /**
     *  {@inheritDoc}
     */
    public String alias()
    {
        return ALIAS;
    }

    /**
     *  {@inheritDoc}
     */
    public String toString()
    {
        return "SleepingMillisIdleStrategy{" +
            "alias=" + ALIAS +
            ", sleepPeriodMs=" + sleepPeriodMs +
            ", sleepPeriodMs2=" + sleepPeriodMs2 +
            '}';
    }
}