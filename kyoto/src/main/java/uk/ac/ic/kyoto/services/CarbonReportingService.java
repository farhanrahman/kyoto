package uk.ac.ic.kyoto.services;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.StateTransformer;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

/**
 * 
 * @author farhanrahman, Stuart
 */
public class CarbonReportingService extends EnvironmentService {

	Logger logger = Logger.getLogger(CarbonReportingService.class);
	
	public static String name = "Report";
	
	@Inject
	protected CarbonReportingService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req){
		super.registerParticipant(req);
	}

	/**
	 * Allows ActionHandlers or Actions
	 * to update the shared state (report)
	 * of a particular participant having
	 * a unique UUID.
	 * @param id
	 * @param carbonEmission
	 * @param simTime
	 */
	public void updateReport(final UUID id, final Double carbonEmission, final Time simTime){
		this.sharedState.change(name, id, new StateTransformer(){
			@Override
			public Serializable transform(Serializable state) {
				@SuppressWarnings("unchecked")
				Map<Integer,Double> s = (Map<Integer,Double>) state;
				s.put(simTime.intValue(), carbonEmission);
				return (Serializable) s;
			}
		});
		
		/*@SuppressWarnings("unchecked")
		Map<Integer, Double> s = (Map<Integer,Double>) this.sharedState.get("Report", id);
		
		for(Integer key : s.keySet()){
			logger.info("ATTIME= "+simTime.toString()+" ID= " + id + " Key: " + key + " Value: " + s.get(key) + "\n");
		}
		
		System.out.println();*/
	}
	
	/**
	 * Returns null if report
	 * does not exist for a participant
	 * at all or there is no report for
	 * participant at simulation time simTime
	 * @param id
	 * @param simTime
	 * @return
	 */
	public Double getReport(UUID id, Time simTime) {
		return this.getReport(id, simTime.intValue());
	}
	
	
	/**
	 * This function should be used if a service
	 * wants to get a report from the past. The
	 * reason for this is that Time cannot be decremented
	 * which becomes an issue when querying for report.
	 * @param id
	 * @param simTime
	 * @return The report of a participant
	 */
	public Double getReport(UUID id, Integer simTime) {
		try{
			@SuppressWarnings("unchecked")
			Map<Integer,Double> reportForParticipant = 
								(Map<Integer,Double>)this.sharedState.get(
											CarbonReportingService.name, 
											id);
			if(reportForParticipant == null){
				throw new NullPointerException("No report for participant " + id);
			}else{
				return reportForParticipant.get(simTime);
			}
		}catch(ClassCastException e){
			logger.warn(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns null if report does
	 * not exist for participant at all
	 * @param id
	 * @return
	 */
	public Map<Integer, Double> getReport(UUID id) {
		try{
			@SuppressWarnings("unchecked")
			Map<Integer,Double> reportForParticipant = 
								(Map<Integer,Double>)this.sharedState.get(
											CarbonReportingService.name, 
											id);
			if(reportForParticipant == null){
				throw new NullPointerException("No report for participant " + id);
			}else{
				return ImmutableMap.copyOf(reportForParticipant);
			}
		}catch(ClassCastException e){
			logger.warn(e);
			throw new RuntimeException(e);
		}
	}	
}
