package uk.ac.ic.kyoto.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;

/**
 * 
 * @author Adam, Piotr
 */

public class FossilPrices extends EnvironmentService {
	
	protected FossilPrices(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
		
		try {
			initializeGasAndOilMaps();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
			
	}

	private final String FOSSIL_FUEL_PRICES_PATH = "src/main/resources/FossilFuelPrices.csv";

	private Logger logger = Logger.getLogger(FossilPrices.class);

	private Map<Long, Double> oilPriceMap = new HashMap<Long, Double>();
	private Map<Long, Double> gasPriceMap = new HashMap<Long, Double>();
	
	
	/**
	 * Reads the values from specified file into two maps.
	 * In case of any problems, clears the maps.
	 * @throws IOException 
	 */
	private void initializeGasAndOilMaps() throws IOException {
		String line;
		String[] entries;
		long year;
		double oilPrice;
		double gasPrice;
		
		File file = new File(FOSSIL_FUEL_PRICES_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		// Read the values into two maps
		line = reader.readLine(); // Drop title line
		while ((line = reader.readLine()) != null) {
			entries = line.split(",");
			year = Long.parseLong(entries[0]);
			oilPrice = Double.parseDouble(entries[1]);
			gasPrice = Double.parseDouble(entries[2]);
			oilPriceMap.put(year, oilPrice);
			gasPriceMap.put(year, gasPrice);
		}
		reader.close();
	}
	
	/**
	 * Tries to obtain oil price for a given year.
	 * If there is no value for that year, or there are errors, returns -1 for safe comparison with 0.
	 */
	public double getOilPrice(long year) {
		double oilPrice;
		
		try {
			if (oilPriceMap.containsKey(year)) {
				oilPrice = oilPriceMap.get(year);
			}
			else {
				oilPrice = -1;
			}
		}
		catch (Exception e) {
			logger.warn("Problem with reading the oil price: " + e);
			oilPrice = -1;
		}
		
		return oilPrice;
	}
	
	/**
	 * Tries to obtain gas price for a given year.
	 * If there is no value for that year, or there are errors, returns -1 for safe comparison with 0.
	 */
	public double getGasPrice(long year) {
		double gasPrice;
		
		try {
			if (gasPriceMap.containsKey(year)) {
				gasPrice = gasPriceMap.get(year);
			}
			else {
				gasPrice = -1;
			}
		} catch (Exception e) {
			logger.warn("Problem with reading the gas price: " + e);
			gasPrice = -1;
		}
		
		return gasPrice;
	}
}
