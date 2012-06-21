package uk.ac.ic.kyoto.testagents;

import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReport;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.trade.InvestmentType;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class TestAgent extends AbstractCountry {
	
	Set<Offer> trades;

	public TestAgent(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, energyOutput, carbonOutput);
	}
	
	@Override
	public void initialiseCountry(){
	}

	@Override
	/**
	 * Iterates over the input queue: i.e. all messages multicast or unicast to THIS agent
	 */
	protected void processInput(Input in) {
		if (this.tradeProtocol.canHandle(in)) {
			this.tradeProtocol.handle(in);
		}
		else {

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
	protected double getReportedCarbonOutput() {
		return super.getReportedCarbonOutput();
	}
	
	@Override
	public void behaviour() {
		
		//logger.info("I have this much money: " + availableToSpend);
		
		try {
			environment.act(new SubmitCarbonEmissionReport(getCarbonOutput()), getID(), authkey);
		} catch (ActionHandlingException e1) {
			e1.printStackTrace();
		}
		
		int quantity=400, unitCost=20;
		//logger.info("I be broadcasting a buy offer, mon!");
		broadcastBuyOffer(quantity, unitCost);
		
		quantity=400;
		unitCost = 25;
		//logger.info("I done kinda broadcast sell offer, eh?");
		broadcastSellOffer(quantity, unitCost);
		
		//logger.info("PARTRIDGE IN A PEAR TREE");
		
		try {
			logger.info("I am investing " + carbonAbsorptionHandler.getInvestmentRequired(getCarbonOutput()*0.005) + " in carbon absorption.");
			double bang = getCarbonAbsorption();
			carbonAbsorptionHandler.investInCarbonAbsorption(getCarbonOutput()*0.005);
			logger.info("My carbon absorption change is " + (getCarbonAbsorption() - bang));
		} catch (NotEnoughCashException e) {
			logger.warn(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		
		try {
			logger.info("I am reducing my energy output by " + getEnergyOutput()*0.01);
			energyUsageHandler.reduceEnergyOutput(getEnergyOutput()*0.01);
			logger.info("My energy output is " + getEnergyOutput());
			logger.info("I am investing " + getAvailableToSpend()*0.01 + " in carbon industry.");
			energyUsageHandler.investInCarbonIndustry(getAvailableToSpend()*0.01);
			logger.info("My energy output has gone back up by " + getEnergyOutput());
		} catch (NotEnoughCashException e) {
			logger.warn(e.getMessage(), e);
		} catch (NotEnoughCarbonOutputException e) {
			logger.warn(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		
		try {
			logger.info("I am investing " + carbonReductionHandler.getInvestmentRequired(getCarbonOutput()*0.005) + " in carbon reduction.");
			double bang = getCarbonOutput();
			carbonReductionHandler.investInCarbonReduction(getCarbonOutput()*0.005);
			logger.info("My carbon output change is " + (getCarbonOutput() - bang));
		} catch (NotEnoughCarbonOutputException e) {
			logger.warn(e.getMessage(), e);
		} catch (NotEnoughCashException e) {
			logger.warn(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		
		if (getName() == "Lolocaust") {
			logger.info("ME WANT INVEST!");
			broadcastInvesteeOffer(400, InvestmentType.REDUCE);
		}
		
	}

	@Override
	public void yearlyFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		if (trade.getUnitCost() == 0) {
			logger.info("Accepting trade!" + trade);
			return true;
		} else {
			logger.info("Refusing trade :( " + trade);
			return false;
		}
	}
}
