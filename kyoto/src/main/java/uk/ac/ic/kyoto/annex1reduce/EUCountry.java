package uk.ac.ic.kyoto.annex1reduce;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.messaging.Input;

/**
 * Extends AbstractCountry, provides a skeleton for all EU member countries
 * @author Nik
 *
 */
public class EUCountry extends AbstractCountry {
	
	public EUCountry(UUID id, String name, double landArea, double arableLandArea, double GDP,
			double GDPRate, double dirtyIndustry, double emissionsTarget, long carbonOffset,
			float availableToSpend, long carbonTraded) {
		
		super(id, name, landArea, arableLandArea, GDP,
					GDPRate, dirtyIndustry, emissionsTarget, carbonOffset,
					availableToSpend, carbonTraded);
		
		EU.addMemberState(this);
	}

	@Override
	protected void processInput(Input arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
