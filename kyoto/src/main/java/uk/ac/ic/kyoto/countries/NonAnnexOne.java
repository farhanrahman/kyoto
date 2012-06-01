package uk.ac.ic.kyoto.countries;

import java.util.UUID;

import uk.ac.imperial.presage2.core.messaging.Input;

public class NonAnnexOne extends AbstractCountry {

	public NonAnnexOne(UUID id, String name, String ISO, double landArea, double arableLandArea,
			double GDP,	double GDPRate, long emissionsTarget, long carbonOffset,
			float availableToSpend, long carbonTraded) {
		super(id, name, ISO, landArea, arableLandArea,
				GDP,GDPRate,emissionsTarget,carbonOffset,
				availableToSpend,carbonTraded);
	}

	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}

}
