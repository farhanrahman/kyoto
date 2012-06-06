package uk.ac.ic.kyoto.countries;

import java.util.UUID;

import com.google.inject.Inject;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

public class TradeActionHandler implements ActionHandler{
	
	final protected TradeActionService tas;

	@Inject
	public TradeActionHandler(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider environment) throws UnavailableServiceException{
		this.tas = environment.getEnvironmentService(TradeActionService.class);
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof TradeAction;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		if(action instanceof TradeAction){
			TradeAction a = (TradeAction) action;
			synchronized(tas){
				this.tas.executeTrade(a.getT(), a.getTime(), a.getInitiator(), a.getReceiver() );
			}
			return null;
		}
		throw new ActionHandlingException("Action not recognized (From TradeActionHandler");
	}

}
