package uk.ac.ic.kyoto.annex1sustain;

import java.util.LinkedList;
import java.util.UUID;
import java.util.List;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.countries.NotEnoughCashException;
import uk.ac.ic.kyoto.countries.NotEnoughLandException;
import uk.ac.ic.kyoto.market.FossilPrices;
import uk.ac.ic.kyoto.services.Economy;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

/**
 * 
 * @author Adam, Piotr
 */
public class AnnexOneSustain extends AbstractCountry {
	
	
	//================================================================================
    // Private Fields
    //================================================================================
	
	protected double 		internalPrice;					// The price of a single carbon credit that we estimate we will be able to successfully sell at
	protected List<Double> 	uncommittedTransactionsCosts;	// List of transactions and their prices that were advertised but not completed
	protected List<Double> 	committedTransactionsCosts;		// List of transactions and their prices that were completed
	protected double		creditsToSellTarget;			// Total amount of credits we aim to sell in current year
	protected double		creditsToSell;					// Credits left for sale from the current sell target
	protected double		absorptionInvestmentTarget;		// The amount (in carbon) of a single carbon absorption investment considered this tick  
	protected double		reductionInvestmentTarget;		// The amount (in carbon) of a single carbon reduction investment considered this tick 
	protected double		lastYearFactor;					// Coefficient reflecting the percentage of credit sales target that was met, adjusted by a constant
	
	
	//================================================================================
    // Constructors
    //================================================================================
	
	public AnnexOneSustain(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			long energyOutput, long carbonOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		
		this.internalPrice = Double.MAX_VALUE;
		this.uncommittedTransactionsCosts = new LinkedList<Double>();
		this.committedTransactionsCosts = new LinkedList<Double>();
		this.creditsToSell = 0;
		this.creditsToSellTarget = 0;
		this.absorptionInvestmentTarget = Constants.INVESTMENT_MIN;
		this.reductionInvestmentTarget = Constants.INVESTMENT_MIN;
		this.lastYearFactor = 1;	
	}
	
	
	//================================================================================
    // Input function
    //================================================================================
	
	/**
	 * Function processing input (what is this?)
	 */
	@Override
	protected void processInput(Input input) {
		// TODO process input (if any)
	}
	
	/**
	 * Initialisation function
	 */
	@Override
	protected void initialiseCountry() {
		// TODO initialisation
	}
	
	/**
	 * Behaviour function
	 */
	@Override
	protected void behaviour() {
		// TODO behaviour
	}
	
	//================================================================================
    // Periodic functions
    //================================================================================
	
	/**
	 * Function called at the end of each tick.
	 * - Updates the internal variables.
	 * - Decides on and initiates investments
	 * 
	 * @param e
	 * The event that is called every simulation tick
	 */
	@EventListener
	public void TickFunction(EndOfTimeCycle e) {
		updateUncommittedTransactions();
		updateCommittedTransactions();
		updateInternalPrice();
		logger.info("Internal Data of Post-Communist Country " + this.getName() + " was updated");
		makeInvestments();
	}
	
	/**
	 * Function called at the end of each year.
	 * - Updates the internal variables.
	 */
	@Override
	public void yearlyFunction() {
		calculateLastYearFactor();
		calculateNewSellingTarget();
		logger.info("Internal Yearly Data of Post-Communist Country " + this.getName() + " was updated");
	}
	
	/**
	 * Function called at the end of each session.
	 */
	@Override
	public void sessionFunction() {
		// TODO implement
	}
	
	
	//================================================================================
    // Tick update function
    //================================================================================
	
	/**
	 * Calculates a new internal price, which is a multiplication of three factors:
	 * - average market price of credits
	 * - time until the end of session
	 * - meeting the sales target from previous year
	 */
	private void updateInternalPrice() {
		internalPrice   = 	calculateMarketPrice() * 
							calculateEndOfRoundFactor() * 
							lastYearFactor;
	}
	
