package uk.ac.ic.kyoto.actions;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.Action;

public class AddRemoveFromMonitor implements Action {
	
	public enum addRemoveType { ADD, REMOVE };
	
	final public AbstractCountry country;
	final public addRemoveType actionToImplement;
	
	public AddRemoveFromMonitor(AbstractCountry country, addRemoveType action) {
		this.country = country;
		this.actionToImplement = action;
	}
}
