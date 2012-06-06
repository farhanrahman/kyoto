package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import uk.ac.ic.kyoto.services.TimeService.EndOfSession;
import uk.ac.ic.kyoto.services.TimeService.EndOfYear;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.util.random.Random;


public class USAgent extends NonParticipant {

	private int yearMod4 = 0;
	
	private boolean democratElected=true;
	
	private long internalEmissionsTarget=(long) (carbonOutput*0.95);

	public USAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput, long carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, availableToSpend, emissionsTarget, carbonOffset,
				energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}
	
	@EventListener
	private void yearCounter(EndOfYear e) {
		yearMod4++;
		if (yearMod4 == 4) {
			yearMod4 = 0;
			election();
		}
		if (democratElected) {
			internalEmissionsTarget = (long) (carbonOutput*0.95);
		}
		else {
			internalEmissionsTarget = carbonOutput;
		}
	}
	
	@EventListener
	private void sessionOver(EndOfSession e) {
		if (carbonOutput <= emissionsTarget) {
			// Consider joining Kyoto here
		}
	}
	
	private void election() {
		int rand = Random.randomInt(100);
		if (rand < 50) {
			democratElected = true;
		}
		else {
			democratElected = false;
		}
	}
	
	@Override
	protected void processInput(uk.ac.imperial.presage2.core.messaging.Input input) {
		
	};
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
