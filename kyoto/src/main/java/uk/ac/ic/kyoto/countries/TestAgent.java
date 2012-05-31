package uk.ac.ic.kyoto.countries;

import java.util.UUID;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public class TestAgent extends AbstractParticipant {
	
	TradeProtocol trade;

	public TestAgent(UUID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initialise(){
		super.initialise();
		try {
			this.trade = new TradeProtocol(getID(), authkey, environment, network) {}
		}
	}

	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}

}
