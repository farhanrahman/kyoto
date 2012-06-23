package uk.ac.ic.kyoto.roguestates;


import java.util.Random; 
import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.ic.kyoto.trade.InvestmentType;
import uk.ac.ic.kyoto.services.FossilPrices;
import uk.ac.ic.kyoto.roguestates.USAgent;
import uk.ac.ic.kyoto.countries.AbstractCountry.KyotoMember;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.countries.Offer;


import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;


public class CanadaAgent extends AbstractCountry {

	private double	AverageGDPRate; // to be stored in an array or DB for furhter analysis.
	long Currentyear;				//The present year global	
	long Prevyear=Currentyear-1;	//Previous year
	int n=8,m=3;					//Number of players and projects initially
	int industry_players=0;
	int industry_projects=0;
	double ratio;
	public CanadaAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double emissionsTarget, double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, energyOutput, carbonOutput);
		//
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		return super.getSharedState();
	}

	
	
	@Override
	public void yearlyFunction() {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * Increase carbon absorption as long as you have land area and money
	 */
	
			
	/******************************************************
	@Override
	//Check Carbon Output data and decide a feasible target to set 
	//as we are intially part of kyoto 
	 * @return
	 *********************************************************/
 
		//FossilPrices price = new FossilPrices();
		
	
		/*Function to check oil prices to ramp or reduce production  */
		/*boolean check_oil_prices(long year){
			if(((price.getOilPrice(year)) > 100)){
				System.out.print("We can reduce production");
				return true;
			}
			else return false;
			}
		*/
		
	/*Emisions target to be checked against to gauge performance  */			
	public double SetEmissionsTarget(){
		return this.getEmissionsTarget();
		
	}
	
	/*Check our performancce  */
	boolean check_emmision(long year){
		if((this.getEnergyOutput()) <= (this.getPrevEnergyOutput())){
			System.out.print("Good progress");
			return true;
		}
		else return false;
	}
	
	Random rand1= new Random();
	Random rand2= new Random();
	
	/*This function checks the industry projects against players  */
	/*to determine feasibility of carbon reduction */
	
	public boolean check_industry(){
				
		industry_players=rand1.nextInt(n);
		industry_projects=rand2.nextInt(m);
		if((industry_players<=8) && (industry_projects<=5)){
			return true;
		}	
		else return false;
	}
	
	/*if the GDP rate is less than or equal to the */
	/*available to spend against gdp ratio we return true and stay in kyoto  */
	public boolean check_ratio(){
		double ratio,carb,gdp_r,gdp_a;
		carb=this.getAvailableToSpend();
		gdp_r=this.getGDPRate();
		gdp_a=this.getGDP();
		ratio=carb/gdp_a;
		if(ratio <= gdp_r){
			return true;
		}
		else return false;
	}
	
	///////////
	
	/*public void SessionFunction() {
		if(JoiningCriteriaMet()){
			KyotoMember.NONANNEXONE;
		}
		
		
	}*/
	
	
	
	
	/*
	boolean JoiningCriteriaMet()
<<<<<<< HEAD
	{
		if(((this.getCarbonOutput() - this.getCarbonOffset()) >this.getEmissionsTarget())){
=======
	{ // TODO emissions target will only resolve if you are already a member of Kyoto
		if(((this.getCarbonOutput()) > this.getEmissionsTarget()) && ()){
>>>>>>> 3804685022313493de0f441c6beb2984c3fea077
			return true;
		}
		//Only useful after 2011 when canada leaves
		else leaveKyoto();
		return false;
		
		//If GDP growth achieved in the past 4 years
		//
	}
*/
	uk.ac.imperial.presage2.core.messaging.Input input;
	@Override
	protected void processInput() {
		if(this.tradeProtocol.canHandle(input)){
			this.tradeProtocol.handle(input);
		}else{
			OfferMessage offerMessage = this.tradeProtocol.decodeInput(input);
			NetworkAddress from =this.tradeProtocol.extractNetworkAddress(input);
			double quantity = offerMessage.getOfferQuantity();
			if(analyse_offer(offerMessage)){
				try{
					this.tradeProtocol.respondToOffer(from, quantity,offerMessage);
				} catch(IllegalArgumentException e1){
					logger.warn(e1);
				} catch(FSMException e1){
					logger.warn(e1);
				}
			}
			
		}
		
		
	};
	
	boolean analyse_offer(OfferMessage offerMessage){
		//If the offer is feasible return true else false
		double cost = offerMessage.getOfferUnitCost();
		double quantity = offerMessage.getOfferQuantity();
		double available = this.getAvailableToSpend();
		double carb_red_cost=this.carbonReductionHandler.getInvestmentRequired(quantity);
		double trade_cost = (cost*quantity);
		if((trade_cost < carb_red_cost) && (trade_cost<available) &&(carb_red_cost<available)){
			return true;
		}
		else return false;
		
	}
	

	
	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	@Override
	protected void behaviour() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void sessionFunction() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void initialiseCountry() {
		// TODO Auto-generated method stub
		
	}



	
	
	/******************************************************************************
	//@Override
	//public void sessionFunction() {
		//if (getCarbonOutput() - getCarbonOffset() > getEmissionsTarget()) {
			// leave Kyoto here
		//}
			
//	}
	
	
	@Override
	public void initialiseCountry() {
		//carbonOutput = 80;
//		try {
//			tradeProtocol = new TradeProtocol(getID(), authkey, environment, network) {
//				@Override
//				protected boolean acceptExchange(NetworkAddress from,
//						Trade trade) {
//					if (carbonOutput - emissionsTarget + carbonOffset > 0) {
//						return true;
//					}
//					return true;
//				}
//			};
//		} catch (FSMException e) {
//			logger.warn(e.getMessage(), e);
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void behaviour() {
//		Set<NetworkAddress> nodes = network.getConnectedNodes();
//		for (NetworkAddress i: nodes) {
//			try {
//				tradeProtocol.offer(i, 10, 5, TradeType.BUY);
//			} catch (FSMException e) {
//				e.printStackTrace();
//			}
//		}
		if (getAvailableToSpend() > 0) {
			try {
				//carbonReductionHandler.invest(availableToSpend*0.1);
				System.out.println("Spending " + getAvailableToSpend()* 0.1 + " on carbon reduction. Current carbon output is " + getCarbonOutput() + ".");
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
				e.printStackTrace();
			}
		}
//		if (availableToSpend > 0) {
//			try {
//				energyUsageHandler.investInCarbonIndustry((long) (availableToSpend*0.1));
//				System.out.println("Spending " + availableToSpend* 0.1 + " on industry investment.");
//				System.out.println();
//				} catch (Exception e) {
//				logger.warn(e.getMessage(), e);
//				e.printStackTrace();
//			}
//		}
//		System.out.println(energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(500));
		System.out.println("I have this much money: " + getAvailableToSpend() + ".");
		System.out.println("My GDPRate is : " + getGDPRate());
		System.out.println("My carbon output is : " + getCarbonOutput());
		System.out.println("My energy output is : " + getEnergyOutput());
	}

	***********************************************************************/
	
}
