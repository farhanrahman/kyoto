package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.NonParticipant;

public class USAgent extends NonParticipant {


	public USAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long emissionsTarget, long carbonOffset,
			float economicOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, emissionsTarget, carbonOffset,
				economicOutput);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void processInput(uk.ac.imperial.presage2.core.messaging.Input input) {
		
	};
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
