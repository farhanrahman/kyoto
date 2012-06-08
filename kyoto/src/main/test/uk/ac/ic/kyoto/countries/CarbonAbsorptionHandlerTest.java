package uk.ac.ic.kyoto.countries;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

import uk.ac.imperial.presage2.core.util.random.Random;


/** @author Nicolas Khadivi */


public class CarbonAbsorptionHandlerTest {
	
//	public static long FOREST_CARBON_OFFSET = 10;
//	public static long FOREST_BLOCK_SIZE = 100;
	
//	public TestCountry(	UUID id, String name, String ISO, 
//						double landArea, double arableLandArea, double GDP,
//						double GDPRate, long availableToSpend, long emissionsTarget,
//						long carbonOffset, long energyOutput, long carbonOutput)
	
	UUID randomID = Random.randomUUID();
	TestCountry france = new TestCountry(randomID, "France", "FRA",
										640427, 182711, 1113056,
										2.34, 0, 0,
										230546, 686403, 399028);
	CarbonAbsorptionHandler testObject = new CarbonAbsorptionHandler (france);

	
	@Test
	public void testCarbonAbsorptionHandler() {
		
		fail("Not implemented yet. Waiting for AbstractCountry constructor to be corrected.");
		
		System.out.println(france.getCarbonOffset());
		System.out.println(testObject.getCost(france.getCarbonOffset()));
	}

}