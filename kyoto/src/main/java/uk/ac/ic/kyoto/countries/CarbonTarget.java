package uk.ac.ic.kyoto.countries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.GlobalTimeService.EndOfSessionCycle;
import uk.ac.ic.kyoto.services.GlobalTimeService.EndOfYearCycle;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.SimTime;

import com.google.inject.Inject;

/**
 *  Participant environment service for setting of carbon targets. Queried by countries via an action.
 * 
 *  Formula can be found on github wiki.
 *  
 *  @author Jonathan Ely
 */

public class CarbonTarget extends EnvironmentService {

	private class countryObject  {
		final public AbstractCountry obj;
		
		public double lastSessionTarget = 0;
		public double currentSessionTarget = 0;
		
		public Map<Integer, Double> yearTargets = new HashMap<Integer, Double>();
		
		public double proportion = 0;
		public double penalty = 0;
		
		public countryObject(AbstractCountry country) {
			this.obj = country;
		}
	}
	
	private ArrayList<countryObject> participantCountries= new ArrayList<countryObject>();
	private Map<String, Double> output1990Data = new HashMap<String, Double>();
	private ArrayList<UUID> cheatersList = new ArrayList<UUID>();
	
	private double worldLastSessionTarget = 0;
	private double worldCurrentSessionTarget = 0;
	private int sessionCounter = 0;
	
	private EventBus eb;
	private GlobalTimeService timeService;
	private CarbonReportingService reportingService;
	private Semaphore exclusiveAccess = new Semaphore(1);
	
	private EnvironmentServiceProvider provider;
	
	@Inject
	public CarbonTarget(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider provider) {
		super(sharedState);
		
		this.provider = provider;
	}
	
	@Inject
	public void setEB(EventBus eb) {
		this.eb = eb;
		eb.subscribe(this);
	}

	/**
	 * Adds states to the service
	 */
	public void addMemberState(AbstractCountry state) {
		countryObject memberState = new countryObject(state);
		this.participantCountries.add(memberState);
	}
	
	/**
	 * Adds 1990 output data to carbon target service (used for initial targets)
	 */
	public void add1990OutputData(String ISO, double outputData){
		this.output1990Data.put(ISO, outputData);
	}
	
	public double querySessionTarget(UUID countryID) {
		countryObject obj = findCountryObject(countryID);
		return obj.currentSessionTarget;
	}
	
	public double queryYearTarget(UUID countryID, int year) {
		countryObject obj = findCountryObject(countryID);
		return obj.yearTargets.get(year);
	}
	
	void addCountryPenalty(UUID countryID, double penaltyValue) {
		try {
			this.exclusiveAccess.acquire();
		} catch (InterruptedException e) {
			System.out.println("Interrupted Exception" + e);
			e.printStackTrace();
		}
		
		countryObject target= findCountryObject(countryID);
		target.penalty += penaltyValue;
		generateYearTarget(target);
		
		this.exclusiveAccess.release();
	}
	
	void retargetDueToCheaters(ArrayList<UUID> theCheaters) {
		this.cheatersList = theCheaters;

		if ((timeService.getCurrentYear() % GameConst.YEARS_IN_SESSION) == 0) {
			updateSessionTargets();
			updateYearTargets();
		}

		this.cheatersList.clear();
	}

	/*
	 * Function to be called after all countries have been added
	 */
	private void initialise(){
		try {
			this.timeService = provider.getEnvironmentService(GlobalTimeService.class);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to get environment service 'TimeService'.");
			e.printStackTrace();
		}
		try {
			this.reportingService = provider.getEnvironmentService(CarbonReportingService.class);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to get environment service 'CarbonReportingService'.");
			e.printStackTrace();
		}
		
		this.worldCurrentSessionTarget = 0;
		
		for (countryObject country : participantCountries) {
			double data = 0;
			try {
				data = output1990Data.get(country.obj.getISO());
			} catch (Exception e) {
				System.out.println("1990 Data not Loaded for country: " + country.obj.getName());
				e.printStackTrace();
			}
			
			country.currentSessionTarget = data;
			this.worldCurrentSessionTarget += data;
		}		
		
		this.worldLastSessionTarget = this.worldCurrentSessionTarget;
		this.worldCurrentSessionTarget = worldLastSessionTarget * GameConst.TARGET_REDUCTION ; 

		updateSessionTargets();
		updateYearTargets();
	}
	
