package uk.ac.ic.kyoto.services;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.Event;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

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
			EndOfYear y = new EndOfYear(yearCounter);
		}
	}
	
	@EventListener
	public void updateYearCounter (EndOfYear e) {
		yearCounter.increment();
		if (yearCounter.intValue() == YEARS_IN_SESSION) {
			EndOfSession s = new EndOfSession(sessionCounter);	
		}
	}
	
	@EventListener
	public void updateSessionCounter (EndOfSession e) {
		sessionCounter.increment();
	}

	//================================================================================
    // Events
    //================================================================================
	
	public class EndOfYear implements Event {
		final Time endedYear;
		
		EndOfYear(Time yearCounter) {
			this.endedYear = yearCounter;
		}
	}
	
	public class EndOfSession implements Event {
		final Time endedSession;
		
		EndOfSession(Time endedSession) {
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
