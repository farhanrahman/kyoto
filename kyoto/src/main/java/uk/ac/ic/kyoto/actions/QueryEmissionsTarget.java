package uk.ac.ic.kyoto.actions;

import uk.ac.imperial.presage2.core.Action;

/*
 * Action object for querying session targets from the CarbonTarget service. 
 * 
 * Author: Jonathan Ely
 */

public class QueryEmissionsTarget implements Action {

	private long emmisionsTarget = 0;
	
	public QueryEmissionsTarget() {
	}
	
	public long getEmissionsTarget() {
		return this.emmisionsTarget;
	}
	
	public void setEmissionsTarget(long target) {
		this.emmisionsTarget = target;
	}
}
