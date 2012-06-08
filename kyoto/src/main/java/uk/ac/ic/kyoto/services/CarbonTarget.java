package uk.ac.ic.kyoto.services;

import java.util.ArrayList;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.services.TimeService.EndOfSessionCycle;
import uk.ac.ic.kyoto.services.TimeService.EndOfYearCycle;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;

import com.google.inject.Inject;

/**
 *  Environment service for setting of carbon targets. Queried by countries via an action.
 * 
 *  Formula can be found on GoogleDrive.
 *  
 *  @author Jonathan Ely
 */

public class CarbonTarget extends EnvironmentService {

	private class countryObject  {
		public UUID countryID;
		public String countryName;
		
		public countryObject(UUID id, String name) {
			this.countryID = id;
			this.countryName = name;
		}
	}
	
	private ArrayList<countryObject> participantCountries= new ArrayList<countryObject>();
	private int session = 0;
	private long lastSessionTotalEmissions = 0;
	
	private EventBus eb;
	private TimeService TimeService;
	
	@Inject
	protected CarbonTarget(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider provider) {
		super(sharedState);
		try {
			this.TimeService = provider.getEnvironmentService(TimeService.class);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to get environment service 'TimeService'.");
			e.printStackTrace();
		}
	}

	/*
	 * On registration of Participant, calculate emissions targets.
	 * 
	 * (non-Javadoc)
	 * @see uk.ac.imperial.presage2.core.environment.EnvironmentService#registerParticipant(uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest)
	 */
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		super.registerParticipant(req);
		
		AbstractCountry agent = (AbstractCountry) req.getParticipant();
		countryObject countryDetails = new countryObject(agent.getID(), agent.getName());
		this.participantCountries.add(countryDetails);
		
		generateSessionTarget(countryDetails);
		sharedState.create("EmissionsSessionTarget", countryDetails.countryID, target);
	}
	
	@Inject
	public void setEB(EventBus eb) {
		this.eb = eb;
		eb.subscribe(this);
	}
	
	/*
	 * Function that is triggered by the end of session event.
	 */
	@EventListener
	public void updateSessionTarget(EndOfSessionCycle e)
	{
		/*
		 *  !! Needs to be implemented !!
		 *  
		 *  Work out end of session total emissions
		 */
		this.lastSessionTotalEmissions = 1000;
		
		for (countryObject country : participantCountries) {
			generateSessionTarget(country);
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
	
	/*
	 * Generates end of session target (binding) from 1990 data
	 */
	private void generateSessionTarget(countryObject country)
	{
		/*
		 *  !! Data needs to be loaded from somewhere  !!
		 */
		long emissons1990 = 100;	
		double proportionOfWorldEmissions = 0.25;
		
		long sessionTarget;
		if (TimeService.getCurrentSession() == 0) {
			sessionTarget = emissons1990 * (long) 0.95;
		} else {
			sessionTarget = ( (long) proportionOfWorldEmissions * this.lastSessionTotalEmissions) * (long) 0.95;
		}
		
		sharedState.change("EmissionsSessionTarget", country.countryID, sessionTarget);
	}
	
	/*
	 * Generates end of year target (non binding) based on last reported emissions and session target.
	 */
	private void generateYearTarget(countryObject country)
	{
		int currentYear = TimeService.getCurrentYear();
		int yearsInSession = TimeService.getYearsInSession();

		/*
		 *  !! This data needs to be loaded from somewhere  !!
		 */		
		long emissions1990 = 100; 
		double proportionOfWorldEmissions = 0.3;
		
		long yearTarget;
		if (currentYear == 0)
		{
			yearTarget = (long) (emissions1990 * (1 - (currentYear % yearsInSession) * (0.05 / yearsInSession)));
		} else {
			yearTarget = (long) (( (long) proportionOfWorldEmissions * this.lastSessionTotalEmissions) * 
									(1 - (currentYear % yearsInSession) * (0.05 / yearsInSession)));
		}
	
		sharedState.change("EmissionsYearTarget", country.countryID, yearTarget);
	}	
	
	public long querySessionTarget(UUID country) {
		Long state = (Long) sharedState.get("EmissionsSessionTarget", country);
		return state.longValue();
	}
	
	public long queryYearTarget(UUID country) {
		Long state = (Long) sharedState.get("EmissionsYearTarget", country);
		return state.longValue();
	}
	
}
