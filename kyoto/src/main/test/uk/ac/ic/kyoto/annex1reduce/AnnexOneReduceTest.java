package uk.ac.ic.kyoto.annex1reduce;

import org.junit.*;

import uk.ac.ic.kyoto.countries.GameConst;

import java.util.UUID;

public class AnnexOneReduceTest {

	static private AnnexOneReduce test;

	@BeforeClass
	public static void initEUCountry() {
		System.out.println("Initialising EUCountry");

//		double landArea = 1000000;
//		double arableLandArea = 0.5 * landArea;
//		double GDP = 10000000;
//		double GDPRate = 0.03;
//		double energyOutput = 1000000;
//		double carbonOutput = 900000;
//		double availableToSpend = GDP * 0.1;
//		double emissionsTarget = 0.99 * carbonOutput;
		
		double landArea = 640427;
		double arableLandArea = 182711;
		double GDP = 1309583456588.0;
		double GDPRate = 0.0297;
		double energyOutput = 703718793;
		double carbonOutput = 409094000;
		double availableToSpend = GDP * GameConst.getPercentageOfGdp();
		double emissionsTarget = 0.99 * carbonOutput;
		
		test = new AnnexOneReduce(UUID.randomUUID(), "JunitTest", "JUT",
				landArea, arableLandArea, GDP, GDPRate, energyOutput,
				carbonOutput);
		
		test.setAvailableToSpend(availableToSpend);
		test.setEmissionsTarget(emissionsTarget);

		test.initialise();
	}

	@Test
	public void testbehaviour() {

//		test.runSimulation();
//		test.performReduceMaintainActions();
//		test.runSimulation();


	}

}
