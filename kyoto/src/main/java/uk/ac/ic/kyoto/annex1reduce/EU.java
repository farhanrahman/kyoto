package uk.ac.ic.kyoto.annex1reduce;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
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
	
	private ArrayList<AnnexOneReduce> euMemberStates = new ArrayList<AnnexOneReduce>();

	protected EU(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	//QueryEmissionsTargetHandler targetHandler = new QueryEmissionsTargetHandler(sharedState, environment);
	
	// TODO Allocate target distribution
	
	@EventListener
	public void monitorCountries (EndOfYearCycle e) {
		for (AnnexOneReduce a : euMemberStates) {
			double realCarbonOutput = a.getCarbonOutput();
			Serializable state = sharedState.get(CarbonReportingService.name, a.getID());
			@SuppressWarnings("unchecked")
			Map<Integer, Double> reports = (Map<Integer, Double>)state;
			if (realCarbonOutput != reports.get(SimTime.get().intValue())) {
				sanction(a);
			}
		}
	}
	
	private void sanction(AnnexOneReduce sanctionee) {
		//TODO Sanctions
	}
	
	/**
	 * Add EU member states to the EU service.
	 * @param state 
	 */
	public void addMemberState(AnnexOneReduce state) {
		euMemberStates.add(state);
	}
}
