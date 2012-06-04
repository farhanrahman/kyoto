package uk.ac.ic.kyoto.annex1sustain;

import java.util.UUID;
import java.util.List;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

public class AbstractPostCommunistCountry extends AbstractCountry {
	
	protected double 		internalPrice;
	protected List<Double> 	uncommittedTransactionsCosts;
	protected List<Double> 	committedTransactionsCosts;
	protected long 			ticksToEndOfRound;
	protected long 			creditsToSell;
	protected long 			creditsToSellTarget;
	protected double		lastYearPercentageSold;
	
	public AbstractPostCommunistCountry(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			float availiableToSpend, long emissionsTarget, long carbonOffset, long energyOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, availiableToSpend, emissionsTarget,
				carbonOffset, energyOutput);
		// TODO Auto-generated constructor stub
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
		double marketPrice = getMarketPrice();
		
		internalPrice   = 	Constants.MARKET_PRICE_COEFFICIENT * marketPrice +
							Constants.TIME_COEFFICIENT * ticksToEndOfRound + 
							Constants.PREVIOUS_OFFER_COEFFICIENT * lastYearPercentageSold;
	}

	protected double getMarketPrice() {
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
			// TODO log the exception
		}
		
		return (maximumCommittedPrice + minimumUncommittedPrice) / 2;
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
	
	protected void calculateLastYearPercentageSold() {
		lastYearPercentageSold = (creditsToSellTarget - creditsToSell) / creditsToSellTarget;
	}
	
	protected double getAvailableCreditsFactor() {
		// TODO implement
		//   Which variable of AbstractCountry represents available credits?
	}
	
	protected double getFossilFuelsFactor() {
		// TODO implement
		//   Will red from csv file to get fossil fuel price gradient
	}
	
	protected double getMarketFactor() {
		switch (Market.EconomyState) {
			case GROWTH:
				return Constants.MARKET_STATE_COEFFICIENT;
			case STABLE:
				return 1;
			case RECESSION:
				return -(Constants.MARKET_STATE_COEFFICIENT);
		}
	}
	
	protected void yearlyFunction() {
		
		try {
			// Calculate the percentage of credits sold last year
			calculateLastYearPercentageSold();
			
			// Calculate the new target
			long newTarget =	getAvailableCreditsFactor() *
								getFossilFuelsFactor() *
								getMarketFactor();
			
			// Adjust the new target if out of possible range
			if (newTarget > availableCredits) {
				newTarget = availableCredits;
			}
			else if (newTarget < 0) {
				newTarget = 0;
			}
			
			// Set the new target
			creditsToSellTarget = newTarget;
		}
		catch (Exception e) {
			// TODO log exception
		}
	}
}
