package uk.ac.ic.kyoto.countries;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.messaging.Input;

public class TestCountry extends AbstractCountry {

	public TestCountry(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			long availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput, long carbonOutput) {
			
			super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, availableToSpend,
			emissionsTarget, carbonOffset, energyOutput, carbonOutput);
			
			this.carbonAbsorptionHandler = new CarbonAbsorptionHandler();
			this.carbonReductionHandler = new CarbonReductionHandler();
	}


	@Override
	protected void processInput(Input input) {
		
	}


	@Override
	public void YearlyFunction() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void SessionFunction() {
		// TODO Auto-generated method stub
		
	}

}
