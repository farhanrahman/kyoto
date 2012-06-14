package uk.ac.ic.kyoto.annex1reduce;

import org.junit.*;

import java.util.UUID;

/**
 * Tests Prolog integration
 * @author Nik
 *
 */
public class EUCountryTest {

	static private AnnexOneReduce test;
	
	@BeforeClass
	public static void initEUCountry() {
		System.out.println("Initialising EUCountry");
		test = new AnnexOneReduce(UUID.randomUUID(), "JunitTest", "JUT",1000000,500000,1000000,1000000,100000,70000);
		
		test.initialise();
	}
	
	@Test
	public void testbehaviour() {
		test.behaviour();

		
	}

}
