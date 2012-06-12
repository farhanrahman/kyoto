package uk.ac.ic.kyoto.annex1sustain;

public final class Constants {
	
	// Constants for Internal Price calculation
	
	// EndOfRoundFactor calculation
	public static final double END_OF_ROUND_FACTOR_MAX = 2;
	public static final double END_OF_ROUND_YEAR_PART = 0.5;
	
	// LastYearFactor calculation constants
	public static final double LAST_YEAR_FACTOR_MIN = 0.8;
	public static final double LAST_YEAR_FACTOR_MAX = 1.2;
	public static final double LAST_YEAR_FACTOR_OFFSET = 0.5;
	
	// Constants for The Quantity to be sold
	public static final double FOSSIL_FUEL_PRICE_COEFFICIENT = 1;
	public static final double MARKET_STATE_COEFFICIENT = 1.1;
	public static final double SELL_AMOUNT_COEFFICIENT = 1;
	
	// Constants to determine when to invest
	public static final long INVESTMENT_MIN = 1;
	public static final long INVESTMENT_MAX = 16;
	public static final double INVESTMENT_SCALING = 2;

}
