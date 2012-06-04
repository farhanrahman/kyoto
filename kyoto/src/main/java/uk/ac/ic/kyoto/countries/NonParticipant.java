package uk.ac.ic.kyoto.countries;

import java.util.UUID;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public abstract class NonParticipant extends AbstractCountry {


	public NonParticipant(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, float availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, availableToSpend, emissionsTarget, carbonOffset,
				energyOutput);
		// TODO Auto-generated constructor stub
	}
}
