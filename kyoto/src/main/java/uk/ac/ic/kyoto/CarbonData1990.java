package uk.ac.ic.kyoto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonData1990 {

	private static Map<String, Double> output1990Data = new ConcurrentHashMap<String, Double>();
  
	/**
	 * Adds 1990 output data to carbon target service (used for initial targets)
	 */
	public static void addCountry(String ISO, double outputData){
		output1990Data.put(ISO, outputData);
	}
	
	public static double get(String ISO){
		return output1990Data.get(ISO).doubleValue();
	}
	
}
