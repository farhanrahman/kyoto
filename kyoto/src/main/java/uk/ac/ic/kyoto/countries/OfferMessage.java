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
	
	private final UUID broadCaster;
	private UUID initiator;

	enum OfferMessageType{
		TRADE_PROTOCOL,
		BROADCAST_MESSAGE
	};

	private final OfferMessageType offerMessageType;
	

	/**
	 * Constructor when OfferMessage is instantiated
	 * from within TradeProtocol. This instance will
	 * have a valid Trade id
	 * @param offer
	 * @param id
	 */
	public OfferMessage(Offer offer, UUID id, OfferMessageType offerMessageType, UUID broadCaster){
		this.offer = offer;
		this.tradeID = id;
		this.offerMessageType = offerMessageType;
		this.broadCaster = broadCaster;
		this.initiator = null;
	}
	
	/**
	 * 
	 * @return
	 */
	public InvestmentType getOfferInvestmentType(){
		return offer.getInvestmentType();
	}
	
	/**
	 * 
	 * @return
	 */
	public TradeType getOfferType(){
		return offer.getType();
	}
	
	/**
	 * 
	 * @return
	 */
	public Double getOfferQuantity(){
		return this.offer.getQuantity();
	}

	/**
	 * 
	 * @return returns unit cost of type Double
	 */
	public Double getOfferUnitCost(){
		return this.offer.getUnitCost();
	}
	
	/**
	 * 
	 * @return returns trade id of type UUID
	 */
	public UUID getTradeID() {
		return tradeID;
	}

	/**
	 * 
	 * @return offer message type
	 */
	public OfferMessageType getOfferMessageType() {
		return offerMessageType;
	}

	public UUID getBroadCaster() {
		return broadCaster;
	}

	/**
	 * @return the initiator
	 */
	public UUID getInitiator() {
		return initiator;
	}

	/**
	 * @param initiator the initiator to set
	 */
	void setInitiator(UUID initiator) {
		this.initiator = initiator;
	}

}