package com.sod.pricing;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import com.lmax.disruptor.EventFactory;
/*
 * Stock tick movement event passed between publisher and subscriber.
 * For Disrupter ring buffer implementation, use the event object itself
 * For Aeron IPC implementation, use the encode / decode method to write to / read from byte buffer. For such simple case, doesn't write SBE encoder/decoder.  
 */
public class TickEvent {
	
	public static final int MAX_BYTE_BUFFER_SIZE = 32;  //used by Aeron IPC to allocate byte buffer
	//assume ticker chars are ascii only, normally. This restriction is only applied to Aeron IPC approach. If longer, blow up the buffer of publisher
	public static final int MAX_TICKER_SIZE_IN_BYTE = MAX_BYTE_BUFFER_SIZE - 8 -1; 
	
	
	public static final EventFactory<TickEvent> FACTORY = TickEvent::new;
	
	private String ticker;
	private double price;
	private int updateTimeInMillis; //no use for now.
	
	public void reset() 
	{
		ticker = null;
		price = 0;
		updateTimeInMillis = 0;
	}
	
	public String getTicker() {return ticker;}
	public double getPrice() {return price;}
	
	//this is used for disruptor approach
	public void copyTo(TickEvent that)
	{
		that.price = price;
		that.ticker = ticker;
	}
	
	//this is used for Aeron IPC approach
	public void encode(UnsafeBuffer buffer) {
        buffer.putDouble(0, this.price);
        buffer.putStringAscii(8, this.ticker);		
	}
	
	//this is used for Aeron IPC approach
	public void decode(DirectBuffer buffer, int offset) {
		this.price = buffer.getDouble(offset);
		this.ticker = buffer.getStringAscii(offset+8);		
	}
	
	public void updateEvent(String ticker, double price) {
		if(isTickerTooLong(ticker))
			throw new IllegalArgumentException("The ticker " + ticker + " is too long and can't send to subscriber if using Aeron IPC");
		
		this.ticker = ticker;
		this.price = price;
		//this.updateTimeInMillis = System.currentTimeMillis(); //no use for now 
	}
	
	public static boolean isTickerTooLong(String ticker) {
		if(ticker.length() > MAX_TICKER_SIZE_IN_BYTE)
			return true;
		else return false;
	}
}
