package uk.ac.ic.kyoto.countries;

import java.util.UUID;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.Time;

/**
 * @author sc1109
 */
public class TradeAction implements Action {
	
	final private Offer offer;
	final private Time time;
	final private UUID initiator;
	final private UUID receiver;
	
	public TradeAction(Offer offer, Time time, UUID initiator, UUID receiver) {
		super();
		this.offer = offer;
		this.time = time;
		this.initiator = initiator;
		this.receiver = receiver;
	}
	
	@Override
	public String toString() {
		return "Trade occurred at: " + time.intValue() + " between "+ initiator + " and " + receiver;
	}

	public Offer getOffer() {
		return offer;
	}
	
	public Time getTime() {
		return time;
	}

	public UUID getInitiator() {
		return initiator;
	}

	public UUID getReceiver() {
		return receiver;
	}

}
