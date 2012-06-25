package uk.ac.ic.kyoto.tradehistory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.singletonfactory.SingletonProvider;
import uk.ac.imperial.presage2.core.Time;

/**
 * Interface for getting Trade history which is updated
 * by the TradeProtocol after a trade has been made successful
 * 
 * In order to get an implementation of this interface use the
 * following method from any point in your code:
 * 
 * SingletonProvider.getTradeHistory();
 * 
 * @author farhanrahman
 *
 */
public interface TradeHistory {
	/**
	 * Method to get the whole trade history since
	 * tick 0.
	 * @return
	 */
	public ConcurrentHashMap<Integer, Map<UUID, OfferMessage>> getHistory();
	
	/**
	 * Method to get a map of trades for that
	 * has happened in tick = simTime.
	 * @param simTime of type Time
	 * @return
	 */
	public ConcurrentHashMap<UUID, OfferMessage> getHistoryForTime(Time simTime);
	
	/**
	 * Method to check whether trade exists
	 * with the trade ID provided
	 * @param id
	 * @return
	 */
	public boolean tradeExists(UUID id);
	
	/**
	 * Method to insert a particular trade that
	 * has happened
	 * @param simTime
	 * @param tradeID
	 * @param trade
	 */
	public void addToHistory(Time simTime, UUID tradeID, OfferMessage trade);
	
	/**
	 * Method to remove an existing trade
	 * in the trade history with the given
	 * id
	 * @param id
	 */
	public void removeTradeHistoryWithID(UUID id);
	
	/**
	 * Dumps the current tick trade data
	 * into the database.
	 */
	public void dumpData();
	
	/**
	 * Method must be called when setting up
	 * the simulation. The simulation id is
	 * required to store the trade data
	 * in the dumpData() method.
	 * 
	 * In order to store simulation id use the following method
	 * 
	 * SingletonProvider.getTradeHistory().setSimID(this.simPersist.getID());
	 * @param simID
	 */
	public void setSimID(Long simID);
}
