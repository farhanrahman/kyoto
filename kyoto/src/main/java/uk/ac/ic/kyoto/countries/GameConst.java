package uk.ac.ic.kyoto.countries;

public final class GameConst {
	
	// GDP Growth Values
	private static double GROWTH_MARKET_STATE = 1;
	private static double STABLE_MARKET_STATE = 0.5;
	private static double RECESSION_MARKET_STATE = 0.1;
	
	private static double GROWTH_MARKET_CHANCE = 0.1;
	private static double STABLE_MARKET_CHANCE = 0.8;
	private static double RECESSION_MARKET_CHANCE = 0.1;
	
	@Deprecated
	private static int ECONOMIC_OUTPUT_REDUCTION = 1; // Deprecated constant
	
	/**
	 * The cost of investment in one extra ton of carbon per year
	 */
	private static double CARBON_INVESTMENT_PRICE = 1;
	
	/**
	 * Normalises value of GDP rate increase
	 */
	private static double GROWTH_SCALER = 0.25;
	
	private static double ENERGY_GROWTH_SCALER = 400;
	
	private static double MAX_GDP_GROWTH = 7;
	/**
	 * Percentage of GDP available to spend on development
	 */
	private static double PERCENTAGE_OF_GDP = 0.005;
	
	
	/**
	 * The price of reducing carbon reduction by one unit when we have 100% dirty industry
	 */
	private static double CARBON_REDUCTION_PRICE_MIN = 10;
	/**
	 * The price of reducing carbon reduction by one unit when we have 0% dirty industry
	 */
	private static double CARBON_REDUCTION_PRICE_MAX = 200;
	
	/**
	 * The price of increasing carbon absorption by one unit when we have 100% arable area
	 */
	private static double CARBON_ABSORPTION_PRICE_MIN = 10;
	/**
	 * The price of increasing carbon absorption by one unit when we have 0% arable area
	 */
	private static double CARBON_ABSORPTION_PRICE_MAX = 200;
	/**
	 * Forest area that absorbs single ton of carbon
	 */
	private static double FOREST_CARBON_ABSORPTION = 1;
	
	///**
	// * Coefficient to calculate the cost of carbon absorption
	// */
	//private static float CARBON_ABSORPTION_COEFF = 1;
	
	///**
	// * Size of a block of arable land
	// */
	//private static float FOREST_BLOCK_SIZE = 100;
	
	///**
	// * Amount of carbon (in tons) that you offset with a single unit of forest PER YEAR
	// */
	//private static long FOREST_CARBON_ABSORPTION = 10;
	
	/**
	 * Percentage of GDP that is levied from availiableToSpend for monitoring
	 */
	private static double MONITOR_COST_PERCENTAGE = 0.0005;
	
	/**
	 * Percentage of GDP taken from country if they fail to meet their targets.  
	 * Increases in linear amounts for each transgression.
	 */
	private static double SANCTION_RATE = 0.05;
	
	/**
	 * Price for each monitor
	 */
	private static double MONITORING_PRICE = 5000;
	
	/**
	 * Number of ticks in a year
	 */
	private static int TICKS_IN_YEAR = 7;
	
	/**
	 * Number of years in a session
	 */
	private static int YEARS_IN_SESSION = 10;
	
	/**
	 * Session to session world target reduction
	 */
	private static double TARGET_REDUCTION = 0.95;
	
	/**
	 * The minimum amount of time, in years, that a country must wait before rejoining Kyoto after leaving
	 */
	private static int MINIMUM_KYOTO_REJOIN_TIME = 4;
	
	/**
	 * The minimum amount of time, in years, that a country must be a member of Kyoto before it can leave
	 */
	private static int MINIMUM_KYOTO_MEMBERSHIP_DURATION = 10;
	
	public GameConst(double growths, double stables, double recs, double growthc,
			double stablec, double recc, double monitorpercent, double sancrate, 
			double monitorprice, int yearsInSession, double targred,
			int minJoinTime, int minKyotoMembership) {
		GROWTH_MARKET_STATE = growths;
		STABLE_MARKET_STATE = stables;
		RECESSION_MARKET_STATE = recs;
		GROWTH_MARKET_CHANCE = growthc;
		STABLE_MARKET_CHANCE = stablec;
		RECESSION_MARKET_CHANCE = recc;
		MONITOR_COST_PERCENTAGE = monitorpercent;
		SANCTION_RATE = sancrate;
		MONITORING_PRICE = monitorprice;
		YEARS_IN_SESSION = yearsInSession;
		TARGET_REDUCTION = targred;
		MINIMUM_KYOTO_REJOIN_TIME = minJoinTime;
		MINIMUM_KYOTO_MEMBERSHIP_DURATION = minKyotoMembership;
	}

	public static double getGrowthMarketState() {
		return GROWTH_MARKET_STATE;
	}

	public static double getStableMarketState() {
		return STABLE_MARKET_STATE;
	}

	public static double getRecessionMarketState() {
		return RECESSION_MARKET_STATE;
	}

	public static double getGrowthMarketChance() {
		return GROWTH_MARKET_CHANCE;
	}

	public static double getStableMarketChance() {
		return STABLE_MARKET_CHANCE;
	}

	public static double getRecessionMarketChance() {
		return RECESSION_MARKET_CHANCE;
	}

	public static int getEconomicOutputReduction() {
		return ECONOMIC_OUTPUT_REDUCTION;
	}

	public static double getCarbonInvestmentPrice() {
		return CARBON_INVESTMENT_PRICE;
	}

	public static double getGrowthScaler() {
		return GROWTH_SCALER;
	}
	
	public static double getEnergyGrowthScaler(){
		return ENERGY_GROWTH_SCALER;
	}

	public static double getMaxGDPGrowth() {
		return MAX_GDP_GROWTH;
	}
	
	public static double getPercentageOfGdp() {
		return PERCENTAGE_OF_GDP;
	}

	public static double getCarbonReductionPriceMin() {
		return CARBON_REDUCTION_PRICE_MIN;
	}

	public static double getCarbonReductionPriceMax() {
		return CARBON_REDUCTION_PRICE_MAX;
	}

	public static double getCarbonAbsorptionPriceMin() {
		return CARBON_ABSORPTION_PRICE_MIN;
	}

	public static double getCarbonAbsorptionPriceMax() {
		return CARBON_ABSORPTION_PRICE_MAX;
	}

	public static double getForestCarbonAbsorption() {
		return FOREST_CARBON_ABSORPTION;
	}

	public static double getMonitorCostPercentage() {
		return MONITOR_COST_PERCENTAGE;
	}

	public static double getSanctionRate() {
		return SANCTION_RATE;
	}

	public static double getMonitoringPrice() {
		return MONITORING_PRICE;
	}

	public static int getTicksInYear() {
		return TICKS_IN_YEAR;
	}

	public static int getYearsInSession() {
		return YEARS_IN_SESSION;
	}

	public static double getTargetReduction() {
		return TARGET_REDUCTION;
	}

	public static int getMinimumKyotoRejoinTime() {
		return MINIMUM_KYOTO_REJOIN_TIME;
	}

	public static int getMinimumKyotoMembershipDuration() {
		return MINIMUM_KYOTO_MEMBERSHIP_DURATION;
	}
}
