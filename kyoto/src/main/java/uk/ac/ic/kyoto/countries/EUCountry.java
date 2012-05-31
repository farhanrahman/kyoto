package uk.ac.ic.kyoto.countries;

import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.messaging.Input;

/**
 * Extends AbstractCountry to provide additional functions for EU countries
 * All EU countries MUST inherit this
 * @author Nik
 *
 */
abstract public class EUCountry extends AbstractCountry {
	
	public EUCountry(UUID id, String name, double landArea, double arableLandArea, double GDP,
			double GDPRate, double dirtyIndustry, double emissionsTarget, long carbonOffset,
			float availableToSpend, long carbonTraded) {
		
		super(id, name, landArea, arableLandArea, GDP,
					GDPRate, dirtyIndustry, emissionsTarget, carbonOffset,
					availableToSpend, carbonTraded);
		
		memberStates.add(this);
	}

	@Override
	abstract protected void processInput(Input arg0);

	@Override
	abstract public void execute();
	
	static private Set<EUCountry> memberStates;
	
	static protected void allocateCredits() {
		for (int i = 0; i < memberStates.size(); i++) {
			
		}
	}


}
