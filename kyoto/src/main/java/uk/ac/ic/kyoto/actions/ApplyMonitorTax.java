package uk.ac.ic.kyoto.actions;

import uk.ac.imperial.presage2.core.Action;

public class ApplyMonitorTax implements Action {

	final public double amount;
	
	public ApplyMonitorTax(double amount) {
		this.amount = amount;
	}
	
}
