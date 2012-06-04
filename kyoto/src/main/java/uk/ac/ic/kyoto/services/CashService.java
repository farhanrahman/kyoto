package uk.ac.ic.kyoto.services;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;

public class CashService extends EnvironmentService {

	Logger logger = Logger.getLogger(CashService.class);
	
	@Inject
	protected CashService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		super.registerParticipant(req);
	}
	
	public long updateCashReserves(long currentReserves) {
		long newValue;
		// formula for GDP growth rate, put value into newValue
		// It should be cumulative, currentReserves == 0 at 
		// start of simulation
		newValue = 0; // to make it compile
		
		return newValue;
	}

}
