package uk.ac.ic.kyoto;

import java.util.UUID;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.messaging.Input;

public class PublicOfferHandler implements ActionHandler {

	@Override
	public boolean canHandle(Action action) {
		return action instanceof PublicOffer;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		// TODO Auto-generated method stub
		return null;
	}

}
