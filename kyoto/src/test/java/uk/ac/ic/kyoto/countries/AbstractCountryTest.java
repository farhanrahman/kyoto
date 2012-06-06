package uk.ac.ic.kyoto.countries;

import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.imperial.presage2.core.util.random.Random;

public class AbstractCountryTest {
	
	@Test
	public void test()
	{
		UUID randomID = Random.randomUUID();	// store a random UUID
		
		TestCountry tester = new TestCountry(randomID, "Poland", "ISO", 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000);
		
		assertTrue(tester.getCash() == 1000*1000*GameConst.PERCENTAGE_OF_GDP);
	}
}
