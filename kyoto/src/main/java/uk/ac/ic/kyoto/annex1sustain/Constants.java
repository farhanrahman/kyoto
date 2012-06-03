package uk.ac.ic.kyoto.annex1sustain;

public final class Constants {
	
	// Constants for Internal Price calculation
	
	public static final double MARKET_PRICE_COEFFICIENT = 1;
	public static final double TIME_COEFFICIENT = 1;
	public static final double PREVIOUS_OFFER_COEFFICIENT = 1;
	
	public static final double FOSSIL_FUEL_PRICE_COEFFICIENT = 1;
	public static final double MARKET_STATE_COEFFICIENT = 0.1;
	public static final double SELL_AMOUNT_COEFFICIENT = 1;
	public static final long WHEN_TIME_STARTS_TO_INFLUENCE_THE_PRICE = 1000;
	public static final double SLOPE_OF_TIME_TO_END_OF_ROUND_VS_PRICE = 2;
	public static final long NUMBER_OF_TICKS_IN_ROUND = 2000;
}
