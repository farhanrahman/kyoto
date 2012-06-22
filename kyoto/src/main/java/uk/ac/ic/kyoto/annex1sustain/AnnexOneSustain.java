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
import uk.ac.ic.kyoto.trade.TradeType;
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
	
	protected String	name;						// Name of the country
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
		
		this.name = this.getName();
		this.surplusCarbonTarget = 0;
		this.surplusCarbon = 0;
		this.expectedSales = Constants.EXPECTED_SALES_INITIAL;
		this.carbonToReduce = 0;
		this.carbonToAbsorb = 0;
		this.tradeSemaphore = new Semaphore(1);
		setKyotoMemberLevel(KyotoMember.ANNEXONE);
	}
	
	@Override
	protected void initialiseCountry() {
		this.surplusCarbonPrice = carbonReductionHandler.getInvestmentRequired(1);
	}
	
	
	//================================================================================
    // Behaviour and Trading
    //================================================================================
	
	@Override
	protected void behaviour() {
		if (isKyotoMember() == KyotoMember.ANNEXONE) {
			// Decrease price if semaphore set to 1 (no conversation or offer accepted at the start). Broadcast own offer.
			if ((tradeSemaphore.availablePermits() == 1) && (Math.round(surplusCarbon) > 0)) {
				surplusCarbonPrice /= Constants.PRICE_FAILURE_SCALER;
				System.out.println(name + ": Decreased internal price to " + surplusCarbonPrice);
				broadcastSellOffer(surplusCarbon, surplusCarbonPrice);
				System.out.println(name + ": Broadcasting offer of sale: " + surplusCarbon + " @ " + surplusCarbonPrice);
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
					
					// If we have enough surplus carbon to cover the transaction
					if (quantityOffered < surplusCarbon) {
						double surplusSaleMargin = calculateSurplusSaleMargin(quantityOffered, priceOffered);
						if (surplusSaleMargin > 0) {
							if (tradeSemaphore.tryAcquire()) {
								this.tradeProtocol.respondToOffer(address, quantityOffered, offer);
								System.out.println(name + ": Responded to offer of " + quantityOffered + " @ " + priceOffered);
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
									System.out.println(name + ": Responded to offer of " + quantityOffered + " @ " + priceOffered);
								}
							}
						}
						
						// Else if absorption is possible
						else if (absorptionPossible) {
							if ((absorptionMargin + surplusSaleMargin) > 0) {
								if (tradeSemaphore.tryAcquire()) {
									this.tradeProtocol.respondToOffer(address, quantityOffered, offer);
									carbonToAbsorb = additionalCarbon;
									System.out.println(name + ": Responded to offer of " + quantityOffered + " @ " + priceOffered);
								}
							}
						}
					}
					
					// Boost internal price if offer is higher
					if (priceOffered > surplusCarbonPrice) {
						surplusCarbonPrice = priceOffered;
						System.out.println(name + ": Boosted internal price to " + surplusCarbonPrice);
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("! " + name + ": Problem with processing offer message: " + e.getMessage());
		}
	}		
	
	@Override
	protected boolean acceptTrade(NetworkAddress address, Offer offer) {
		if (surplusCarbon >= offer.getQuantity()) {
			if (tradeSemaphore.tryAcquire()) {
				System.out.println(name + ": Accepting response from address " + address);
				return true;
			}
			else {
				System.out.println(name + ": Rejecting response from address " + address + ", already doing other trade");
				return false;
			}
		}
		else {
			System.out.println(name + ": Rejecting response from address " + address + ", old offer");
			return false;
		}
	}
	
	@Override
	protected void tradeWasSuccessful(NetworkAddress from, OfferMessage offerMessage) {
		try {
			if (Math.round(carbonToReduce) > 0) {
				carbonReductionHandler.investInCarbonReduction(carbonToReduce);
				System.out.println(name + ": Trade successful, reducing carbon by " + carbonToReduce);
			}
			else if (Math.round(carbonToAbsorb) > 0) {
				carbonAbsorptionHandler.investInCarbonAbsorption(carbonToAbsorb);
				System.out.println(name + ": Trade successful, absorbing carbon by " + carbonToReduce);
			}
			else {
				System.out.println(name + ": Trade successful");
			}
			carbonToReduce = 0;
			carbonToAbsorb = 0;
			updateSurplusCarbon();
			if (Math.round(surplusCarbon) > 0) {
				surplusCarbonPrice *= Constants.PRICE_SUCCESS_SCALER;
				System.out.println(name + ": Scaled internal price to " + surplusCarbonPrice);
			}
			tradeSemaphore.release();
		}
		catch (Exception e) {
			System.out.println("! " + name + ": Problem with investments after successful trade: " + e.getMessage());
		}
	}
	
	@Override
	protected void tradeHasFailed(NetworkAddress from, OfferMessage offerMessage) {
		carbonToReduce = 0;
		carbonToAbsorb = 0;
		System.out.println(name + ": Trade has failed");
		tradeSemaphore.release();
	}
	
	@Override
	protected void tradeWasRejected(NetworkAddress from, OfferMessage offerMessage) {
		carbonToReduce = 0;
		carbonToAbsorb = 0;
		System.out.println(name + ": Trade has been rejected");
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
		
//		System.out.println("+++ Tick " + timeService.getCurrentTick());
//		System.out.println("+++ Year " + timeService.getCurrentYear());
//		System.out.println("+++ Ticks until end: " + getTicksUntilEnd());
	}
	
	@Override
	public void sessionFunction() {
		try {
			double emissionIncrease = this.getEmissionsTarget() - (this.getCarbonOutput() - this.getCarbonAbsorption());
			
			if (timeService.getCurrentTick() != 0) {
				if (Math.round(emissionIncrease) < 0) {
					leaveKyoto();
					System.out.println("Leaving Kyoto, my target is below my emissions");
				}
				else {
					System.out.println("Staying in Kyoto, my target is above my emissions");
				}
			}
		}
		catch (Exception e) {
			System.out.println("! " + name + ": Problem with session function: " + e.getMessage());
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
				System.out.println(name + ": Invested in industry and scaled back with reduction of " + carbonGained + " carbon, which cost " + totalInvestment);
			}
			else {
				carbonReductionHandler.investInCarbonReduction(carbonGained);
				System.out.println(name + ": Invested in industry and scaled back with absorption of " + carbonGained + " carbon, which cost " + totalInvestment);
			}
			
			System.out.println("*** Moneyafter investment: " + this.getAvailableToSpend());
			System.out.println("*** Carbon output after investment: " + this.getCarbonOutput());
			System.out.println("*** ArableLandArea after investment: " + this.getArableLandArea());
			System.out.println("*** Absorption after investment: " + this.getCarbonAbsorption());
		}
		catch (Exception e) {
			System.out.println("! " + name + ": Problem with investing in industry: " + e.getMessage());
		}
	}
	
	protected void finalInvestments() {
		try {
			// If credits are left, increase energy output to session goal or as much as credits/money allow
			if (surplusCarbon > 0) {
				double investmentCost = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(surplusCarbon);
				double availableFunds = this.getAvailableToSpend();
						
				if (investmentCost < availableFunds) {
					energyUsageHandler.investInCarbonIndustry(investmentCost);
					System.out.println(name + ": invested " + investmentCost + " in industry as an end-year investment, output increased by " + 
							energyUsageHandler.calculateCarbonIndustryGrowth(investmentCost));
				}
				else {
					energyUsageHandler.investInCarbonIndustry(availableFunds);
					System.out.println(name + ": invested " + availableFunds + " in industry as an end-year investment for lack of more funds, output increased by " + 
							energyUsageHandler.calculateCarbonIndustryGrowth(availableFunds));
				}
			}
		}
		catch (Exception e) {
			System.out.println("! " + name + ": Problem with end of year investments: " + e.getMessage());
		}
	}
	
	protected void updateExpectedSales() {
		try {
			if (Math.round(surplusCarbonTarget) == 0) {
				System.out.println(name + ": There was no surplus carbon to sell, nothing to do");
			}
			else if (Math.round(surplusCarbon) == 0) {
				expectedSales = 1;
				System.out.println(name + ": Setting expected sales to 100%");
			}
			else {
				double percentageSold = (surplusCarbonTarget - surplusCarbon) / surplusCarbonTarget;
				expectedSales = percentageSold;
				System.out.println(name + ": Setting expected sales to " + (percentageSold * 100) + "%");
			}
		}
		catch (Exception e) {
			System.out.println("! " + name + ": Problem with updating expected sales: " + e.getMessage());
		}
	}
	
	protected void resetYearlyTargets() {
		updateSurplusCarbon();
		surplusCarbonTarget = surplusCarbon;
		
//		System.out.println("*** emissionsTarget: " + this.getEmissionsTarget());
//		System.out.println("*** carbonOutput: " + this.getCarbonOutput());
//		System.out.println("*** carbonAbsorption: " + this.getCarbonAbsorption());
//		System.out.println("*** carbonOffset: " + this.getCarbonOffset());
//		System.out.println("*** surplusCarbonTarget: " + surplusCarbonTarget);
	}
	
	
	//================================================================================
    // Trade decisions
    //================================================================================
	
	protected void updateSurplusCarbon() {
		try{
			surplusCarbon = this.getEmissionsTarget() - (this.getCarbonOutput() - this.getCarbonAbsorption()) + this.getCarbonOffset();
			System.out.println(name + ": Surplus carbon updated to " + surplusCarbon);
		}
		catch (Exception e) {
			System.out.println("! " + name + ": Problem with calculating surplus carbon: " + e.getMessage());
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
			System.out.println("! " + name + ": Problem with calculating offset gain :" + e.getMessage());
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
			System.out.println("! " + name + ": Problem with assessing possibility of carbon reduction: " + e.getMessage());
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
			System.out.println("! " + name + ": Problem with assessing possibility of carbon absorption: " + e.getMessage());
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
			System.out.println("! " + name + ": Problem with assessing margin of carbon reduction: " + e.getMessage());
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
			System.out.println("! " + name + ": Problem with assessing margin of carbon absorption: " + e.getMessage());
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
			System.out.println("! " + name + ": Problem with assessing profitability of sale: " + e.getMessage());
			surplusSaleMargin = -10000000; // To make sure not profitable
		}
		
		return surplusSaleMargin;
	}
	
	
	//================================================================================
    // Time functions
    //================================================================================
	
//	protected int getTicksUntilEnd() {
//		int ticksUntilEnd;
//		
//		try {
//			ticksUntilEnd = timeService.getTicksInYear() - (timeService.getCurrentTick() % timeService.getTicksInYear());
//		}
//		catch (Exception e) {
//			System.out.println("! " + name + ": Problem with calculating time: " + e.getMessage());
//			ticksUntilEnd = 0;
//		}
//		
//		return ticksUntilEnd;
//	}
	
}