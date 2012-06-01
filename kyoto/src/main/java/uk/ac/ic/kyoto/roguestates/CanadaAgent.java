package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.NonParticipant;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class CanadaAgent extends NonParticipant {

	public CanadaAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long emissionsTarget, long carbonOffset,
			float economicOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, emissionsTarget, carbonOffset,
				economicOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void initialise() {
		super.initialise();
		try {
			tradeProtocol = new TradeProtocol(getID(), authkey, environment, network) {
				@Override
				protected boolean acceptExchange(NetworkAddress from,
						Trade trade) {
					if (carbonOutput - emissionsTarget + carbonOffset > 0) {
						return true;
					}
					return false;
				}
			};
		} catch (FSMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
	}

}
