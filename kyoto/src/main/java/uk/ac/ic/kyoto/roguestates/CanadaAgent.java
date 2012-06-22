package uk.ac.ic.kyoto.roguestates;

import java.util.Random; 
import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.ic.kyoto.trade.InvestmentType;
import uk.ac.ic.kyoto.services.FossilPrices;
import uk.ac.ic.kyoto.roguestates.USAgent;

import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;


public class CanadaAgent extends AbstractCountry {

	private double	AverageGDPRate; // to be stored in an array or DB for furhter analysis.
	long Currentyear;				//The present year global	
	long Prevyear=Currentyear-1;	//Previous year
	int n=8,m=3;
	int industry_players=0;
	int industry_projects=0;
	double ratio;
	public CanadaAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double emissionsTarget, double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, energyOutput, carbonOutput);
		leaveKyoto();
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		return super.getSharedState();
	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub

	}
	//A useful function but not 100% sure where it might help
	private void CalculateAverageGDP() {
		// Previous cumulative GDP changes divided by new total years elapsed. 
			try {
				ParticipantTimeService timeService = getEnvironmentService(ParticipantTimeService.class);			
			} 
			catch (UnavailableServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int YearsElapsed = timeService.getCurrentYear(); // returns years elapsed from year 0
			
			if(YearsElapsed==0) {
				AverageGDPRate = GDPRate; // GDPRates are seeded from historical data. 
			}
			else {
				AverageGDPRate = (AverageGDPRate + GDPRate) / YearsElapsed;
			}
		}
	
	
	@Override
	public void yearlyFunction() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Increase carbon absorption as long as you have land area and money
	 */
	private void investMax() {
		double carbonAbsorptionChange = 1000;
		
		try {
			double investmentRequired = carbonAbsorptionHandler.getInvestmentRequired(carbonAbsorptionChange);
			double areaRequired = carbonAbsorptionHandler.getForestAreaRequired(carbonAbsorptionChange);
			double reverseCarbonAbsorption = carbonAbsorptionHandler.getCarbonAbsorptionChange(investmentRequired);
			
			System.out.println("****************************************");
			
			System.out.println("* " + this.getName() + ": Carbon Absorption before: " + carbonAbsorption);
			System.out.println("* " + this.getName() + ": Money before: " + availableToSpend);
			System.out.println("* " + this.getName() + ": Arable land before: " + arableLandArea);
			
			System.out.println("* " + this.getName() + ": I want to invest in " + carbonAbsorptionChange + " carbon absorption");
			System.out.println("* " + this.getName() + ": It will cost me: " + investmentRequired);
			System.out.println("* " + this.getName() + ": It will reduce my land by: " + areaRequired);
			
			System.out.println("* " + this.getName() + ": For that investment, I should get at least " + reverseCarbonAbsorption + " carbon absorption");

			carbonAbsorptionHandler.investInCarbonAbsorption(carbonAbsorptionChange);
			
			System.out.println("* " + this.getName() + ": Success!");
			
			System.out.println("* " + this.getName() + ": Carbon Absorption after: " + carbonAbsorption);
			System.out.println("* " + this.getName() + ": Money after: " + availableToSpend);
			System.out.println("* " + this.getName() + ": Arable land after: " + arableLandArea);
			
			System.out.println("****************************************");

			//investmentAmount = (long) (availableToSpend * 0.1);
			//carbonAbsorptionHandler.invest(investmentAmount);
		}
				
		
//These are the functions that will help with determining the rejoining of Canada 
			
	/******************************************************
	@Override
	//Check Carbon Output data and decide a feasible target to set 
	//as we are intially part of kyoto 
	 * @return
	 *********************************************************/

		FossilPrices price = new FossilPrices();
		
	
		/*Function to check oil prices to ramp or reduce production  */
		boolean check_oil_prices(long year){
			if(((price.getOilPrice(year)) > 100)){
				System.out.print("We can reduce production");
				return true;
			}
			else return false;
			}
		
		
	/*Emmisions target to be cheked against to gauge performance  */			
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
	
	boolean JoiningCriteriaMet()
	{
		if(((this.getCarbonOutput() - this.getCarbonOffset()) >this.getEmissionsTarget()) && (Aver)){
			return true;
		}
		//Only useful after 2011 when canada leaves
		else leaveKyoto();
		
		//If GDP growth achieved in the past 4 years
		//
	}
	
	private void energy_increase_with_care(double money_invest) throws IllegalArgumentException, Exception
	{
		double carbon_difference; 
		double available_area;
		available_area = getArableLandArea();
		
		
		if (getCarbonOutput() + energyUsageHandler.calculateCarbonIndustryGrowth(money_invest) <= environment_friendly_target)
		{ //invest but also check if we meet our environment friendly target.
			try{
				energyUsageHandler.investInCarbonIndustry(money_invest);
				logger.info("Invest in carbon industry successful");
				logger.info("Country meets its environment friendy target");
				green_lands = true;
			} 
			catch (Exception e) {
				logger.warn("Invest in carbon industry not successful");
			}
		}	
		
		else
		{
			logger.info("Country exceeded its environment friendly goal, invest in carbon industry but also invest in carbon absorption");
			green_lands = false;
			try{
				energyUsageHandler.investInCarbonIndustry(money_invest);
			} 
			catch (Exception e){
				logger.warn("Invest in carbon industry not successful");
			}
			
			try{ //also since country exceeds its own carbon target, invests in carbon absorption or carbon reduction in order to get carbon offset.
				carbon_difference = (getCarbonOutput() + energyUsageHandler.calculateCarbonIndustryGrowth(money_invest)) - environment_friendly_target;
				
				if (carbonAbsorptionHandler.getInvestmentRequired(carbon_difference) < getAvailableToSpend() )
					{
					carbonAbsorptionHandler.investInCarbonAbsorption(carbonAbsorptionHandler.getInvestmentRequired(carbon_difference));
					logger.info("Country invests in carbon absorption to increase carbon absorption and thus reach environment target carbon output");
					}
				
				else if ((carbonAbsorptionHandler.getInvestmentRequired(carbon_difference) < getAvailableToSpend() ) && (carbonAbsorptionHandler.getForestAreaRequired(carbon_difference) >= available_area))
				
					{
					logger.info("Country reach limit of available pre-set land, not possible to invest in carbon absorption, try invest in carbon reduction");
					if (carbonReductionHandler.getInvestmentRequired(carbon_difference) < getAvailableToSpend())
						{
						carbonReductionHandler.investInCarbonReduction(carbon_difference);
						logger.info("Country has enough cash to invest in carbon reduction, invests!");
						}
					}
				else 
					{
					logger.info("Country has insufficient funds to reach environment friendly target");
					green_care = false;
					}
					
			}
			catch (Exception e){
				logger.warn("Problem with investing in carbon absorption: " + e);
			}
		}
		
		change_emission_target(environment_friendly_target,green_lands);
		
		
	}
	
	/*Implement all the behaviours of canada */
	public void behaviour(){
		//If not in part of kyoto check the conditions which make it feasible to 
		// join kyoto
		
    	//else if part of kyoto check for different conditons
		if
		
		
		
		
	}
	
	

	// Add a function to check the gdp rte against emmsision
	//Add a function for ratio of GDP against the emmision
	
	
	
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
