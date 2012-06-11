package uk.ac.ic.kyoto.tradehistory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.trade.Offer;
import uk.ac.imperial.presage2.core.Time;

import com.google.inject.Singleton;

/**
 * Singleton class that provides
 * read and write access to the trade
 * history so far
 * @author farhanrahman
 *
 */
@Singleton
public class TradeHistoryImplementation implements TradeHistory{
	private static Map<Integer, Map<UUID, Offer>> history = new HashMap<Integer,Map<UUID,Offer>>();
	
	public Map<Integer,Map<UUID,Offer>> getHistory(){
		return new HashMap<Integer, Map<UUID,Offer>>(history);
	}

	public Map<UUID,Offer> getHistoryForTime(Time simTime){
		if(history.get(simTime.intValue()) == null){
			return new HashMap<UUID,Offer>();
		}else{
			return new HashMap<UUID,Offer>(history.get(simTime.intValue()));
		}
	}
	
	public boolean tradeExists(UUID id){
		synchronized(history){
			for(Integer time : history.keySet()){
				for(UUID uid : history.get(time).keySet()){
					uid.equals(id);
					return true;
				}
			}
		}
		return false;
	}
	
	public void addToHistory(Time simTime, UUID tradeID, Offer trade){
		synchronized(history){
			Map<UUID, Offer> t = new HashMap<UUID,Offer>();
			t.put(tradeID, trade);
			history.put(simTime.intValue(), t);
		}
	}
	
}
