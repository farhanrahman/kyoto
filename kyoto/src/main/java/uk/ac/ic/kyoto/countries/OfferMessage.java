package uk.ac.ic.kyoto.countries;

import java.util.UUID;

import uk.ac.ic.kyoto.trade.InvestmentType;
import uk.ac.ic.kyoto.trade.TradeType;


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

	enum OfferMessageType{
		TRADE_PROTOCOL,
		BROADCAST_MESSAGE
	};

	private final OfferMessageType offerMessageType;
	/**
	 * Constructor when OfferMessage is sent
	 * as a MultiCast message. No conversation
	 * has been started so tradeID is null
	 * @param offer
	 */
	public OfferMessage(Offer offer, OfferMessageType offerMessageType){
		this.offer = offer;
		this.tradeID = null;
		this.offerMessageType = offerMessageType;
	}

	/**
	 * Constructor when OfferMessage is instantiated
	 * from within TradeProtocol. This instance will
	 * have a valid Trade id
	 * @param offer
	 * @param id
	 */
	public OfferMessage(Offer offer, UUID id, OfferMessageType offerMessageType){
		this.offer = offer;
		this.tradeID = id;
		this.offerMessageType = offerMessageType;
	}
	
	public InvestmentType getOfferInvestmentType(){
		return offer.getInvestmentType();
	}
	
	public TradeType getOfferType(){
		return offer.getType();
	}
	
	public Integer getOfferQuantity(){
		return this.offer.getQuantity();
	}

	public Integer getOfferUnitCost(){
		return this.offer.getUnitCost();
	}
	
	public UUID getTradeID() {
		return tradeID;
	}

	public OfferMessageType getOfferMessageType() {
		return offerMessageType;
	}

}