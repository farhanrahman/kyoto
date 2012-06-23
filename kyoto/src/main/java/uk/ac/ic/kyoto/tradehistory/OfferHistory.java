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
 * @author farhanrahman
 *
 */
public class OfferHistory {
	
	private static Map<Integer, Map<UUID, OfferMessage>> history = new HashMap<Integer,Map<UUID,OfferMessage>>();
	/**
	 * 
	 */
	
	/**
	 * Public constructor
	 */
	public OfferHistory(){
		
	}
	

	public Map<Integer, Map<UUID, OfferMessage>> getHistory() {
		return Collections.unmodifiableMap(new HashMap<Integer, Map<UUID,OfferMessage>>(history));
	}

	public Map<UUID, OfferMessage> getHistoryForTime(Time simTime) {
		return Collections.unmodifiableMap(history.get(simTime.intValue()));
	}
	
	public Map<UUID, OfferMessage> getHistoryForTime(Integer simTime) {
		return Collections.unmodifiableMap(history.get(simTime));
	}

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


	public void addToHistory(Time simTime, UUID tradeID, OfferMessage trade) {
		synchronized(history){
			Map<UUID, OfferMessage> t = new HashMap<UUID,OfferMessage>();
			t.put(tradeID, trade);
			history.put(simTime.intValue(), t);
		}
		
	}

}
