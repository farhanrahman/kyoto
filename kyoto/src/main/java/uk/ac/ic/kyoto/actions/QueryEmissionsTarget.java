package uk.ac.ic.kyoto.actions;

import uk.ac.imperial.presage2.core.Action;

/**
 * Action object for querying session targets from the CarbonTarget service. 
 * 
 * @author Jonathan Ely
 */

public class QueryEmissionsTarget implements Action {

	public enum targetType {
		SESSION, YEAR;
	}

	private targetType TargetPeriod;
	private double emmisionsTarget = 0;
	
	public QueryEmissionsTarget(targetType Period) {
		this.TargetPeriod = Period;
	}
	
	public targetType getTargetPeriod(){
		return this.TargetPeriod;
	}
	
	public double getEmissionsTarget() {
		return this.emmisionsTarget;
	}
	
	public void setEmissionsTarget(double target) {
		this.emmisionsTarget = target;
	}
}
