package uk.ac.ic.kyoto.actions;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.Action;

/**
 * Action object for adding country to CarbonTarget service
 * 
 * @author Jonathan Ely
 */
public class AddToCarbonTarget implements Action {

	final public AbstractCountry countryObject;
	
	public AddToCarbonTarget(AbstractCountry country) {
		this.countryObject = country;
	}
}
