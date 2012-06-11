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
public class TimeService extends EnvironmentService implements TimeDriven {

	private int tickCounter=0;
	private int yearCounter=0;
	private int sessionCounter=0;
	
	EventBus eb;
	
	//@Parameter(name="ticksInYear")
	int ticksInYear=365;
	
	//@Parameter(name="yearsInSession")
	int yearsInSession=10;
	
	@Inject
	public TimeService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	@Inject
	public void setEB(EventBus eb) {
		this.eb = eb;
		eb.subscribe(this);
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		super.registerParticipant(req);
	}
	
	@Override
	public void incrementTime() {
		tickCounter++;
		if (getCurrentTick() == ticksInYear) {
			EndOfYearCycle y = new EndOfYearCycle(yearCounter);
			eb.publish(y);
		}
	}
	
//	@EventListener
//	public void updateTickCounter (EndOfTimeCycle e) {
//		tickCounter++;
//		if (getCurrentTick() == ticksInYear) {
//			EndOfYearCycle y = new EndOfYearCycle(yearCounter);
//			eb.publish(y);
//		}
//	}
	
	@EventListener
	public void updateYearCounter (EndOfYearCycle e) {
		yearCounter++;
		if (yearCounter == yearsInSession) {
			EndOfSessionCycle s = new EndOfSessionCycle(sessionCounter);
			eb.publish(s);
		}
	}
	
	@EventListener
	public void updateSessionCounter (EndOfSessionCycle e) {
		sessionCounter++;
	}

	//================================================================================
    // Events
    //================================================================================
	
	public class EndOfYearCycle implements Event {
		final int endedYear;
		
		EndOfYearCycle(int yearCounter) {
			this.endedYear = yearCounter;
		}
	}
	
	public class EndOfSessionCycle implements Event {
		final int endedSession;
		
		EndOfSessionCycle(int endedSession) {
			this.endedSession = endedSession;
		}
	}
	
	//================================================================================
    // Public getters
    //================================================================================
	
	public int getCurrentTick() {
		return tickCounter - yearCounter * ticksInYear;
	}
	
	public int getCurrentYear() {
		return yearCounter - sessionCounter * yearsInSession;
	}
	
	public int getCurrentSession() {
		return sessionCounter;
	}
	
	public int getTicksInYear() {
		return ticksInYear;
	}
	
	public int getYearsInSession() {
		return yearsInSession;
	}
}
