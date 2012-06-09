package uk.ac.ic.kyoto.monitor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import com.google.inject.Inject;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.CarbonTarget;
import uk.ac.ic.kyoto.services.TimeService.EndOfYearCycle;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.SimTime;

/**
 * Monitoring service
 * @author ov109, Stuart, sc1109, Adam
 *
 */
@ServiceDependencies({CarbonReportingService.class})
public class Monitor extends EnvironmentService {
	
	// The amount that the Monitor can spend on monitoring in a current year
	private double cash = 0;
	
	// List of all the countries registered for the service
	private ArrayList<AbstractCountry> memberStates = new ArrayList<AbstractCountry>();

	// Structure that counts the number of times the country cheated
	private Map<AbstractCountry, Integer> sinBin;
	
	/**
	 * percentage increase in target i.e. 1.05 for 5%
	 */
	@Parameter(name="target_penalty")
	int target_penalty;
	
	/**
	 * percentage decrease in cash i.e. 0.95 for 5%
	 */
	@Parameter(name="cash_penalty")
	int cash_penalty;
	
	private CarbonReportingService carbonReportingService;
	private CarbonTarget carbonTargetingService;
	
	@Inject
	public Monitor(EnvironmentSharedStateAccess sharedState,
					EnvironmentServiceProvider provider) {
		super(sharedState);
		
		// Register for the carbon reporting service
		try {
			this.carbonReportingService = provider.getEnvironmentService(CarbonReportingService.class);
		} catch (UnavailableServiceException e) {
			e.printStackTrace();
		}
		if(this.carbonReportingService == null){
			System.err.println("PROBLEM");
		}
		
		// Register for the carbon emission targeting service
		try {
			this.carbonTargetingService = provider.getEnvironmentService(CarbonTarget.class);
		} catch (UnavailableServiceException e) {
			e.printStackTrace();
		}
		if(this.carbonTargetingService == null){
			System.err.println("PROBLEM");
		}
	}
	
	/**
	 * Add member states to the Monitor. Allows operation of sanctions, 
	 * credits, etc.
	 * @param state 
	 */
	public void addMemberState(AbstractCountry state) {
		memberStates.add(state);
	}
	
	@EventListener
	public void yearlyFunction(EndOfYearCycle e) {
		checkReports();
		monitorCountries();
	}
	
	private void checkReports () {
		for (AbstractCountry country : memberStates) {
			double reportedEmission = carbonReportingService.getReport(country.getID(), SimTime.get());
			double emissionTarget = country.getEmissionsTarget();
			
			if (reportedEmission < emissionTarget) {
				targetSanction(country);
			}
		}
	}
	
	// TODO add logging
	private void monitorCountries () {
		// Find how many countries can be monitored with the available cash
		int noToMonitor = (int) Math.floor(cash / GameConst.MONITORING_PRICE);
		
		// Create a list of countries that were already monitored this year
		ArrayList<AbstractCountry> monitoredCountries = new ArrayList<AbstractCountry>();
		
		// Instantiate random number generator that will be used to pick the countries to monitor
		Random randGenerator = new Random();
		
		for (int i = 0; i < noToMonitor; i++) {
			// Pick a country that was not yet monitored
			AbstractCountry pickedCountry;
			do {
				int randomCountryIndex = randGenerator.nextInt(memberStates.size());
				pickedCountry = memberStates.get(randomCountryIndex);
			}
			while (monitoredCountries.contains(pickedCountry) );
			
			// Monitor the country
			long realCarbonOutput = pickedCountry.getMonitored();
					
			// Note that the country was monitored
			monitoredCountries.add(pickedCountry);
			
			// Apply sanctions if a country has cheated
			double reportedCarbonOutput = carbonReportingService.getReport(pickedCountry.getID(), SimTime.get());
			if (realCarbonOutput > reportedCarbonOutput)
			cheatSanction(pickedCountry);
		}
	}
	
	// TODO add emissionTarget change to sanctioning
	
	/**
	 * Apply cheating sanctions. 
	 * @param sanctionee
	 * The country that should be sanctioned
	 */
	private void cheatSanction(AbstractCountry sanctionee) {
		
		int sinCount;
		
		// Update the list of countries that have cheated
		if (sinBin.containsKey(sanctionee)) {
			sinCount = sinBin.get(sanctionee) + 1;
		}
		else {
			sinCount = 1;
		}
		sinBin.put(sanctionee, sinCount);
		
		// Deduct the cash from the country that has cheated
		// newCash = oldCash - GDP * cash_penalty
		sanctionee.setAvailableToSpend( (long) (sanctionee.getAvailableToSpend()-sanctionee.getGDP()*(sinCount-1)*cash_penalty));
	}
	
	/**
	 * Sanction for not meeting targets
	 * @param sanctionee
	 * The country to be sanctioned
	 */
	public void targetSanction(AbstractCountry sanctionee) {
		
		//5% higher target regardless of number of sins (compound)
		//sanctionee.setEmissionsTarget((long) (sanctionee.getEmissionsTarget()*target_penalty));  TODO: Decide on this penalty

	}
	
	/**
	 * Give a pre-determined amount for monitoring
	 * @param tax
	 */
	public void applyTaxation (double tax) {
		cash += tax;
	}

}
