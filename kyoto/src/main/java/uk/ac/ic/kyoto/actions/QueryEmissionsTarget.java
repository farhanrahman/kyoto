package uk.ac.ic.kyoto.actions;

import uk.ac.imperial.presage2.core.Action;

/**
 * Action object for querying session targets from the CarbonTarget service. 
 * 
 * @author Jonathan Ely
 */

public class QueryEmissionsTarget implements Action {

	public enum TargetPeriodType {
		SESSION, YEAR;
	}

	private TargetPeriodType TargetPeriod;
	private double emmisionsTarget = 0;
	
	public QueryEmissionsTarget(TargetPeriodType Period) {
		this.TargetPeriod = Period;
	}
	
	public TargetPeriodType getTargetPeriod(){
		return this.TargetPeriod;
	}
	
	public double getEmissionsTarget() {
		return this.emmisionsTarget;
	}
	
	public void setEmissionsTarget(double target) {
		this.emmisionsTarget = target;
	}
}
