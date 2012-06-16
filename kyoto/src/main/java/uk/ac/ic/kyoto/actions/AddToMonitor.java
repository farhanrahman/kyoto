package uk.ac.ic.kyoto.actions;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.Action;

/**
 * Action object for adding country to Monitor service
 * 
 * @author Stuart Holland
 */
public class AddToMonitor implements Action {
	
	public AbstractCountry country;
	
	public AddToMonitor(AbstractCountry country) {
		this.country = country;
	}
}
