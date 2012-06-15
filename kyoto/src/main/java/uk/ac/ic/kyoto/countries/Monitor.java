package uk.ac.ic.kyoto.countries;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.GlobalTimeService.EndOfYearCycle;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.SimTime;

import com.google.inject.Inject;


/**
 * Monitoring service
 * @author ov109, Stuart, sc1109, Adam, Jonathan Ely
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

	EventBus eb;
	
	private EnvironmentServiceProvider provider;
	private GlobalTimeService timeService;
	private CarbonReportingService carbonReportingService;
	private CarbonTarget carbonTargetingService;
	
	@Inject
	public Monitor(EnvironmentSharedStateAccess sharedState,
					EnvironmentServiceProvider provider) {
		super(sharedState);
	
		try {
			this.timeService = provider.getEnvironmentService(GlobalTimeService.class);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to get environment service 'TimeService'.");
			e.printStackTrace();
		}
		
		this.provider = provider;
	}
	
	/**
	 * Add member states to the Monitor. Allows operation of sanctions, 
	 * credits, etc.
	 * @param state 
	 */
	public void addMemberState(AbstractCountry state) {
		memberStates.add(state);
	}
	
	@Inject
	public void setEB(EventBus eb) {
		this.eb = eb;
		eb.subscribe(this);
	}
	
	@EventListener
	public void yearlyFunction(EndOfYearCycle e) {
		checkReports();
		monitorCountries();
	}
	
	private void checkReports () {
		for (AbstractCountry country : memberStates) {
			double reportedEmission = carbonReportingService.getReport(country.getID(), SimTime.get());
			double emissionTarget = carbonTargetingService.queryYearTarget(country.getID(), (timeService.getCurrentYear() - 1));

			if (reportedEmission > emissionTarget) {
				targetSanction(country, emissionTarget - reportedEmission);
			}
		}
	}

	
	@EventListener
	private void initialize(EndOfTimeCycle E) {
		if (SimTime.get().intValue() == 1) {
			// Register for the carbon reporting service
			try {
				this.carbonReportingService = provider.getEnvironmentService(CarbonReportingService.class);
			} catch (UnavailableServiceException e) {
				e.printStackTrace();
			}
			if(this.carbonReportingService == null){
				System.err.println("PROBLEM");
			}
			
			// Register for the carbon emissions targeting service
			try {
				this.carbonTargetingService = provider.getEnvironmentService(CarbonTarget.class);
			} catch (UnavailableServiceException e) {
				e.printStackTrace();
			}
			if(this.carbonTargetingService == null){
				System.err.println("PROBLEM");
			}
		}
	}
	
	// TODO add logging
	private void monitorCountries () {
		// Find how many countries can be monitored with the available cash
		int noToMonitor = (int) Math.floor(cash / GameConst.MONITORING_PRICE);

		ArrayList<UUID> cheaters = new ArrayList<UUID>();

		// Check if all the countries can be monitored
		if (noToMonitor >= memberStates.size()) {
			// monitor all the countries
			
			for (AbstractCountry country: memberStates) {
				double realCarbonOutput = country.getMonitored();
				cash -= GameConst.MONITORING_PRICE;
				double reportedCarbonOutput = carbonReportingService.getReport(country.getID(), SimTime.get());
				if (realCarbonOutput != reportedCarbonOutput) {
					cheaters.add(country.getID());
					cheatSanction(country);
					double targetDiff = realCarbonOutput - carbonTargetingService.queryYearTarget(country.getID(), (timeService.getCurrentYear() - 1));
					if (targetDiff > 0)
						targetSanction(country, targetDiff);
				}
			}
			// TODO log the information about it
		}
		else {
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
				cash -= GameConst.MONITORING_PRICE;
				double realCarbonOutput = pickedCountry.getMonitored();
						
				// Note that the country was monitored
				monitoredCountries.add(pickedCountry);
				
				// Apply sanctions if a country has cheated and rechecks against target
				double reportedCarbonOutput = carbonReportingService.getReport(pickedCountry.getID(), SimTime.get());
				if (realCarbonOutput != reportedCarbonOutput)
				{
					cheaters.add(pickedCountry.getID());
					cheatSanction(pickedCountry);
					double targetDiff = realCarbonOutput - carbonTargetingService.queryYearTarget(pickedCountry.getID(), (timeService.getCurrentYear() - 1));
					if (targetDiff > 0)
						targetSanction(pickedCountry, targetDiff);
				}
			}
		}
		if (!cheaters.isEmpty())
			carbonTargetingService.retargetDueToCheaters(cheaters);
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
		sanctionee.setAvailableToSpend(Math.round((sanctionee.getAvailableToSpend()-sanctionee.getGDP()*(sinCount-1)* GameConst.SANCTION_RATE)));
	}
	
	/**
	 * Sanction for not meeting targets
	 * @param country
	 * The country to be sanctioned
	 */
	public void targetSanction(AbstractCountry country, double carbonExcess) {
		double penalty = carbonExcess * 1.3;
		carbonTargetingService.addCountryPenalty(country.getID(), penalty);
		
		// Charge the country for not meeting the target
		country.setAvailableToSpend( Math.round( (country.getAvailableToSpend() - carbonExcess * GameConst.SANCTION_RATE)) );
		
	}
	
	/**
	 * Give a pre-determined amount for monitoring
	 * @param tax
	 */
	public void applyTaxation (double tax) {
		cash += tax;
	}

}
