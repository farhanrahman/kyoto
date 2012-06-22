package uk.ac.ic.kyoto.annex1sustain;

import java.util.UUID;
import java.util.concurrent.Semaphore;

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
	protected double	surplusCarbon;				// Number of credits we still have left
	protected double	surplusCarbonPrice;			// Price of surplus carbon while within target
	protected double	expectedSales;				// Specifies where our acceptable price will lie between two extreme profitability points
	protected double	carbonToReduce;				// Carbon we have to reduce if sale was successful
	protected double 	carbonToAbsorb;				// Carbon we have to absorb if sale was successful
	protected Semaphore tradeSemaphore;				// Semaphore making sure only one trade happens at any given time
	
	
	//================================================================================
    // Constructor / Initialisation
    //================================================================================
	
	public AnnexOneSustain(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		
		this.surplusCarbonTarget = 0;
		this.surplusCarbon = 0;
		this.surplusCarbonPrice = Constants.EXPECTED_SALES_INITIAL;
		this.expectedSales = Constants.EXPECTED_SALES_INITIAL;
		this.carbonToReduce = 0;
		this.carbonToAbsorb = 0;
		this.tradeSemaphore = new Semaphore(1);
	}
	
	@Override
	protected void initialiseCountry() {
		setKyotoMemberLevel(KyotoMember.ANNEXONE);
	}
	
	
	//================================================================================
    // Behaviour and Trading
    //================================================================================
	
	@Override
	protected void behaviour() {
		int ticksUntilEnd = getTicksUntilEnd();
		
		if (isKyotoMember() == KyotoMember.ANNEXONE) {
			
			// Normal behaviour during all but last 10 ticks of the year
			if (ticksUntilEnd > 10) {
				// Decrease price if semaphore set to 1 (no conversation or offer accepted at the start)
				if (tradeSemaphore.availablePermits() == 1) {
					surplusCarbonPrice /= Constants.PRICE_FAILURE_SCALER;
					broadcastSellOffer(surplusCarbon, surplusCarbonPrice);
				}
			}
			
			// Final industry investment function
			else if (ticksUntilEnd == 7) {
				finalInvestments();
			}
		}
		else {
			// TODO: What to do when not in Kyoto?
		}
	}
	
	@Override
	protected void processInput(Input input) {
		try {
			if (this.tradeProtocol.canHandle(input)) {
				this.tradeProtocol.handle(input);
			}
			else {
				OfferMessage offer = this.tradeProtocol.decodeInput(input);
				NetworkAddress address = this.tradeProtocol.extractNetworkAddress(input);
				
				double quantityOffered = offer.getOfferQuantity();
				double priceOffered = offer.getOfferUnitCost();
				
				// If we have enough surplus carbon to cover the transaction
				if (quantityOffered < surplusCarbon) {
					double surplusSaleMargin = calculateSurplusSaleMargin(quantityOffered, priceOffered);
					if (surplusSaleMargin > 0) {
						if (tradeSemaphore.tryAcquire()) {
							this.tradeProtocol.respondToOffer(address, quantityOffered, offer);
						}
					}
				}
				// Else we need to invest to get required carbon
				else {
					double additionalCarbon = quantityOffered - surplusCarbon;
					double additionalFunds = priceOffered * quantityOffered;
					double surplusSaleMargin = calculateSurplusSaleMargin(surplusCarbon, priceOffered);
					double reductionMargin = calculateReductionMargin(additionalCarbon, priceOffered);
					double absorptionMargin = calculateAbsorptionMargin(additionalCarbon, priceOffered);
					boolean reductionPossible = isReductionPossible(additionalCarbon, additionalFunds);
					boolean absorptionPossible = isAbsorptionPossible(additionalCarbon, additionalFunds);
					
					// If reduction is possible and (more profitable than absorption or absorption impossible)
					if ((reductionMargin > absorptionMargin || !absorptionPossible) && reductionPossible) {
						if ((reductionMargin + surplusSaleMargin) > 0) {
							if (tradeSemaphore.tryAcquire()) {
								this.tradeProtocol.respondToOffer(address, quantityOffered, offer);
								carbonToReduce = additionalCarbon;
							}
						}
					}
					// Else if absorption is possible
					else if (absorptionPossible) {
						if ((absorptionMargin + surplusSaleMargin) > 0) {
							if (tradeSemaphore.tryAcquire()) {
								this.tradeProtocol.respondToOffer(address, quantityOffered, offer);
								carbonToAbsorb = additionalCarbon;
							}
						}
					}
				}
				
				// Boost internal price if offer is higher
				if (priceOffered > surplusCarbonPrice) {
					surplusCarbonPrice = priceOffered;
				}
			}
		}
		catch (IllegalArgumentException e) {
			logger.warn(e);
		}
		catch (FSMException e) {
			logger.warn(e);
		}
	}		
	
	@Override
	protected boolean acceptTrade(NetworkAddress address, Offer offer) {
		if (tradeSemaphore.tryAcquire()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	protected void tradeWasSuccessful(NetworkAddress from, OfferMessage offerMessage) {
		try {
			if (Math.round(carbonToReduce) > 0) {
				carbonReductionHandler.investInCarbonReduction(carbonToReduce);
			}
			else if (Math.round(carbonToAbsorb) > 0) {
				carbonAbsorptionHandler.investInCarbonAbsorption(carbonToAbsorb);
			}
			carbonToReduce = 0;
			carbonToAbsorb = 0;
			if (Math.round(surplusCarbon) > 0) {
				surplusCarbonPrice *= Constants.PRICE_SUCCESS_SCALER;;
			}
			tradeSemaphore.release();
		}
		catch (Exception e) {
			logger.warn(this.getName() + ": Problem with investments after successful trade: " + e.getMessage());
		}
	}

	protected void tradeHasFailed(NetworkAddress from, OfferMessage offerMessage) {
		carbonToReduce = 0;
		carbonToAbsorb = 0;
		tradeSemaphore.release();
	}

	protected void tradeWasRejected(NetworkAddress from, OfferMessage offerMessage) {
		carbonToReduce = 0;
		carbonToAbsorb = 0;
		tradeSemaphore.release();
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
		try {
			double emissionIncrease = this.getEmissionsTarget() - (this.getCarbonOutput() - this.getCarbonAbsorption());
			
			if (Math.round(emissionIncrease) < 0) {
				leaveKyoto();
				logger.info("Leaving Kyoto, my target is below my emissions");
			}
		}
		catch (Exception e) {
			logger.warn("Problem with session function: " + e.getMessage());
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
			if (surplusCarbon > 0) {
				double investmentCost = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(surplusCarbon);
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
		try {
			if (Math.round(surplusCarbonTarget) == 0) {
				logger.info("AnnexOneSustain country " + this.getName() + ": There was no surplus carbon to sell, nothing to do");
			}
			else if (Math.round(surplusCarbon) == 0) {
				expectedSales = 1;
				logger.info("AnnexOneSustain country " + this.getName() + ": Setting expected sales to 100%");
			}
			else {
				double percentageSold = (surplusCarbonTarget - surplusCarbon) / surplusCarbonTarget;
				expectedSales = percentageSold;
				logger.info("AnnexOneSustain country " + this.getName() + ": Setting expected sales to " + String.valueOf(percentageSold * 100) + "%");
			}
		}
		catch (Exception e) {
			logger.warn("Problem with updating expected sales: " + e.getMessage());
		}
	}
	
	protected void resetYearlyTargets() {
		updateSurplusCarbon();
		surplusCarbonTarget = surplusCarbon;
		
		System.out.println("*** emissionsTarget: " + this.getEmissionsTarget());
		System.out.println("*** carbonOutput: " + this.getCarbonOutput());
		System.out.println("*** carbonAbsorption: " + this.getCarbonAbsorption());
		System.out.println("*** carbonOffset: " + this.getCarbonOffset());
		System.out.println("*** surplusCarbonTarget: " + surplusCarbonTarget);
	}
	
	
	//================================================================================
    // Trade decisions
    //================================================================================
	
	protected void updateSurplusCarbon() {
		try{
			surplusCarbon = this.getEmissionsTarget() - (this.getCarbonOutput() - this.getCarbonAbsorption()) + this.getCarbonOffset();
		}
		catch (Exception e) {
			logger.warn("Problem with calculating surplus carbon: " + e.getMessage());
			surplusCarbon = 0;
		}
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
	
	protected boolean isReductionPossible(double reduction, double money) {
		boolean possible;
		
		try {
			double fundsLeft = this.getAvailableToSpend() + money;
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
	
	protected boolean isAbsorptionPossible(double absorption, double money) {
		boolean possible;
		
		try {
			double fundsLeft = this.getAvailableToSpend() + money;
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
	
	protected double calculateReductionMargin(double reduction, double price) {
		double reductionMargin;
		
		try {
			double cost = carbonReductionHandler.getInvestmentRequired(reduction);
			double profit = calculateSessionOffsetGain(reduction) * price * expectedSales;
			
			reductionMargin = profit - cost;
		}
		catch (Exception e) {
			logger.warn("Problem with assessing margin of carbon reduction: " + e.getMessage());
			reductionMargin = -10000000; // To make sure not profitable
		}
		
		return reductionMargin;
	}
	
	protected double calculateAbsorptionMargin(double absorption, double price) {
		double absorptionMargin;
		
		try {
			double cost = carbonAbsorptionHandler.getInvestmentRequired(absorption);
			double profit = calculateSessionOffsetGain(absorption) * price * expectedSales;
			
			absorptionMargin = profit - cost;
		}
		catch (Exception e) {
			logger.warn("Problem with assessing margin of carbon absorption: " + e.getMessage());
			absorptionMargin = -10000000; // To make sure not profitable
		}
		
		return absorptionMargin;
	}
	
	protected double calculateSurplusSaleMargin(double quantity, double price) {
		double surplusSaleMargin;
		
		try {
			surplusSaleMargin = quantity * (surplusCarbonPrice - price);
		}
		catch (Exception e) {
			logger.warn("Problem with assessing profitability of sale: " + e.getMessage());
			surplusSaleMargin = -10000000; // To make sure not profitable
		}
		
		return surplusSaleMargin;
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