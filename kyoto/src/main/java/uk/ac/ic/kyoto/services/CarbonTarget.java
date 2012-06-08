package uk.ac.ic.kyoto.services;

import java.util.ArrayList;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.services.TimeService.EndOfSessionCycle;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventListener;

/**
 * <p>Environment service for setting of carbon targets. Queried by countries via an action.</p>
 * 
 * <p><b>Formula:</b></p>
 * 
 * <p>
 * Assuming 5% drop per session<br />
 * total1990Emission = sum(existing countries’ emissions in 1990)<br />
 * </p>
 * <p>
 * <b>For each country :</b><br />
 * firstSessionTarget = 1990emission*(1 - 5%)<br />
 * yearTarget = 1990emission *(1 - (yearNumber % sessionLength)*(5% / sessionLength))<br />
 * proportionOfWorldEmissions = 1990emission / total1990Emission<br />
 * </p>
 * 
 * <p>endofSessionEmissions = sum(all reported emissions at end of session)</p>
 * 
 * <p>
 *  <b>For each country :</b><br />
 *  nextSessionTarget = (proportionOfWorldEmissions * endofSessionEmissions)*(1 - 5%)<br />
 *  nextYearTarget = (proportionOfWorldEmissions * endofSessionEmissions)*(1 - (yearNumber % sessionLength)*(5% / sessionLength)) <br />
 *  </p>
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
	
	protected CarbonTarget(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}

	/*
	 * On registration of Participant, calculate emissions target.
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
		
		// Get target
		long target = generateSessionTarget(countryDetails, 0);
				
		// Save target to shared state
		sharedState.create("EmissionsTarget", countryDetails.countryID, target);
	}
	
	/*
	 * Function that is triggered by the end of session event.
	 * 
	 * Loops through participants updating saved session target
	 */
	@EventListener
	public void updateSessionTarget(EndOfSessionCycle e)
	{
		// Increment session
		session++;
		
		// Loop through countries updating targets
		for (countryObject country : participantCountries) {
			
			// Generate new emissions target
			long newTarget = generateSessionTarget(country, session);
			
			// Save target to shared state
			sharedState.change("EmissionsTarget", country.countryID, newTarget);
		}
	}
	
	/*
	 * Generates end of session target (binding) from 1990 data
	 */
	private int generateSessionTarget(countryObject country, int Session)
	{
		/*
		 * TO BE IMPLEMENTED
		 * 
		 * Data needs to be loaded from somewhere. MongoDB?
		 */
			
		return 0;
	}
	
	/*
	 * Generates end of year target (non binding) based on last reported emissions and session target.
	 */
	private int generateYearTarget(countryObject country)
	{
		/*
		 * TO BE IMPLEMENTED
		 */
			
		return 0;
	}	
	
	public long queryTarget(UUID country) {
		 Long state = (Long) sharedState.get("EmissionsTarget", country);
		return state.longValue();
	}
	
}
