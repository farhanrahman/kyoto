package uk.ac.ic.kyoto.tradehistory;

import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.imperial.presage2.core.Time;

/**
 * Interface for getting
 * Trade history which is updated
 * by the TradeProtocol after
 * a trade has been made successful
 * @author farhanrahman
 *
 */
public interface TradeHistory {
	public Map<Integer,Map<UUID,Offer>> getHistory();
	
	public Map<UUID,Offer> getHistoryForTime(Time simTime);
	
	public boolean tradeExists(UUID id);
	
	public void addToHistory(Time simTime, UUID tradeID, Offer trade);
	
	public void removeTradeHistoryWithID(UUID id);
}
