package uk.ac.ic.kyoto.services;

import java.util.UUID;

import uk.ac.ic.kyoto.services.TimeService.EndOfSessionCycle;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.event.EventListener;

/**
 * <p>Environment service for setting of carbon targets. Queried by countries via an action.git</p><br>
 * <p>
 * Formula:<br>
 * <br>
 * 	Assuming 5% drop per session<br>
 * 	total1990Emission = sum(existing countries’ emissions in 1990)<br>
 * <br>
 * 	for each country {<br>
 * 		firstSessionTarget = 1990emission*(1 - 5%)<br>
 * 		yearTarget = 1990emission *(1 - (yearNumber % sessionLength)*(5% / sessionLength))<br>
 * 		proportionOfWorldEmissions = 1990emission / total1990Emission<br>
 * 	}<br>
 * <br>
 * 	endofSessionEmissions = sum(all reported emissions at end of session)<br>
 * 	<br>
 *  for each country {<br>
 *  	nextSessionTarget = (proportionOfWorldEmissions * endofSessionEmissions)*(1 - 5%)<br>
 * 		nextYearTarget = (proportionOfWorldEmissions * endofSessionEmissions)*(1 - (yearNumber % sessionLength)*(5% / sessionLength)) <br>
 *  }<br>
 * </p>
 *  @author Jonathan Ely
 */

public class CarbonTarget extends EnvironmentService {

	protected CarbonTarget(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
		// TODO Auto-generated constructor stub
	}

	/*
	 * On registration of Participant, calculate emissions target.
	 * 
	 * (non-Javadoc)
	 * @see uk.ac.imperial.presage2.core.environment.EnvironmentService#registerParticipant(uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest)
	 */
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		// TODO Auto-generated method stub
		super.registerParticipant(req);
		
		// Load country data
		UUID country = req.getParticipantID();
		int data1990 = 0; 
		
		// Get target
		int target = generateSessionTarget(country, data1990, 0);
		
		// Save target to shared state
	}
	
	/*
	 * Function that is triggered by the end of session event.
	 * 
	 * Loops through participants updating saved session target
	 */
	@EventListener
	public void updateSessionTarget(EndOfSessionCycle e)
	{
		// Loop through participants
		
		// Generate new target
		
		// Save target to shared state

	}
	
	/*
	 * Generates end of session target (binding) from 1990 data
	 */
	private int generateSessionTarget(UUID country, int data1990, int Session)
	{
		
		return 0;
		
	}
	
	/*
	 * Generates end of year target (non binding) based on last reported emissions and session target.
	 */
	private int generateYearTarget()
	{
		return 0;
		
	}	
	
}