package uk.ac.ic.kyoto.annex1reduce;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

// TODO Everything

/**
 * Main EU body
 * Provides functions for allocating credits and imposing sactions on EU members
 * Provisional: Singleton which instantiates all EU member countries
 * @author Nik
 *
 */
public class EU extends AbstractParticipant {
	
	static final private UUID id = UUID.randomUUID();
	static final private String name = "EuropeanUnion";
	static final private Set<EUCountry> memberStates = new LinkedHashSet<EUCountry>();
	
	public EU() {
		super(id,name);
	}
	
	@Override
	protected void processInput(Input arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		super.execute();
	}

	/**
	 * Add member states to the EU. Allows credits to be allocated.
	 * @param state
	 */
	static public void addMemberState(EUCountry state) {
		memberStates.add(state);
	}
	
}
