package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.trade.Offer;
import uk.ac.ic.kyoto.trade.OfferMessage;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.MulticastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class FakeCanadaAgent extends NonParticipant {

	Logger logger = Logger.getLogger(FakeCanadaAgent.class);
	
	public FakeCanadaAgent(UUID id, String name, String ISO){
		super(id, name, ISO);
	}
	
	public FakeCanadaAgent(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long emissionsTarget, long energyOutput, long carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, emissionsTarget,
				energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void processInput(Input in) {
		if (this.tradeProtocol.canHandle(in)) {
			this.tradeProtocol.handle(in);
		}
		
		if(in instanceof Message){
			try{
				@SuppressWarnings("unchecked")
				Message<OfferMessage> m = (Message<OfferMessage>) in;
				Offer t = m.getData().getOffer();

				if(!this.tradeProtocol
						.getActiveConversationMembers()
							.contains(m.getFrom())){
					try {
						this.tradeProtocol.offer(
								m.getFrom(), 
								t.getQuantity(), 
								t.getUnitCost(), 
								t.reverse().getType());
					} catch (FSMException e) {
						e.printStackTrace();
					}
				}
			}catch(ClassCastException e){
				logger.warn("Class cast exception");
				logger.warn(e);
			}
		}			
	}

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
			e.printStackTrace();
		}		
		
	}
	
	private int counter = 0;

	@Override
	protected void behaviour() {
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
			//TODO might need to acquire Trade ID here
					);
		counter++;
		}
	}
}
