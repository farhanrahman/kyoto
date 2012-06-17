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

	public Time getSimTime() {
		return simTime;
	}
	
	public Double getCarbonEmission() {
		return carbonEmission;
	}
	
}