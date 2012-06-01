package uk.ac.ic.kyoto.countries;

import java.util.UUID;

public class USAgent extends NonParticipant {

	public USAgent(UUID id, String name, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			double dirtyIndustry, double emissionsTarget, long carbonOffset,
			float availableToSpend, long carbonTraded) {
		super(id, name, landArea, arableLandArea, GDP, GDPRate, dirtyIndustry,
				emissionsTarget, carbonOffset, availableToSpend, carbonTraded);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
