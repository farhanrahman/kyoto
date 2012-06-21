package uk.ac.ic.kyoto.annex1sustain;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.countries.AbstractCountry.KyotoMember;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;
import uk.ac.ic.kyoto.services.Economy;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;

/**
 * @author Piotr
 */
public class AnnexOneSustain extends AbstractCountry {
	
	
	//================================================================================
    // Protected Fields
    //================================================================================
	
	protected double	surplusCarbonTarget;		// Number of credits we can and want to sell without carbon absorption/reduction
	protected double	surplusCarbonSold;			// Number of credits that we sold this year
	protected double	surplusCarbonTemp;			// Number of credits that we will have sold providing that current transactions are successful
	protected double	surplusCarbonPrice;			// Price of surplus carbon while within target
	protected double	expectedSales;				// Specifies where our acceptable price will lie between two extreme profitability points
	
	
	//================================================================================
    // Constructor / Initialisation
    //================================================================================
	
	public AnnexOneSustain(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		
		this.surplusCarbonTarget = 0;
		this.surplusCarbonSold = 0;
		this.surplusCarbonTemp = 0;
		this.surplusCarbonPrice = 0;
		this.expectedSales = 0;
	}
	
	@Override
	protected void initialiseCountry() {
		setKyotoMemberLevel(KyotoMember.ANNEXONE);
		surplusCarbonPrice = carbonReductionHandler.getInvestmentRequired(surplusCarbonTarget);
		expectedSales = Constants.EXPECTED_SALES_INITIAL;
	}
	
	
	//================================================================================
    // Behaviour
    //================================================================================
	
	@Override
	protected void processInput(Input input) {
		// TODO process input
	}
	
	@Override
	protected void behaviour() {
		int ticksUntilEnd = getTicksUntilEnd();
		
		if (isKyotoMember() == KyotoMember.ANNEXONE) {
			if (ticksUntilEnd > 10) {
				// Normal behaviour during all but last 10 ticks of the year
				//	Look at offers:
				//	-> good buy offer exists:
				//		- accept, scale price up
				//	-> else:
				//		- post own offer
				//		- scale up if taken, down every turn failed
				//	-> repeat until surplus <= 0, then:
				//		- accept profitable buy offers only
			}
			else if (ticksUntilEnd == 7) {
				finalInvestments();
			}
		}
		else {
			// TODO: What to do when not in Kyoto?
		}
	}
	
	@Override
	protected boolean acceptTrade(NetworkAddress address, Offer offer) {
		// TODO accepting trade
		return false;
	}
	
	
	//================================================================================
    // Periodic functions
    //================================================================================
	
	@Override
	public void yearlyFunction() {
		initialInvestments();
		updateExpectedSales();
		resetYearlyTargets();
		System.out.println("+++ Tick " + timeService.getCurrentTick());
		System.out.println("+++ Year " + timeService.getCurrentYear());
	}
	
	@Override
	public void sessionFunction() {
		if ((surplusCarbonTarget < 0) && (isKyotoMember() == KyotoMember.ANNEXONE) && (timeService.getCurrentTick() != 0)) {
			leaveKyoto();
			logger.info("Leaving Kyoto, my target is below my output");
			//System.out.println("*** Leaving Kyoto");
		}
	}
	
	protected void initialInvestments() {
		double totalInvestment;
		double industryInvestment;
		double investmentDiff;
		double carbonGained = 0;
		double carbonRedCost = 0;
		double carbonAbsCost = 0;
		double carbonAbsTrees = 0;
		
		try {
			totalInvestment = this.getAvailableToSpend() * Constants.INDUSTRY_GROWTH_MONEY_PERCENTAGE / 2;
			industryInvestment = totalInvestment / 2;
			investmentDiff = industryInvestment;
			
			for (int i = 0 ; i < 30 ; i++) {
				investmentDiff /= 2;
				carbonGained = energyUsageHandler.calculateCarbonIndustryGrowth(industryInvestment);
				carbonRedCost = carbonReductionHandler.getInvestmentRequired(carbonGained, (this.getCarbonOutput() + carbonGained), (this.getEnergyOutput() + carbonGained));
				carbonAbsCost = carbonAbsorptionHandler.getInvestmentRequired(carbonGained);
				carbonAbsTrees = carbonAbsorptionHandler.getForestAreaRequired(carbonGained);
				
				if ((carbonRedCost + industryInvestment < totalInvestment) ||
					((carbonAbsCost + industryInvestment < totalInvestment) && (carbonAbsTrees < this.getArableLandArea())))
				{
					industryInvestment += investmentDiff;
				}
				else {
					industryInvestment -= investmentDiff;
				}
			}
			
			System.out.println("*** Money before investment: " + this.getAvailableToSpend());
			System.out.println("*** Of which for investment: " + totalInvestment);
			System.out.println("*** Carbon output before investment: " + this.getCarbonOutput());
			System.out.println("*** Which we will increase by: " + carbonGained);
			System.out.println("*** ArableLandArea before investment: " + this.getArableLandArea());
			System.out.println("*** Absorption before investment: " + this.getCarbonAbsorption());
			
			energyUsageHandler.investInCarbonIndustry(industryInvestment);
			
			System.out.println("*** INVESTED");
			
			if ((carbonAbsCost < carbonRedCost) && (carbonAbsTrees < this.getArableLandArea())) {
				carbonAbsorptionHandler.investInCarbonAbsorption(carbonGained);
				System.out.println("*** Absorption done");
			}
			else {
				carbonReductionHandler.investInCarbonReduction(carbonGained);
				System.out.println("*** Reduction done");
			}
			
			System.out.println("*** Moneyafter investment: " + this.getAvailableToSpend());
			System.out.println("*** Carbon output after investment: " + this.getCarbonOutput());
			System.out.println("*** ArableLandArea after investment: " + this.getArableLandArea());
			System.out.println("*** Absorption after investment: " + this.getCarbonAbsorption());
		}
		catch (Exception e) {
			logger.warn("Problem with investing in industry: " + e.getMessage());
		}
	}
	
