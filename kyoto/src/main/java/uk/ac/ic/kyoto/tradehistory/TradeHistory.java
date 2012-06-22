package uk.ac.ic.kyoto.tradehistory;

import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.OfferMessage;
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
	public Map<Integer, Map<UUID, OfferMessage>> getHistory();
	
	public Map<UUID, OfferMessage> getHistoryForTime(Time simTime);
	
	public boolean tradeExists(UUID id);
	
	public void addToHistory(Time simTime, UUID tradeID, OfferMessage trade);
	
	public void removeTradeHistoryWithID(UUID id);
	
	public void dumpData();
	
	public void setSimID(Long simID);
}
