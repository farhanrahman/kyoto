package uk.ac.ic.kyoto.market;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * 
 * @author Adam Zysko
 */


public class FossilPrices
{
	private static final String FOSSIL_FUEL_PRICES_PATH = "FossilFuelPrices.csv";
	
	private static Logger logger = Logger.getLogger(FossilPrices.class);
	
	private static Map<Long,Double> oilPriceMap = new HashMap<Long,Double>();
	private static Map<Long,Double> gasPriceMap = new HashMap<Long,Double>();
	
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
}
