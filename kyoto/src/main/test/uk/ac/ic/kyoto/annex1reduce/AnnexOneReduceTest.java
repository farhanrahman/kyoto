package uk.ac.ic.kyoto.annex1reduce;

import org.junit.*;

import java.util.UUID;

public class AnnexOneReduceTest {

	static private AnnexOneReduce test;
	
	@BeforeClass
	public static void initEUCountry() {
		System.out.println("Initialising EUCountry");
		test = new AnnexOneReduce(UUID.randomUUID(), "JunitTest", "JUT",1000000,500000,1000000,1000000,1000000,700000);
		test.setAvailableToSpend(10000000);
		test.setEmissionsTarget(680000);
		
		test.initialise();
	}
	
	@Test
	public void testbehaviour() {
		
		test.behaviour();

		
	}

}
