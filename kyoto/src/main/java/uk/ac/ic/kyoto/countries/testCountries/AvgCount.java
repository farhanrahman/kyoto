package uk.ac.ic.kyoto.countries.testCountries;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;


/**
 *  Behaviour for injected average country.  Used for balancing
 *  @author ct
 *
 */
public class AvgCount extends AbstractCountry {

	public AvgCount(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput,
				carbonOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void behaviour() {
		
		
		logger.debug("Current GDP: " + this.getGDP());
		logger.debug("Current Cash: " + this.getAvailableToSpend());
		logger.debug("Current GDP Rate: " + this.getGDPRate());
		logger.debug("Current Energy Output: " + this.getEnergyOutput());
		logger.debug("Current Previous Energy Output: " + this.getPrevEnergyOut());
		logger.debug("Current CO2 Output: " + this.getCarbonOutput());
		logger.debug("Emissions Target: " + this.getEmissionsTarget());

	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void yearlyFunction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void sessionFunction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initialiseCountry() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		// TODO Auto-generated method stub
		return false;
	}

}
