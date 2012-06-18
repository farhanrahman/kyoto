/**
 * 
 */
package uk.ac.ic.kyoto.countries;

import java.util.Map;
import java.util.TreeMap;

import uk.ac.imperial.presage2.core.simulator.SimTime;

/**
 * @author farhanrahman
 *
 */
public class DataStore {
//	this.persist.getState(time).setProperty("GDP", Double.toString(GDP));
//	this.persist.getState(time).setProperty("GDPRate", Double.toString(GDPRate));
//	this.persist.getState(time).setProperty("Available_to_spend", Double.toString(availableToSpend));
//	this.persist.getState(time).setProperty("Emissions_target", Double.toString(emissionsTarget));
//	this.persist.getState(time).setProperty("Carbon_offset", Double.toString(carbonOffset));
//	this.persist.getState(time).setProperty("Carbon_output", Double.toString(carbonOutput));
//	this.persist.getState(time).setProperty("Energy_output", Double.toString(energyOutput));
//	this.persist.getState(time).setProperty("Is_kyoto?", Boolean.toString(isKyotoMember));
	
	private Map<Integer, String> gdpHistory = new TreeMap<Integer, String>();
	private Map<Integer, String> gdpRateHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> availableToSpendHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> emissionsTargetHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> carbonOffsetHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> carbonOutputHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> isKyotoMemberHistory = new TreeMap<Integer,String>();
	/**
	 * 
	 */
	public DataStore() {

	}

	/**
	 * Update GDP history.
	 * @param gdp
	 */
	public void addGdp(double gdp){
		this.gdpHistory.put(SimTime.get().intValue(), Double.toString(gdp));
	}
	
	/**
	 * Update GDPRate history.
	 * @param gdpRate
	 */
	public void addGdpRate(double gdpRate){
		this.gdpRateHistory.put(SimTime.get().intValue(), Double.toString(gdpRate));
	}
	
	/**
	 * Update available to spend history
	 * @param availableToSpend
	 */
	public void addAvailableToSpend(double availableToSpend){;
		this.availableToSpendHistory.put(SimTime.get().intValue(), Double.toString(availableToSpend));
	}
	
	/**
	 * Update emissions target history
	 * @param emissionsTarget
	 */
	public void addEmissionsTarget(double emissionsTarget){
		this.emissionsTargetHistory.put(SimTime.get().intValue(), Double.toString(emissionsTarget));
	}
	
	public void addCarbonOffset(double carbonOffset){
		this.carbonOffsetHistory.put(SimTime.get().intValue(), Double.toString(carbonOffset));
	}
	
	public void addCarbonOutput(double carbonOutput){
		this.carbonOutputHistory.put(SimTime.get().intValue(), Double.toString(carbonOutput));
	}
	
	public void addIsKyotoMember(boolean isKyotoMember){
		this.isKyotoMemberHistory.put(SimTime.get().intValue(), Boolean.toString(isKyotoMember));
	}
}
