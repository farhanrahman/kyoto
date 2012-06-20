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
	protected double	surplusCarbonPrice;			// Price of surplus carbon while within target
	protected double	carbonToReduce;				// Carbon reduction that we need to perform before end of year to match target
	protected double	carbonToAbsorb;				// Carbon absorption that we need to perform before end of year to match target
	protected double	expectedSales;			// Specifies where our acceptable price will lie between two extreme profitability points
	
	
	//================================================================================
    // Constructor / Initialisation
    //================================================================================
	
	public AnnexOneSustain(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			long energyOutput, long carbonOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		
		this.surplusCarbonTarget = 0;
		this.surplusCarbonSold = 0;
		this.surplusCarbonPrice = 0;
		this.carbonToReduce = 0;
		this.carbonToAbsorb = 0;
		this.expectedSales = 0;
	}
	
	@Override
	protected void initialiseCountry() {
		setKyotoMemberLevel(KyotoMember.ANNEXONE);
		resetYearlyTargets();
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
		
		if (ticksUntilEnd < 10) {
			// Normal behaviour during all but last 10 ticks of the year
		}
		else if (ticksUntilEnd == 10) {
			// End-of-year function
			finalInvestments();
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
	}
	
	@Override
	public void sessionFunction() {
		// TODO implement
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
			
			energyUsageHandler.investInCarbonIndustry(industryInvestment);
			
			if ((carbonAbsCost < carbonRedCost) && (carbonAbsTrees < this.getArableLandArea())) {
				carbonAbsorptionHandler.investInCarbonAbsorption(carbonGained);
			}
			else {
				carbonReductionHandler.investInCarbonReduction(carbonGained);
			}
		}
		catch (Exception e) {
			logger.warn("Problem with investing in industry: " + e.getMessage());
		}
	}
	
	protected void finalInvestments() {
		try {
			// Invest in planned carbon reduction and absorption, to offset credits sold
			carbonReductionHandler.investInCarbonReduction(carbonToReduce);
			carbonAbsorptionHandler.investInCarbonAbsorption(carbonToAbsorb);
			
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
		catch (ArithmeticException e) {
			logger.info("AnnexOneSustain country " + this.getName() + ": There was no surplus carbon to sell, nothing to do");
		}
		catch (Exception e) {
			logger.warn("Problem with setting: " + e.getMessage());
		}
	}
	
	protected void resetYearlyTargets() {
		surplusCarbonTarget = this.getEmissionsTarget() - (this.getCarbonOutput() - this.getCarbonAbsorption()) + this.getCarbonOffset();
		surplusCarbonSold = 0;
		carbonToReduce = 0;
		carbonToAbsorb = 0;
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