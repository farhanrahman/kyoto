package uk.ac.ic.kyoto.services;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.Event;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

public class TimeService extends EnvironmentService {

	int tickcounter=0;
	int yearyearcounter=0;
	int sessionyearcounter=0;
	int sessioncounter=0;
	
	final int ticksinayear=365;
	final int yearsinasession=10;
	
	protected TimeService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
		// TODO Auto-generated constructor stub
	}
	
	@EventListener
	public void yearEventGenerator (EndOfTimeCycle e) {
		tickcounter++;
		if (tickcounter == ticksinayear) {
				tickcounter=0;
				yearyearcounter++;
				EndOfYear y = new EndOfYear(yearyearcounter);	
		}
	}
	
	@EventListener
	public void sessionEventGenerator (EndOfYear e) {
		sessionyearcounter++;
		if (sessionyearcounter == yearsinasession) {
				sessionyearcounter=0;
				sessioncounter++;
				EndOfSession s = new EndOfSession(sessioncounter);	
		}
	}
	
	public class EndOfYear implements Event {
		final int endedYear;
		
		EndOfYear(int endedYear) {
			this.endedYear = endedYear;
		}
	}
	
	public class EndOfSession implements Event {
		final int endedSession;
		
		EndOfSession(int endedSession) {
			this.endedSession = endedSession;
		}
	}

}
