package uk.ac.ic.kyoto.annex1sustain;

import java.util.UUID;
import java.util.List;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.market.Economy;
import uk.ac.ic.kyoto.market.FossilPrices;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

import org.apache.log4j.Logger;

public class AbstractPostCommunistCountry extends AbstractCountry {
	
	//================================================================================
    // PrivateFields
    //================================================================================
	
	protected double 		internalPrice;
	protected List<Double> 	uncommittedTransactionsCosts;
	protected List<Double> 	committedTransactionsCosts;
	protected long 			ticksToEndOfRound;
	protected long 			creditsToSell;
	protected long 			creditsToSellTarget;
	protected double		lastYearFactor;
	
	// temporary variables
	protected Logger		logger;
	protected long 			currentYear;
	protected long 			availableCredits;
	
	//================================================================================
    // Constructors
    //================================================================================
	
	public AbstractPostCommunistCountry(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			long emissionsTarget, long carbonOffset, long energyOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, emissionsTarget,
				carbonOffset, energyOutput, energyOutput);
		// TODO Initialize the fields
		
		// Initialize logger. Should be done in AbstractCountry
		logger = Logger.getLogger(AbstractPostCommunistCountry.class);
	}
	
	//================================================================================
    // Overridden functions
    //================================================================================
	
	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub
	}

	
	//================================================================================
    // Methods called once per tick
    //================================================================================
	
	@EventListener
	public void updateInternalData(EndOfTimeCycle e) {
		updateCounter();
		addUncommittedTransaction();
		addCommittedTransaction();
		updateInternalPrice();
	}
	
	protected void updateInternalPrice() {
		internalPrice   = 	calculateMarketPrice() * 
							calculateEndOfRoundFactor() * 
							lastYearFactor;
	}

	protected double calculateMarketPrice() {
		double marketPrice;
		double maximumCommittedPrice = 0;
		double minimumUncommittedPrice = Double.MAX_VALUE;
		
		try {
			// Find maximum price of the committed transactions
			for (double price : committedTransactionsCosts) {
				if (price > maximumCommittedPrice)
					maximumCommittedPrice = price;
			}
			
			// Find minimum price of the uncommitted transactions
			for (double price : uncommittedTransactionsCosts) {
				if (price < minimumUncommittedPrice)
					minimumUncommittedPrice = price;
			}
			marketPrice = (maximumCommittedPrice + minimumUncommittedPrice) / 2;
		}
		catch (Exception e) {
			logger.warn("Problem calculating marketPrice: " + e);
			marketPrice = 0;
		}
		
		return marketPrice;
	}
	
	protected double calculateEndOfRoundFactor() {
		double endOfRoundFactor = 1;
		try {
			if(ticksToEndOfRound < Constants.END_OF_ROUND_MINIMUM_NUMBER_OF_TICKS)
				endOfRoundFactor = 	Constants.END_OF_ROUND_FACTOR_SLOPE *
									(
										Constants.NUMBER_OF_TICKS_IN_ROUND
										- Constants.END_OF_ROUND_MINIMUM_NUMBER_OF_TICKS
										- ticksToEndOfRound
									);
		}
		catch (Exception e) {
			logger.warn("Problem calculating endOfRoundFactor: " + e);
			endOfRoundFactor = 1;
		}
		return endOfRoundFactor;
	}
	
	protected void addUncommittedTransaction() {
		// TODO implement
	}
	
	protected void addCommittedTransaction() {
		// TODO implement
	}
	
	protected void updateCounter() {
		ticksToEndOfRound--;
	}
	
	//================================================================================
    // Methods called once per year
    //================================================================================
	
	/**
	 * Gets the number of credits available to sell.
	 * Multiplies it by a constant factor and returns it.
	 */
	protected double calculateAvailableCreditsFactor() {
		double availableCreditsFactor;
		
		try {
			// TODO implement
			//   Which variable of AbstractCountry represents available credits?
			availableCreditsFactor = 1;
		}
		catch (Exception e) {
			logger.warn("Problem when calculating availableCreditsFactor " + e);
			availableCreditsFactor = 1; // This "default" value will actually need to be set to all available credits
		}
		return availableCreditsFactor;
	}
	
	/**
	 * Reads oil and gas prices from file FossilFuelPrices.
	 * Calculates a gradient of change, and returns an appropriate factor.
	 */
	protected double calculateFossilFuelsFactor() {
		double fossilFuelsFactor;
		
		try {
			double newOilPrice = FossilPrices.getOilPrice(currentYear);
			double oldOilPrice = FossilPrices.getOilPrice(currentYear - 1);
			double newGasPrice = FossilPrices.getGasPrice(currentYear);
			double oldGasPrice = FossilPrices.getGasPrice(currentYear - 1);
			double oilGradient = (newOilPrice - oldOilPrice) / oldOilPrice;
			double gasGradient = (newGasPrice - oldGasPrice) / oldGasPrice;
			
			fossilFuelsFactor = Constants.FOSSIL_FUEL_PRICE_COEFFICIENT * (oilGradient + gasGradient) / 2;
		}
		catch (Exception e) {
			logger.warn("Problem when calculating fossilFuelsFactor " + e);
			fossilFuelsFactor = 1;
		}
		return fossilFuelsFactor;
	}
	
	/**
	 * Returns a factor based on the current state of economy.
	 */
	protected double calculateMarketFactor() {
		double marketFactor;
		
		try {
			Economy.State economyState = Economy.getEconomyState();
			
			switch (economyState) {
				case GROWTH:
					marketFactor = 1 + Constants.MARKET_STATE_COEFFICIENT;
					break;
				case RECESSION:
					marketFactor =  1 - Constants.MARKET_STATE_COEFFICIENT;
					break;
				default:
					marketFactor = 1;
					break;
			}
		}
		catch (Exception e) {
			logger.warn("Problem when calculating marketFactor " + e);
			marketFactor = 1;
		}
		return marketFactor;
	}
	
	/**
	 * Returns a new target, which is a multiplication of three factors:
	 * - available credits
	 * - fossil fuels historical prices
	 * - current state of the market
	 * All adjusted with a constant coefficient.
	 */
	protected void calculateNewTarget() {
		long newTarget;
		
		try {
			// Calculate new target based on three factors
			newTarget =	(long) 
						( calculateAvailableCreditsFactor() *
						  calculateFossilFuelsFactor() *
						  calculateMarketFactor() );
			
			// Adjust the new target if out of possible range
			if (newTarget > availableCredits) {
				newTarget = availableCredits;
			}
			/*else if (newTarget < 0) {
				// Isn't this a bug? Should probably send a warning
				newTarget = 0;
			}*/
		}
		catch (Exception e) {
			logger.warn("Problem when calculating newTarget " + e);
			newTarget = creditsToSellTarget;
		}
		creditsToSellTarget = newTarget;
	}
	
	/**
	 * Calculates the percentage of credits successfully sold in previous year.
	 * Returns the factor based on that percentage, which is used to set the price we sell at.
	 */
	protected void calculateLastYearFactor() {
		double lastYearPercentageSold;
		
		try {
			// Calculate the percentage of successfully sold credits in the last year
			lastYearPercentageSold = (creditsToSellTarget - creditsToSell) / creditsToSellTarget;
			
			// Adjust if out of boundaries
			if (lastYearPercentageSold > 100) {
				logger.warn("The calculated percentage of carbon emission sold exceeded 100%");
				lastYearPercentageSold = 100;
			}
			if (lastYearPercentageSold < 0 ) {
				logger.warn("The calculated percentage of carbon emission sold was lower than 0%");
				lastYearPercentageSold = 0;
			}
		}
		catch (ArithmeticException e) {
			logger.warn("Division by 0 error: " + e);
			lastYearPercentageSold = 0;
		}
		
		try {
			// Calculate the factor
			lastYearFactor = 1 + Constants.LAST_YEAR_FACTOR_SLOPE * (lastYearPercentageSold - Constants.LAST_YEAR_FACTOR_OFFSET);
		}
		catch (Exception e) {
			logger.warn("Problem when calculating lastYearFactor " + e);
			lastYearFactor = 1;
		}
	}
	
	/**
	 * Called at the beginning of each year.
	 */
	protected void yearlyFunction() {
		// Calculate the lastYearFactor for the current year
		calculateLastYearFactor();
		
		// Calculate the new target
		calculateNewTarget();
	}
}
