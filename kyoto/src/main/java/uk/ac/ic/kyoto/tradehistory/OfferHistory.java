/**
 * 
 */
package uk.ac.ic.kyoto.tradehistory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.imperial.presage2.core.Time;

/**
 * Stores all the offers that has been
 * made by participants. This will therefore
 * store successful and unsuccessful trades.
 * 
 * NOTE: this will also store failed trades
 * 
 * This is just to see what participants are
 * offering or has offered over time.
 * 
 * In order to use this you must declare a field
 * in your class and instantiate it in the following way:
 * 
 * OfferHistory offerHistory = new OfferHistory();
 * 
 * @author farhanrahman
 *
 */
public class OfferHistory {
	
	private static Map<Integer, Map<UUID, OfferMessage>> history = new HashMap<Integer,Map<UUID,OfferMessage>>();
	/**
	 * 
	 */
	
	/**
	 * Public constructor.
	 */
	public OfferHistory(){
		
	}
	

	/**
	 * The method returns an unmodifiable map of the offers
	 * that were made since tick 0.
	 * 
	 * @return the offer histories since tick 0.
	 */
	public Map<Integer, Map<UUID, OfferMessage>> getHistory() {
		return Collections.unmodifiableMap(new HashMap<Integer, Map<UUID,OfferMessage>>(history));
	}

	/**
	 * Use this method if you want to extract out the information
	 * on a particular simulation tick.
	 * @param simTime type=Time.
	 * @return the offer history for tick = simTime
	 */
	public Map<UUID, OfferMessage> getHistoryForTime(Time simTime) {
		return Collections.unmodifiableMap(history.get(simTime.intValue()));
	}
	
	/**
	 * This is the preferred method since you can query for any simulation
	 * time, whereas with the getHistoryForTime(Time simTime) method you cannot
	 * subtract or add to simTime.
	 * @param simTime type=Integer
	 * @return the offer history for simulation tick = simTime.
	 */
	public Map<UUID, OfferMessage> getHistoryForTime(Integer simTime) {
		return Collections.unmodifiableMap(history.get(simTime));
	}

	/**
	 * Use this method to verify or query the existence of
	 * a particular offer made
	 * @param id
	 * @return true if offer exists with given id.
	 */
	public boolean tradeExists(UUID id) {
		synchronized(history){
			for(Integer time : history.keySet()){
				for(UUID uid : history.get(time).keySet()){
					if(uid.equals(id))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Use this method to add to the history of offers. You should
	 * ideally not use this method. This is used by TradeProtocol only.
	 * @param simTime
	 * @param tradeID
	 * @param trade
	 */
	public void addToHistory(Time simTime, UUID tradeID, OfferMessage trade) {
		synchronized(history){
			Map<UUID, OfferMessage> t = new HashMap<UUID,OfferMessage>();
			t.put(tradeID, trade);
			history.put(simTime.intValue(), t);
		}
		
	}

}
