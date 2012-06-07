package uk.ac.ic.kyoto.actions;

import java.util.UUID;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.messaging.Input;

/*
 * Action handler to enable agents to query session targets.
 * 
 * Author: Jonathan Ely
 */

public class QueryEmissionsTargetHandler implements ActionHandler {

	@Override
	public boolean canHandle(Action action) {
		return action instanceof QueryEmissionsTarget;
	}

	@Override
	public Input handle(Action action, UUID countryID) throws ActionHandlingException {
		// Cast action object
		QueryEmissionsTarget actionObject = (QueryEmissionsTarget) action;
		
		// Look up target in shared state
		long target = 0;
		
		// Set target in action object
		actionObject.setEmissionsTarget(target);
		
		return null;
	}

}
