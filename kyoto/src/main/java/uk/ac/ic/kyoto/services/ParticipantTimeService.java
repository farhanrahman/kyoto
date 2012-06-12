package uk.ac.ic.kyoto.services;

import com.google.inject.Inject;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.TimeDriven;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.Event;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.SimTime;

/** 
 * @author sc1109 & azyzio & Stuart
 */
public class ParticipantTimeService extends EnvironmentService {
	
	//@Parameter(name="ticksInYear")
	int ticksInYear;
	
	//@Parameter(name="yearsInSession")
	int yearsInSession;
	
	@Inject
	public ParticipantTimeService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		super.registerParticipant(req);
	}
	
	//================================================================================
    // Public getters
    //================================================================================
	
	public int getCurrentTick() {
		ticksInYear = (Integer) sharedState.getGlobal("TicksInYear");
		yearsInSession = (Integer) sharedState.getGlobal("YearsInSession");
		return SimTime.get().intValue();
	}
	
	public int getCurrentYear() {
		return (Integer) sharedState.getGlobal("YearCount");
	}
	
	public int getCurrentSession() {
		return (Integer) sharedState.getGlobal("SessionCount");
	}
	
	public int getTicksInYear() {
		return ticksInYear;
	}
	
	public int getYearsInSession() {
		return yearsInSession;
	}
}
