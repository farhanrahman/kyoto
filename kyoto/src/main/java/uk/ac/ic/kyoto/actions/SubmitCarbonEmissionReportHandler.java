package uk.ac.ic.kyoto.actions;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import com.google.inject.Inject;

/**
 * ActionHandler for CarbonEmissionReport Service
 * 
 * @author farhanrahman
 */
@ServiceDependencies({CarbonReportingService.class})
public class SubmitCarbonEmissionReportHandler implements ActionHandler{
	private Logger logger = Logger.getLogger(SubmitCarbonEmissionReportHandler.class);
	final protected CarbonReportingService crs;
	
	@Inject
	public SubmitCarbonEmissionReportHandler(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider environment) throws UnavailableServiceException{
		//this.sharedState = sharedState;
		this.crs = environment.getEnvironmentService(CarbonReportingService.class);
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof SubmitCarbonEmissionReport;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		if(action instanceof SubmitCarbonEmissionReport){
			SubmitCarbonEmissionReport reportAction = (SubmitCarbonEmissionReport) action;
			synchronized(crs){
				this.crs.updateReport(actor, reportAction.getCarbonEmission(), reportAction.getSimTime());
			}
			logger.info("REPORT HERE, TIME: " + reportAction.getSimTime() + " AND EMISSION: " + reportAction.getCarbonEmission());
			return null;
		}
		throw new ActionHandlingException("Action not recognized (From SubmitCarbonEmissionReportHandler)");
	}

}
