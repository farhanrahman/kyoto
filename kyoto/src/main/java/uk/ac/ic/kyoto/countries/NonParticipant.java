package uk.ac.ic.kyoto.countries;

import java.util.UUID;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public class NonParticipant extends AbstractParticipant {

	public NonParticipant(UUID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}

}
