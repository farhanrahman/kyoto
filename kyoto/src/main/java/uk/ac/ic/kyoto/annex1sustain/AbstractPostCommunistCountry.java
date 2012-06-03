package uk.ac.ic.kyoto.annex1sustain;

import java.util.UUID;
import java.util.List;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Market;
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
			long emissionsTarget, long carbonOffset, long energyOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, emissionsTarget,
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
		
		internalPrice   = 	calculateMarketPrice() * 
							calculateEndOfRoundFactor() * 
							Constants.PREVIOUS_OFFER_COEFFICIENT * lastYearPercentageSold;
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
			// TODO log the exception
		}
		
		return (maximumCommittedPrice + minimumUncommittedPrice) / 2;
	}
	
	protected double calculateEndOfRoundFactor() {
		double endOfRoundFactor = 1;
		if(ticksToEndOfRound < Constants.WHEN_TIME_STARTS_TO_INFLUENCE_THE_PRICE)
			endOfRoundFactor = 	Constants.SLOPE_OF_TIME_TO_END_OF_ROUND_VS_PRICE *
								(
									Constants.NUMBER_OF_TICKS_IN_ROUND
									- Constants.WHEN_TIME_STARTS_TO_INFLUENCE_THE_PRICE
									- ticksToEndOfRound
								);
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
	
	protected void calculateLastYearPercentageSold() {
		// TODO exception handling (division by 0)
		lastYearPercentageSold = (creditsToSellTarget - creditsToSell) / creditsToSellTarget;
	}
	
	protected double getAvailableCreditsFactor() {
		// TODO implement
		//   Which variable of AbstractCountry represents available credits?
		double availableCreditsFactor = 1;
		return availableCreditsFactor;
	}
	
	protected double getFossilFuelsFactor() {
		// TODO implement
		//   Will red from csv file to get fossil fuel price gradient
		double fossilFuelsFactor = 1;
		return fossilFuelsFactor;
	}
	
	protected double calculateMarketFactor() {
		// TODO exception handling
		Market.EconomyState economyState = Market.getEconomyState();
		double marketFactor;
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
		return marketFactor;
	}
	
	protected void yearlyFunction() {
		
		try {
			// Calculate the percentage of credits sold last year
			calculateLastYearPercentageSold();
			
			// Calculate the new target
			long newTarget =	(long) 
								( getAvailableCreditsFactor() *
								  getFossilFuelsFactor() *
								  calculateMarketFactor() );
			
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
