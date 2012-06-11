package uk.ac.ic.kyoto.annex1reduce;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.trade.Offer;
import uk.ac.ic.kyoto.trade.OfferMessage;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.MulticastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class EUTest1 extends AbstractCountry{
	
	Logger logger = Logger.getLogger(EUTest1.class);
	
	private TradeProtocol tradeProtocol;
	
	public EUTest1(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			long emissionsTarget, long energyOutput, long carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, emissionsTarget,
				energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub
		
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
			this.tradeProtocol = new TradeProtocol(getID(), authkey, environment, network) {
				
				@Override
				protected boolean acceptExchange(NetworkAddress from, Offer trade) {
					//TODO Make this smart
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
		
		this.tradeProtocol.incrementTime();	// Why is this incremented here?
		
		int quantity = 10;
		int unitCost = 2;
		
		Offer trade = new Offer(quantity, unitCost, TradeType.SELL);
		
		this.network.sendMessage(
					new MulticastMessage<OfferMessage>(
							Performative.PROPOSE, 
							Offer.TRADE_PROPOSAL, 
							SimTime.get(), 
							this.network.getAddress(),							// Get your own network address
							this.tradeProtocol.getAgentsNotInConversation(),	
							new OfferMessage(trade))
				);
		
	}

}
