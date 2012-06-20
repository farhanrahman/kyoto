package uk.ac.ic.kyoto.countries;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.GlobalTimeService.EndOfYearCycle;
import uk.ac.ic.kyoto.services.GlobalTimeService.TimeToMonitor;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
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
 * 
 * @author ov109, Stuart, sc1109, Adam, Jonathan Ely
 */
@ServiceDependencies({CarbonReportingService.class})
public class Monitor extends EnvironmentService {
	
	/*static Semaphore offsetAccess = new Semaphore(1);*/
	
	/* The amount that the Monitor can spend on monitoring in a current year */
	private double cash = 0;
	
	/* List of all the countries registered for the service */
	private Map<UUID, AbstractCountry> memberStates = new ConcurrentHashMap<UUID, AbstractCountry>();

	/* Structure that counts the number of times the country cheated */
	private Map<AbstractCountry, Integer> sinBin = new ConcurrentHashMap<AbstractCountry, Integer>();
	
	EventBus eb;
	
	private EnvironmentServiceProvider provider;
	private GlobalTimeService timeService;
	private CarbonReportingService carbonReportingService;
	private CarbonTarget carbonTargetingService;
	
	@Inject
	public Monitor(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider provider) {
		super(sharedState);
		
		this.provider = provider;
	}
	
	/**
	 * Add member states to the Monitor. Allows operation of sanctions, 
	 * credits, etc.
	 * @param state 
	 */
	public void addMemberState(AbstractCountry state) {
		/* not required: it's a map, it'll just overwrite the previous record. If we want to check, we have to throw something... */
		//if (!memberStates.containsKey(state.getID()))
		memberStates.put(state.getID(), state);
	}
	
	public void removeMemberState(AbstractCountry state) {
		memberStates.remove(state.getID());
	}
	
	@Inject
	public void setEB(EventBus eb) {
		this.eb = eb;
		eb.subscribe(this);
	}
	
	@EventListener
	public void yearlyFunction(EndOfYearCycle e) {
		if (e.getEndedYear() >= 0) {
			System.out.println("Yearly monitoring starting");
			checkReports();
			System.out.println("Checked reports");
			monitorCountries();
			System.out.println("Monitored country");
			carbonTargetingService.targetsForMonitor();
		}
	}
	
	private void checkReports () {
		for (AbstractCountry country : memberStates.values()) {
			if(country instanceof AbstractCountry){
				try {
					country.reportCarbonOutput();
					System.out.println("Looking for report from this time: " + SimTime.get().intValue());
					System.out.println("Current Year: " + timeService.getCurrentYear());
					System.out.println("This Country ID: " + country.getID());
					double reportedEmission = carbonReportingService.getReport(country.getID(), SimTime.get().intValue());
					double emissionTarget = carbonTargetingService.queryYearTarget(country.getID(), (timeService.getCurrentYear()-1));
					
					if (Math.round(reportedEmission) > Math.round(emissionTarget)) {
						targetSanction(country,  reportedEmission - emissionTarget);
					}
				} catch (ActionHandlingException e) {
					e.printStackTrace();
				}
			} else {
				throw new RuntimeException("this country is not an instance of AbstractCountry");
			}
		}
	}

	
	@EventListener
	public void initialize(EndOfTimeCycle e) {
		if (SimTime.get().intValue() == 1) {
			try {
				this.timeService = provider.getEnvironmentService(GlobalTimeService.class);
			} catch (UnavailableServiceException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			
			// Register for the carbon reporting service
			try {
				this.carbonReportingService = provider.getEnvironmentService(CarbonReportingService.class);
			} catch (UnavailableServiceException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			if (this.carbonReportingService == null) {
				throw new RuntimeException("Null carbonReportingService provided");
			}
			
			// Register for the carbon emissions targeting service
			try {
				this.carbonTargetingService = provider.getEnvironmentService(CarbonTarget.class);
			} catch (UnavailableServiceException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			if (this.carbonTargetingService == null) {
				throw new RuntimeException("Null carbonTargetingService provided");
			}
		}
	}
	
	// TODO add logging
	private void monitorCountries () {	
		ArrayList<UUID> cheaters = new ArrayList<UUID>();

		// Find how many countries can be monitored with the available cash
		int noToMonitor = (int) Math.floor(cash / GameConst.getMonitoringPrice());
		// Check if all the countries can be monitored 
		if (noToMonitor >= memberStates.size()) {
			// monitor all the countries
			
			for (AbstractCountry country: memberStates.values()) {
				double realCarbonOutput = country.getCarbonOutput();
				cash -= GameConst.getMonitoringPrice();
				double reportedCarbonOutput = carbonReportingService.getReport(country.getID(), SimTime.get().intValue());
				if (Math.round(realCarbonOutput) != Math.round(reportedCarbonOutput)) {
					cheaters.add(country.getID());
					cheatSanction(country);
					double targetDiff = realCarbonOutput - carbonTargetingService.queryYearTarget(country.getID(), (timeService.getCurrentYear()-1));
					if (Math.round(targetDiff) > 0) {
						targetSanction(country, targetDiff);
					}
				}
				country.updateCarbonOffsetYearly();
			}
			// TODO log the information about it
		} else {
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
				} while (monitoredCountries.contains(pickedCountry));
				
				// Monitor the country
				cash -= GameConst.getMonitoringPrice();
				double realCarbonOutput = pickedCountry.getCarbonOutput();
						
				// Note that the country was monitored
				monitoredCountries.add(pickedCountry);
				
				// Apply sanctions if a country has cheated and rechecks against target
				double reportedCarbonOutput = carbonReportingService.getReport(pickedCountry.getID(), SimTime.get());
				if ( Math.round(realCarbonOutput) != Math.round(reportedCarbonOutput)) {
					cheaters.add(pickedCountry.getID());
					cheatSanction(pickedCountry);
					double targetDiff = Math.round(realCarbonOutput - carbonTargetingService.queryYearTarget(pickedCountry.getID(), (timeService.getCurrentYear() - 1)));
					if (targetDiff > 0) {
						targetSanction(pickedCountry, targetDiff);
					}
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
		
		System.out.println("SANCTIONING: " + sanctionee.getName());
		
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
		sanctionee.setAvailableToSpend(Math.round((sanctionee.getAvailableToSpend()-sanctionee.getGDP()*(sinCount-1)* GameConst.getSanctionRate())));
	}
	
	/**
	 * Sanction for not meeting targets
	 * @param country
	 * The country to be sanctioned
	 */
	public void targetSanction(AbstractCountry country, double carbonExcess) {
		double penalty = carbonExcess * GameConst.getPenaltyCoef();
		carbonTargetingService.addCountryPenalty(country.getID(), penalty);
		
		// Charge the country for not meeting the target - financial penalties aren't applied by kyoto
		//country.setAvailableToSpend(Math.round((country.getAvailableToSpend() - carbonExcess * GameConst.getSanctionRate())));
		
	}
	
	/**
	 * Give a pre-determined amount for monitoring
	 * @param tax
	 */
	public void applyTaxation (double tax) {
		cash += tax;
	}
}
