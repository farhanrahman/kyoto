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
		@SuppressWarnings("unchecked")
		Map<Integer, Double> s = (Map<Integer,Double>) this.sharedState.get("Report", id);
		
		for(Integer key : s.keySet()){
			logger.info("ATTIME= "+simTime.toString()+" ID= " + id + " Key: " + key + " Value: " + s.get(key) + "\n");
			//logger.info("ATTIME= "+simTime.toString()+" ID= " + id + " Key: " + 1 + " Value: " + s.get(new IntegerTime(1)) + "\n");
		}
		
		System.out.println();
		
		this.sharedState.change("Report", id, new StateTransformer(){
			@Override
			public Serializable transform(Serializable state) {
				@SuppressWarnings("unchecked")
				Map<Integer,Double> s = (Map<Integer,Double>) state;
				s.put(simTime.intValue(), carbonEmission);
				return (Serializable) s;
			}
		});
	}
}
