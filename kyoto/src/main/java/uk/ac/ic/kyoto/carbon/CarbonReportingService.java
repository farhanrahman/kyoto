package uk.ac.ic.kyoto.carbon;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.StateTransformer;

import com.google.inject.Inject;


public class CarbonReportingService extends EnvironmentService {

	Logger logger = Logger.getLogger(CarbonReportingService.class);
	
	
	@Inject
	protected CarbonReportingService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req){
		super.registerParticipant(req);
	}

	public void updateReport(final UUID id, final Double carbonEmission, final Time simTime){
		/*@SuppressWarnings("unchecked")
		Map<Time, Double> s = (Map<Time,Double>) this.sharedState.get("Report", id);
		
		for(Time key : s.keySet()){
			logger.info("Key: " + key + " Value: " + s.get(key) + "\n");
		}*/
		
		
		
		this.sharedState.change("Report", id, new StateTransformer(){
			@Override
			public Serializable transform(Serializable state) {
				@SuppressWarnings("unchecked")
				Map<Time,Double> s = (Map<Time,Double>) state;
				s.put(simTime, carbonEmission);
				return (Serializable) s;
			}
		});
	}
}
