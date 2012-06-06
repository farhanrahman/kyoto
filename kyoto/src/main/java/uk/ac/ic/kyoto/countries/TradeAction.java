package uk.ac.ic.kyoto.countries;

import java.util.UUID;

import uk.ac.ic.kyoto.trade.Trade;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.Time;

/**
 * @author sc1109
 */
public class TradeAction implements Action {
	
	final private Trade t;
	final private Time time;
	final private UUID initiator;
	final private UUID receiver;
	
	public TradeAction(Trade t, Time time, UUID initiator, UUID receiver) {
		super();
		this.t = t;
		this.time = time;
		this.initiator = initiator;
		this.receiver = receiver;
	}
	
	@Override
	public String toString() {
		return "Trade occurred";
	}

	public Trade getT() {
		return t;
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
