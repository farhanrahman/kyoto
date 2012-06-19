package uk.ac.ic.kyoto.annex1reduce;

import org.junit.*;

import java.util.UUID;

public class AnnexOneReduceTest {

	static private AnnexOneReduce test;

	@BeforeClass
	public static void initEUCountry() {
		System.out.println("Initialising EUCountry");

		double landArea = 1000000;
		double arableLandArea = 0.5 * landArea;
		double GDP = 10000000;
		double GDPRate = 0.03;
		double energyOutput = 1000000;
		double carbonOutput = 900000;
		double availableToSpend = GDP * 0.1;
		double emissionsTarget = 0.99 * carbonOutput;
		
		test = new AnnexOneReduce(UUID.randomUUID(), "JunitTest", "JUT",
				landArea, arableLandArea, GDP, GDPRate, energyOutput,
				carbonOutput);
		// test = new AnnexOneReduce(UUID.randomUUID(), "JunitTest",
		// "JUT",1000000,500000,1000000,1000000,1000000,700000);
		test.setAvailableToSpend(availableToSpend);
		test.setEmissionsTarget(emissionsTarget);

		test.initialise();
	}

	@Test
	public void testbehaviour() {

		test.behaviour();

	}

}
