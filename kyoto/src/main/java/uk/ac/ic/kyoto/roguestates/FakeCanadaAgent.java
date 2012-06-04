package uk.ac.ic.kyoto.roguestates;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.mongodb.MongoException.Network;

import uk.ac.ic.kyoto.countries.NonParticipant;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class FakeCanadaAgent extends NonParticipant {

	public FakeCanadaAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, float availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, availableToSpend, emissionsTarget, carbonOffset,
				energyOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processInput(Input in) {
		if (this.tradeProtocol != null && this.tradeProtocol.canHandle(in)) {
			this.tradeProtocol.handle(in);
		}
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
					if (carbonOutput - emissionsTarget + carbonOffset < 0) {
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
	
	@Override
	public void execute() {
		super.execute();
		
//		Set<NetworkAddress> nodes = network.getConnectedNodes();
//		Iterator i = nodes.iterator();
//		while (i.hasNext()) {
//			(NetworkAddress)i.
//		}
//		for (NetworkAddress i: nodes) {
//			try {
//				tradeProtocol.offer(i, 10, 5, TradeType.BUY);
//			} catch (FSMException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		this.tradeProtocol.incrementTime();
		
		if (this.tradeProtocol != null) {
			for (NetworkAddress a : this.network.getConnectedNodes()) {
				try {
					if(!this.tradeProtocol.getActiveConversations().contains(a))
						this.tradeProtocol.offer(a, 10, 5, TradeType.BUY);
				} catch (FSMException e) {
					logger.warn("Error creating token offer", e);
				}
			}
			this.tradeProtocol.incrementTime();
		}
	}
}
