package uk.ac.ic.kyoto.actions;

import java.util.UUID;

import com.google.inject.Inject;

import uk.ac.ic.kyoto.countries.CarbonTarget;
import uk.ac.ic.kyoto.countries.Monitor;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

public class ApplyMonitorTaxHandler implements ActionHandler {

	final protected Monitor monitor;
	
	@Inject
	public ApplyMonitorTaxHandler(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider environment) throws UnavailableServiceException {
		this.monitor = environment.getEnvironmentService(Monitor.class);
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof ApplyMonitorTax;
	}
	
	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		ApplyMonitorTax obj = (ApplyMonitorTax) action;
		monitor.applyTaxation(obj.amount);
		return null;
	}
}
