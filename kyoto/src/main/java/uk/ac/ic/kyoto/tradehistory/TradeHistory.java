package uk.ac.ic.kyoto.tradehistory;

import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.trade.Offer;
import uk.ac.imperial.presage2.core.Time;

public interface TradeHistory {
	public Map<Integer,Map<UUID,Offer>> getHistory();
	
	public Map<UUID,Offer> getHistoryForTime(Time simTime);
	
	public boolean tradeExists(UUID id);
	
	public void addToHistory(Time simTime, UUID tradeID, Offer trade);
}
