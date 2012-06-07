package uk.ac.ic.kyoto.actions;

import java.util.UUID;

import com.google.inject.Inject;

import uk.ac.ic.kyoto.services.CarbonTarget;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

/*
 * Action handler to enable agents to query session targets.
 * 
 * Author: Jonathan Ely
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
		// Cast action object
		QueryEmissionsTarget actionObject = (QueryEmissionsTarget) action;
		
		// Look up target in shared state
		long target = this.ct.queryTarget(countryID);
		
		// Set target in action object
		actionObject.setEmissionsTarget(target);
		
		return null;
	}

}
