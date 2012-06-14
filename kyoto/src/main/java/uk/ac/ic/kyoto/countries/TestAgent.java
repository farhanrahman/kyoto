package uk.ac.ic.kyoto.countries;

import java.util.Set;
import java.util.UUID;

import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.BroadcastMessage;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public class TestAgent extends AbstractCountry {
	
	Set<Offer> trades;

	public TestAgent(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, energyOutput, carbonOutput);
	}
	
	@Override
	public void initialiseCountry(){
		
		try {
			this.tradeProtocol = new TradeProtocol(getID(), this.authkey, environment, network, null) {
				
				@Override
				protected boolean acceptExchange(NetworkAddress from, Offer trade) {
					// TODO decide if we should accept the trade
					// for example...
					if(trade.getUnitCost() == 0){
						return true;
					} else {
						return false;
					}
				}
			};
		} catch (FSMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * Iterates over the input queue: i.e. all messages multicast or unicast to THIS agent
	 */
	protected void processInput(Input in) {
		// TODO Auto-generated method stub
		if (in instanceof Message){
			@SuppressWarnings("unchecked")
			Message<Offer> m = (Message<Offer>) in;
			if(m.getType().equalsIgnoreCase("Trade")){
				//Offer t = (Offer) m.getData();
				//Update our knowledgebase
				//trades.add(t);
				
				
			}
		}

	}
	
	@Override
	public void behaviour() {
//		this.network.sendMessage(
//				new BroadcastMessage<Object>(
//						Performative.PROPOSE, 
//						"TRADE", 
//						SimTime.get(), 
//						network.getAddress(), 
//						new OfferMessage(new Offer(0, 0, TradeType.SELL), authkey)
//				)
//			);
		
		logger.info("I have this much money: " + availableToSpend);
		
		int quantity=400, unitCost=20;
		broadcastBuyOffer(quantity, unitCost);
		
		quantity=400;
		unitCost = 25;
		broadcastSellOffer(quantity, unitCost);
		
		logger.info("PARTRIDGE IN A PEAR TREE");
		
		try {
			logger.info("I am investing " + carbonAbsorptionHandler.getInvestmentRequired(1000) + " in carbon absorption.");
			double bang = carbonAbsorption;
			carbonAbsorptionHandler.investInCarbonAbsorption(1000);
			logger.info("My carbon absorption change is " + (carbonAbsorption - bang));
		} catch (NotEnoughCarbonOutputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotEnoughCashException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			logger.info("I am reducing my energy output by " + energyOutput*0.01);
			energyUsageHandler.reduceEnergyOutput(energyOutput*0.01);
			logger.info("My energy output is " + energyOutput);
			logger.info("I am investing " + availableToSpend*0.01 + " in carbon industry.");
			energyUsageHandler.investInCarbonIndustry(availableToSpend*0.01);
			logger.info("My energy output has gone back up by " + energyOutput);
		} catch (NotEnoughCashException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotEnoughCarbonOutputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			logger.info("I am investing " + carbonReductionHandler.getInvestmentRequired(5000) + " in carbon reduction.");
			double bang = carbonOutput;
			carbonReductionHandler.investInCarbonReduction(5000);
			logger.info("My carbon output change is " + (carbonOutput - bang));
		} catch (NotEnoughCarbonOutputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotEnoughCashException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void YearlyFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SessionFunction() {
		// TODO Auto-generated method stub
		
	};

}
