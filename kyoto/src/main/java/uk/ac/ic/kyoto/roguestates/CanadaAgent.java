package uk.ac.ic.kyoto.roguestates;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.mongodb.MongoException.Network;

import uk.ac.ic.kyoto.services.TimeService.EndOfSession;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class CanadaAgent extends NonParticipant {

	public CanadaAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput, long carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, availableToSpend, emissionsTarget, carbonOffset,
				energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void initialise() {
		super.initialise();
		carbonOutput = 80;
		try {
			tradeProtocol = new TradeProtocol(getID(), authkey, environment, network) {
				@Override
				protected boolean acceptExchange(NetworkAddress from,
						Trade trade) {
					if (carbonOutput - emissionsTarget + carbonOffset > 0) {
						return true;
					}
					return true;
				}
			};
		} catch (FSMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EventListener
	private void SessionEnd(EndOfSession e) {
		if (carbonOutput - carbonOffset > emissionsTarget) {
			// leave Kyoto here
		}
	}
	
	@Override
	public void execute() {
		super.execute();
		Set<NetworkAddress> nodes = network.getConnectedNodes();
//		Iterator i = nodes.iterator();
//		while (i.hasNext()) {
//			(NetworkAddress)i.
//		}
		for (NetworkAddress i: nodes) {
			try {
				tradeProtocol.offer(i, 10, 5, TradeType.BUY);
			} catch (FSMException e) {
				e.printStackTrace();
			}
		}
	}

}
