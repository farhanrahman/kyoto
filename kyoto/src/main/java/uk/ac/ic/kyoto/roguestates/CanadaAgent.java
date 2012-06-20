package uk.ac.ic.kyoto.roguestates;

import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.ic.kyoto.trade.InvestmentType;

import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

public class CanadaAgent extends AbstractCountry {

	
	public CanadaAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double emissionsTarget, double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, energyOutput, carbonOutput);

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
		
		
	public boolean JoiningCriteriaMet()
	{
		if((this.getCarbonOutput() - this.getCarbonOffset()) >this.getEmissionsTarget()){
			return true;
		}
		//Only useful after 2011 when canada leaves
		else return false;
		
		//If GDP growth achieved in the past 4 years
		//
	}
	
	@Override
	//Check Carbon Output data and decide a feasible target to set 
			//as we are intially part of kyoto 
	public double SetEmissionsTarget(){
		return this.getEmissionsTarget();
		
	}
	
	
	@Override
	public void sessionFunction() {
		if (getCarbonOutput() - getCarbonOffset() > getEmissionsTarget()) {
			// leave Kyoto here
		}
			
	}
	
	
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

	
	
}
