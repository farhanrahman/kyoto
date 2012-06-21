package uk.ac.ic.kyoto.util.sim.jsonobjects.tradedata;

import uk.ac.ic.kyoto.countries.OfferMessage;

/**
 * Object used to store JSON Object to
 * store into the mongodb
 * @author farhanrahman
 *
 */
public class TradeData {
	private String tick;
	private String simID;
	private TradeObject tradeObject;
	
	
	public TradeData(OfferMessage offerMessage, String simTick, String simID){
		this.tick = simTick;
		this.simID = simID;
		this.tradeObject = new TradeObject(offerMessage);
	}
	
	@Override
	public String toString(){
		String s = "{";
		s += " \"simID\" : \"" + this.simID + "\",";
		s += " \"tick\" : \"" + this.tick + "\",";
		s += this.tradeObject.toString();
		s += "}";
		return s;
	}
	
	/*============GETTERS & SETTER==============*/
	/**
	 * @return the time
	 */
	public String getTime() {
		return tick;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(String tick) {
		this.tick = tick;
	}

	/**
	 * @return the simID
	 */
	public String getSimID() {
		return simID;
	}
	/**
	 * @param simID the simID to set
	 */
	public void setSimID(String simID) {
		this.simID = simID;
	}
	
	
	
}
