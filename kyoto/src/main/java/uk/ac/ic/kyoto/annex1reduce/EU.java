package uk.ac.ic.kyoto.annex1reduce;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;


import uk.ac.ic.kyoto.actions.QueryEmissionsTarget;
import uk.ac.ic.kyoto.actions.QueryEmissionsTargetHandler;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.GlobalTimeService.EndOfYearCycle;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.SimTime;

/**
 * EU service. Allocates carbon emissions targets for EU countries and does local monitoring and sanctioning.
 * @author ov109
 */
public class EU extends EnvironmentService {
	
	private ArrayList<EUCountry> euMemberStates = new ArrayList<EUCountry>();

	protected EU(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	//QueryEmissionsTargetHandler targetHandler = new QueryEmissionsTargetHandler(sharedState, environment);
	
	// TODO Allocate target distribution
	
	@EventListener
	public void monitorCountries (EndOfYearCycle e) {
		for (EUCountry a : euMemberStates) {
			double realCarbonOutput = a.getMonitored();
			Serializable state = sharedState.get(CarbonReportingService.name, a.getID());
			@SuppressWarnings("unchecked")
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
