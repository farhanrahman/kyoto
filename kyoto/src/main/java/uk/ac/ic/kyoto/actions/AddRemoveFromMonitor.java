package uk.ac.ic.kyoto.actions;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.Action;

/**
 * Action object for adding country to Monitor service
 * 
 * @author Stuart Holland, Jonathan Ely
 */
public class AddRemoveFromMonitor implements Action {
	
	public enum addRemoveType { ADD, REMOVE };
	
	final public AbstractCountry country;
	final public addRemoveType actionToImplement;
	
	public AddRemoveFromMonitor(AbstractCountry country, addRemoveType action) {
		this.country = country;
		this.actionToImplement = action;
	}
}
