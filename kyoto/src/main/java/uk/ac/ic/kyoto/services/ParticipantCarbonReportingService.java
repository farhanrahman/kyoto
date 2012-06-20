package uk.ac.ic.kyoto.services;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;

/**
 * @author farhanrahman
 *
 */
public class ParticipantCarbonReportingService extends EnvironmentService {

	Logger logger = Logger.getLogger(ParticipantCarbonReportingService.class);
	
	/**
	 * @param sharedState
	 */
	@Inject
	public ParticipantCarbonReportingService(
			EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	
	/**
	 * Helper method to create shared state for participant
	 * @param name
	 * @param data
	 * @param participantID
	 * @return
	 */
	public static ParticipantSharedState createSharedState(Map<?,?> data, UUID participantID){
		return new ParticipantSharedState(CarbonReportingService.name, (Serializable) data, participantID);	
	}
	
	@Deprecated
	public Map<UUID, Map<Integer,Double>> getAllReports(){
		Map<UUID,Map<Integer,Double>> allReports = new HashMap<UUID,Map<Integer,Double>>();
		//need to get a list of all the participants
		//TODO Should this return ImmutableMap?
		return allReports;
	}
	
	/**
	 * Method for getting report for
	 * participant with ID = participantID
	 * @param participantId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer,Double> getReportFor(UUID participantId){
		Map<Integer,Double> report = new HashMap<Integer,Double>();
		Serializable data = this.sharedState.get(CarbonReportingService.name, participantId);
		try{
			report = (Map<Integer,Double>) data;
			return ImmutableMap.copyOf(report);
		}catch(ClassCastException e){
			logger.warn(e);
			throw new NullPointerException();
		}

	}

}
