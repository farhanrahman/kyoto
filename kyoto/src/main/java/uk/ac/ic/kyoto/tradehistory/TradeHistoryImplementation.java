package uk.ac.ic.kyoto.tradehistory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.imperial.presage2.core.Time;

import com.google.inject.Singleton;

@Singleton
public class TradeHistoryImplementation implements TradeHistory{
	private static Map<Integer, Map<UUID, Offer>> history = new HashMap<Integer,Map<UUID,Offer>>();
	
	public final Map<Integer,Map<UUID,Offer>> getHistory(){
		return Collections.unmodifiableMap(new HashMap<Integer, Map<UUID,Offer>>(history));
	}

	public Map<UUID,Offer> getHistoryForTime(Time simTime){
		return Collections.unmodifiableMap(history.get(simTime.intValue()));
	}
	
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
	
	public void addToHistory(Time simTime, UUID tradeID, Offer trade){
		synchronized(history){
			Map<UUID, Offer> t = new HashMap<UUID,Offer>();
			t.put(tradeID, trade);
			history.put(simTime.intValue(), t);
		}
	}
	
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