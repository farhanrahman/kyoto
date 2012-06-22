package uk.ac.ic.kyoto.actions;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.simulator.SimTime;

/**
 * Action object for CarbonEmissionReport Service
 * 
 * @author farhanrahman
 */
public class SubmitCarbonEmissionReport implements Action {
	
	final private Double carbonEmission;	
	final private Time simTime;
	
	public SubmitCarbonEmissionReport(final Double carbonEmission){
		this.carbonEmission = carbonEmission;
		this.simTime = SimTime.get();
	}
	
	@Override
	public String toString() {
		return "Carbon emission reported = ["
				+ this.carbonEmission+ "]";
	}

	/**
	 * 
	 * @return simulation time when action was called
	 */
	public Time getSimTime() {
		return simTime;
	}
	
	/**
	 * 
	 * @return reported carbonEmission
	 */
	public Double getCarbonEmission() {
		return carbonEmission;
	}
	
}