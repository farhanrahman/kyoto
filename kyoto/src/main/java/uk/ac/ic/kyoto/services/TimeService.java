package uk.ac.ic.kyoto.services;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.Event;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

/**
 * Stores all messages (trade and investment), and automagically
 * computes high, low & average.
 * 
 * @author sc1109 & azyzio
 */
public class TimeService extends EnvironmentService {

	private Time tickCounter;
	private Time yearCounter;
	private Time sessionCounter;
	
	final static int TICKS_IN_YEAR = 365;
	final static int YEARS_IN_SESSION = 10;
	
	protected TimeService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	@EventListener
	public void updateTickCounter (EndOfTimeCycle e) {
		tickCounter.increment();
		if (getCurrentTick() == TICKS_IN_YEAR) {
			EndOfYearCycle y = new EndOfYearCycle(yearCounter);
		}
	}
	
	@EventListener
	public void updateYearCounter (EndOfYearCycle e) {
		yearCounter.increment();
		if (yearCounter.intValue() == YEARS_IN_SESSION) {
			EndOfSessionCycle s = new EndOfSessionCycle(sessionCounter);	
		}
	}
	
	@EventListener
	public void updateSessionCounter (EndOfSessionCycle e) {
		sessionCounter.increment();
	}

	//================================================================================
    // Events
    //================================================================================
	
	public class EndOfYearCycle implements Event {
		final Time endedYear;
		
		EndOfYearCycle(Time yearCounter) {
			this.endedYear = yearCounter;
		}
	}
	
	public class EndOfSessionCycle implements Event {
		final Time endedSession;
		
		EndOfSessionCycle(Time endedSession) {
			this.endedSession = endedSession;
		}
	}
	
	//================================================================================
    // Public getters
    //================================================================================
	
	public int getCurrentTick() {
		return tickCounter.intValue() - yearCounter.intValue() * TICKS_IN_YEAR;
	}
	
	public int getCurrentYear() {
		return yearCounter.intValue() - sessionCounter.intValue() * YEARS_IN_SESSION;
	}
	
	public int getCurrentSession() {
		return sessionCounter.intValue();
	}
}
