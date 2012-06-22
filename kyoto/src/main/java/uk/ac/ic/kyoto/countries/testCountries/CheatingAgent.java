package uk.ac.ic.kyoto.countries.testCountries;

import java.util.UUID;

import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReport;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;

public class CheatingAgent extends AbstractCountry {

	public CheatingAgent(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate,
				energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void behaviour() {
		logger.info("Hehe, you'll NEVER catch me");
	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void yearlyFunction() {
		logger.info("NEW YEAR'S!");

	}

	@Override
	protected void sessionFunction() {
		logger.info("NEW SESSION!");

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
	
	@Override
	protected double getReportedCarbonOutput() {
		return getCarbonOutput()/2;
	}

}
