package com.sod.pricing;

import org.apache.commons.math3.distribution.NormalDistribution;

/*
 * BSOptionPricerImpl has implemented BS-model for European Option pricing.
 */
public class BSOptionPricerImpl implements InstrumentPricer {
	
	//private NormalDistribution cnd = new NormalDistribution();
	
	@Override
	public double getPrice(TickEvent event, SecurityDef def) {	
		OptionDef option = (OptionDef)def;
		return calculate(event.getPrice(), option.strike, option.maturity, option.rate, option.vol, option.type);
	}

	public static double calculate(double S, double K, double T, double r, double vol, SecurityType type)
	{
	    double dplus = (Math.log(S / K) + (r + Math.pow(vol,2) / 2) * T) / (vol * Math.sqrt(T));
	    double dminus = dplus - vol * Math.sqrt(T);
	    double price;
	    
	    if (type == SecurityType.CALL_EUR) 
	    	 price = S * CND(dplus) - K * CND(dminus) * Math.exp(-r * T);    //call option
	    else price = K * CND(-dminus) * Math.exp(-r * T) - S * CND(-dplus);  //put option
	    return price;
	}
	
	public static double CND(double x) {
		 //return cnd.cumulativeProbability(x); //it works but unsure how much garbage it creates in the runtime
		 double a1 = 0.31938153;
		 double a2 = -0.356563782;
		 double a3 = 1.781477937;
		 double a4 = -1.821255978;
		 double a5 = 1.330274429;

		 double L = Math.abs(x);
		 double K = 1 / (1 + 0.2316419 * L);
		 double res = 1 - 1 / Math.sqrt(2 * Math.PI) * Math.exp(-Math.pow(L,2) / 2) * (a1 * K + a2 * Math.pow(K,2) + a3 * Math.pow(K,3) + a4 * Math.pow(K,4) + a5 * Math.pow(K,5));
		 if (x<0)
			 res = 1 - res; // if x negative, then reverse approximation by applying 1-x
		 
		 return res;
	}	
}
