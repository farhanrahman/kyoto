package uk.ac.ic.kyoto.countries;

public final class GameConst {
	
	// GDP Growth Values
	public static final double GROWTH_MARKET_STATE = 0.05;
	public static final double STABLE_MARKET_STATE = 0.02;
	public static final double RECESSION_MARKET_STATE = 0.03;
	
	public static final double GROWTH_MARKET_CHANCE = 0.1;
	public static final double STABLE_MARKET_CHANCE = 0.8;
	public static final double RECESSION_MARKET_CHANCE = 0.1;
	
	@Deprecated
	public static final int ECONOMIC_OUTPUT_REDUCTION = 1; // Deprecated constant
	
	/**
	 * The cost of investment in one extra ton of carbon per year
	 */
	public static final double CARBON_INVESTMENT_PRICE = 1;
	
	/**
	 * Normalises value of GDP rate increase
	 */
	public static final double GROWTH_SCALER = 0.001;
	
	/**
	 * Percentage of GDP available to spend on development
	 */
	public static final double PERCENTAGE_OF_GDP = 0.1;
	
	
	/**
	 * The price of reducing carbon reduction by one unit when we have 100% dirty industry
	 */
	public static double CARBON_REDUCTION_PRICE_MIN = 10;
	/**
	 * The price of reducing carbon reduction by one unit when we have 0% dirty industry
	 */
	public static double CARBON_REDUCTION_PRICE_MAX = 200;
	
	/**
	 * The price of increasing carbon absorption by one unit when we have 100% arable area
	 */
	public static double CARBON_ABSORPTION_PRICE_MIN = 10;
	/**
	 * The price of increasing carbon absorption by one unit when we have 0% arable area
	 */
	public static double CARBON_ABSORPTION_PRICE_MAX = 200;
	/**
	 * Forest area that absorbs single ton of carbon
	 */
	public static double FOREST_CARBON_ABSORPTION = 1;
	
	///**
	// * Coefficient to calculate the cost of carbon absorption
	// */
	//public static float CARBON_ABSORPTION_COEFF = 1;
	
	///**
	// * Size of a block of arable land
	// */
	//public static float FOREST_BLOCK_SIZE = 100;
	
	///**
	// * Amount of carbon (in tons) that you offset with a single unit of forest PER YEAR
	// */
	//public static long FOREST_CARBON_ABSORPTION = 10;
	
	/**
	 * Percentage of GDP that is levied from availiableToSpend for monitoring
	 */
	public static double MONITOR_COST_PERCENTAGE = 0.005;
	
	/**
	 * Percentage of GDP taken from country if they fail to meet their targets.  
	 * Increases in linear amounts for each transgression.
	 */
	public static double SANCTION_RATE = 0.05;
	
	/**
	 * Price for each monitor
	 */
	public static double MONITORING_PRICE = 5000;
	
	/**
	 * Number of ticks in a year
	 */
	public static int TICKS_IN_YEAR = 365;
	
	/**
	 * Number of years in a session
	 */
	public static int YEARS_IN_SESSION = 10;
	
	/**
	 * Session to session world target reduction
	 */
	public static double TARGET_REDUCTION = 0.95;
	
	/**
	 * The minimum amount of time, in years, that a country must wait before rejoining Kyoto after leaving
	 */
	public static int MINIMUM_KYOTO_REJOIN_TIME = 4;
	
	/**
	 * The minimum amount of time, in years, that a country must be a member of Kyoto before it can leave
	 */
	public static int MINIMUM_KYOTO_MEMBERSHIP_DURATION = 10;
}
