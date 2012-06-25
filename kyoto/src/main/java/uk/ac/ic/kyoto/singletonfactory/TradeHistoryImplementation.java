package uk.ac.ic.kyoto.singletonfactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.tradehistory.TradeHistory;
import uk.ac.ic.kyoto.util.sim.jsonobjects.DataStorer;
import uk.ac.ic.kyoto.util.sim.jsonobjects.tradedata.TradeData;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.simulator.SimTime;

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
	private static Map<Integer, Map<UUID, OfferMessage>> history = new HashMap<Integer,Map<UUID,OfferMessage>>();
	
	private String simID = null;
	
	private Logger logger = Logger.getLogger(TradeHistoryImplementation.class);
	
	private DataStorer dataStorer = new DataStorer();
	/**
	 * @return returns a history of all the trade histories in the form of
	 * a map simulation time -> map of trades that happened in that simulation time
	 */
	public final Map<Integer,Map<UUID,OfferMessage>> getHistory(){
		synchronized(history){
			return (new HashMap<Integer, Map<UUID,OfferMessage>>(history));
		}
	}

	/**
	 * @param simTime
	 * @return the history of trades for simulation
	 * time = simTime
	 */
	public Map<UUID,OfferMessage> getHistoryForTime(Time simTime){
		synchronized(history){
			return history.get(simTime.intValue());
		}
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
	public void addToHistory(Time simTime, UUID tradeID, OfferMessage trade){
		synchronized(history){
			Map<UUID, OfferMessage> t = history.get(simTime.intValue());
			if(t == null){
				t = new HashMap<UUID,OfferMessage>();
			}
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


	/**
	 * Dumps the trade data in the current tick into
	 * the database
	 */
	public void dumpData() {
		try{
			if(this.simID == null){
				throw new Exception("sim id is null, please initialise it in the simulation file");
			}
		
			synchronized(history){
				Integer simTick = SimTime.get().intValue();
				Map<UUID,OfferMessage> trades = history.get(simTick);
				if(trades != null){
					for(UUID id : trades.keySet()){
						TradeData data = new TradeData(trades.get(id), simTick.toString(), this.simID);
						dataStorer.storeTradeData(data.toString());
						logger.debug(data.toString());
					}
				}
			}
		} catch(Exception e){
			logger.warn(e);
		}
	}

	@Override
	public void setSimID(Long simID) {
		this.simID = Long.toString(simID);
	}
}