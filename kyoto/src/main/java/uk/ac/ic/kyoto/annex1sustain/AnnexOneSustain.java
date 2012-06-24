package uk.ac.ic.kyoto.annex1sustain;

import java.util.UUID;
import java.util.concurrent.Semaphore;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.AbstractCountry.KyotoMember;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.exceptions.CannotLeaveKyotoException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;
import uk.ac.ic.kyoto.services.Economy;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAddress;


/**
 * @author Piotr
 */
public class AnnexOneSustain extends AbstractCountry {
	
	
	//================================================================================
    // Protected Fields
    //================================================================================
	
	protected String	name;						// Name of the country
	protected double	surplusCarbonTarget;		// Number of credits we still have left at the beginning of the year
	protected double	surplusCarbon;				// Number of credits we still have left
	protected double	surplusCarbonPrice;			// Price of surplus carbon at which we are ready to sell
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
	}
	
	@Override
	protected void initialiseCountry() {
		this.name = this.getName();
		this.surplusCarbonTarget = 0;
		this.surplusCarbon = 0;
		this.surplusCarbonPrice = carbonReductionHandler.getInvestmentRequired(1);
		this.expectedSales = Constants.EXPECTED_SALES_INITIAL;
		this.carbonToReduce = 0;
		this.carbonToAbsorb = 0;
		setKyotoMemberLevel(KyotoMember.ANNEXONE);
		this.tradeSemaphore = new Semaphore(1);
	}
	
	
	//================================================================================
    // Behaviour and Trading
    //================================================================================
	
	@Override
	protected void behaviour() {
		if (isKyotoMember() == KyotoMember.ANNEXONE) {
			
			// If semaphore is not taken (not in conversations) decrease price and broadcast own offer if carbon left
			if ((tradeSemaphore.availablePermits() == 1) && (Math.round(surplusCarbon) > 0)) {
				
				surplusCarbonPrice /= Constants.PRICE_FAILURE_SCALER;
				logger.info(name + ": Decreased internal price to " + surplusCarbonPrice);
				
				broadcastSellOffer(surplusCarbon, surplusCarbonPrice);
				logger.info(name + ": Broadcasting offer of sale: " + surplusCarbon + " @ " + surplusCarbonPrice);
			}
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
				
				// Only respond to buy offers, we are not interested in anything else
				if (offer.getOfferType() == TradeType.BUY) {
					
					// Check if we have enough surplus carbon to cover the transaction
					if (quantityOffered < surplusCarbon) {
						double surplusSaleMargin = calculateSurplusSaleMargin(quantityOffered, priceOffered);
						
						// Invest if profitable
						if (surplusSaleMargin >= 0) {
							if (tradeSemaphore.tryAcquire()) {
								this.tradeProtocol.respondToOffer(address, quantityOffered, offer);
								logger.info(name + ": Responded to offer of " + quantityOffered + " @ " + priceOffered);
							}
						}
					}
					
					// If not enough surplus carbon, need to invest to get required carbon
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
							// Invest if profitable
							if ((reductionMargin + surplusSaleMargin) > 0) {
								if (tradeSemaphore.tryAcquire()) {
									this.tradeProtocol.respondToOffer(address, quantityOffered, offer);
									carbonToReduce = additionalCarbon;
									logger.info(name + ": Responded to offer of " + quantityOffered + " @ " + priceOffered);
								}
							}
						}
						
						// Else if absorption is possible
						else if (absorptionPossible) {
							// Invest if profitable
							if ((absorptionMargin + surplusSaleMargin) > 0) {
								if (tradeSemaphore.tryAcquire()) {
									this.tradeProtocol.respondToOffer(address, quantityOffered, offer);
									carbonToAbsorb = additionalCarbon;
									logger.info(name + ": Responded to offer of " + quantityOffered + " @ " + priceOffered);
								}
							}
						}
					}
					
					// Boost internal price if offer is higher
					if (priceOffered > surplusCarbonPrice) {
						surplusCarbonPrice = priceOffered;
						logger.info(name + ": Boosted internal price to " + surplusCarbonPrice);
					}
				}
			}
		}
		catch (Exception e) {
			logger.warn(name + ": Problem with processing offer message: " + e.getMessage());
		}
	}		
	
	@Override
	protected boolean acceptTrade(NetworkAddress address, Offer offer) {
		if (surplusCarbon >= offer.getQuantity()) {
			if (tradeSemaphore.tryAcquire()) {
				logger.info(name + ": Accepting response from address " + address);
				return true;
			}
			else {
				logger.info(name + ": Rejecting response from address " + address + ", already doing other trade");
				return false;
			}
		}
		else {
			logger.info(name + ": Rejecting response from address " + address + ", old offer");
			return false;
		}
	}
	
	@Override
	protected void tradeWasSuccessful(NetworkAddress from, OfferMessage offerMessage) {
		try {
			if (Math.round(carbonToReduce) > 0) {
				carbonReductionHandler.investInCarbonReduction(carbonToReduce);
				logger.info(name + ": Trade successful, reducing carbon by " + carbonToReduce);
			}
			else if (Math.round(carbonToAbsorb) > 0) {
				carbonAbsorptionHandler.investInCarbonAbsorption(carbonToAbsorb);
				logger.info(name + ": Trade successful, absorbing carbon by " + carbonToAbsorb);
			}
			else {
				logger.info(name + ": Trade successful");
			}
			carbonToReduce = 0;
			carbonToAbsorb = 0;
			updateSurplusCarbon();
			if (Math.round(surplusCarbon) > 0) {
				surplusCarbonPrice *= Constants.PRICE_SUCCESS_SCALER;
				logger.info(name + ": Scaled internal price to " + surplusCarbonPrice);
			}
			tradeSemaphore.release();
		}
		catch (Exception e) {
			logger.warn(name + ": Problem with investments after successful trade: " + e.getMessage());
		}
	}
	
	@Override
	protected void tradeHasFailed(NetworkAddress from, OfferMessage offerMessage) {
		carbonToReduce = 0;
		carbonToAbsorb = 0;
		logger.info(name + ": Trade has failed");
		tradeSemaphore.release();
	}
	
	@Override
	protected void tradeWasRejected(NetworkAddress from, OfferMessage offerMessage) {
		carbonToReduce = 0;
		carbonToAbsorb = 0;
		logger.info(name + ": Trade has been rejected");
		tradeSemaphore.release();
	}
	
	
	//================================================================================
    // Periodic functions
    //================================================================================
	
	@Override
	public void sessionFunction() {
	}
	
	@Override
	public void yearlyFunction() {
		initialInvestments();
		updateExpectedSales();
		resetYearlyTargets();
		decideOnKyoto();
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
			totalInvestment = this.getAvailableToSpend() * Constants.INDUSTRY_GROWTH_MONEY_PERCENTAGE;
			industryInvestment = totalInvestment / 2;
			investmentDiff = industryInvestment;
			
			for (int i = 0 ; i < 30 ; i++) {
				investmentDiff /= 2;
				carbonGained = energyUsageHandler.calculateCarbonIndustryGrowth(industryInvestment);
				carbonRedCost = carbonReductionHandler.getInvestmentRequired(carbonGained, (this.getCarbonOutput() + carbonGained), (this.getEnergyOutput() + carbonGained));
				carbonAbsTrees = carbonAbsorptionHandler.getForestAreaRequired(carbonGained);
				if (carbonAbsTrees < this.getArableLandArea()) {
					carbonAbsCost = carbonAbsorptionHandler.getInvestmentRequired(carbonGained);
				}
				else {
					carbonAbsCost = Double.MAX_VALUE;
				}
				
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
				logger.info(name + ": Invested in industry and scaled back with reduction of " + carbonGained + " carbon, which cost " + totalInvestment);
			}
			else {
				carbonReductionHandler.investInCarbonReduction(carbonGained);
				logger.info(name + ": Invested in industry and scaled back with absorption of " + carbonGained + " carbon, which cost " + totalInvestment);
			}
		}
		catch (Exception e) {
			logger.warn(name + ": Problem with investing in industry: " + e.getMessage());
		}
	}
	
	protected void updateExpectedSales() {
		try {
			if (Math.round(surplusCarbonTarget) == 0) {
				logger.info(name + ": There was no surplus carbon to sell, nothing to do");
			}
			else if (Math.round(surplusCarbon) == 0) {
				expectedSales = 1;
				logger.info(name + ": Setting expected sales to 100%");
			}
			else {
				double percentageSold = (surplusCarbonTarget - surplusCarbon) / surplusCarbonTarget;
				expectedSales = percentageSold;
				logger.info(name + ": Setting expected sales to " + (percentageSold * 100) + "%");
			}
		}
		catch (Exception e) {
			logger.warn(name + ": Problem with updating expected sales: " + e.getMessage());
		}
	}
	
	protected void updateSurplusCarbon() {
		try{
			surplusCarbon = this.getEmissionsTarget() - (this.getCarbonOutput() - this.getCarbonAbsorption()) + this.getCarbonOffset();
			logger.info(name + ": Surplus carbon updated to " + surplusCarbon);
		}
		catch (Exception e) {
			logger.warn(name + ": Problem with calculating surplus carbon: " + e.getMessage());
			surplusCarbon = 0;
		}
	}
	
	protected void resetYearlyTargets() {
		updateSurplusCarbon();
		surplusCarbonTarget = surplusCarbon;
	}
	
	protected void decideOnKyoto() {
		try {
			if (isKyotoMember() == KyotoMember.ANNEXONE) {
				if (Math.round(surplusCarbon) < 0) {
					
					// If an honest agent, try to reduce emissions or leave Kyoto if unable to
					if (Constants.CHEATER == false) {
						double necessaryReduction = (-surplusCarbon);
						double reductionCost = carbonReductionHandler.getInvestmentRequired(necessaryReduction);
						boolean reductionPossible = isReductionPossible(necessaryReduction, 0);
						boolean absorptionPossible = isAbsorptionPossible(necessaryReduction, 0);
						double absorptionCost;
						
						try {
							absorptionCost = carbonAbsorptionHandler.getInvestmentRequired(necessaryReduction);
						}
						catch (NotEnoughLandException e) {
							absorptionCost = Double.MAX_VALUE;
						}
						
						if ((reductionCost < absorptionCost || !absorptionPossible) && reductionPossible) {
							carbonReductionHandler.investInCarbonReduction(necessaryReduction);
							logger.info(name + ": Reduced carbon by " + (necessaryReduction) + " to remain in Kyoto");
						}
						else if (absorptionPossible) {
							carbonAbsorptionHandler.investInCarbonAbsorption(necessaryReduction);
							logger.info(name + ": Absorbed carbon by " + (necessaryReduction) + " to remain in Kyoto");
						}
						else {
							leaveKyoto();
							logger.info(name + ": Leaving Kyoto, my target is below my emissions and can't do anything about it");
						}
					}
					// If a cheater, leave Kyoto if have been caught too many times
					else {
						int timesCaughtCheating = this.getTimesCaughtCheating();
						logger.info(name + "I have been caught cheating " + timesCaughtCheating + " times");
						if (timesCaughtCheating > Constants.ALLOWED_TIMES_CAUGHT) {
							leaveKyoto();
							logger.info(name + ": Leaving Kyoto, I have been caught cheating too many times");
						}
					}
				}
				else {
					logger.info(name + ": Staying in Kyoto, my target is above my emissions");
				}
			}
		}
		catch (CannotLeaveKyotoException e) {
			logger.info(name + ": Want to leave Kyoto, but cannot");
		}
		catch (Exception e) {
			logger.warn(name + ": Problem with deciding whether to stay in Kyoto");
		}
	}
	
	@Override
	protected double getReportedCarbonOutput() {
		double realCarbonOutput = this.getCarbonOutput();
		double requiredCarbonOutput = this.getEmissionsTarget() - this.getCarbonAbsorption() - this.getCarbonOffset();
		// If not a cheater or within target, report true value
		if (Constants.CHEATER == false || realCarbonOutput < requiredCarbonOutput) {
			return realCarbonOutput;
		}
		// Else, return value that matches the target exactly
		else {
			logger.info(name + ": I am cheating, reporting " + requiredCarbonOutput + " instead of the real value of " + realCarbonOutput);
			return requiredCarbonOutput;
		}
	}
	
	
	//================================================================================
    // Trade decisions
    //================================================================================
	
	protected double calculateSessionOffsetGain(double carbonDifference) {
		double sessionOffsetGain;
		
		try {
			int yearsInSession = timeService.getYearsInSession();
			int yearNumber = timeService.getCurrentYear() % yearsInSession;
			int yearsUntilEnd = yearsInSession - yearNumber - 1;
			sessionOffsetGain = carbonDifference * yearsUntilEnd;
		}
		catch (Exception e) {
			logger.warn(name + ": Problem with calculating offset gain :" + e.getMessage());
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
			logger.warn(name + ": Problem with assessing possibility of carbon reduction: " + e.getMessage());
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
			logger.warn(name + ": Problem with assessing possibility of carbon absorption: " + e.getMessage());
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
			logger.warn(name + ": Problem with assessing margin of carbon reduction: " + e.getMessage());
			reductionMargin = -Double.MAX_VALUE; // To make sure not profitable
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
			logger.warn(name + ": Problem with assessing margin of carbon absorption: " + e.getMessage());
			absorptionMargin = -Double.MAX_VALUE; // To make sure not profitable
		}
		
		return absorptionMargin;
	}
	
	protected double calculateSurplusSaleMargin(double quantity, double price) {
		double surplusSaleMargin;
		
		try {
			surplusSaleMargin = quantity * (price - surplusCarbonPrice);
		}
		catch (Exception e) {
			logger.warn(name + ": Problem with assessing profitability of sale: " + e.getMessage());
			surplusSaleMargin = -Double.MAX_VALUE; // To make sure not profitable
		}
		
		return surplusSaleMargin;
	}

}