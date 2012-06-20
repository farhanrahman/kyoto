package uk.ac.ic.kyoto.actions;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry.KyotoMember;
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
 * Action object handler for adding country to Monitor service
 * 
 * @author Stuart Holland, Jonathan Ely
 */
public class AddRemoveFromMonitorHandler implements ActionHandler {
	
	final protected Monitor monitor;
	
	@Inject
	public AddRemoveFromMonitorHandler(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider environment) throws UnavailableServiceException {
		this.monitor = environment.getEnvironmentService(Monitor.class);
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof AddRemoveFromMonitor;
	}

	@Override
	public Input handle(Action action, UUID actor) throws ActionHandlingException {
		AddRemoveFromMonitor obj = (AddRemoveFromMonitor) action;
		if (obj.country.getID() == actor) {
			switch (obj.actionToImplement) {
			case ADD :
				if (obj.country.isKyotoMember() == KyotoMember.ANNEXONE)
					this.monitor.addMemberState(obj.country);
				else
					this.monitor.addNonSanctionedState(obj.country);
				break;
			case REMOVE:
				if (obj.country.isKyotoMember() == KyotoMember.ANNEXONE)
					this.monitor.removeMemberState(obj.country);
				else
					this.monitor.removeNonSanctionedState(obj.country);
				break;
			}

		}
		return null;
	}

}
