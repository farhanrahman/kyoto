package uk.ac.ic.kyoto.annex1sustain;

public final class Constants {
	
	// Constants for Internal Price calculation
	
	// EndOfRoundFactor calculation
	public static final double END_OF_ROUND_FACTOR_SLOPE = 2;
	public static final long END_OF_ROUND_MINIMUM_NUMBER_OF_TICKS = 1000;
	public static final long NUMBER_OF_TICKS_IN_ROUND = 2000;
	
	// LastYearFactor calculation constants
	public static final double LAST_YEAR_FACTOR_SLOPE = 1;
	public static final double LAST_YEAR_FACTOR_OFFSET = 50; // expressed in %
	
	// Constants for The Quantity to be sold
	public static final double FOSSIL_FUEL_PRICE_COEFFICIENT = 1;
	public static final double MARKET_STATE_COEFFICIENT = 0.1;
	public static final double SELL_AMOUNT_COEFFICIENT = 1;

}
