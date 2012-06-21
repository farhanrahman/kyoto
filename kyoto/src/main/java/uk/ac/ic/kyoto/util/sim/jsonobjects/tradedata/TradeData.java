package uk.ac.ic.kyoto.util.sim.jsonobjects.tradedata;

import java.util.Map;
import java.util.UUID;

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
	private TradeObject trades;
	
	
	public TradeData(Map<UUID, OfferMessage> trades, String simTick, String simID){
		this.tick = simTick;
		this.simID = simID;
		this.trades = new TradeObject(trades);
	}
	
	@Override
	public String toString(){
		String s = "{";
		s += " \"simID\" : \"" + this.simID + "\",";
		s += " \"tick\" : \"" + this.tick + "\",";
		s += " \"trades\" : " + this.trades.toString();
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
	 * @return the trades
	 */
	public TradeObject getTrades() {
		return trades;
	}
	/**
	 * @param trades the trades to set
	 */
	public void setTrades(TradeObject trades) {
		this.trades = trades;
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
