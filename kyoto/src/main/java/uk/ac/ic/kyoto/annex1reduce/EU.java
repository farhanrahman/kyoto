package uk.ac.ic.kyoto.annex1reduce;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.TimeService.EndOfYearCycle;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;

/**
 * EU service. Allocates carbon emissions targets for EU countries and does local monitoring and sanctioning.
 * @author ov109
 */
public class EU extends EnvironmentService {
	
	private ArrayList<EUCountry> euMemberStates = new ArrayList<EUCountry>();

	protected EU(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	// TODO Allocate target distribution
	
	@EventListener
	public void monitorCountries (EndOfYearCycle e) {
		for (EUCountry a : euMemberStates) {
			long realCarbonOutput = a.getMonitored();
			Serializable state = sharedState.get(CarbonReportingService.name, a.getID());
			Map<Integer, Double> reports = (Map<Integer, Double>)state;
			if (realCarbonOutput != reports.get(SimTime.get().intValue())) {
				sanction(a);
			}
		}
	}
	
	private void sanction(EUCountry sanctionee) {
		/// TODO Sanctions
	}
	
	/**
	 * Add EU member states to the EU service.
	 * @param state 
	 */
	public void addMemberState(EUCountry state) {
		euMemberStates.add(state);
	}

	
}
