/**
 * 
 */
package uk.ac.ic.kyoto.countries;

import java.util.Map;
import java.util.TreeMap;

import uk.ac.ic.kyoto.countries.AbstractCountry.KyotoMember;
import uk.ac.imperial.presage2.core.simulator.SimTime;

/**
 * @author farhanrahman
 *
 */
public class DataStore {
	private Map<Integer, String> gdpHistory = new TreeMap<Integer, String>();
	private Map<Integer, String> gdpRateHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> availableToSpendHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> emissionsTargetHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> carbonOffsetHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> carbonOutputHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> energyOutputHistory = new TreeMap<Integer,String>();
	private Map<Integer, String> isKyotoMemberHistory = new TreeMap<Integer,String>();

	
	public static final String gdpKey = "gdp";
	public static final String gdpRateKey = "gdp_rate";
	public static final String availableToSpendKey = "available_to_spend";
	public static final String emissionTargetKey = "emission_target";
	public static final String carbonOffsetKey = "carbon_offset";
	public static final String carbonOutputKey = "carbon_output";
	public static final String energyOutputKey = "energy_output";
	public static final String isKyotoMemberKey = "is_kyoto_member";
	
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
	
	/**
	 * Update carbon offset history
	 * @param carbonOffset
	 */
	public void addCarbonOffset(double carbonOffset){
		this.carbonOffsetHistory.put(SimTime.get().intValue(), Double.toString(carbonOffset));
	}
	
	/**
	 * Update carbon output history
	 * @param carbonOutput
	 */
	public void addCarbonOutput(double carbonOutput){
		this.carbonOutputHistory.put(SimTime.get().intValue(), Double.toString(carbonOutput));
	}
	
	public void addEnergyOutput(double energyOutput){
		this.energyOutputHistory.put(SimTime.get().intValue(), Double.toString(energyOutput));
	}
	
	/**
	 * update whether the particpant is a member
	 * of the kyoto protocol
	 * @param kyotoMember
	 */
	public void addIsKyotoMember(KyotoMember kyotoMember){
		this.isKyotoMemberHistory.put(SimTime.get().intValue(), kyotoMember.name());
	}
	
	/**
	 * 
	 * @return map of gdp history
	 */
	public Map<Integer, String> getGdpHistory() {
		return gdpHistory;
	}

	/**
	 * 
	 * @return map of gdp rate history
	 */
	public Map<Integer, String> getGdpRateHistory() {
		return gdpRateHistory;
	}

	/**
	 * 
	 * @return map of available to spend history
	 */
	public Map<Integer, String> getAvailableToSpendHistory() {
		return availableToSpendHistory;
	}

	/**
	 * 
	 * @return a map of emissions target history
	 */
	public Map<Integer, String> getEmissionsTargetHistory() {
		return emissionsTargetHistory;
	}

	/**
	 * 
	 * @return a map of carbon offset history
	 */
	public Map<Integer, String> getCarbonOffsetHistory() {
		return carbonOffsetHistory;
	}

	/**
	 * 
	 * @return a map of carbon output history
	 */
	public Map<Integer, String> getCarbonOutputHistory() {
		return carbonOutputHistory;
	}

	/**
	 * 
	 * @return a map of energy output history
	 */
	public Map<Integer, String> getEnergyOutputHistory() {
		return energyOutputHistory;
	}

	/**
	 * 
	 * @return returns a map of kyoto member state history
	 */
	public Map<Integer, String> getIsKyotoMemberHistory() {
		return isKyotoMemberHistory;
	}

}
