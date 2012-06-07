package uk.ac.ic.kyoto.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;

/**
 * Monitoring service
 * @author ov109, Stuart, sc1109
 *
 */
public class Monitor extends EnvironmentService {

	private ArrayList<AbstractCountry> memberStates = new ArrayList<AbstractCountry>();
	private double cash = 0;
	
	private Map<AbstractCountry, Integer> sinBin;
	
	//percentage increase in target i.e. 1.05 for 5%
	@Parameter(name="target_penalty")
	int target_penalty;
	
	//percentage decrease in cash i.e. 0.95 for 5%
	@Parameter(name="cash_penalty")
	int cash_penalty;
	
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
					cheatSanction(a);
				}				
			}
		}
	}
	
	//compare real output to target and sanction if not met
	public void checkTargets () {
		for (AbstractCountry a : memberStates) {
			long realCarbonOutput = a.getMonitored();
			//TODO - Like above but using jonny's CarbonTargetService?
			}
	}
	
	private void cheatSanction(AbstractCountry sanctionee) {
		
		//sanctionee added to sinBin/increase sinCount
		int sinCount=1;
		if (sinBin.containsKey(sanctionee)) {
			sinCount = sinBin.get(sanctionee) + 1;
		}
		sinBin.put(sanctionee, sinCount);
		
		//linearly increasing fine - first time free, 5% more for each time after
		sanctionee.setAvailableToSpend(sanctionee.getID(), (long) (sanctionee.getAvailableToSpend()-sanctionee.getGDP()*(sinCount-1)*cash_penalty));
	}
	
	//sanction for not meeting targets
	private void targetSanction(AbstractCountry sanctionee) {
		
		//5% higher target regardless of number of sins (compound)
		// TODO Should this apply straight away or from next session?
		//sanctionee.setEmissionsTarget((long) (sanctionee.getEmissionsTarget()*target_penalty));  TODO: Decide on this penalty
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
