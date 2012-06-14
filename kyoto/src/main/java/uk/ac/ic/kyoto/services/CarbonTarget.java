package uk.ac.ic.kyoto.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.services.GlobalTimeService.EndOfSessionCycle;
import uk.ac.ic.kyoto.services.GlobalTimeService.EndOfYearCycle;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;

import com.google.inject.Inject;

/**
 *  Participant environment service for setting of carbon targets. Queried by countries via an action.
 * 
 *  Formula can be found on github wiki.
 *  
 *  TODO:
 *  1. First session data
 *  2. Finish year target shiz
 *  
 *  @author Jonathan Ely
 */

public class CarbonTarget extends EnvironmentService {

	private class countryObject  {
		final public AbstractCountry obj;
		
		public double lastSessionTarget = 0;
		public double currentSessionTarget = 0;
		public double currentYearTarget = 0;
		
		public double proportion = 0;
		public double penalty = 0;
		
		public countryObject(AbstractCountry country) {
			this.obj = country;
		}
	}
	
	private ArrayList<countryObject> participantCountries= new ArrayList<countryObject>();
	private Map<String, Double> output1990Data = new HashMap<String, Double>();
	
	private double worldLastSessionTarget = 0;
	private double worldCurrentSessionTarget = 0;
	
	private EventBus eb;
	private ParticipantTimeService timeService;

	@Inject
	protected CarbonTarget(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider provider) {
		super(sharedState);
		try {
			this.timeService = provider.getEnvironmentService(ParticipantTimeService.class);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to get environment service 'TimeService'.");
			e.printStackTrace();
		}
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
	
	public double queryYearTarget(UUID countryID) {
		countryObject obj = findCountryObject(countryID);
		return obj.currentYearTarget;
	}
	
	public void setCountryPenalty(UUID countryID, double penaltyValue) {
		countryObject target= findCountryObject(countryID);
		target.penalty += penaltyValue;
		generateYearTarget(target);
	}
	
	/*
	 * Function to be called after all countries have been added
	 */
	private void initialise(){
		double kyotoTarget = 100;
		
		for (countryObject country : participantCountries) {
			generateSessionTarget(country, kyotoTarget);
			generateYearTarget(country);
		}
	}
	
	private double getReportedCarbonOutput(UUID County){
		/*
		 * NEEDS TO BE IMPLEMENTED
		 */
		return 0;
	}
	
	private countryObject findCountryObject(UUID countryID){
		countryObject result = null;
		for (countryObject country : participantCountries) {
			if (country.obj.getID() == countryID) {
				result = country;
			}
		}
		return result;
	}
	
	@EventListener
	public void onEndOfSession(EndOfSessionCycle e){
		updateSessionTarget();
	}
	
	@EventListener
	public void onEndOfYear(EndOfYearCycle e){
		updateYearTarget();
	}
	
	private void updateSessionTarget(){
		this.worldLastSessionTarget = this.worldCurrentSessionTarget;
		this.worldCurrentSessionTarget = worldLastSessionTarget * GameConst.TARGET_REDUCTION ; 
		
		double rogueCarbonOutput = 0;
		for (countryObject country : participantCountries) {
			if(!country.obj.getIsKyotoMember()){
				rogueCarbonOutput += getReportedCarbonOutput(country.obj.getID());
			}
		}
		
		double kyotoTarget = this.worldCurrentSessionTarget - rogueCarbonOutput;
		
		/*
		 * TODO: Set country carbon output proportions
		 */
		
		for (countryObject country : participantCountries) {
			generateSessionTarget(country, kyotoTarget);
		}
	}
	
	public void updateYearTarget()
	{
		for (countryObject country : participantCountries) {
			generateYearTarget(country);
		}
	}
	
	/*
	 * Generates end of session target (binding) from 1990 data
	 */
	private void generateSessionTarget(countryObject country, double kyotoTarget)
	{	
		country.penalty = 0;
		country.currentSessionTarget = country.proportion * kyotoTarget;
	}
	
	/*
	 * Generates end of year target (non binding) based on last reported emissions and session target.
	 */
	private void generateYearTarget(countryObject country)
	{
		double sessionProgress = (timeService.getCurrentYear() % GameConst.YEARS_IN_SESSION) / GameConst.YEARS_IN_SESSION;
		double diffTargets = country.lastSessionTarget - country.currentSessionTarget;
		country.currentYearTarget = country.lastSessionTarget - (diffTargets * sessionProgress) - country.penalty;
	}	
}
