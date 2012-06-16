package uk.ac.ic.kyoto.actions;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.CarbonTarget;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

import com.google.inject.Inject;

/**
 * Action object handler for adding country to CarbonTarget service
 * 
 * @author Jonathan Ely
 */
public class AddToCarbonTargetHandler implements ActionHandler {

	final protected CarbonTarget ct;
	
	@Inject
	public AddToCarbonTargetHandler (EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider environment) throws UnavailableServiceException {
		this.ct = environment.getEnvironmentService(CarbonTarget.class);
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof AddToCarbonTarget;
	}

	@Override
	public Input handle(Action action, UUID actor) throws ActionHandlingException {
		AddToCarbonTarget obj = (AddToCarbonTarget) action;
		if (actor == obj.countryObject.getID())
			ct.addMemberState(obj.countryObject);
		return null;
	}

}
