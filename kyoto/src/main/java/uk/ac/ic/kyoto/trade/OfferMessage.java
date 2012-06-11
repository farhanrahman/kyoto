package uk.ac.ic.kyoto.trade;

import java.util.UUID;

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
public class OfferMessage {
	private final Offer offer;
	private final UUID tradeID;
	
	public OfferMessage(Offer offer){
		this.offer = offer;
		this.tradeID = null;
	}
	
	public OfferMessage(Offer offer, UUID id){
		this.offer = offer;
		this.tradeID = id;
	}

	public Offer getOffer() {
		return offer;
	}

	public UUID getTradeID() {
		return tradeID;
	}
	
}
