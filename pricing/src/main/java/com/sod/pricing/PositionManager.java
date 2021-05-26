package com.sod.pricing;

import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sod.pricing.Schema.FIELDS.*;
/*
 * Basically, it is inappropriate to concurrently update the portfolio. If allowed, the portfolio must be updated in the time order of the events.
 * For now, per requirements, the event subscriber is fast enough to handle required event rate. 
 * I keep the design simpler, just assume no concurrent update after initialisation. As a result, we can use simple list, instead of concurrent list.
 * 
 * PositionManager loads the security definitions (either CSV or SQLite DB based on flag passed in) and positions from CSV files. 
 */
public class PositionManager implements TickEventHandler {
	private static final Logger LOG = LoggerFactory.getLogger(PositionManager.class);
	
	private final ArrayList<SecurityPosition> positionList;
	
	public Map<String, SecurityDef> SECURITY_DEF_MAP; //unmodifiable once initialized, publicly accessible
	
	private int tickCount = 0;
	
	public PositionManager() 
	{
		positionList = new ArrayList<>(10);
		SECURITY_DEF_MAP = new HashMap<>();
	}
	
	public void initialize(boolean useDB2Init) throws Exception
	{

		if(useDB2Init)
			 initializeSecurityDefWithDb();
		else initializeSecurityDefWithCsv();
		initializePosition();
		printPositions("SYS_START", Double.NaN);
	}
	
	protected void initializePosition() throws Exception
	{
		try ( Reader inPos = new FileReader(Util.getFileFromResource("csv/position.csv", getClass().getClassLoader())) ) {	
			Iterable<CSVRecord> positionRec = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(inPos);
			int qty;
			for (CSVRecord record : positionRec) {
				SecurityDef def = SECURITY_DEF_MAP.get(record.get(TICKER));
				if(def == null) {
					LOG.error("The position file contains ticker {} which is not defined in security definitions, skip it", record.get(0));
					continue;
				}
				try {
					qty = Integer.parseInt(record.get(QTY));
				    if(def.type == SecurityType.STOCK) {
						positionList.add(new StockPosition((StockDef)def, qty, def.openPrice));
				    } else {
						positionList.add(new EurOptionPosition((OptionDef)def, qty, def.openPrice));
				    }
				} catch (IllegalArgumentException ex) {
		    		LOG.error("Can't create the position for the ticker {} because of {}, skip it", def.ticker, ex);
		    	} 
			}
		}
		if(positionList.size() == 0) {
			throw new IllegalStateException("Can't load any valid stocks from security_def db and can't run the subscriber.");
		}
	}

	protected void initializeSecurityDefWithCsv() throws Exception
	{
		try ( Reader inDef = new FileReader(Util.getFileFromResource("csv/security_def.csv", getClass().getClassLoader())) ){
			
			Iterable<CSVRecord> defRec = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(inDef);
			
			SecurityType type;
			String ticker;
			for (CSVRecord record : defRec) {
				type = SecurityType.valueOf(record.get(SEC_TYPE));
				ticker = record.get(TICKER);
				try {
			    if(type == SecurityType.STOCK) 
			    	SECURITY_DEF_MAP.put(ticker, new StockDef(ticker, Double.parseDouble(record.get(DRIFT)), Double.parseDouble(record.get(STDDEV)),
			    		Double.parseDouble(record.get(OPEN_PRICE))));
			    else 
			    	SECURITY_DEF_MAP.put(ticker, new OptionDef(ticker, type, record.get(UNDERLYING_TICKER), Double.parseDouble(record.get(STRIKE)), 
			    		Double.parseDouble(record.get(IML_VOL)), Double.parseDouble(record.get(MATURITY)),Double.parseDouble(record.get(RF_RATE)),
			    		Double.parseDouble(record.get(OPEN_PRICE))));
				} catch (IllegalArgumentException ex) {
		    		LOG.error("Can't create the intrument definition for the ticker {} because of {}, skip it", ticker, ex);
		    	} 
			}		
			SECURITY_DEF_MAP = java.util.Collections.unmodifiableMap(SECURITY_DEF_MAP);
		}
		if(SECURITY_DEF_MAP.size() == 0) {
			throw new IllegalStateException("Can't load any valid stocks from security_def.csv and can't run the subscriber.");
		}
	}


