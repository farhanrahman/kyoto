package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;

public abstract class NonParticipant extends AbstractCountry {


	public NonParticipant(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate,double emissionsTarget, double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, emissionsTarget, energyOutput, carbonOutput);

		// TODO Auto-generated constructor stub
	}
}
