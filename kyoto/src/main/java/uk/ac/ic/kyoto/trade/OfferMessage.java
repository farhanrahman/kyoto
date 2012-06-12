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
	
	public static enum OfferType{
		TRADE_PROTOCOL,
		BROADCAST_MESSAGE
	};
	
	private final OfferType offerState;
	/**
	 * Constructor when OfferMessage is sent
	 * as a MultiCast message. No conversation
	 * has been started so tradeID is null
	 * @param offer
	 */
	public OfferMessage(Offer offer){
		this.offer = offer;
		this.tradeID = null;
		this.offerState = OfferMessage.OfferType.BROADCAST_MESSAGE;
	}
	
	/**
	 * Constructor when OfferMessage is instantiated
	 * from within TradeProtocol. This instance will
	 * have a valid Trade id
	 * @param offer
	 * @param id
	 */
	public OfferMessage(Offer offer, UUID id){
		this.offer = offer;
		this.tradeID = id;
		this.offerState = OfferMessage.OfferType.TRADE_PROTOCOL;
	}

	public Offer getOffer() {
		return offer;
	}

	public UUID getTradeID() {
		return tradeID;
	}

	public OfferType getOfferState() {
		return offerState;
	}
	
}