	/**
	 * Calculates market price basing on the internal log of successful and unsuccessful transactions.
	 */
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
			logger.warn("Problem with calculating marketPrice: " + e);
			marketPrice = 0;
		}
		
		return marketPrice;
	}
	
	/**
	 * Calculates the factor representing how far we are in the session.
	 * The less ticks till the end, the cheaper we sell.
	 */
	private double calculateEndOfRoundFactor() {
		double endOfRoundFactor;
		int ticksInYear;
		int currentTick;
		int tresholdTick;
		
		try {
			// Create time service and get the tick variables
			ParticipantTimeService timeService = getEnvironmentService(ParticipantTimeService.class);
			
			ticksInYear = timeService.getTicksInYear();
			currentTick = timeService.getCurrentTick();
			tresholdTick = (int) (ticksInYear * Constants.END_OF_ROUND_YEAR_PART);
			
			// If in the final part of the year, adjust the constant factor
			if (currentTick > tresholdTick) {
				endOfRoundFactor = 1 + ((Constants.END_OF_ROUND_FACTOR_MAX - 1) * 
										(currentTick - tresholdTick) /
										(ticksInYear - tresholdTick));
			}
			// Else, set factor to default 1
			else {
				endOfRoundFactor = 1;
			}
		}
		catch (Exception e) {
			logger.warn("Problem with calculating endOfRoundFactor: " + e);
			endOfRoundFactor = 1;
		}
		
		return endOfRoundFactor;
	}
	
	/**
	 * Stores unsuccessful transactions from last X ticks in a list.
	 */
	private void updateUncommittedTransactions() {
		// TODO implement
	}
	
	/**
	 * Stores successful transactions from last X ticks in a list.
	 */
	private void updateCommittedTransactions() {
		// TODO implement
	}
	
	
	//================================================================================
    // Investment functions
    //================================================================================
	
	/**
	 * Increases the new carbon absorption investment target. Adjusts it if out of limits.
	 */
	private void increaseAbsorptionInvestmentTarget() {
		absorptionInvestmentTarget = absorptionInvestmentTarget * Constants.INVESTMENT_SCALING;
		if (absorptionInvestmentTarget > Constants.INVESTMENT_MAX) {
			absorptionInvestmentTarget = Constants.INVESTMENT_MAX;
		}
	}
	
	/**
	 * Decreases the new carbon absorption investment target. Adjusts it if out of limits.
	 */
	private void decreaseAbsorptionInvestmentTarget() {
		absorptionInvestmentTarget = absorptionInvestmentTarget / Constants.INVESTMENT_SCALING;
		if (absorptionInvestmentTarget < Constants.INVESTMENT_MIN) {
			absorptionInvestmentTarget = Constants.INVESTMENT_MIN;
		}
	}
	
	/**
	 * Increases the new carbon reduction investment target. Adjusts it if out of limits.
	 */
	private void increaseReductionInvestmentTarget() {
		reductionInvestmentTarget = reductionInvestmentTarget * Constants.INVESTMENT_SCALING;
		if (reductionInvestmentTarget > Constants.INVESTMENT_MAX) {
			reductionInvestmentTarget = Constants.INVESTMENT_MAX;
		}
	}
	
	/**
	 * Decreases the new carbon reduction investment target. Adjusts it if out of limits.
	 */
	private void decreaseReductionInvestmentTarget() {
		reductionInvestmentTarget = reductionInvestmentTarget / Constants.INVESTMENT_SCALING;
		if (reductionInvestmentTarget < Constants.INVESTMENT_MIN) {
			reductionInvestmentTarget = Constants.INVESTMENT_MIN;
		}
	}
	
	/**
	 * Calculates if potential profit made by selling acquired credits outweighs the cost of investment in carbon absorption.
	 * If so, tries to invest. Increases the next investment target on success, decreases on failure.
	 */
	private void carbonAbsorptionInvestment () {
		double investmentCost;
		double potentialProfit;
		
		try {
			investmentCost = carbonAbsorptionHandler.getInvestmentRequired(absorptionInvestmentTarget);
			potentialProfit = absorptionInvestmentTarget * internalPrice;
			
			if (potentialProfit > investmentCost) {
				carbonAbsorptionHandler.investInCarbonAbsorption(investmentCost);
				increaseAbsorptionInvestmentTarget();
				logger.info("Post-Communist Country " + this.getName() + " invested " + String.valueOf(investmentCost) + " in carbon absorption");
			}
			else {
				decreaseAbsorptionInvestmentTarget();
				logger.info("Post-Communist Country " + this.getName() + " deemed carbon absorption not profitable");
			}
		}
		catch (NotEnoughCashException e) {
			decreaseAbsorptionInvestmentTarget();
			logger.info("Post-Communist Country " + this.getName() + " has insufficient funds for carbon absorption");
		}
		catch (NotEnoughLandException e) {
			decreaseAbsorptionInvestmentTarget();
			logger.info("Post-Communist Country " + this.getName() + " has insufficient land for carbon absorption");
		}
		catch (Exception e) {
			logger.warn("Problem with investing in carbon absorption: " + e);
		}
	}
	
	/**
	 * Calculates if potential profit made by selling acquired credits outweighs the cost of investment in carbon reduction.
	 * If so, tries to invest. Increases the next investment target on success, decreases on failure.
	 */
	private void carbonReductionInvestment () {
		double investmentCost;
		double potentialProfit;
		
		try {
			investmentCost = carbonReductionHandler.getInvestmentRequired(reductionInvestmentTarget);
			potentialProfit = reductionInvestmentTarget * internalPrice;
			
			if (potentialProfit > investmentCost) {
				carbonReductionHandler.investInCarbonReduction(investmentCost);
				increaseReductionInvestmentTarget();
				logger.info("Post-Communist Country " + this.getName() + " invested " + String.valueOf(investmentCost) + " in carbon reduction");
			}
			else {
				decreaseReductionInvestmentTarget();
				logger.info("Post-Communist Country " + this.getName() + " deemed carbon reduction not profitable");
			}
		}
		catch (NotEnoughCashException e) {
			decreaseReductionInvestmentTarget();
			logger.info("Post-Communist Country " + this.getName() + " has insufficient funds for carbon reduction");
		}
		catch (NotEnoughCarbonOutputException e) {
			decreaseReductionInvestmentTarget();
			logger.info("Post-Communist Country " + this.getName() + " has insufficient carbon output for carbon reduction");
		}
		catch (Exception e) {
			logger.warn("Problem with investing in carbon reduction: " + e);
		}
	}
	
	/**
	 * Calculates if potential profit made by selling acquired credits outweighs the cost of investment in other countries.
	 * If so, tries to invest. Increases the next investment target on success, decreases on failure.
	 */
	private void otherCountriesInvestment () {
		// TODO implement
		//   There are no handlers for investing in other countries yet
	}
	
	/**
	 * Calls all the investment functions.
	 */
	private void makeInvestments() {
		carbonAbsorptionInvestment();
		carbonReductionInvestment();
		otherCountriesInvestment();
	}
	
	
	//================================================================================
    // Yearly update functions
    //================================================================================
	
	/**
	 * Returns a new target, which is a multiplication of three factors:
	 * - available credits
	 * - fossil fuels historical prices
	 * - current state of the market
	 * All adjusted with a constant coefficient.
	 */
	protected void calculateNewSellingTarget() {
		double newSellingTarget;
		
		try {
			// Calculate new target based on three factors
			newSellingTarget =	calculateAvailableCreditsFactor() *
								calculateFossilFuelsFactor() *
								calculateMarketFactor();
			
			// Adjust the new target if out of possible range
			if (newSellingTarget > getCarbonOffset()) {
				newSellingTarget = Math.round(getCarbonOffset());
			}
		}
		catch (Exception e) {
			logger.warn("Problem with calculating newTarget: " + e);
			newSellingTarget = creditsToSellTarget;
		}
		
		creditsToSellTarget = newSellingTarget;
	}
	
	/**
	 * Gets the number of credits available to sell.
	 * Multiplies it by a constant factor and returns it.
	 */
	private double calculateAvailableCreditsFactor() {
		double availableCreditsFactor;
		
		try {
			availableCreditsFactor = getCarbonOffset() * Constants.SELL_AMOUNT_COEFFICIENT;
		}
		catch (Exception e) {
			logger.warn("Problem with calculating availableCreditsFactor: " + e);
			availableCreditsFactor = getCarbonOffset();
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
			
			// Get current year from the Time service
			ParticipantTimeService timeService = getEnvironmentService(ParticipantTimeService.class);
			int currentYear = timeService.getCurrentYear();
			
			// Get the data from the FossilPrices Service
			FossilPrices fossilPrices = getEnvironmentService(FossilPrices.class);
			double newOilPrice = fossilPrices.getOilPrice(currentYear);
			double oldOilPrice = fossilPrices.getOilPrice(currentYear - 1);
			double newGasPrice = fossilPrices.getGasPrice(currentYear);
			double oldGasPrice = fossilPrices.getGasPrice(currentYear - 1);
			
			// If values for this and previous year exist, calculate gradient and factor
			if ((newOilPrice >= 0) && (oldOilPrice >= 0) && (newGasPrice >= 0) && (oldGasPrice >= 0) ) {
				double oilGradient = (newOilPrice - oldOilPrice) / oldOilPrice;
				double gasGradient = (newGasPrice - oldGasPrice) / oldGasPrice;
				fossilFuelsFactor = Constants.FOSSIL_FUEL_PRICE_COEFFICIENT * (oilGradient + gasGradient) / 2;
			}
			
			// If no data exists, return default 1 value.
			else
				fossilFuelsFactor = 1;
		}
		catch (UnavailableServiceException e) {
			logger.warn("Unable to reach the fossil fuel service: " + e);
			fossilFuelsFactor = 1;
		}
		catch (Exception e) {
			logger.warn("Problem with calculating fossilFuelsFactor: " + e);
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
			Economy economy = getEnvironmentService(Economy.class);
			
			switch (economy.getEconomyState()) {
				case GROWTH:
					marketFactor = Constants.MARKET_STATE_COEFFICIENT;
					break;
				case RECESSION:
					marketFactor =  2 - Constants.MARKET_STATE_COEFFICIENT;
					break;
				default:
					marketFactor = 1;
					break;
			}
		}
		catch (Exception e) {
			logger.warn("Problem with calculating marketFactor: " + e);
			marketFactor = 1;
		}
		
		return marketFactor;
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
			// Calculate the lastYearFactor
			if (lastYearPercentageSold >= Constants.LAST_YEAR_FACTOR_OFFSET) {
				lastYearFactor = 1 + ((Constants.LAST_YEAR_FACTOR_MAX - 1) *
									  (lastYearPercentageSold - Constants.LAST_YEAR_FACTOR_OFFSET) /
									  (1 - Constants.LAST_YEAR_FACTOR_OFFSET));
			}
			else {
				lastYearFactor = 1 - ((1 - Constants.LAST_YEAR_FACTOR_MAX) *
									  (Constants.LAST_YEAR_FACTOR_OFFSET - lastYearPercentageSold) /
									  (Constants.LAST_YEAR_FACTOR_OFFSET));
			}
		}
		catch (ArithmeticException e) {
			logger.warn("LAST_YEAR_FACTOR_OFFSET must be > 0 and < 1");
			lastYearFactor = 0;
		}
		catch (Exception e) {
			logger.warn("Problem with calculating lastYearFactor: " + e);
			lastYearFactor = 1;
		}
	}
}
