package uk.ac.ic.kyoto.countries;

public final class GameConst {
	
	// GDP Growth Values
	private static double GROWTH_MARKET_STATE = 1;
	private static double STABLE_MARKET_STATE = 0.5;
	private static double RECESSION_MARKET_STATE = 0.1;
	
	private static double GROWTH_MARKET_CHANCE = 0.1;
	private static double STABLE_MARKET_CHANCE = 0.8;
	private static double RECESSION_MARKET_CHANCE = 0.1;
	
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
	 * The cost of investment in one extra ton of carbon per year
	 * A larger number increases the cost of carbon investment
	 */
	private static double CARBON_INVESTMENT_PRICE = 200;
	
	
	/**
	 * The price of reducing carbon reduction by one unit when we have 100% dirty industry
	 */
	private static double CARBON_REDUCTION_PRICE_MIN = 60;
	/**
	 * The price of reducing carbon reduction by one unit when we have 0% dirty industry
	 */
	private static double CARBON_REDUCTION_PRICE_MAX = 1200;

	
	
	/**
	 * The price of increasing carbon absorption by one unit when we have 100% arable area
	 */
	private static double CARBON_ABSORPTION_PRICE_MIN = 10;
	/**
	 * The price of increasing carbon absorption by one unit when we have 0% arable area
	 */
	private static double CARBON_ABSORPTION_PRICE_MAX = 750;
	/**
	 * Forest area that absorbs single ton of carbon
	 */
	private static double FOREST_CARBON_ABSORPTION = 0.0156;
	
	
	/**
	 * Percentage of GDP that is levied from availiableToSpend for monitoring
	 * Currently allows for a 10th of countries to be monitored.
	 */
	private static double MONITOR_COST_PERCENTAGE = 0.00025;   // 2.5% of absolute cash given out
	
	/**
	 * Percentage of GDP taken from country if they fail to meet their targets.  
	 * Increases in linear amounts for each transgression.
	 */
	private static double SANCTION_RATE = 0.0025;
	
	/**
	 * Price for each monitor
	 */
	private static double MONITORING_PRICE = 847;
	
	/**
	 * Target penalty coefficient
	 */
	private static double PENALTY_COEF = 0.3;
	
	/**
	 * Number of ticks in a year
	 */
	private static int TICKS_IN_YEAR = 20;
	
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
			double stablec, double recc, double maxgdp, double monitorpercent, double sancrate, 
			double monitorprice, int yearsInSession, double targred,
			int minJoinTime, int minKyotoMembership, int ticksInYear,
			
			double carbonInvestmentPrice,
			double growthScaler,
			double percentageOfGDP,
			double carbonReductionPriceMin,
			double carbonReductionPriceMax,
			double carbonAbsorptionPriceMin,
			double carbonAbsorptionPriceMax,
			double forestCarbonAbsorption			
			) {
		GROWTH_MARKET_STATE = growths;
		STABLE_MARKET_STATE = stables;
		RECESSION_MARKET_STATE = recs;
		GROWTH_MARKET_CHANCE = growthc;
		STABLE_MARKET_CHANCE = stablec;
		RECESSION_MARKET_CHANCE = recc;
		MAX_GDP_GROWTH = maxgdp;
		MONITOR_COST_PERCENTAGE = monitorpercent;
		SANCTION_RATE = sancrate;
		MONITORING_PRICE = monitorprice;
		YEARS_IN_SESSION = yearsInSession;
		TARGET_REDUCTION = targred;
		MINIMUM_KYOTO_REJOIN_TIME = minJoinTime;
		MINIMUM_KYOTO_MEMBERSHIP_DURATION = minKyotoMembership;
		TICKS_IN_YEAR = ticksInYear;
		CARBON_INVESTMENT_PRICE = carbonInvestmentPrice;
		GROWTH_SCALER = growthScaler;
		PERCENTAGE_OF_GDP = percentageOfGDP;
		CARBON_REDUCTION_PRICE_MIN = carbonReductionPriceMin;
		CARBON_REDUCTION_PRICE_MAX = carbonReductionPriceMax;
		CARBON_ABSORPTION_PRICE_MIN = carbonAbsorptionPriceMin;
		CARBON_ABSORPTION_PRICE_MAX = carbonAbsorptionPriceMax;
		FOREST_CARBON_ABSORPTION = forestCarbonAbsorption;
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
	
	public static double getPenaltyCoef() {
		return PENALTY_COEF;
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
