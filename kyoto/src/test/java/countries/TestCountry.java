package countries;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;

public class TestCountry extends AbstractCountry {

	public TestCountry(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			long availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput, long carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, availableToSpend,
				emissionsTarget, carbonOffset, energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}

}
