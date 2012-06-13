package uk.ac.ic.kyoto.services;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.simulator.SimTime;

/** 
 * @author sc1109 & azyzio & Stuart
 */
public class ParticipantTimeService extends EnvironmentService {
	
	//@Parameter(name="ticksInYear")
	int ticksInYear;
	
	//@Parameter(name="yearsInSession")
	int yearsInSession;
	
	Logger logger = Logger.getLogger(ParticipantTimeService.class);
	
	GlobalTimeService globalTimeService;
	
	@Inject
	public ParticipantTimeService(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider provider) {
		super(sharedState);
		try {
			globalTimeService = provider.getEnvironmentService(GlobalTimeService.class);
		} catch (UnavailableServiceException e) {
			logger.warn(e.getMessage(), e);
			e.printStackTrace();
		}
		ticksInYear = globalTimeService.ticksInYear;
		yearsInSession = globalTimeService.yearsInSession;
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		super.registerParticipant(req);
	}
	
	//================================================================================
    // Public getters
    //================================================================================
	
	public int getCurrentTick() {		
		return SimTime.get().intValue();
	}
	
	public int getCurrentYear() {
		return globalTimeService.getYear();
	}
	
	public int getCurrentSession() {
		return globalTimeService.getSession();
	}
	
	public int getTicksInYear() {
		return ticksInYear;
	}
	
	public int getYearsInSession() {
		return yearsInSession;
	}
}
