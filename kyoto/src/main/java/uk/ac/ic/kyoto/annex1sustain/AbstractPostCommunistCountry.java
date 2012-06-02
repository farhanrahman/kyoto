package uk.ac.ic.kyoto.annex1sustain;

import java.util.UUID;
import java.util.List;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.messaging.Input;

public class AbstractPostCommunistCountry extends AbstractCountry {
	
	protected double 		internalPrice;
	protected List<Double> 	uncommittedTransactionsCosts;
	protected List<Double> 	committedTransactionsCosts;
	protected long 			ticksToEndOfRound;
	protected long 			carbonToSell;
	protected long 			carbonToSellTarget;
	
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

	protected void calculateInternalPrice() {
		// TODO implement
	}
	
	protected double getMarketPrice() {
		double maximumCommittedPrice = 0;
		double minimumUncommittedPrice = Integer.MAX_VALUE;
		
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
}