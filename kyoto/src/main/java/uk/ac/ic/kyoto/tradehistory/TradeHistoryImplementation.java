package uk.ac.ic.kyoto.tradehistory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.imperial.presage2.core.Time;

import com.google.inject.Singleton;

/**
 * Singleton class implementation providing
 * access to the history of trades that has
 * happened between participants.
 * @author farhanrahman
 *
 */
@Singleton
public class TradeHistoryImplementation implements TradeHistory{
	private static Map<Integer, Map<UUID, Offer>> history = new HashMap<Integer,Map<UUID,Offer>>();
	
	/**
	 * @return returns a history of all the trade histories in the form of
	 * a map simulation time -> map of trades that happened in that simulation time
	 */
	public final Map<Integer,Map<UUID,Offer>> getHistory(){
		return Collections.unmodifiableMap(new HashMap<Integer, Map<UUID,Offer>>(history));
	}

	/**
	 * @param simTime
	 * @return the history of trades for simulation
	 * time = simTime
	 */
	public Map<UUID,Offer> getHistoryForTime(Time simTime){
		return Collections.unmodifiableMap(history.get(simTime.intValue()));
	}
	
	/**
	 * @param id
	 * @return true if trade of trade id = id
	 * has been registered in the map.
	 */
	public boolean tradeExists(UUID id){
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
	 * updates trade history with
	 * the given information
	 * @param simTime, tradeID, trade
	 */
	public void addToHistory(Time simTime, UUID tradeID, Offer trade){
		synchronized(history){
			Map<UUID, Offer> t = new HashMap<UUID,Offer>();
			t.put(tradeID, trade);
			history.put(simTime.intValue(), t);
		}
	}
	
	/**
	 * removes trade from the history with UUID = id
	 * @param id
	 */
	public void removeTradeHistoryWithID(UUID id){
		synchronized(history){
			for(Integer time : history.keySet()){
				for(UUID uid : history.get(time).keySet()){
					if(uid.equals(id))
						history.get(time).remove(uid);
				}
			}
		}		
	}
	
}