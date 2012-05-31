package uk.ac.ic.kyoto.trade;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

// Action object for responding to advertised offer
@Deprecated
/**
 * Deprecated in preference for TradeProtocol
 * 
 * @author cmd08
 *
 */
public final class InitiateTrade implements Action {
	
	private final PublicOffer offer;
	private final int quantity;
	private final int unitPrice;
	private final AbstractParticipant owner;
	private final AbstractParticipant responder;
	
	public InitiateTrade(PublicOffer offer, int quantity, int unitPrice,
			AbstractParticipant owner, AbstractParticipant responder) {
		this.offer = offer;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.owner = owner;
		this.responder = responder;
	}

	public PublicOffer getOffer() {
		return offer;
	}
	public int getQuantity() {
		return quantity;
	}
	public int getUnitPrice() {
		return unitPrice;
	}
	public AbstractParticipant getOwner() {
		return owner;
	}
	public AbstractParticipant getResponder() {
		return responder;
	}
}
