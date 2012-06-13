package uk.ac.ic.kyoto.trade;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.TradeProtocol;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.MulticastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class TradeProtocolTestAgent extends AbstractCountry {
	
	Logger logger = Logger.getLogger(TradeProtocolTestAgent.class);
	
	private TradeProtocol tradeProtocol;
	
	public TradeProtocolTestAgent(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			long emissionsTarget, long energyOutput, long carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, emissionsTarget,
				energyOutput);
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
		try {
			tradeProtocol = new TradeProtocol(getID(), authkey, environment, network, null) {
				@Override
				protected boolean acceptExchange(NetworkAddress from,
						Offer trade) {
					return true;
					/*if (carbonOutput - emissionsTarget + carbonOffset < 0) {
						return true;
					}
					return true;*/
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
		//if(counter < 7){
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
		//}
		
	}
	
}
