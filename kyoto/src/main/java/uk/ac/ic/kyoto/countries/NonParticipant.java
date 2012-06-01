package uk.ac.ic.kyoto.countries;

import java.util.UUID;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public abstract class NonParticipant extends AbstractCountry {

	public NonParticipant(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double emissionsTarget, long carbonOffset,
			float availableToSpend, long carbonTraded) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, emissionsTarget, 
				carbonOffset, availableToSpend, carbonTraded);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}

}
