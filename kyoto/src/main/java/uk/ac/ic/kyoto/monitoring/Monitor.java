package uk.ac.ic.kyoto.monitoring;

import java.util.UUID;

import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * 
 * @author ov109 & sc1109
 *
 */

public class Monitor extends AbstractParticipant{

	public Monitor(UUID id, String name) {
		super(id, name);
	}

	@Override
	protected void processInput(Input in) {
		
	}
	
	@Override
	public void initialise(){
		super.initialise();
		
	}
}
