package uk.ac.ic.kyoto.countries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * 
 * @author Adam Zysko
 */


public class Market
{
	public enum EconomyState {
		GROWTH,
		STABLE,
		RECESSION
	}
	
	private static final String FOSSIL_FUEL_PRICES_PATH = "FossilFuelPrices.csv";
	
	private static Map<Long,Double> oilPriceMap = new HashMap<Long,Double>();
	private static Map<Long,Double> gasPriceMap = new HashMap<Long,Double>();
	private static Logger logger = Logger.getLogger(Market.class);
	
	private static EconomyState currentEconomyState;

	// Values used for determining which State is defined by a given random number.
	private final static int growthNumberLimit = (int) (100 * GameConst.GROWTH_MARKET_CHANCE);
	private final static int stableNumberLimit = (int) (growthNumberLimit + 100 * GameConst.STABLE_MARKET_CHANCE);
	private final static int recessionNumberLimit = (int) (stableNumberLimit + 100 * GameConst.RECESSION_MARKET_CHANCE);
	
	// Static initializer
	static
	{
		initializeGasAndOilMaps();
	}
	
	private static void initializeGasAndOilMaps()
	{
		String[] entries;
		long year;
		double oilPrice;
		double gasPrice;
		try {
			File file = new File(FOSSIL_FUEL_PRICES_PATH); // path?
			BufferedReader reader  = new BufferedReader(new FileReader(file));
			
			// Read the values into two maps
			String line = reader.readLine(); // to drop the title line - not really elegant
			while((line = reader.readLine()) != null) {
				 entries = line.split(",");
				 year = Long.parseLong(entries[0]);
				 oilPrice = Double.parseDouble(entries[1]);
				 gasPrice = Double.parseDouble(entries[2]);
				 oilPriceMap.put(year, oilPrice);
				 gasPriceMap.put(year, gasPrice);
			}
			reader.close();
		}
		catch (Exception e){
			logger.warn("Problems with the file " + FOSSIL_FUEL_PRICES_PATH + ": " + e);
			oilPriceMap.clear();
			gasPriceMap.clear();
		}
	}
	
	public static double getOilPrice(long year)	{
		double oilPrice;
		try {
			if (oilPriceMap.containsKey(year))
				oilPrice = oilPriceMap.get(year);
			else 
				oilPrice = 0;
		}
		catch(Exception e) {
			logger.warn("Problems reading the oil price: " + e);
			oilPrice = 0;
		}
		return oilPrice;
	}
	
	public static double getGasPrice(long year)	{
		double gasPrice;
		try {
			if (gasPriceMap.containsKey(year))
				gasPrice = gasPriceMap.get(year);
			else 
				gasPrice = 0;
		}
		catch(Exception e) {
			logger.warn("Problems reading the gas price: " + e);
			gasPrice = 0;
		}
		return gasPrice;
	}
	
	/**
	 * Function changes the State of Economy for a given year 
	 * using the probabilities defined in Game Constants
	 */
	public static void updateEconomyState()
	{
		try
		{
			/** Generate a random number between 0 and 99*/
			Random randGenerator = new Random();
			int randomNumber = randGenerator.nextInt(100);
			
			/** Change the currentEconomyState according to the rules */
			if (randomNumber < growthNumberLimit)
				currentEconomyState = EconomyState.GROWTH;
			else if (randomNumber < stableNumberLimit)
				currentEconomyState = EconomyState.STABLE;
			else if (randomNumber < recessionNumberLimit)
				currentEconomyState = EconomyState.RECESSION;
			else
				logger.warn("Generated random number is out of bounds");
			
			/** Log the change in economy state */
			logger.info("The new state of economy is " + currentEconomyState.toString());
		}
		catch (Exception e){
			logger.warn(e);
		}
	}
	
	public static EconomyState getEconomyState() {
		return currentEconomyState;
	}
	
	
	
}