	protected void finalInvestments() {
		try {
			// If credits are left, increase energy output if money available, as probably won't sell them next year either
			double unsoldCarbon = surplusCarbonTarget - surplusCarbonSold;
			if (unsoldCarbon > 0) {
				double investmentCost = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(unsoldCarbon);
				double availableFunds = this.getAvailableToSpend();
				if (investmentCost < availableFunds) {
					energyUsageHandler.investInCarbonIndustry(investmentCost);
				}
				else {
					energyUsageHandler.investInCarbonIndustry(availableFunds);
				}
			}
		}
		catch (Exception e) {
			logger.warn("Problem with end of year investments: " + e.getMessage());
		}
	}
	
	protected void updateExpectedSales() {
		double percentageSold;
		
		try {
			if (surplusCarbonTarget < 0.1) {
				logger.info("AnnexOneSustain country " + this.getName() + ": There was no surplus carbon to sell, nothing to do");
			}
			else {
				percentageSold = surplusCarbonSold / surplusCarbonTarget;
				
				if (percentageSold > 1) {
					expectedSales = 1;
					logger.info("AnnexOneSustain country " + this.getName() + ": Setting profitability treshold to 100%");
				}
				else {
					expectedSales = percentageSold;
					logger.info("AnnexOneSustain country " + this.getName() + ": Setting profitability treshold to " + String.valueOf(percentageSold * 100) + "%");
				}
			}
		}
		catch (Exception e) {
			logger.warn("Problem with setting: " + e.getMessage());
		}
	}
	
	protected void resetYearlyTargets() {
		surplusCarbonTarget = this.getEmissionsTarget() - (this.getCarbonOutput() - this.getCarbonAbsorption()) + this.getCarbonOffset();
		System.out.println("*** emissionsTarget: " + this.getEmissionsTarget());
		System.out.println("*** carbonOutput: " + this.getCarbonOutput());
		System.out.println("*** carbonAbsorption: " + this.getCarbonAbsorption());
		System.out.println("*** carbonOffset: " + this.getCarbonOffset());
		System.out.println("*** surplusCarbonTarget: " + surplusCarbonTarget);
		surplusCarbonSold = 0;
		surplusCarbonTemp = 0;
	}
	
	
	//================================================================================
    // Trade decisions
    //================================================================================
	
	protected double calculateExpectedProfit(double carbon, double price) {
		double expectedProfit;
		
		try {
			expectedProfit = calculateSessionOffsetGain(carbon) * price * expectedSales;
		}
		catch (Exception e) {
			logger.warn("Problem with calculating expected profit of invesment: " + e.getMessage());
			expectedProfit = 0;
		}
		
		return expectedProfit;
	}
	
	protected double calculateSessionOffsetGain(double carbonDifference) {
		double sessionOffsetGain;
		
		try {
			int yearsInSession = timeService.getYearsInSession();
			int yearNumber = timeService.getCurrentYear() % yearsInSession;
			int yearsUntilEnd = yearsInSession - yearNumber - 1;
			sessionOffsetGain = carbonDifference * yearsUntilEnd;
		}
		catch (Exception e) {
			logger.warn("Problem with calculating offset gain :" + e.getMessage());
			sessionOffsetGain = 0;
		}
		
		return sessionOffsetGain;
	}
	
	protected boolean isReductionPossible(double reduction) {
		boolean possible;
		
		try {
			double fundsLeft = this.getAvailableToSpend();
			double fundsRequired = carbonReductionHandler.getInvestmentRequired(reduction);
			
			if (fundsLeft < fundsRequired) {
				possible = false;
			}
			else if (reduction > this.getCarbonOutput()) {
				possible = false;
			}
			else {
				possible = true;
			}
		}
		catch (Exception e) {
			logger.warn("Problem with assessing possibility of carbon reduction: " + e.getMessage());
			possible = false;
		}
		
		return possible;
	}
	
	protected boolean isAbsorptionPossible(double absorption) {
		boolean possible;
		
		try {
			double fundsLeft = this.getAvailableToSpend();
			double fundsRequired = carbonAbsorptionHandler.getInvestmentRequired(absorption);
			double areaLeft = this.getArableLandArea();
			double areaRequired = carbonAbsorptionHandler.getForestAreaRequired(absorption);
			
			if (fundsLeft < fundsRequired) {
				possible = false;
			}
			else if (areaLeft < areaRequired) {
				possible = false;
			}
			else {
				possible = true;
			}
		}
		catch (Exception e) {
			logger.warn("Problem with assessing possibility of carbon absorption: " + e.getMessage());
			possible = false;
		}
		
		return possible;
	}
	
	
	//================================================================================
    // Time functions
    //================================================================================
	
	protected int getTicksUntilEnd() {
		int ticksUntilEnd;
		
		try {
			ticksUntilEnd = timeService.getTicksInYear() - (timeService.getCurrentTick() % timeService.getTicksInYear());
		}
		catch (Exception e) {
			logger.warn("Problem with calculating time: " + e.getMessage());
			ticksUntilEnd = 0;
		}
		
		return ticksUntilEnd;
	}
	
}