package uk.ac.ic.kyoto.roguestates;

import java.util.UUID;

import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.ic.kyoto.countries.AbstractCountry;
/*
 * ToDo
 * 
 * General
 * 
 * CDM investments - party that implements that increases its likelihood of re-election. 
 * 	-> some sort of condition on this? i.e. only if the previous year was a good year economically,
 *	   the justification being 	
 *
 * Amount of money to be spent on investments/CDM each year. 
 * Need to set overall goal state. Goal changes the year on year range of accepted values for reduction. 
 * -> Maximise Reduction
 * -> Maximise Wealth
 * 
 * Specific
 * 
 * - Calculate what target would be under Kyoto.
 * 	-> Used then for testing
 */
public class USAgent extends AbstractCountry {

	private static final int CampaignTargetIncrease = 5; // democrats campaign to further the reduction target
															// by a random number up 0 to this. 	
	private double			AverageGDPRate; // to be stored in an array or DB for furhter analysis. 

	private boolean democratElected; 			// chosen at random on class instantiation
	private double AbsolutionReductionTarget; 	// Units in metric tonnes C02
												// Can be positive or negative
	private long IntensityReductionTarget; 	// Units percentage (%)
	private long IntensityRatio;				// Units tonnes / million $

	public USAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, energyOutput, carbonOutput);
		SetInitialPoliticalParty();
		SetInitialIntensityRatio();
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#YearlyFunction()
	 * Called by execute() every year.
	 */
	public void YearlyFunction() {
		// GDPRate function calculates what the GDPRate was for the previous year.
		CalculateAverageGDP(); // takes the previously saved AverageGDPRate, adds the just calculated
		// value and divides by the elapsed number of years.
		// Election results are affected by the previous years GDPRate
		if(IsElectionYear()) {
			HoldElection(); // will set democratElected to either true or false
		}
		SetEmissionsTarget();
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
/*
 * Emissions must decrease in absolute terms, rather than just the intensity. 
 */
		if(JoiningCriteriaMet()) {
			KyotoMember.NONANNEXONE;
		}
			
		
	}
	
	boolean JoiningCriteriaMet() {
		// Calculate what target would be under Kyoto
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
			ParticipantTimeService timeService = getEnvironmentService(ParticipantTimeService.class);
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
		double  DemocratCampaignTarget;
		boolean CampaignTargetIsHigh;
		// Choose democrat next election period target intensity ratio reduction. Remember that
		// this target will be translated into an absolute metric value, thus higher values result
		// in a greater reduction. 		
		DemocratCampaignTarget = this.IntensityReductionTarget + Random.randomInt(CampaignTargetIncrease);
		
		if(DemocratCampaignTarget > (CampaignTargetIncrease / 2)) {
			CampaignTargetIsHigh = true;
		}
		else {
			CampaignTargetIsHigh = false;
		}
		// How to define what is a 'good' / 'bad' GDPRate for the previous year?
		// Long term average?
		// GDPRate += 
	}
	
	private void CalculateAverageGDP() {
	// Previous cumulative GDP changes divided by new total years elapsed. 
		try {
			ParticipantTimeService timeService = getEnvironmentService(ParticipantTimeService.class);			
		} 
		catch (UnavailableServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int YearsElapsed = timeService.getCurrentYear(); // returns years elapsed from year 0
		
		if(YearsElapsed==0) {
			AverageGDPRate = GDPRate; // GDPRates are seeded from historical data. 
		}
		else {
			AverageGDPRate = (AverageGDPRate + GDPRate) / YearsElapsed;
		}
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
