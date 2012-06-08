package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import uk.ac.ic.kyoto.trade.Offer;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class FakeCanadaAgent extends NonParticipant {

	public FakeCanadaAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput, long carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, availableToSpend, emissionsTarget, carbonOffset,
				energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processInput(Input in) {
		if (this.tradeProtocol != null && this.tradeProtocol.canHandle(in)) {
			this.tradeProtocol.handle(in);
		}
	}
	
/*	private int counter = 0;
	@Override
	public void execute() {
		super.execute();
		
		this.tradeProtocol.incrementTime();
		if(counter < 3){
			int quantity = 10;
			int unitCost = 2;
			Offer trade = new Offer(quantity, unitCost, TradeType.SELL);
			this.network.sendMessage(
						new MulticastMessage<OfferMessage>(
								Performative.PROPOSE, 
								Offer.TRADE_PROPOSAL, 
								SimTime.get(), 
								this.network.getAddress(),
								this.tradeProtocol.getAgentsNotInConversation(),
								new OfferMessage(trade))
					);
		counter++;
		}else{
			TradeHistory tradeHistory = SingletonProvider.getTradeHistory();
			Map<Integer,Map<UUID,Offer>> m = tradeHistory.getHistory();
			logger.info(m);
			logger.info("Test");
		}
	}*/

	@Override
	public void YearlyFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SessionFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initialiseCountry() {
		// TODO Auto-generated method stub
		carbonOutput = 80;
		try {
			tradeProtocol = new TradeProtocol(getID(), authkey, environment, network) {
				@Override
				protected boolean acceptExchange(NetworkAddress from,
						Offer trade) {
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
	protected void behaviour() {
		// TODO Auto-generated method stub
		
	}
}
