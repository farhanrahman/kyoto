package uk.ac.ic.kyoto.monitor;

import java.util.ArrayList;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.util.random.Random;

/**
 * Monitoring service
 * @author ov109
 *
 */
public class Monitor extends EnvironmentService {

	private ArrayList<AbstractCountry> memberStates = new ArrayList<AbstractCountry>();
	private double cash = 0;
	
	public Monitor(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	@EventListener
	public void monitorCountries (EndOfTimeCycle e) {
		// Generate a randomInt and if divisible by 30 then monitor all countries.
		// TODO monitor only individual countries.
		if (Random.randomInt() % 30 == 0 && cash >= GameConst.MONITORING_PRICE) {
			cash -= GameConst.MONITORING_PRICE;
			for (AbstractCountry a : memberStates) {
				a.getMonitored();
			}
		}
	}
	
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
	// TODO: synchronised?? (to avoid data sharing issue)
	synchronized public void taxForMonitor (double tax) {
		cash += tax;
	}

}
