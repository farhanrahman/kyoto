package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.services.TimeService;
import uk.ac.ic.kyoto.services.TimeService.EndOfSessionCycle;
import uk.ac.ic.kyoto.services.TimeService.EndOfYearCycle;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.util.random.Random;


public class USAgent extends NonParticipant {

	private int yearMod4 = 0;
	
	private boolean democratElected=true;
	
	private long internalEmissionsTarget=(long) (carbonOutput*0.95);

	public USAgent(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long emissionsTarget, long energyOutput, long carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, emissionsTarget,
				energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}
	
	@EventListener
	private void yearCounter(EndOfYearCycle e) {
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
	
	@Override
	public void YearlyFunction() {
		try {
			TimeService timeService = getEnvironmentService(TimeService.class);
			if (timeService.getCurrentYear() % 4 == 0) {
				election();
			}
		} catch (UnavailableServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (democratElected) {
			internalEmissionsTarget = (long) (carbonOutput*0.95);
		}
		else {
			internalEmissionsTarget = carbonOutput;
		}
	}
	
	@Override
	public void SessionFunction() {
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
	public void initialiseCountry() {
		
	}
	
	@Override
	protected void processInput(uk.ac.imperial.presage2.core.messaging.Input input) {
		
	};
	
	@Override
	public void behaviour() {
		
	}

}
