package uk.ac.ic.kyoto.services;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.Event;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.Parameter;

/** 
 * @author sc1109 & azyzio
 */
public class TimeService extends EnvironmentService {

	private Time tickCounter;
	private Time yearCounter;
	private Time sessionCounter;
	
	@Parameter(name="ticksInYear")
	int ticksInYear;
	
	@Parameter(name="yearsInSession")
	int yearsInSession;
	
	
	protected TimeService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	@EventListener
	public void updateTickCounter (EndOfTimeCycle e) {
		tickCounter.increment();
		if (getCurrentTick() == ticksInYear) {
			EndOfYearCycle y = new EndOfYearCycle(yearCounter);
		}
	}
	
	@EventListener
	public void updateYearCounter (EndOfYearCycle e) {
		yearCounter.increment();
		if (yearCounter.intValue() == yearsInSession) {
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
		return tickCounter.intValue() - yearCounter.intValue() * ticksInYear;
	}
	
	public int getCurrentYear() {
		return yearCounter.intValue() - sessionCounter.intValue() * yearsInSession;
	}
	
	public int getCurrentSession() {
		return sessionCounter.intValue();
	}
	
	public int getTicksInYear() {
		return ticksInYear;
	}
	
	public int getYearsInSession() {
		return yearsInSession;
	}
}
