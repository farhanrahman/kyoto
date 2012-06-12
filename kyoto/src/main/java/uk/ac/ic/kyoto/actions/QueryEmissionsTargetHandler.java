package uk.ac.ic.kyoto.actions;

import java.util.UUID;

import uk.ac.ic.kyoto.services.CarbonTarget;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

import com.google.inject.Inject;

/**
 * Action handler to enable agents to query session targets.
 * 
 * @author Jonathan Ely
 */

public class QueryEmissionsTargetHandler implements ActionHandler {
	
	final protected CarbonTarget ct;
	
	@Inject
	public QueryEmissionsTargetHandler(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider environment) throws UnavailableServiceException {
		this.ct = environment.getEnvironmentService(CarbonTarget.class);
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof QueryEmissionsTarget;
	}
	
	@Override
	public Input handle(Action action, UUID countryID) throws ActionHandlingException {
		QueryEmissionsTarget actionObject = (QueryEmissionsTarget) action;
		
		long target = 0;
		
		switch (actionObject.getTargetPeriod()) {
			case SESSION:
				target = this.ct.querySessionTarget(countryID);
				break;
			
			case YEAR:
				target = this.ct.queryYearTarget(countryID);
				break;
		}
				
		actionObject.setEmissionsTarget(target);
		return null;
	}

}
