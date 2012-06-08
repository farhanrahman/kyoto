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
public class OfferMessage {
	private final Offer offer;
	
	public OfferMessage(Offer offer){
		this.offer = offer;
	}

	public Offer getOffer() {
		return offer;
	}

}
