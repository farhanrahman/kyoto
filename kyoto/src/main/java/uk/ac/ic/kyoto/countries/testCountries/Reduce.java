package uk.ac.ic.kyoto.countries.testCountries;

import java.util.UUID;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;

/**
 * 
 * Reduce attempts to reduce CO2 output by investing in carbon reduction technology as much as possible.
 * 
 * @author cmd08
 *
 */

public class Reduce extends AbstractCountry {

	public Reduce(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput,
				carbonOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initialiseCountry() {
	}

	@Override
	protected void behaviour() {
		/*
		 * Attempt to minimise carbon output, spend all available money on carbon absorption & reduction
		 */
		logger.debug(this.getName());

		logger.debug("Available Cash: " + getAvailableToSpend());
		logger.debug("CO2 Reduction Max: "+ carbonReductionHandler.getCarbonOutputChange(getAvailableToSpend()));
		
		double reduction = carbonReductionHandler.getCarbonOutputChange(getAvailableToSpend());
		
		logger.debug("Attempting reduction of: "+reduction+" Tonnes");
		
		try {
			carbonReductionHandler.investInCarbonReduction(reduction);
			/*These should never ever be thrown happen, we did a runtime check above */
		} catch (NotEnoughCarbonOutputException e) {
			throw new RuntimeException(e);
		} catch (NotEnoughCashException e) {
			throw new RuntimeException(e);
		}

		logger.debug("Current GDP: " + this.getGDP());
		logger.debug("Current Energy Output: " + this.getEnergyOutput());
		logger.debug("Current CO2 Output: " + this.getCarbonOutput());
	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void yearlyFunction() {
	}

	@Override
	protected void sessionFunction() {
	}

	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		// TODO Auto-generated method stub
		return false;
	}


}
