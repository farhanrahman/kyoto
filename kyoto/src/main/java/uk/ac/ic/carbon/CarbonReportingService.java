package uk.ac.ic.carbon;

import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
/**
 * Use this class to alter the Total Carbon reserve present in the environment
 * @author farhanrahman
 *
 */
public class CarbonReportingService extends EnvironmentService {

	protected CarbonReportingService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req){
		super.registerParticipant(req);
		//TODO extra stuff required when participant registers?
	}
	
	
	

}
