package com.sod.pricing;


import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sod.pricing.Schema.FIELDS.*;
 
public abstract class StockPricePublisher extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(StockPricePublisher.class);
	
	protected final ArrayList<StockInstrument> stockList;
	protected int stockListLen;
	protected final StockPriceGenerator randomWalkPrice;
	
	public StockPricePublisher() 
	{
		randomWalkPrice = new StockPriceGBMGeneratorImpl();
		stockList = new ArrayList<>(10);
	}
	
	public void initialize(boolean useDb2Init) throws Exception
	{			
		if(useDb2Init)
			 initializeWithDb();
		else initializeWithCsv();
			
	}
	
	private void initializeWithDb() throws SQLException 
	{
		//use try with resource statement to auto-closee
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:security_def");)
	    {
	      
			Statement statement = connection.createStatement();
	        statement.setQueryTimeout(15);
	        ResultSet rs = statement.executeQuery("select * from security");
	        StockInstrument stock;
		    String ticker;
	        while(rs.next())
	        {
	        	if(SecurityType.valueOf(rs.getString(SEC_TYPE.name())) == SecurityType.STOCK) 
	        	{
	        		ticker = rs.getString(TICKER.name());
				    	
				    //To avoid blow off the bytebuffer allocated if using the Aeron IPC to send message. But no such check is required if using RingBuffer.
	        		if(TickEvent.isTickerTooLong(ticker)) {
	        			LOG.warn("The ticker {} is too long, skip this ticker, pls check", ticker);
	        			continue;
	        		}
				 //normalized the drift and stddev in second
				 stock = new StockInstrument(ticker, Util.getNullableDoubleDbField(rs, OPEN_PRICE), 
				    		Util.getNullableDoubleDbField(rs, DRIFT), Util.getNullableDoubleDbField(rs,STDDEV));
				  //assign a random seed so as to generate a different a stream of pseudorandom numbers.
				  stock.setRandomGenerator(new Random(ticker.hashCode() + System.currentTimeMillis())); 
				  stockList.add(stock);
	        	}
	        }
	        stockListLen = stockList.size();
	        if(stockListLen == 0) {
	        	throw new IllegalStateException("Can't load any valid stocks from security_def db and can't run the publisher.");
	        }
	    }
	}

	private void initializeWithCsv() throws Exception 
	{
		try ( Reader inDef = new FileReader(Util.getFileFromResource("csv/security_def.csv", getClass().getClassLoader())) ) 
		{
			
			Iterable<CSVRecord> defRec = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(inDef);
			StockInstrument stock;
			String ticker;
			for (CSVRecord record : defRec) 
			{
				if(SecurityType.valueOf(record.get("SEC_TYPE")) == SecurityType.STOCK) 
				{
				    ticker = record.get("TICKER");
				    	
				    //To avoid blow off the bytebuffer allocated if using the Aeron IPC to send message. But no such check is required if using RingBuffer.
				    if(TickEvent.isTickerTooLong(ticker)) {
				    	LOG.warn("The ticker {} is too long, skip this ticker, pls check", ticker);
				    	continue;
				    }
				    //normalized the drift and stddev in second
				    stock = new StockInstrument(ticker, Double.parseDouble(record.get("OPEN_PRICE")), 
				    		Double.parseDouble(record.get("DRIFT")), Double.parseDouble(record.get("STDDEV")));
				    stock.setRandomGenerator(new Random(ticker.hashCode() + System.currentTimeMillis())); //assign a random seed so as to generate a different a stream of pseudorandom numbers.
				    stockList.add(stock);
			    }
			}
			stockListLen = stockList.size();
			if(stockListLen == 0) {
				throw new IllegalStateException("Can't load any valid stocks from security_def.csv and can't run the publisher.");
			}
		}		
	}

	protected StockInstrument doStockRandomWalk() 
	{
		//randomly pick a stock and send tick.
		StockInstrument tick = stockList.get(ThreadLocalRandom.current().nextInt(stockListLen));
		tick.updatePrice(randomWalkPrice.getPrice(tick));
		if(LOG.isDebugEnabled())
			LOG.debug("produce a random-walk stock price: ticker = {}, price = {}, time = {}", tick.ticker, tick.getPrice(), tick.getUpdateTimeInMillis());
		
		return tick;
	}

	protected abstract int sendTickEvent(StockInstrument tick);
	
}
