package uk.ac.ic.kyoto.annex1sustain;

import java.util.UUID;
import java.util.List;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Market;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

import org.apache.log4j.Logger;

public class AbstractPostCommunistCountry extends AbstractCountry {
	
	protected Logger		logger;
	protected double 		internalPrice;
	protected List<Double> 	uncommittedTransactionsCosts;
	protected List<Double> 	committedTransactionsCosts;
	protected long 			ticksToEndOfRound;
	protected long 			creditsToSell;
	protected long 			creditsToSellTarget;
	protected double		lastYearFactor;
	
	public AbstractPostCommunistCountry(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			long emissionsTarget, long carbonOffset, long energyOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, emissionsTarget,
				carbonOffset, energyOutput);
		// TODO Initialize the fields
		
		// Initialize logger. Should be done in AbstractCountry
		logger = Logger.getLogger(AbstractPostCommunistCountry.class);
	}
	
	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub
	}

	
	// Functions called once per tick
	
	@EventListener
	public void updateInternalData(EndOfTimeCycle e)
	{
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
		}
		catch (Exception e) {
			logger.warn("Problem calculating marketPrice: " + e);
		}
		
		return (maximumCommittedPrice + minimumUncommittedPrice) / 2;
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
	
	
	// Functions called once per year
	
	protected double calculateAvailableCreditsFactor() {
		double availableCreditsFactor;
		
		try {
			// TODO implement
			//   Which variable of AbstractCountry represents available credits?
		}
		catch (Exception e) {
			logger.warn("Problem when calculating availableCreditsFactor " + e);
			availableCreditsFactor = 1; // This "default" value will actually need to be set to all available credits
		}
		return availableCreditsFactor;
	}
	
	protected double calculateFossilFuelsFactor() {
		double fossilFuelsFactor;
		Map<long,double> oilPriceMap = new HashMap<long,double>();
		Map<long,double> gasPriceMap = new HashMap<long,double>();
		String line;
		String[] entries;
		long year;
		long currentYear;
		double oilPrice;
		double gasPrice;
		
		try {
			File file = new File("FossilFuelPrices.csv"); // path?
			BufferedReader reader  = new BufferedReader(new FileReader(file));
			
			// Read the values into two maps
			line = reader.readLine(); // to drop the title line - not really elegant
			while((line = reader.readLine()) != null) {
				 entries = data.split(",");
				 year = Long.parseLong(entries[0]);
				 oilPrice = Double.parseDouble(entries[1]);
				 gasPrice = Double.parseDouble(entries[2]);
				 oilPriceMap.put(year, oilPrice);
				 gasPriceMap.put(year, gasPrice);
			}
			reader.close();
			
			// Calculate which year are we in
			//   TODO currentYear = ...
			//     How many ticks are in one year?
			//     Should probably be a separate function
			
			// Make sure current and previous year are in the map
			if (oilPriceMap.containsKey(currentYear) && oilPriceMap.containsKey(currentYear - 1)) {
				double newOilPrice = oilPriceMap.get(currentYear);
				double oldOilPrice = oilPriceMap.get(currentYear - 1);
				double newGasPrice = gasPriceMap.get(currentYear);
				double oldGasPrice = gasPriceMap.get(currentYear - 1);
				double oilGradient = (newOilPrice - oldOilPrice) / oldOilPrice;
				double gasGradient = (newGasPrice - oldGasPrice) / oldGasPrice;
				
				fossilFuelsFactor = FOSSIL_FUEL_PRICE_COEFFICIENT * (oilGradient + gasGradient) / 2;
			}
			else {
				fossilFuelsFactor = 1;
			}
		}
		catch (Exception e) {
			logger.warn("Problem when calculating fossilFuelsFactor " + e);
			fossilFuelsFactor = 1;
		}
		return fossilFuelsFactor;
	}
	
	protected double calculateMarketFactor() {
		double marketFactor;
		
		try {
			Market.EconomyState economyState = Market.getEconomyState();
			
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
	
	protected void calculateNewTarget() {
		long newTarget;
		
		try {
			// Calculate new target based on three factors
			newTarget =	(long) 
						( calculateAvailableCreditsFactor() *
						  calculateFuelsFactor() *
						  calculateMarketFactor() );
			
			// Adjust the new target if out of possible range
			if (newTarget > availableCredits) {
				newTarget = availableCredits;
			}
			else if (newTarget < 0) {
				newTarget = 0;
			}
		}
		catch (Exception e) {
			logger.warn("Problem when calculating newTarget " + e);
			newTarget = creditsToSellTarget;
		}
		creditsToSellTarget = newTarget;
	}
	
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
			logger.warn("Division by 0 error " + e);
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
	
	protected void yearlyFunction() {
		// Calculate the lastYearFactor for the current year
		calculateLastYearFactor();
		
		// Calculate the new target
		calculateNewTarget();
	}
}