	protected void initializeSecurityDefWithDb() throws SQLException
	{
	    //use try with resource statement to auto-close
	    try (Connection connection = DriverManager.getConnection("jdbc:sqlite:security_def");)
	    {
	      
	      Statement statement = connection.createStatement();
	      statement.setQueryTimeout(15);
	      ResultSet rs = statement.executeQuery("select * from security");
	      SecurityType type;
	      String ticker;
	      while(rs.next())
	      {
	    	type = SecurityType.valueOf(rs.getString(SEC_TYPE.name()));
	    	ticker = rs.getString(TICKER.name());
	    	try {
		    	if(type == SecurityType.STOCK) {
				    SECURITY_DEF_MAP.put(ticker, 
				    	new StockDef(ticker,Util.getNullableDoubleDbField(rs,DRIFT),Util.getNullableDoubleDbField(rs,STDDEV),
				    			Util.getNullableDoubleDbField(rs,OPEN_PRICE)));
		    	} else { 
				    SECURITY_DEF_MAP.put(ticker, 
				    	new OptionDef(ticker, type, rs.getString(UNDERLYING_TICKER.name()),Util.getNullableDoubleDbField(rs,STRIKE),
				    			Util.getNullableDoubleDbField(rs,IML_VOL), Util.getNullableDoubleDbField(rs,MATURITY), 
				    			Util.getNullableDoubleDbField(rs,RF_RATE), Util.getNullableDoubleDbField(rs,OPEN_PRICE)));
		    	}
	    	} catch (IllegalArgumentException ex) {
	    		LOG.error("Can't create the intrument definition for the ticker {} because of {}, skip it", ticker, ex);
	    	}
	      }
	      SECURITY_DEF_MAP = java.util.Collections.unmodifiableMap(SECURITY_DEF_MAP);
	    }	    
	    if(SECURITY_DEF_MAP.size() == 0) {
			LOG.error("None of security can be loaded from SQLite, security_def db, do nothing and exit the program");
			System.exit(0);
		}
	}
			
	@Override
	public void onTick(TickEvent tick) {
		boolean positionUpdate = false;
		
		++tickCount;
		for(SecurityPosition position : positionList) //If the list is big, need to find a better lookup approach
		{
			if(position instanceof EurOptionPosition) 
			{
				if( ((EurOptionPosition)position).def.underlyingTicker.equals(tick.getTicker()) ) {
					position.onTick(tick);
					positionUpdate = true;
				}
			} 
			else if(position.getTicker().equals(tick.getTicker())) 
			{
				position.onTick(tick);
				positionUpdate = true;
			}
		}
		if(positionUpdate)
			 printPositions(tick.getTicker(), tick.getPrice());
		else System.out.printf("## %d Market Data Update of %s. But no such stock found in the positions and hence the portfolio remains unchanged\n", tickCount, tick.getTicker());
	}
	
	private void printPositions(String ticker, double price) 
	{
		double portfolioVal = 0D; 
		System.out.println("**************************************************************************************************");
		System.out.printf("\n## %d Market Data Updates\n", tickCount);
		System.out.printf("%s changes to %f\n", ticker, price);
		System.out.printf("\nPortfolio Update:\n", ticker, price);
		System.out.printf("Symbol                   |Price     |Qty     |Value\n");
		System.out.println("-------------------------------------------------------------------------------------------------");
		for(SecurityPosition position : positionList) {
		    System.out.printf("%-25s|%-10.3f|%-8d|%.3f\n", position.getTicker(), position.price, position.qty, position.value);
			portfolioVal += position.value;
		}
		System.out.println("\n-------------------------------------------------------------------------------------------------");
		System.out.printf("Portfolio Value: %.2f\n", portfolioVal);
		System.out.println("**************************************************************************************************");
	}
	
	public static void main(String args[]) throws Exception {
		PositionManager mgr = new PositionManager();
		mgr.initializeSecurityDefWithDb();
		for(Map.Entry<String, SecurityDef> rec : mgr.SECURITY_DEF_MAP.entrySet()) {
			System.out.println("key = " + rec.getKey() + ", value = " + rec.getValue());
		}
		
	}

}