	private double getReportedCarbonOutput(UUID countryID, int year){
		double result;
		if (cheatersList.contains(countryID)){
			result = findCountryObject(countryID).obj.getMonitored();
		} else {
			Map<Integer, Double> reports = reportingService.getReport(countryID);
			int simTime = timeService.getTicksInYear() * (year +1);
			result = reports.get(simTime);
		}
		return result;
	}
	
	private countryObject findCountryObject(UUID countryID) {
		countryObject result = null;
		for (countryObject country : participantCountries) {
			if (country.obj.getID() == countryID) {
				result = country;
			}
		}		
		return result;
	}
	
	@EventListener
	public void onTimeCycle(EndOfTimeCycle e) {
		if (SimTime.get().intValue() == 1) {
			initialise();
		}
	}
	
	@EventListener
	public void onEndOfSession(EndOfSessionCycle e) {
		if (timeService.getCurrentSession() != 0) {
			this.worldLastSessionTarget = this.worldCurrentSessionTarget;
			this.worldCurrentSessionTarget = worldLastSessionTarget * GameConst.TARGET_REDUCTION ; 
			updateSessionTargets();
			this.sessionCounter++;
		}
	}
	
	@EventListener
	public void onEndOfYear(EndOfYearCycle e) {
		if (timeService.getCurrentYear() != 0) {
			updateYearTargets();
		}
	}
	
	private void updateSessionTargets(){
		double worldOutput = 0;
		double rogueCarbonOutput = 0;
		
		int lastYear = timeService.getCurrentYear() - 1;
		
		for (countryObject country : participantCountries) {
			double output = getReportedCarbonOutput(country.obj.getID(), lastYear);
			worldOutput += output;
			if(!country.obj.getIsKyotoMember()){
				rogueCarbonOutput += output ;
			}
		}
		
		double kyotoTarget = this.worldCurrentSessionTarget - rogueCarbonOutput;
		
		for (countryObject country : participantCountries) {
			if (country.obj.getIsKyotoMember()){
				double output = getReportedCarbonOutput(country.obj.getID(), lastYear);
				country.proportion = output / (worldOutput - rogueCarbonOutput);
				generateSessionTarget(country, kyotoTarget);
			}
		}
	}
	
	public void updateYearTargets()
	{
		try {
			this.exclusiveAccess.acquire();
		} catch (InterruptedException e) {
			System.out.println("Interrupted Exception : " + e);
			e.printStackTrace();
		}
		
		while (this.sessionCounter != timeService.getCurrentSession()) {}
		
		for (countryObject country : participantCountries) {
			generateYearTarget(country);
		}
		
		this.exclusiveAccess.release();
	}
	
	/*
	 * Generates end of session target (binding) from 1990 data
	 */
	private void generateSessionTarget(countryObject country, double kyotoTarget)
	{	
		country.penalty = 0;
		country.lastSessionTarget = country.currentSessionTarget;
		country.currentSessionTarget = country.proportion * kyotoTarget;
	}
	
	/*
	 * Generates end of year target (non binding) based on last reported emissions and session target.
	 */
	private void generateYearTarget(countryObject country)
	{
		double sessionProgress = (timeService.getCurrentYear() % GameConst.YEARS_IN_SESSION) / GameConst.YEARS_IN_SESSION;
		double diffTargets = country.lastSessionTarget - country.currentSessionTarget;
		double newTarget = country.lastSessionTarget - (diffTargets * sessionProgress) - country.penalty;
		country.yearTargets.put(timeService.getCurrentYear(), newTarget);
		country.obj.emissionsTarget = newTarget;
	}	
}
