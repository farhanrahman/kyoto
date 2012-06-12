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
	private boolean democratElected; 			// chosen at random on class instantiation
	private long AbsolutionReductionTarget; 	// Units in metric tonnes C02
												// Can be positive or negative
	private long IntensityReductionTarget; 	// Units percentage (%)
	private long IntensityRatio;				// Units tonnes / million $

	public USAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long emissionsTarget,
			long energyOutput, long carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, emissionsTarget,
				energyOutput, carbonOutput);
		SetInitialPoliticalParty();
		SetInitialIntensityRatio();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#YearlyFunction()
	 * Called by execute() every year.
	 */
	public void YearlyFunction() {
		/*
		 * Function is executed at the end of every year. 
		 */
		if(IsElectionYear()) {
			HoldElection(); // will set democratElected to either true or false
		}
		SetEmissionsTarget();
		if (democratElected) {
			AbsolutionReductionTarget = (long) (carbonOutput*0.95);
		}
		else {
			AbsolutionReductionTarget = carbonOutput;
		}
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#SessionFunction()
	 * Called by execute every session.
	 * Notes:
	 * Carbon offsets are wiped at the beginning of each session. 
	 */ 
	public void SessionFunction() {
		if (carbonOutput <= emissionsTarget) {
			// Consider joining Kyoto here
		}
	}
	
	/*
	 * Sets the emissions target for the year after taking into account various factors. 
	 * Variable will hold an absolute value, but the agent itself will be targeting an
	 * intensity ratio. 
	 */
	public void SetEmissionsTarget() {
		
	}
	/*
	 * Functions returns true if it is an election year, false otherwise. 
	 */
	public boolean IsElectionYear() {
		try {
			TimeService timeService = getEnvironmentService(TimeService.class);
			if (timeService.getCurrentYear() % 4 == 0) {
				return(true);
			}
		} 
		catch (UnavailableServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return(false);
	}
		
	/* US state goal is to achieve a specified ratio of economic growth to carbon output. 
	 * Bush administration committed to reduce "greenhouse gas intensity” (ratio of emissions to economic output) of the U.S. economy by 18% over the next 10 years.
	 * Units of TonsCO2/Million$ GDP
	 * Politics of the US affect the current attitude toward carbon reduction. 
	 * Democrats are willing to accept a lower ratio, republicans a higher.
	 * General trend over time is for a lowering of the target ratio 
	 * 	-> must be a limit to how low this can go
	 * 	-> this will depend on what normal values of GDP growth turn out to be in the function. 
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
	private void HoldElection() {		
		// Local variables
		double DemocratCampaignTarget;
		
		// Choose democrat next election period target intensity ratio reduction. Remember that
		// this target will be translated into an absolute metric value, thus higher values result
		// in a greater reduction. 		
		DemocratCampaignTarget = this.IntensityReductionTarget + Random.randomInt(5);
		
		
	}
	
	private double CalculateTargetRatio(){
		
		return 0;
	}
	/*
	 * This function only called when country is instantiated. 
	 */
	private void SetInitialPoliticalParty() {
		int rand = Random.randomInt(100);
		if (rand < 50) {
			democratElected = true;
		}
		else {
			democratElected = false;
		}
	}
	
	/*
	 * Called on agent object instantiation. 
	 */
	private void SetInitialIntensityRatio() {
		CalculateIntensityRatio();
	}
	/*
	 * Calculation based on the currently held values. 
	 */
	private void CalculateIntensityRatio() {
		this.IntensityRatio = (long) (this.GDP / this.carbonOutput); // can remove the casting if AbstractCountry standardises types. 
	}
	
	@Override
	public void initialiseCountry() {
		
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#processInput(uk.ac.imperial.presage2.core.messaging.Input)
	 * Called by execute when input objects are waiting on your agent. 
	 */
	protected void processInput(uk.ac.imperial.presage2.core.messaging.Input input) {
		
	};
	
	@Override
	public void behaviour() {
		
	}

}
