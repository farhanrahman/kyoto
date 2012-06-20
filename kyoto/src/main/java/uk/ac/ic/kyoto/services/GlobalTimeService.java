package uk.ac.ic.kyoto.services;

import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.Event;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import com.google.inject.Inject;

/**
 * 
 * @author stuart
 *
 */

public class GlobalTimeService extends EnvironmentService {
	
	public static String name = "GlobalTime";
	
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
		System.out.println("updateTickCounter called. SimTime: "+SimTime.get().intValue());
		if (SimTime.get().intValue() - getCurrentYear() * ticksInYear == ticksInYear) {
			System.out.println("END OF YEAR "+getCurrentYear());
			TimeToMonitor m = new TimeToMonitor(SimTime.get().intValue());
			eb.publish(m);
		}
		if (SimTime.get().intValue() % getTicksInYear() == 0) {
			System.out.println("END OF YEAR "+(getCurrentYear()-1));
			EndOfYearCycle y = new EndOfYearCycle(getCurrentYear()-1);
			eb.publish(y);
		}
		System.out.println("updateTickCounter returning. SimTime: "+SimTime.get().intValue());
	}
	
	@EventListener
	public void updateYearCounter (EndOfTimeCycle e) {
		if (SimTime.get().intValue() % getTicksInYear() == 0) {
			System.out.println("end of the year");
			sharedState.changeGlobal("YearCount", getCurrentYear());
			if (getCurrentYear() - getCurrentSession() * yearsInSession == yearsInSession) {
				System.out.println("end of the session");
				EndOfSessionCycle s = new EndOfSessionCycle(getCurrentSession());
				eb.publish(s);
			}
		}
	}
	
	@EventListener
	public void updateSessionCounter (EndOfSessionCycle e) {
		sharedState.changeGlobal("SessionCount", getCurrentSession());
	}
	
	//================================================================================
    // Events
    //================================================================================
	
	public class EndOfYearCycle implements Event {
		final int endedYear;
		
		EndOfYearCycle(int yearCounter) {
			this.endedYear = yearCounter;
		}
		
		public int getEndedYear() {
			return endedYear;
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
		return (int) Math.floor((double)getCurrentTick()/(getTicksInYear()));
	}
	
	public int getCurrentSession() {
		return (int) Math.floor((double)getCurrentTick()/(getYearsInSession()*getTicksInYear()));
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
