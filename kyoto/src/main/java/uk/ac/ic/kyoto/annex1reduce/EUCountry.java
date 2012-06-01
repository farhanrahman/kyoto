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
	
	public EUCountry(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double emissionsTarget, long carbonOffset,
			float economicOutput) {
		
		super(id, name, ISO, landArea, arableLandArea, GDP,
					GDPRate, emissionsTarget, carbonOffset,
					economicOutput);
		
		EU.addMemberState(this);
	}

	/**
	 * Take an input and process the data.
	 * May or may not be used
	 */
	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub
		
	}

}
>>>>>>> development
