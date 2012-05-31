package uk.ac.ic.kyoto.countries;

import java.util.Set;
import java.util.UUID;
import uk.ac.ic.kyoto.trade.TradeMessage;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.ic.kyoto.trade.TradeProtocol.Trade;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.MulticastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public class TestAgent extends AbstractParticipant {
	
	TradeProtocol trade;
	Set<Trade> trades;

	public TestAgent(UUID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initialise(){
		super.initialise();
		
		try {
			this.trade = new TradeProtocol(getID(), this.authkey, environment, network) {
				
				@Override
				protected boolean acceptExchange(NetworkAddress from, Trade trade) {
					// TODO decide if we should accept the trade
					// for example...
					if(trade.getUnitCost() == 0){
						return true;
					} else {
						return false;
					}
				}
			};
		} catch (FSMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * Iterates over the input queue: i.e. all messages multicast or unicast to THIS agent
	 */
	protected void processInput(Input in) {
		// TODO Auto-generated method stub
		if (in instanceof Message){
			Message m = (Message) in;
			if(m.getType().equalsIgnoreCase("Trade")){
				Trade t = (Trade) m.getData();
				//Update our knowledgebase
				//trades.add(t);
				
				
			}
		}

	}
	
	public void execute() {
		super.execute();
		this.network.sendMessage(
				new MulticastMessage<Object>(
						Performative.PROPOSE, 
						"TRADE", 
						SimTime.get(), 
						network.getAddress(), 
						new TradeMessage()
				)
			);
		
		//Negotiate over the network here...
		
		
	};

}
