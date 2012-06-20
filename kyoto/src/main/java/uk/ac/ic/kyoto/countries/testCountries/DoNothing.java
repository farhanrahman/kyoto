package uk.ac.ic.kyoto.countries.testCountries;

import java.util.UUID;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;

/**
 * 
 * The purpose of this country is to just do nothing.
 * 
 * @author cmd08
 *
 */
public class DoNothing extends AbstractCountry {

	public DoNothing(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP, double GDPRate, 
			double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processInput(Input input) {
		/*
		 * Do nothing: single agent system.
		 */

	}

	@Override
	protected void yearlyFunction() {
		/*
		 * Do nothing: basic test.
		 */

	}

	@Override
	protected void sessionFunction() {
		/*
		 * Do nothing: basic test.
		 */
	}

	@Override
	protected void initialiseCountry() {
		/*
		 * Do nothing: basic test.
		 */
		this.setKyotoMemberLevel(KyotoMember.ROGUE);
	}

	@Override
	protected void behaviour() {
		/*
		 * Do nothing: basic test.
		 */
		
		int time = SimTime.get().intValue();
		
		if (this.persist != null) {
			this.persist.getState(time).setProperty("GDP", new Double(this.getGDP()).toString());
			this.persist.getState(time).setProperty("Energy Output", new Double(this.getEnergyOutput()).toString());
			this.persist.getState(time).setProperty("CO2 Output", new Double(this.getCarbonOutput()).toString());
		}
	}

	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		// TODO Auto-generated method stub
		return false;
	}

}
