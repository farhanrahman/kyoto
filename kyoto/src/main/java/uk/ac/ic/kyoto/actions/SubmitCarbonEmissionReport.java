package uk.ac.ic.kyoto.actions;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * 
 * @author farhanrahman
 */
public class SubmitCarbonEmissionReport implements Action {
	
	final private Double carbonEmission;	

	final private Time simTime;
	
	final AbstractParticipant participant;
	
	public SubmitCarbonEmissionReport(final Double carbonEmission, final Time simTime, final AbstractParticipant participant){
		this.carbonEmission = carbonEmission;
		this.participant = participant;
		this.simTime = simTime;
	}
	
	@Override
	public String toString() {
		return "Report submission [player=" + participant.getID() + ", carbon emission reported="
				+ this.carbonEmission+ "]";
	}

	public Time getSimTime() {
		return simTime;
	}
	
	public Double getCarbonEmission() {
		return carbonEmission;
	}
	
}