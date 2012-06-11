package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;

public abstract class NonParticipant extends AbstractCountry {

	/*Constructor for testing*/
	public NonParticipant(UUID id, String name, String ISO){
		super(id,name,ISO);
	}
	
	public NonParticipant(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long emissionsTarget, long energyOutput, long carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, emissionsTarget,
				energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}
}
