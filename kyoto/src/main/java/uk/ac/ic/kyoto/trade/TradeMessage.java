package uk.ac.ic.kyoto.trade;

/**
 * Wrapper class for Trade
 * data. This is to distinguish
 * between Trade data used in
 * TradeProtocol and Trade data
 * used by Participant broadcasting
 * a message that it wants to Trade
 * @author farhanrahman
 *
 */
public class TradeMessage {
	private final Trade trade;
	
	public TradeMessage(Trade trade){
		this.trade = trade;
	}

	public Trade getTrade() {
		return trade;
	}

}
