package uk.ac.ic.kyoto.monitor;

import java.util.ArrayList;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.util.random.Random;


public class Monitor extends EnvironmentService {

	final private ArrayList<AbstractCountry> memberStates = new ArrayList<AbstractCountry>();
	private final double MONITORING_PRICE = 0; //Decide on a price for monitoring
	private double cash;
	
	public Monitor(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
/*	// Generate a randomInt and if divisible by 30 then monitor all countries.
	// TODO monitor only individual countries.
	if (Random.randomInt() % 30 == 0 && cash >= MONITORING_PRICE) {
		cash -= MONITORING_PRICE;
		for (AbstractCountry a : memberStates) {
			a.getMonitored();
		}
	}*/
	
	/**
	 * Add member states to the Monitor. Allows operation of sanctions, 
	 * credits, etc.
	 * @param state 
	 */
	public void addMemberState(AbstractCountry state) {
		memberStates.add(state);
	}
	
	/**
	 * Give a pre-determined amount for monitoring
	 * @param tax
	 */
	// TODO: synchronised??
	synchronized public void taxForMonitor (double tax) {
		cash += tax;
	}

}
