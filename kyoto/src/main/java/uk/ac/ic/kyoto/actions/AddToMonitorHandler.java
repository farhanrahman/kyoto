package uk.ac.ic.kyoto.actions;

import java.util.UUID;
import uk.ac.ic.kyoto.countries.Monitor;
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
 * @author Stuart Holland
 */
public class AddToMonitorHandler implements ActionHandler {
	
	final protected Monitor monitor;
	
	@Inject
	public AddToMonitorHandler(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider environment) throws UnavailableServiceException {
		this.monitor = environment.getEnvironmentService(Monitor.class);
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof AddToMonitor;
	}

	@Override
	public Input handle(Action action, UUID actor) throws ActionHandlingException {
		AddToMonitor obj = (AddToMonitor) action;
		monitor.addMemberState(obj.country);
		return null;
	}

}
