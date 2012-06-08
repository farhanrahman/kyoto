package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.countries.NonParticipant;
import uk.ac.ic.kyoto.trade.Offer;
import uk.ac.ic.kyoto.trade.OfferMessage;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class CanadaAgent extends NonParticipant {

	Logger logger = Logger.getLogger(CanadaAgent.class);
	
	public CanadaAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, float availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, availableToSpend, emissionsTarget, carbonOffset,
				energyOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processInput(Input in) {

		if(this.tradeProtocol.canHandle(in))
			this.tradeProtocol.handle(in);
		
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
	public void initialise() {
		super.initialise();
		carbonOutput = 80;
		try {
			tradeProtocol = new TradeProtocol(getID(), authkey, environment, network) {
				@Override
				protected boolean acceptExchange(NetworkAddress from,
						Offer trade) {
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
	
	@Override
	public void execute() {
		super.execute();
		this.tradeProtocol.incrementTime();

		
	}
}
