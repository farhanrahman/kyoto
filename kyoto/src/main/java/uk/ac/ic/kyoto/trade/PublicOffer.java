package uk.ac.ic.kyoto.trade;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

// Action object for advertising that you want to buy/sell
public final class PublicOffer implements Action {

	public enum TradeType{
		BUY, SELL
	}

	private final TradeType type;
	private final int quantity;
	private final int unitPrice;
	private final AbstractParticipant owner;

	public PublicOffer(TradeType type, int quantity, int unitPrice, AbstractParticipant owner) {
		this.type = type;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.owner = owner;
	}
	
	public TradeType getType() {
		return type;
	}
	public int getQuantity() {
		return quantity;
	}
	public int getUnitPrice() {
		return unitPrice;
	}
	public AbstractParticipant getOwner(){
		return owner;
	}
	
}
