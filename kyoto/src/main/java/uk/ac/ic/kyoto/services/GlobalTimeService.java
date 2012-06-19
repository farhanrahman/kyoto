package uk.ac.ic.kyoto.services;

import com.google.inject.Inject;

import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.Event;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.SimTime;

/**
 * 
 * @author stuart
 *
 */

public class GlobalTimeService extends EnvironmentService {
	
	public static String name = "GlobalTime";
	
	private volatile int tickCounter=0, yearCounter=0, sessionCounter=0;

	//@Parameter(name="ticksInYear")
	public int ticksInYear=GameConst.getTicksInYear();
	
	//@Parameter(name="yearsInSession")
	public int yearsInSession=GameConst.getYearsInSession();
	
	EventBus eb;
	
	@Inject
	public GlobalTimeService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
		sharedState.changeGlobal("TicksInYear", ticksInYear);
		sharedState.changeGlobal("YearsInSession", yearsInSession);
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
	
	@EventListener
	public void updateTickCounter (EndOfTimeCycle e) {
		tickCounter++;
		if (SimTime.get().intValue() - yearCounter * ticksInYear == ticksInYear-2) {
			TimeToMonitor m = new TimeToMonitor(tickCounter);
			eb.publish(m);
		}
		if (SimTime.get().intValue() - yearCounter * ticksInYear == ticksInYear-1) {
			EndOfYearCycle y = new EndOfYearCycle(yearCounter);
			eb.publish(y);
		}
	}
	
	@EventListener
	public void updateYearCounter (EndOfYearCycle e) {
		yearCounter++;
		sharedState.changeGlobal("YearCount", yearCounter);
		if (yearCounter - sessionCounter * yearsInSession == yearsInSession) {
			EndOfSessionCycle s = new EndOfSessionCycle(sessionCounter);
			eb.publish(s);
		}
	}
	
	@EventListener
	public void updateSessionCounter (EndOfSessionCycle e) {
		sessionCounter++;
		sharedState.changeGlobal("SessionCount", sessionCounter);
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
	
	public class TimeToMonitor implements Event {
		final int monitorTick;
		
		TimeToMonitor(int tickCounter) {
			this.monitorTick = tickCounter;
		}
	}
	
	public int getCurrentYear() {
		return yearCounter;
	}
	
	public int getCurrentSession() {
		return sessionCounter;
	}
	
	
	/**
	 * 
	 * @param year - the year you want the information for
	 * @return - the tick number of the first turn in the year AFTER
	 */
	public int yearToSimTime(int year) {
		return (ticksInYear * year);
	}
	
	public int getCurrentTick() {		
		return SimTime.get().intValue();
	}
	
	public int getTicksInYear() {
		return ticksInYear;
	}
	
	public int getYearsInSession() {
		return yearsInSession;
	}

}
