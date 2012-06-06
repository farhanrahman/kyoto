package uk.ac.ic.kyoto.nonannexone;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.messaging.Input;

public class NonAnnexOne extends AbstractCountry {


	public NonAnnexOne(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput, long carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, availableToSpend, emissionsTarget, carbonOffset,
				energyOutput, carbonOutput);
	}

	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub - what does this mean?
		
		//write some behaviors pseudocode
	}

}
