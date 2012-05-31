package uk.ac.ic.kyoto.annex1reduce;

import java.util.ArrayList;
import java.util.UUID;

import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

// TODO Everything

/**
 * Main EU body
 * Provides functions for allocating credits and imposing sactions on EU members
 * Singleton class with mostly static variables and methods
 * @author Nik
 *
 */
public class EU extends AbstractParticipant {
	
	static final private UUID id = UUID.randomUUID();
	static final private String NAME = "EuropeanUnion";
	static final private ArrayList<EUCountry> memberStates = new ArrayList<EUCountry>();
	
	public EU() {
		super(id,NAME);
	}
	
	//TODO PLACEHOLDER Determine how input names work
	static final String NEWROUND = "NEWROUND";
	
	/**
	 * Called by execute multiple times for many inputs
	 * @param input The current input to be handled
	 */
	@Override
	protected void processInput(Input input) {
		String name = input.getType();

		//If the input is relevant, perform an action
		if (name == NEWROUND) {
			applySanctions();
			allocateCredits();
		}
		//TODO handle more actions
		
		
	}

	/**
	 * Called when a round ends. Calculates and applies sanctions to 
	 * member states
	 */
	static private void applySanctions() {
		for (int i=0; i<memberStates.size();i++) {
			EUCountry euc = memberStates.get(i);
			//TODO calculate and apply sanctions to member states
			
		}
	}
	
	/**
	 * Called when a round ends. Calculates and allocates credits to
	 * member states
	 */
	static private void allocateCredits() {
		for (int i=0; i<memberStates.size();i++) {
			EUCountry euc = memberStates.get(i);
			//TODO Calculate credits to give to all EU member states
		}
	}
	
	/**
	 * Add member states to the EU. Allows operation of sanctions, 
	 * credits, etc.
	 * @param state 
	 */
	static public void addMemberState(EUCountry state) {
		memberStates.add(state);
	}
	
}
