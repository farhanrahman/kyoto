package uk.ac.ic.kyoto.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;

/**
 * Monitoring service
 * @author ov109, Stuart
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
				long realCarbonOutput = a.getMonitored();
				Serializable state = sharedState.get(CarbonReportingService.name, a.getID());
				Map<Integer, Double> reports = (Map<Integer, Double>)state;
				if (realCarbonOutput != reports.get(SimTime.get().intValue())) {
					sanction(a);
				}
			}
		}
	}
	
	private void sanction(AbstractCountry sanctionee) {
		// TODO implementation of sanctions for cheating
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
