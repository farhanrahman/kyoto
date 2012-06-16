package uk.ac.ic.kyoto.countries.testCountries;

import java.util.UUID;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.messaging.Input;

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
		try {
			logger.debug("Available Cash: " + getAvailableToSpend());
			logger.debug("CO2 Reduction Max: "+ carbonReductionHandler.getCarbonOutputChange(getAvailableToSpend()));
			double reduction = carbonReductionHandler.getCarbonOutputChange(getAvailableToSpend()-100);
			logger.debug("Attempting reduction of: "+reduction+" Tonnes");
			carbonReductionHandler.investInCarbonReduction(reduction);
		} catch (Exception e) {
			e.printStackTrace();
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


}
