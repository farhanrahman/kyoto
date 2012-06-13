package uk.ac.ic.kyoto.services;

import java.util.ArrayList;
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
		public double proportion;
		public double penalty = 0;
		
		public countryObject(AbstractCountry country) {
			this.obj = country;
		}
	}
	
	private ArrayList<countryObject> participantCountries= new ArrayList<countryObject>();
	
	/*
	 * Store world session targets 
	 */
	private double lastSessionTarget;
	private double currentSessionTarget;
	
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

	/**
	 * Adds states to the service
	 */
	public void addMemberState(AbstractCountry state) {
		countryObject memberState = new countryObject(state);
		this.participantCountries.add(memberState);
	}
	
	@Inject
	public void setEB(EventBus eb) {
		this.eb = eb;
		eb.subscribe(this);
	}
	
	public double querySessionTarget(UUID country) {
		double state = (Double) sharedState.get("EmissionsSessionTarget", country);
		return state;
	}
	
	public double queryYearTarget(UUID country) {
		double state = (Double) sharedState.get("EmissionsYearTarget", country);
		return state;
	}
	
	private double getReportedCarbonOutput(UUID County)
	{
		/*
		 * NEEDS TO BE IMPLEMENTED
		 */
		return 0;
	}
	
	/*
	 * Function that is triggered by the end of session event.
	 */
	@EventListener
	public void updateSessionTarget(EndOfSessionCycle e)
	{
		this.lastSessionTarget = this.currentSessionTarget;
		this.currentSessionTarget = lastSessionTarget * GameConst.TARGET_REDUCTION ; 
		
		double rogueCarbonOutput = 0;
		for (countryObject country : participantCountries) {
			if(!country.obj.getIsKyotoMember()){
				rogueCarbonOutput += getReportedCarbonOutput(country.obj.getID());
			}
		}
		
		double kyotoTarget = this.currentSessionTarget - rogueCarbonOutput;
		
		/*
		 * TODO: Set country carbon output proportions
		 */
		
		for (countryObject country : participantCountries) {
			generateSessionTarget(country, kyotoTarget);
		}
	}
	
	/*
	 * Function that is triggered by the end of year event.
	 */
	@EventListener
	public void updateYearTarget(EndOfYearCycle e)
	{
		for (countryObject country : participantCountries) {
			generateYearTarget(country);
		}
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
	
	/**
	 * Applies penalty to year targets for given country 
	 * 
	 * @param country
	 * @param penaltyValue
	 */
	public void setCountryPenalty(UUID countryID, double penaltyValue) {
		countryObject target= findCountryObject(countryID);
		target.penalty += penaltyValue;

		generateYearTarget(target);
	}
	
	/*
	 * Generates end of session target (binding) from 1990 data
	 */
	private void generateSessionTarget(countryObject country, double kyotoTarget)
	{	
		country.penalty = 0;
		double sessionTarget = country.proportion * kyotoTarget;
		sharedState.change("EmissionsSessionTarget", country.obj.getID(), sessionTarget);
	}
	
	/*
	 * Generates end of year target (non binding) based on last reported emissions and session target.
	 */
	private void generateYearTarget(countryObject country)
	{
		double sessionProgress = (timeService.getCurrentYear() % GameConst.YEARS_IN_SESSION) / GameConst.YEARS_IN_SESSION;
		
		/*
		 * NEED TO GET THESE FROM SOMEWHERE
		 */
		double lastSessionTarget = 1000; 
		double currentSessionTarget = 100;
		
		double diffTargets = lastSessionTarget - currentSessionTarget;
		double penalty = country.penalty;
		
		double yearTarget = lastSessionTarget - (diffTargets * sessionProgress) - penalty;
		
		sharedState.change("EmissionsYearTarget", country.obj.getID(), yearTarget);
	}	
}
