package uk.ac.ic.kyoto.actions;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.CarbonTarget;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

import com.google.inject.Inject;

public class RejoinKyotoHandler implements ActionHandler {

	final protected CarbonTarget ct;
	
	@Inject
	public RejoinKyotoHandler(EnvironmentServiceProvider environment) throws UnavailableServiceException {
		this.ct = environment.getEnvironmentService(CarbonTarget.class);
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof RejoinKyoto;
	}

	@Override
	public Input handle(Action action, UUID actor) throws ActionHandlingException {
		ct.queueToJoin.add(actor);
		return null;
	}

}
