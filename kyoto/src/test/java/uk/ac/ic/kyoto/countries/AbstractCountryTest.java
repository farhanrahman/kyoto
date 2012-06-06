package uk.ac.ic.kyoto.countries;

import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.imperial.presage2.core.util.random.Random;

public class AbstractCountryTest {
	
	private TestCountry tester;
	
	@Test
	public void getCashTest()
	{
		UUID randomID = Random.randomUUID();	// store a random UUID
		
		TestCountry tester = new TestCountry(randomID, "Poland", "ISO", 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000);

		assertTrue(tester.getCash() == 1000*GameConst.PERCENTAGE_OF_GDP);
		
	}
	
	@Test
	public void carbonReductionHandlerTest()
	{
		assertTrue(tester.carbonReductionHandler.getCarbonOutputChange(1000) == 1000*1000/GameConst.CARBON_REDUCTION_COEFF);
	}
}
