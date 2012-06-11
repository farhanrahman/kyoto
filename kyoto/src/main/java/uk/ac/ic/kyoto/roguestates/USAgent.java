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

	public USAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput, long carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, availableToSpend, emissionsTarget, carbonOffset,
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
	
	/* US state goal is to achieve a specified ratio of economic growth to carbon output. 
	 * Politics of the US affect the current attitude toward carbon reduction. 
	 * Democrats are willing to accept a lower ratio, republicans a higher.
	 * General trend over time is for a lowering of the target ratio 
	 * 	-> must be a limit to how low this can go
	 * 	-> this will depend on what normal values of GDP growth turn out to be in the function. 
	 * Every 4 years an election is held.
	 * Only have two parties, democrats or republicans. 
	 * Winner of each election is decided by a function of economic performance of the past four years and a certain amount of randomness. 
	 * Republican parties will target whatever the previous term's ratio was. 
	 * Democrats will try to lower each term. 
	 * 	-> during each election democrats will campaign to lower by a certain amount. 
	 * 	-> if previous session economic performance was poor - higher value will improve election chances. 
	 * 		-> why? poor performance governments will promise 'green jobs' etc and increase investment. 
	 *  -> else if performance was good - higher value will lower election chances 
	 *  	-> why? opposition party will frame it as unnecessary expenditure. 
	 *  
	 *  Rough flow of execution:
	 *  Calculate resultant ratio from any action.
	 *  Check current party
	 *  	-> is resultant ratio  
	 */
	
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
	public void initialise() {
		super.initialise();
	}
	
	@Override
	protected void processInput(uk.ac.imperial.presage2.core.messaging.Input input) {
		
	};
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
