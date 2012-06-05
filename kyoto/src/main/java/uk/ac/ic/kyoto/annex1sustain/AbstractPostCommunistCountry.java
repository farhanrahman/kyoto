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
	private Logger		logger;
	protected long 			currentYear;
	protected long 			availableCredits; // corresponds to carbon offset
	
	//================================================================================
    // Constructors
    //================================================================================
	
	public AbstractPostCommunistCountry(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			float availiableToSpend, long emissionsTarget, long carbonOffset, long energyOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, emissionsTarget,
				carbonOffset, energyOutput, energyOutput, energyOutput);
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
    // Public methods to update data
    //================================================================================
	
	/**
	 * Updates the internal data that is supposed to change every tick
	 *  
	 * @param e
	 * The event that is called every simulation tick
	 */
	@EventListener
	public void updateTickData(EndOfTimeCycle e) {
		updateCounter();
		updateUncommittedTransactions();
		updateCommittedTransactions();
		updateInternalPrice();
		logger.info("Internal Data of Post Communist Country " + this.getName() + " was updated");
	}
	
	/**
	 * Called at the beginning of each year.
	 */
	public void updateYearlyData() {
		calculateLastYearFactor();
		calculateNewSellingTarget();
		logger.info("Internal Yearly Data of Post Communist Country " + this.getName() + " was updated");
	}
	
	//================================================================================
    // Private functions called every tick
    //================================================================================
	
	private void updateInternalPrice() {
		internalPrice   = 	calculateMarketPrice() * 
							calculateEndOfRoundFactor() * 
							lastYearFactor;
	}

	private double calculateMarketPrice() {
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
	
	private double calculateEndOfRoundFactor() {
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
	
	private void updateUncommittedTransactions() {
		// TODO implement
	}
	
	private void updateCommittedTransactions() {
		// TODO implement
	}
	
	// temporary function
	private void updateCounter() {
		ticksToEndOfRound--;
	}
	
	//================================================================================
    // Private functions called every year
    //================================================================================
	
	/**
	 * Gets the number of credits available to sell.
	 * Multiplies it by a constant factor and returns it.
	 */
	private double calculateAvailableCreditsFactor() {
		double availableCreditsFactor;
		
		try {
			availableCreditsFactor = carbonOffset * Constants.SELL_AMOUNT_COEFFICIENT;
		}
		catch (Exception e) {
			logger.warn("Problem when calculating availableCreditsFactor " + e);
			availableCreditsFactor = carbonOffset;
		}
		return availableCreditsFactor;
	}
	
	/**
	 * Gets oil and gas prices from Market data.
	 * Calculates a gradient of change, and returns an appropriate factor.
	 */
	private double calculateFossilFuelsFactor() {
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
	private double calculateMarketFactor() {
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
	protected void calculateNewSellingTarget() {
		long newSellingTarget;
		
		try {
			// Calculate new target based on three factors
			newSellingTarget =	(long) 
						( calculateAvailableCreditsFactor() *
						  calculateFossilFuelsFactor() *
						  calculateMarketFactor() );
			
			// Adjust the new target if out of possible range
			if (newSellingTarget > availableCredits) {
				newSellingTarget = availableCredits;
			}
		}
		catch (Exception e) {
			logger.warn("Problem when calculating newTarget " + e);
			newSellingTarget = creditsToSellTarget;
		}
		creditsToSellTarget = newSellingTarget;
	}
	
	/**
	 * Calculates the percentage of credits successfully sold in previous year.
	 * Returns the factor based on that percentage, which is used to set the price we sell at.
	 */
	private void calculateLastYearFactor() {
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
	

}
