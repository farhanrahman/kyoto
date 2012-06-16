package uk.ac.ic.kyoto.trade;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
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
		}else{

			if(in instanceof Message){
				try{
					@SuppressWarnings("unchecked")
					Message<OfferMessage> m = (Message<OfferMessage>) in;
					OfferMessage o = m.getData();
					if(!this.tradeProtocol
							.getActiveConversationMembers()
								.contains(m.getFrom())){
						try {
							this.tradeProtocol.offer(
									m.getFrom(), 
									o.getOfferQuantity(), 
									o.getOfferUnitCost(), 
									o);
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
			tradeProtocol = new TradeProtocol(getID(), authkey, environment, network, this) {
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
		if(this.getName().equals("Stuart")){
			//if(counter == 0){
				int quantity = 10;
				//double unitCost = 2;
				//this.broadcastBuyOffer(quantity, unitCost);
			//	counter++;
			
			  	InvestmentType i = InvestmentType.ABSORB;
			  	//InvestmentType i = InvestmentType.REDUCE;
		  
			  	this.broadcastInvesteeOffer(quantity, i);
			
			//}
		}
		
		
		//this.tradeProtocol.incrementTime();
		logger.info("Myname: " + this.getName() + ", I have this much money: " + this.getAvailableToSpend() + ".");
		//logger.info("Myname: " + this.getName() + ", My GDPRate is : " + GDPRate);
		//logger.info("Myname: " + this.getName() + ", My carbon output is : " + carbonOutput);
		//logger.info("Myname: " + this.getName() + ", My energy output is : " + energyOutput);
		logger.info("Myname: " + this.getName() + ", My carbonOffset is : " + this.getCarbonOffset());
	}
	
}
